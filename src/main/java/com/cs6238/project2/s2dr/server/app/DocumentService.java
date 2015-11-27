package com.cs6238.project2.s2dr.server.app;

import com.cs6238.project2.s2dr.server.app.exceptions.DocumentNotFoundException;
import com.cs6238.project2.s2dr.server.app.exceptions.NoQueryResultsException;
import com.cs6238.project2.s2dr.server.app.exceptions.UnexpectedQueryResultsException;
import com.cs6238.project2.s2dr.server.app.exceptions.UserLacksPermissionException;
import com.cs6238.project2.s2dr.server.app.objects.CurrentUser;
import com.cs6238.project2.s2dr.server.app.objects.DelegatePermissionParams;
import com.cs6238.project2.s2dr.server.app.objects.DocumentDownload;
import com.cs6238.project2.s2dr.server.app.objects.DocumentPermission;
import com.cs6238.project2.s2dr.server.app.objects.SecurityFlag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.File;
import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.Set;

public class DocumentService {

    private static final Logger LOG = LoggerFactory.getLogger(DocumentService.class);

    private final CurrentUser currentUser;
    private final DocumentDao documentDao;

    @Inject
    public DocumentService(
            CurrentUser currentUser,
            DocumentDao documentDao) {

        this.currentUser = currentUser;
        this.documentDao = documentDao;
    }

    public void uploadDocument(File document, String documentName, Set<SecurityFlag> securityFlags)
            throws SQLException, FileNotFoundException, UnexpectedQueryResultsException, UserLacksPermissionException {

        if (!documentDao.documentExists(documentName)) {
            LOG.info("Uploading new document \"{}\"", documentName);
            documentDao.uploadDocument(document, documentName);
        } else {
            // because the document already exists, we must check if the current user has WRITE permission
            // before allowing them to overwrite the document.
            LOG.info("Checking if user \"{}\" has proper permission to over-write document \"{}\"",
                    currentUser.getUserName(), documentName);
            if (!hasWritePermission(documentDao.getDocPermsForCurrentUser(documentName))) {

                LOG.info("User \"{}\" lacks WRITE permission for document \"{}\"",
                        currentUser.getUserName(), documentName);

                throw new UserLacksPermissionException(
                        "You must have the correct permission before writing to an existing file");
            }

            LOG.info("Overwriting document");
            documentDao.overwriteDocument(documentName, document);

            LOG.info("Removing current security flags");
            documentDao.clearDocumentSecurity(documentName);
        }

        for (SecurityFlag securityFlag: securityFlags) {
            LOG.info("Adding SecurityFlag \"{}\" to document \"{}\"", securityFlag, documentName);
            documentDao.setDocumentSecurity(documentName, securityFlag);
        }

        String currentUserName = currentUser.getUserName();

        // when a user uploads a new document, we add an "Owner" permission for that user.
        LOG.info("Adding owner permission to document \"{}\" for user \"{}\"", documentName, currentUserName);
        documentDao.delegatePermissions(
                documentName, DelegatePermissionParams.getUploaderPermissions(currentUserName));
    }

    public DocumentDownload downloadDocument(String documentName)
            throws SQLException, DocumentNotFoundException, UnexpectedQueryResultsException, UserLacksPermissionException {

        LOG.info("Checking if user \"{}\" has proper permission to check-out document \"{}\"",
                currentUser.getUserName(), documentName);

        // if the user doesn't have read permission, then we throw an exception
        if (!hasReadPermission(documentDao.getDocPermsForCurrentUser(documentName))) {
            LOG.info("User \"{}\" lacks READ permission for document \"{}\"", currentUser.getUserName(), documentName);
            throw new UserLacksPermissionException("You must have the correct permission before checking-out a file");
        }

        LOG.info("User \"{}\" checking-out document \"{}\"", currentUser.getUserName(), documentName);
        return documentDao.downloadDocument(documentName);
    }

    public void delegatePermissions(String documentName, DelegatePermissionParams delegateParams) throws SQLException {

        LOG.info("Delegating permission to file \"{}\": {}", documentName, delegateParams);
        documentDao.delegatePermissions(documentName, delegateParams);
    }

    public void deleteDocument(String documentName)
            throws SQLException, NoQueryResultsException, UserLacksPermissionException {

        LOG.info("Checking if user \"{}\" has proper permission to delete document \"{}\"",
                currentUser.getUserName(), documentName);
        // TODO does WRITE permission allow for deletion too?
        if (!hasOwnerPermission(documentDao.getDocPermsForCurrentUser(documentName))) {
            LOG.info("User \"{}\" must have a valid OWNER permission to delete a document", currentUser.getUserName());
            throw new UserLacksPermissionException("Only a document's owner is allowed to delete a file.");
        }

        // delete all permissions for the document before deleting the document
        LOG.info("Deleting all permissions for document \"{}\"", documentName);
        documentDao.deleteAllDocumentPermissions(documentName);

        LOG.info("Removing all security flags for document \"{}\"", documentName);
        documentDao.clearDocumentSecurity(documentName);

        LOG.info("Performing safe delete on document \"{}\"", documentName);
        documentDao.deleteDocument(documentName);

        LOG.info("Successfully deleted document \"{}\"", documentName);
    }

    private boolean hasReadPermission(Set<DocumentPermission> permissions) {
        return permissions.contains(DocumentPermission.READ)
                || permissions.contains(DocumentPermission.BOTH)
                || permissions.contains(DocumentPermission.OWNER);
    }

    private boolean hasWritePermission(Set<DocumentPermission> permissions) {
        return permissions.contains(DocumentPermission.WRITE)
                || permissions.contains(DocumentPermission.BOTH)
                || permissions.contains(DocumentPermission.OWNER);
    }

    private boolean hasOwnerPermission(Set<DocumentPermission> permissions) {
        return permissions.contains(DocumentPermission.OWNER);
    }
}
