package com.cs6238.project2.s2dr.server.services;

import com.cs6238.project2.s2dr.server.dao.DocumentDao;
import com.cs6238.project2.s2dr.server.exceptions.DocumentNotFoundException;
import com.cs6238.project2.s2dr.server.exceptions.UnexpectedQueryResultsException;
import com.cs6238.project2.s2dr.server.pojos.DelegatePermissionParams;
import com.cs6238.project2.s2dr.server.pojos.DocumentDownload;
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

        return documentDao.uploadDocument(document, documentName, securityFlag);
    }

    public DocumentDownload downloadDocument(int documentId)
            throws SQLException, DocumentNotFoundException, UnexpectedQueryResultsException {

        return documentDao.downloadDocument(documentId);
    }

    public void delegatePermissions(int documentId, DelegatePermissionParams delegateParams) throws SQLException {
        documentDao.delegatePermissions(documentId, delegateParams);
    }

    public void deleteDocument(int documentId) throws SQLException {
        documentDao.deleteDocument(documentId);
    }
}
