package com.cs6238.project2.s2dr.server.app;

import com.cs6238.project2.s2dr.server.app.exceptions.DocumentNotFoundException;
import com.cs6238.project2.s2dr.server.app.exceptions.UnexpectedQueryResultsException;
import com.cs6238.project2.s2dr.server.app.objects.CurrentUser;
import com.cs6238.project2.s2dr.server.app.objects.DelegatePermissionParams;
import com.cs6238.project2.s2dr.server.app.objects.DocumentDownload;
import com.cs6238.project2.s2dr.server.app.objects.DocumentPermission;
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

    public int uploadDocument(File document, String documentName, String securityFlag)
            throws SQLException, FileNotFoundException, UnexpectedQueryResultsException {

        int documentId = documentDao.uploadDocument(document, documentName, securityFlag);

        int currentUserId = currentUser.getCurrentUser().getUserId();

        // when a user uploads a new document, we add an "Owner" permission for that user.
        // TODO once we add the "time" parameter, this should add an "unlimited" time for the uploader
        documentDao.delegatePermissions(documentId,
                new DelegatePermissionParams(DocumentPermission.OWNER, currentUserId, true));

        return documentId;
    }

    public DocumentDownload downloadDocument(int documentId)
            throws SQLException, DocumentNotFoundException, UnexpectedQueryResultsException {

        return documentDao.downloadDocument(documentId);
    }

    public void delegatePermissions(int documentId, DelegatePermissionParams delegateParams) throws SQLException {
        documentDao.delegatePermissions(documentId, delegateParams);
    }

    public void deleteDocument(int documentId) throws SQLException {

        // delete all permissions for the document before deleting the document
        LOG.info("Deleting all permissions for document: {}", documentId);
        documentDao.deleteAllDocumentPermissions(documentId);

        LOG.info("Performing safe delete on document: {}", documentId);
        documentDao.deleteDocument(documentId);
    }
}
