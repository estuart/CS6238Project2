package com.cs6238.project2.s2dr.web;

import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.Map;

@Path("s2dr")
public class RestEndpoint {

    private static final Logger LOG = LoggerFactory.getLogger(RestEndpoint.class);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, String> helloWorld() {
        LOG.trace("Received GET Request");
        return ImmutableMap.of("message", "Hello World!");
    }
}
