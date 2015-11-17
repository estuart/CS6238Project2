package com.cs6238.project2.s2dr.server.web;

import com.cs6238.project2.s2dr.server.exceptions.DocumentNotFoundException;
import com.cs6238.project2.s2dr.server.pojos.DocumentDownload;
import com.cs6238.project2.s2dr.server.pojos.DelegatePermissionParams;
import com.cs6238.project2.s2dr.server.services.Service;
import com.sun.jersey.core.header.ContentDisposition;
import com.sun.jersey.multipart.FormDataParam;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.FileNotFoundException;
import java.net.URI;
import java.net.URISyntaxException;
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
    public Response uploadDocument(
            @FormDataParam("document") File document,
            @FormDataParam("documentName") String documentName)
            throws SQLException, FileNotFoundException, URISyntaxException {

        int newDocumentId = service.uploadDocument(document, documentName);

        // return HTTP 201 with URI to the created resource
        return Response
                .created(new URI("/document/" + newDocumentId))
                .build();
    }

    @GET
    @Path("/document/{documentId}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response downloadDocument(@PathParam("documentId") int documentId) throws SQLException {
        DocumentDownload download;
        try {
            download = service.downloadDocument(documentId);
        } catch (DocumentNotFoundException e) {
            // return a 404
            return Response
                    .status(Response.Status.NOT_FOUND)
                    .build();
        }

        ContentDisposition contentDisposition = ContentDisposition.type("attachment")
                .fileName(download.getDocumentName())
                .build();

        // return HTTP 200
        return Response
                .ok(download.getContents())
                .header("Content-Disposition", contentDisposition)
                .build();
    }

    @PUT
    @Path("/document/{documentId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response delegate(@PathParam("documentId") int documentId,
                             DelegatePermissionParams delegateParams) throws SQLException {

        service.delegatePermissions(documentId, delegateParams);

        // return 200
        return Response.ok().build();
    }

    @DELETE
    @Path("/document/{documentId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteDocument(@PathParam("documentId") int documentId) throws SQLException{
        service.deleteDocument(documentId);

        // return 200
        return Response.ok().build();
    }
}
