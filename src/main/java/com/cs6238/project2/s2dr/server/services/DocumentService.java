package com.cs6238.project2.s2dr.server.services;

import com.cs6238.project2.s2dr.server.dao.DocumentDao;
import com.cs6238.project2.s2dr.server.exceptions.DocumentNotFoundException;
import com.cs6238.project2.s2dr.server.exceptions.UnexpectedQueryResultsException;
import com.cs6238.project2.s2dr.server.pojos.DelegatePermissionParams;
import com.cs6238.project2.s2dr.server.pojos.DocumentDownload;
import com.cs6238.project2.s2dr.server.pojos.DocumentPermission;
import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.File;
import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;

public class DocumentService {

    private static final Logger LOG = LoggerFactory.getLogger(DocumentService.class);

    private final DocumentDao documentDao;

    @Inject
    public DocumentService(DocumentDao documentDao) {
        this.documentDao = documentDao;
    }

    public Map<String, String> getHelloMessage(Optional<Integer> userId)
            throws SQLException, UnexpectedQueryResultsException {

        String name;
        if (!userId.isPresent()) {
            name = "World";
        } else {
            name = documentDao.getUserName(userId.get());
        }
        String message = "Hello " + name + "!";
        return ImmutableMap.of("message", message);
    }

    public int uploadDocument(File document, String documentName, String securityFlag)
            throws SQLException, FileNotFoundException, UnexpectedQueryResultsException {

        int documentId = documentDao.uploadDocument(document, documentName, securityFlag);

        // TODO once we add a user session (login), we will have access to the "currentUserId"
        // TODO without making a query. For now the name is just hardcoded in.
        int currentUserId = documentDao.getUserIdByName("Puckett, Michael");

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
