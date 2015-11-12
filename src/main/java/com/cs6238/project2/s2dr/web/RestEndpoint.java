package com.cs6238.project2.s2dr.web;

import com.cs6238.project2.s2dr.services.SystemService;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.Map;
import java.util.Optional;

@Path("s2dr")
public class RestEndpoint {

    // TODO enable logging using slf4j

    private final SystemService systemService;

    @Inject
    RestEndpoint(SystemService systemService) {
        this.systemService = systemService;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, String> helloWorld() {
        return systemService.getHelloMessage(Optional.<String>empty());
    }

    @GET
    @Path("/personal")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, String> helloName(@QueryParam("name") String name) {
        return systemService.getHelloMessage(Optional.ofNullable(name));
    }
}
