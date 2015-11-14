package com.cs6238.project2.s2dr.server.web;

import com.cs6238.project2.s2dr.server.services.Service;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;

@Path("s2dr")
public class RestEndpoint {

    // TODO enable logging using slf4j

    private final Service service;

    @Inject
    RestEndpoint(Service service) {
        this.service = service;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, String> helloWorld() throws SQLException {
        return service.getHelloMessage(Optional.<Integer>empty());
    }

    @GET
    @Path("/personal")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, String> helloName(@QueryParam("userId") Integer userId) throws SQLException {
        return service.getHelloMessage(Optional.ofNullable(userId));
    }
}
