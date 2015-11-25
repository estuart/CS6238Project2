package com.cs6238.project2.s2dr.server.config;

import com.cs6238.project2.s2dr.server.app.DocumentService;
import com.cs6238.project2.s2dr.server.app.LoginService;
import com.cs6238.project2.s2dr.server.app.objects.CurrentUser;
import com.cs6238.project2.s2dr.server.config.authentication.UserAuthShiroModule;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.google.inject.Stage;
import com.google.inject.servlet.GuiceServletContextListener;
import com.google.inject.servlet.ServletModule;
import com.google.inject.servlet.SessionScoped;
import org.apache.shiro.guice.web.ShiroWebModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import java.sql.Connection;
import java.sql.SQLException;

public class GuiceServletConfig extends GuiceServletContextListener {

    private static final Logger LOG = LoggerFactory.getLogger(DatabaseModule.class);

    public static Injector injector;
    private ServletContext servletContext;

    @Override
    protected Injector getInjector() {
        injector = Guice.createInjector(Stage.DEVELOPMENT,
                new ServletModule() {
                    @Override
                    protected void configureServlets() {
                        // apparently because of the jersey-guice-bridge, anything
                        // that gets injected into a REST service configured with
                        // Jersey must be explicitly bound to Guice. Dependencies
                        // that are injected into non Jersey-configured classes
                        // will not require this
                        bind(DocumentService.class);
                        bind(LoginService.class);

                        bind(CurrentUser.class).in(SessionScoped.class);

                        // include shiro filters to the guice filter
                        ShiroWebModule.bindGuiceFilter(binder());
                    }
                },
                new DatabaseModule(),
                new H2ServerGuiceModule(),
                new UserAuthShiroModule(servletContext));

        return injector;
    }
    
    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        // very important that `this.servletContext` is set before calling
        // `super.contextInitialized`
        this.servletContext = servletContextEvent.getServletContext();

        super.contextInitialized(servletContextEvent);

        // uncomment these lines to interact with the H2 server. You will also need to
        // uncomment the H2ServerRunner code below in `contextDestroyed` to prevent
        // rogue threads
//        H2ServerRunner runner = injector.getInstance(H2ServerRunner.class);
//        runner.startAsync();
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        super.contextDestroyed(servletContextEvent);

        // close the database connection
        Connection conn = injector.getInstance(Connection.class);
        try {
            conn.close();
        } catch (SQLException e) {
            LOG.error("Unable to close the database connection");
        }

        // uncomment these lines if you are interacting with the H2 server
//        H2ServerRunner runner = injector.getInstance(H2ServerRunner.class);
//        runner.triggerShutdown();
//        runner.stopAsync();
    }

    static class H2ServerGuiceModule extends AbstractModule {

        @Override
        protected void configure() {
            bind(H2ServerRunner.class).in(Singleton.class);
        }
    }
}
