package com.cs6238.project2.s2dr.server.app;

import com.cs6238.project2.s2dr.server.app.exceptions.DocumentNotFoundException;
import com.cs6238.project2.s2dr.server.app.exceptions.NoQueryResultsException;
import com.cs6238.project2.s2dr.server.app.exceptions.UnexpectedQueryResultsException;
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

    public void uploadDocument(File document, String documentName, SecurityFlag securityFlag)
            throws SQLException, FileNotFoundException, UnexpectedQueryResultsException {

        if (!documentDao.documentExists(documentName)) {
            documentDao.uploadDocument(document, documentName, securityFlag);
        } else {

            // TODO #6 need to check for write permission
            documentDao.overwriteDocument(documentName, document, securityFlag);
        }

        int currentUserId = currentUser.getCurrentUser().getUserId();

        // when a user uploads a new document, we add an "Owner" permission for that user.
        // TODO once we add the "time" parameter, this should add an "unlimited" time for the uploader
        documentDao.delegatePermissions(documentName,
                new DelegatePermissionParams(DocumentPermission.OWNER, currentUserId, true));
    }

    public DocumentDownload downloadDocument(String documentName)
            throws SQLException, DocumentNotFoundException, UnexpectedQueryResultsException {

        return documentDao.downloadDocument(documentName);
    }

    public void delegatePermissions(String documentName, DelegatePermissionParams delegateParams) throws SQLException {
        documentDao.delegatePermissions(documentName, delegateParams);
    }

    public void deleteDocument(String documentName) throws SQLException, NoQueryResultsException {

        // delete all permissions for the document before deleting the document
        LOG.info("Deleting all permissions for document: {}", documentName);
        documentDao.deleteAllDocumentPermissions(documentName);

        LOG.info("Performing safe delete on document: {}", documentName);
        documentDao.deleteDocument(documentName);
    }
}
