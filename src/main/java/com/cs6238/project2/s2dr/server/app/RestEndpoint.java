package com.cs6238.project2.s2dr.server.app;

import com.cs6238.project2.s2dr.server.app.exceptions.DocumentNotFoundException;
import com.cs6238.project2.s2dr.server.app.exceptions.UnexpectedQueryResultsException;
import com.cs6238.project2.s2dr.server.app.objects.DelegatePermissionParams;
import com.cs6238.project2.s2dr.server.app.objects.DocumentDownload;
import org.glassfish.jersey.media.multipart.ContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.*;
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

    private static final Logger LOG = LoggerFactory.getLogger(RestEndpoint.class);

    private final DocumentService documentService;

    @Inject
    RestEndpoint(DocumentService documentService) {
        this.documentService = documentService;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, String> helloWorld() throws SQLException, UnexpectedQueryResultsException {

        LOG.info("Hello World");
        return documentService.getHelloMessage(Optional.<Integer>empty());
    }

    @GET
    @Path("/personal")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, String> helloName(@QueryParam("userId") Integer userId)
            throws SQLException, UnexpectedQueryResultsException {

        LOG.info("Getting personal hello message for userId: {}", userId);
        return documentService.getHelloMessage(Optional.ofNullable(userId));
    }

    @POST
    @Path("/upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response uploadDocument(
            @FormDataParam("document") File document,
            @FormDataParam("documentName") String documentName,
            @FormDataParam("securityFlag") String securityFlag)
            throws SQLException, FileNotFoundException, URISyntaxException, UnexpectedQueryResultsException {

        LOG.info("Uploading new document named: {}", documentName);

        int newDocumentId = documentService.uploadDocument(document, documentName, securityFlag);

        LOG.info("Successfully uploaded document with ID: {}", newDocumentId);
        // return HTTP 201 with URI to the created resource
        return Response
                .created(new URI("/document/" + newDocumentId))
                .build();
    }

    @GET
    @Path("/document/{documentId}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response downloadDocument(@PathParam("documentId") int documentId)
            throws SQLException, UnexpectedQueryResultsException {
        
        LOG.info("Downloading document: {}", documentId);

        DocumentDownload download;
        try {
            download = documentService.downloadDocument(documentId);
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

        LOG.info("Delegating permissions: {}, for document: {}", delegateParams, documentId);

        documentService.delegatePermissions(documentId, delegateParams);

        // return 200
        return Response.ok().build();
    }

    @DELETE
    @Path("/document/{documentId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteDocument(@PathParam("documentId") int documentId) throws SQLException{
        LOG.info("Received request to delete document: {}", documentId);
        documentService.deleteDocument(documentId);

        // return 200
        return Response.ok().build();
    }
}
