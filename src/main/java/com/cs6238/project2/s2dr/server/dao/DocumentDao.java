package com.cs6238.project2.s2dr.server.dao;

import com.cs6238.project2.s2dr.server.exceptions.DocumentNotFoundException;
import com.cs6238.project2.s2dr.server.exceptions.NoQueryResultsException;
import com.cs6238.project2.s2dr.server.exceptions.UnexpectedQueryResultsException;
import com.cs6238.project2.s2dr.server.exceptions.TooManyQueryResultsException;
import com.cs6238.project2.s2dr.server.pojos.DelegatePermissionParams;
import com.cs6238.project2.s2dr.server.pojos.DocumentDownload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DocumentDao {

    private static final Logger LOG = LoggerFactory.getLogger(DocumentDao.class);

    private final Connection conn;

    @Inject
    public DocumentDao(Connection conn) {
        this.conn = conn;
    }

    public String getUserName(int userId) throws SQLException, UnexpectedQueryResultsException {
        String query =
                "SELECT *" +
                "  FROM s2dr.Users" +
                " WHERE userId = ?";

        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = conn.prepareStatement(query);
            preparedStatement.setInt(1, userId);

            ResultSet rs = preparedStatement.executeQuery();

            String firstName, lastName;

            if (!rs.next()) {
                // no results were found when we were expecting one
                throw new NoQueryResultsException(
                        String.format("Was expecting at least one result from query: %s", query));
            }

            firstName = rs.getString("firstName");
            lastName = rs.getString("lastName");

            if (rs.next()) {
                // multiple results were returned when we only expected one
                throw new TooManyQueryResultsException(
                        String.format("Was expecting only a single result from query: %s", query));
            }

            return firstName + " " + lastName;
        } finally {
            if (preparedStatement != null) {
                preparedStatement.close();
            }
        }
    }

    public int uploadDocument(File document, String documentName)
            throws SQLException, FileNotFoundException, UnexpectedQueryResultsException {
        String query =
                "INSERT INTO s2dr.Documents (documentName, contents)" +
                "     VALUES (?, ?)";

        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(query);

            ps.setString(1, documentName);
            ps.setClob(2, new FileReader(document));

            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();

            if (!rs.next()) {
                // no results were found when we were expecting one
                throw new NoQueryResultsException(
                        String.format("Was expecting at least one result from query: %s", query));
            }

            int newDocumentId = rs.getInt(1);

            if (rs.next()) {
                // multiple results were returned when we only expected one
                throw new TooManyQueryResultsException(
                        String.format("Was expecting only a single result from query: %s", query));
            }

            return newDocumentId;

        } finally {
            if (ps != null) {
                ps.close();
            }
        }
    }

    public DocumentDownload downloadDocument(int documentId)
            throws SQLException, DocumentNotFoundException, UnexpectedQueryResultsException {
        String query =
                "SELECT *" +
                "  FROM s2dr.Documents" +
                " WHERE documentId = ?";

        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(query);

            ps.setInt(1, documentId);

            ResultSet rs = ps.executeQuery();

            if (!rs.next()) {
                // no documents matched the given documentId
                throw new DocumentNotFoundException();
            }

            DocumentDownload download = DocumentDownload.builder()
                    .setDocumentId(rs.getInt("documentId"))
                    .setDocumentName(rs.getString("documentName"))
                    .setContents(rs.getClob("contents").getAsciiStream())
                    .build();

            if (rs.next()) {
                // multiple results were returned when we only expected one
                throw new TooManyQueryResultsException(
                        String.format("Was expecting only a single result from query: %s", query));
            }

            return download;

        } finally {
            if (ps != null) {
                ps.close();
            }
        }
    }

    public void delegatePermissions(int documentId, DelegatePermissionParams delegateParams) throws SQLException {
        String query =
                "INSERT INTO s2dr.DocumentPermissions" +
                "            (documentId, clientId, permission, canPropogate)" +
                "     VALUES (?, ?, ?, ?)";

        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(query);

            ps.setInt(1, documentId);
            ps.setString(2, delegateParams.getClientId());
            ps.setString(3, delegateParams.getPermission().toString());
            ps.setBoolean(4, delegateParams.getCanPropogate());

            ps.executeUpdate();

        } finally {
            if (ps != null) {
                ps.close();
            }
        }
    }

    public void deleteDocument(int documentId) throws SQLException {
        String query =
                "DELETE" +
                "  FROM s2dr.Documents" +
                " WHERE documentId = ?";

        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(query);
            ps.setInt(1, documentId);

            ps.executeUpdate();
        } finally {
            if (ps != null) {
                ps.close();
            }
        }
    }
}
