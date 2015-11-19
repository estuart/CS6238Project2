package com.cs6238.project2.s2dr.server.guice;

import com.cs6238.project2.s2dr.server.services.DocumentService;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Stage;
import com.google.inject.servlet.GuiceServletContextListener;
import com.google.inject.servlet.ServletModule;
import org.h2.tools.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContextEvent;
import java.sql.Connection;
import java.sql.SQLException;

public class GuiceServletConfig extends GuiceServletContextListener {

    public static Injector injector;
    private static final Logger LOG = LoggerFactory.getLogger(DatabaseModule.class);
    private Thread thread;

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
                    }
                },
                new DatabaseModule());

        return injector;
    }
    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent){
        super.contextInitialized(servletContextEvent);
        try {
            final Connection conn = injector.getInstance(Connection.class);
            thread = new Thread(){
                public void run() {
                    try {
                        Server.startWebServer(conn);
                    } catch (SQLException e) {
                        LOG.debug("Server Not Starting");
                    }
                }
            };
            thread.start();
        } finally {
            LOG.debug("Exiting H2 server.");
        }
    }

    @Override
    public void contextDestroyed( ServletContextEvent servletContextEvent){
        thread.stop();
        super.contextDestroyed(servletContextEvent);

    }
}
