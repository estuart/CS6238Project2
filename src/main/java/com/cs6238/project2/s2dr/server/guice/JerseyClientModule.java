package com.cs6238.project2.s2dr.server.guice;

import com.fasterxml.jackson.module.guice.ObjectMapperModule;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;

public class JerseyClientModule extends AbstractModule {
    @Override
    protected void configure() {
        install(new ObjectMapperModule());
    }

    @Provides
    @Singleton
    private Client client() {
        return ClientBuilder
                .newBuilder()
                .register(new Jackson2Feature())
                .build();
    }
}
