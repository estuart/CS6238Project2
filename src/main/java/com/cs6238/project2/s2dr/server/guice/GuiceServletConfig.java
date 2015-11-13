package com.cs6238.project2.s2dr.server.guice;

import com.cs6238.project2.s2dr.server.web.RestEndpoint;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Stage;
import com.google.inject.servlet.GuiceServletContextListener;
import com.sun.jersey.guice.JerseyServletModule;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;

public class GuiceServletConfig extends GuiceServletContextListener {
    @Override
    protected Injector getInjector() {
        return Guice.createInjector(
                Stage.DEVELOPMENT,
                new JerseyServletModule() {
                    @Override
                    protected void configureServlets() {
                        install(new JerseyClientModule());

                        bind(RestEndpoint.class);

                        serve("/*").with(GuiceContainer.class);
                    }
                }
        );
    }
}
