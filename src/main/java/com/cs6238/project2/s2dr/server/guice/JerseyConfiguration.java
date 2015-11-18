package com.cs6238.project2.s2dr.server.guice;

import com.cs6238.project2.s2dr.server.web.RestEndpoint;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.jvnet.hk2.guice.bridge.api.GuiceBridge;
import org.jvnet.hk2.guice.bridge.api.GuiceIntoHK2Bridge;

import javax.inject.Inject;

public class JerseyConfiguration extends ResourceConfig {

    @Inject
    public JerseyConfiguration(ServiceLocator serviceLocator) {

        super(Jackson2Feature.class, MultiPartFeature.class);

        GuiceBridge.getGuiceBridge().initializeGuiceBridge(serviceLocator);
        GuiceIntoHK2Bridge guiceBridge = serviceLocator.getService(GuiceIntoHK2Bridge.class);
        guiceBridge.bridgeGuiceInjector(GuiceServletConfig.injector);

        register(RestEndpoint.class);
    }
}
