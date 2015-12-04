package com.cs6238.project2.s2dr.server.app;

import com.cs6238.project2.s2dr.server.app.exceptions.DocumentNotFoundException;
import com.cs6238.project2.s2dr.server.app.exceptions.DocumentIntegrityVerificationException;
import com.cs6238.project2.s2dr.server.app.exceptions.NoQueryResultsException;
import com.cs6238.project2.s2dr.server.app.exceptions.UnexpectedQueryResultsException;
import com.cs6238.project2.s2dr.server.app.exceptions.UserLacksPermissionException;
import com.cs6238.project2.s2dr.server.app.objects.CurrentUser;
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
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.cert.X509Certificate;
import java.sql.SQLException;
import java.util.Set;

@Path("s2dr")
public class RestEndpoint {

    private static final Logger LOG = LoggerFactory.getLogger(RestEndpoint.class);

    private final CurrentUser currentUser;
    private final DocumentService documentService;
    private final LoginService loginService;

    @Inject
    RestEndpoint(
            CurrentUser currentUser,
            DocumentService documentService,
            LoginService loginService) {

        this.currentUser = currentUser;
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
            @FormDataParam("securityFlag") Set<SecurityFlag> securityFlags,
            @FormDataParam("signature") InputStream signature)
            throws SQLException, FileNotFoundException, URISyntaxException, UnexpectedQueryResultsException {

        LOG.info("User \'{}\" requesting to check-in document \"{}\"", currentUser.getUserName(), documentName);

        try {
            documentService.uploadDocument(document, documentName, securityFlags, signature);
        } catch (UserLacksPermissionException e) {
            // return a 401
            return Response
                    .status(Response.Status.UNAUTHORIZED)
                    .entity(e.getMessage())
                    .build();
        }

        LOG.info("Successfully uploaded document");

        // return HTTP 201 with URI to the resource
        return Response
                .created(new URI("/s2dr/document/" + documentName))
                .build();
    }

    @GET
    @Path("/document/{documentName}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response downloadDocument(@PathParam("documentName") String documentName)
            throws SQLException, UnexpectedQueryResultsException {
        
        LOG.info("User \"{}\" requesting to check-out document \"{}\"", currentUser.getUserName(), documentName);

        DocumentDownload download;
        try {
            download = documentService.downloadDocument(documentName);
        } catch (DocumentNotFoundException e) {
            // return a 404
            return Response
                    .status(Response.Status.NOT_FOUND)
                    .build();
        } catch (UserLacksPermissionException e) {
            // return a 401
            return Response
                    .status(Response.Status.UNAUTHORIZED)
                    .entity(e.getMessage())
                    .build();
        } catch (DocumentIntegrityVerificationException e) {
            return Response
                    .status(Response.Status.NOT_FOUND) // this probably isn't the correct status code
                    .entity("Unable to verify the integrity of the document")
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

    @GET
    @Path("/document/{documentName}/signature")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDocumentRequest(@PathParam("documentName") String documentName)
            throws UnexpectedQueryResultsException, SQLException {

        InputStream signature;
        try {
            signature = documentService.getDocumentSignature(documentName);
        } catch (UserLacksPermissionException e) {
            // return a 401
            return Response
                    .status(Response.Status.UNAUTHORIZED)
                    .entity(e.getMessage())
                    .build();
        } catch (DocumentNotFoundException e) {
            // return a 404
            return Response
                    .status(Response.Status.NOT_FOUND)
                    .build();
        }

        ContentDisposition contentDisposition = ContentDisposition.type("attachment")
                .fileName(documentName)
                .build();

        return Response
                .ok(signature)
                .header("Content-Disposition", contentDisposition)
                .build();
    }

    @PUT
    @Path("/document/{documentName}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response delegate(@PathParam("documentName") String documentName,
                             DelegatePermissionParams delegateParams) throws SQLException, NoQueryResultsException {

        try {
            documentService.delegatePermissions(documentName, delegateParams);
        } catch (UserLacksPermissionException e) {
            // return a 401
            return Response
                    .status(Response.Status.UNAUTHORIZED)
                    .entity(e.getMessage())
                    .build();
        }

        // return 200
        return Response.ok().build();
    }

    @DELETE
    @Path("/document/{documentName}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteDocument(@PathParam("documentName") String documentName)
            throws SQLException, NoQueryResultsException {

        LOG.info("User \"{}\" is requesting to delete document \"{}\"", currentUser.getUserName(), documentName);
        try {
            documentService.deleteDocument(documentName);
        } catch (UserLacksPermissionException e) {
            // return a 401 if the user doesn't have permission
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(e.getMessage())
                    .build();
        }

        // return 200
        return Response.ok().build();
    }

    @POST
    @Path("/logout")
    @Produces(MediaType.APPLICATION_JSON)
    public Response logout() {
        LOG.info("User \"{}\" now logging out", currentUser.getUserName());
        Subject currentUser = SecurityUtils.getSubject();
        currentUser.logout();

        // TODO #35 figure out how to handle the session scoped CurrentUser

        return Response.ok().build();
    }
}
