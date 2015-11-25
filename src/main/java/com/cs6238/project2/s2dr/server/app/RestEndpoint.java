package com.cs6238.project2.s2dr.server.app;

import com.cs6238.project2.s2dr.server.app.exceptions.DocumentNotFoundException;
import com.cs6238.project2.s2dr.server.app.exceptions.NoQueryResultsException;
import com.cs6238.project2.s2dr.server.app.exceptions.UnexpectedQueryResultsException;
import com.cs6238.project2.s2dr.server.app.objects.DelegatePermissionParams;
import com.cs6238.project2.s2dr.server.app.objects.DocumentDownload;
import com.cs6238.project2.s2dr.server.app.objects.SecurityFlag;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.glassfish.jersey.media.multipart.ContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.FileNotFoundException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.cert.X509Certificate;
import java.sql.SQLException;

@Path("s2dr")
public class RestEndpoint {

    private static final Logger LOG = LoggerFactory.getLogger(RestEndpoint.class);

    private final DocumentService documentService;
    private final LoginService loginService;

    @Inject
    RestEndpoint(
            DocumentService documentService,
            LoginService loginService) {

        this.documentService = documentService;
        this.loginService = loginService;
    }

    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response login(@Context HttpServletRequest request) {

        X509Certificate cert = ((X509Certificate[]) request.getAttribute("javax.servlet.request.X509Certificate"))[0];

        try {
            loginService.login(cert);
        } catch (Exception e) {
            LOG.error("Error logging user in", e);
            return Response.serverError().entity(e).build();
        }

        return Response.ok().build();
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

        documentService.uploadDocument(document, documentName, SecurityFlag.valueOf(securityFlag));

        LOG.info("Successfully uploaded document");

        // return HTTP 201 with URI to the created resource
        return Response
                .created(new URI("/s2dr/document/" + documentName))
                .build();
    }

    @GET
    @Path("/document/{documentName}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response downloadDocument(@PathParam("documentName") String documentName)
            throws SQLException, UnexpectedQueryResultsException {
        
        LOG.info("Downloading document: {}", documentName);

        DocumentDownload download;
        try {
            download = documentService.downloadDocument(documentName);
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
    @Path("/document/{documentName}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response delegate(@PathParam("documentName") String documentName,
                             DelegatePermissionParams delegateParams) throws SQLException {

        LOG.info("Delegating permissions: {}, for document: {}", delegateParams, documentName);

        documentService.delegatePermissions(documentName, delegateParams);

        // return 200
        return Response.ok().build();
    }

    @DELETE
    @Path("/document/{documentName}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteDocument(@PathParam("documentName") String documentName)
            throws SQLException, NoQueryResultsException {

        LOG.info("Received request to delete document: {}", documentName);
        documentService.deleteDocument(documentName);

        // return 200
        return Response.ok().build();
    }

    @POST
    @Path("/logout")
    @Produces(MediaType.APPLICATION_JSON)
    public Response logout() {
        Subject currentUser = SecurityUtils.getSubject();
        currentUser.logout();

        // TODO #35 figure out how to handle the session scoped CurrentUser

        return Response.ok().build();
    }
}
