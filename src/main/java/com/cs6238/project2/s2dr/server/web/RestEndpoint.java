package com.cs6238.project2.s2dr.server.web;

import com.cs6238.project2.s2dr.server.pojos.DocumentDownload;
import com.cs6238.project2.s2dr.server.services.Service;
import com.google.common.collect.ImmutableMap;
import com.sun.jersey.core.header.ContentDisposition;
import com.sun.jersey.multipart.FormDataParam;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.FileNotFoundException;
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

    @POST
    @Path("/upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Integer> uploadDocument(
            @FormDataParam("document") File document,
            @FormDataParam("documentName") String documentName) throws SQLException, FileNotFoundException {

        int newDocumentId = service.uploadDocument(document, documentName);

        return ImmutableMap.of("documentId", newDocumentId);
    }

    @GET
    @Path("/download/{documentId}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response downloadDocument(@PathParam("documentId") Integer documentId) throws SQLException {
        DocumentDownload download = service.downloadDocument(documentId);

        ContentDisposition contentDisposition = ContentDisposition.type("attachment")
                .fileName(download.getDocumentName())
                .build();

        return Response
                .ok(download.getContents())
                .header("Content-Disposition", contentDisposition)
                .build();
    }
}
