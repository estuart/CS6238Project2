package com.cs6238.project2.s2dr.server.guice;

import com.cs6238.project2.s2dr.server.services.DocumentService;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Stage;
import com.google.inject.servlet.GuiceServletContextListener;
import com.google.inject.servlet.ServletModule;

public class GuiceServletConfig extends GuiceServletContextListener {

    public static Injector injector;

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
}
