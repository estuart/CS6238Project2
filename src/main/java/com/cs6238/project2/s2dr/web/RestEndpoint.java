package com.cs6238.project2.s2dr.web;

import com.google.common.collect.ImmutableMap;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.Map;

@Path("s2dr")
public class RestEndpoint {

    // TODO enable logging using slf4j

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, String> helloWorld() {
        return ImmutableMap.of("message", "Hello World!");
    }
}
