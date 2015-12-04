package com.cs6238.project2.s2dr.server.config;

import com.cs6238.project2.s2dr.server.app.DocumentService;
import com.cs6238.project2.s2dr.server.app.LoginService;
import com.cs6238.project2.s2dr.server.app.objects.CurrentUser;
import com.cs6238.project2.s2dr.server.app.objects.ServerKeyPair;
import com.cs6238.project2.s2dr.server.config.authentication.UserAuthShiroModule;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provides;
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
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.sql.Connection;
import java.sql.SQLException;

public class GuiceServletConfig extends GuiceServletContextListener {

    private static final Logger LOG = LoggerFactory.getLogger(GuiceServletConfig.class);

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
                new ServerKeyPairGuiceModule(),
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

    static class ServerKeyPairGuiceModule extends AbstractModule {

        // in a real system we would not hardcode the password, but it's only a school project
        private static final String DEFAULT_CERT_PASSWORD = "changeit";
        private static final String KEYSTORE_FILE_PATH = "tomcat/conf/keystore.jks";
        private static final String SERVER_ALIAS = "s2drServer";

        @Override
        protected void configure() {}

        @Provides
        @Singleton
        private ServerKeyPair provideServerKeyPair() {
            ServerKeyPair keyPair = null;
            try {
                KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());

                java.io.FileInputStream fis = null;
                try {
                    fis = new java.io.FileInputStream(KEYSTORE_FILE_PATH);
                    keyStore.load(fis, DEFAULT_CERT_PASSWORD.toCharArray());

                    X509Certificate serverCert = (X509Certificate) keyStore.getCertificate(SERVER_ALIAS);

                    PublicKey publicKey = serverCert.getPublicKey();
                    PrivateKey privateKey = (PrivateKey) keyStore.getKey(
                            SERVER_ALIAS, DEFAULT_CERT_PASSWORD.toCharArray());

                    keyPair = new ServerKeyPair(publicKey, privateKey);
                } finally {
                    if (fis != null) {
                        fis.close();
                    }
                }
            } catch (Exception e) {
                LOG.error("Error obtaining the server keypair", e);
                throw new RuntimeException();
            }

            return keyPair;
        }
    }
}
