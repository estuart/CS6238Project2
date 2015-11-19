package com.cs6238.project2.s2dr.server.dao;

import com.cs6238.project2.s2dr.server.exceptions.DocumentNotFoundException;
import com.cs6238.project2.s2dr.server.exceptions.NoQueryResultsException;
import com.cs6238.project2.s2dr.server.exceptions.UnexpectedQueryResultsException;
import com.cs6238.project2.s2dr.server.exceptions.TooManyQueryResultsException;
import com.cs6238.project2.s2dr.server.pojos.DelegatePermissionParams;
import com.cs6238.project2.s2dr.server.pojos.DocumentDownload;
import org.apache.commons.lang3.StringUtils;
import org.h2.tools.Server;
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
    public DocumentDao(Connection conn) throws SQLException {
        this.conn = conn;
    }


    public String getUserName(int userId) throws SQLException, UnexpectedQueryResultsException {
        String query =
                "SELECT * " +
                "FROM s2dr.Users " +
                "WHERE userId = ?";

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

    public int uploadDocument(File document, String documentName, String securityFlag)
            throws SQLException, FileNotFoundException, UnexpectedQueryResultsException {
        String query =
                "INSERT INTO s2dr.Documents (documentName, contents, securityFlag) " +
                "VALUES (?, ?, ?)";

        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(query);

            ps.setString(1, documentName);
            ps.setClob(2, new FileReader(document));
            ps.setString(3, securityFlag);

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
                "SELECT * " +
                "FROM s2dr.Documents " +
                "WHERE documentId = ?";

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
                "   (documentId, clientId, permission, canPropogate)" +
                "VALUES (?, ?, ?, ?)";

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
        Integer clobLength;
        //This query gets the length of the current clob
        String sizeQuery =
                "SELECT * " +
                "FROM s2dr.Documents " +
                "WHERE documentId = (?)";
        PreparedStatement ps1 = null;
        try {
            ps1 = conn.prepareStatement(sizeQuery);
            ps1.setInt(1, documentId);
            ResultSet rs = ps1.executeQuery();

            if (!rs.next()) {
                // no documents matched the given documentId
                throw new SQLException();
            }
            //get clob size
            clobLength = rs.getClob("contents").getSubString((long)1, (int)(rs.getClob("contents").length())).length() - 1;
        } finally {
            if (ps1 != null) {
                ps1.close();
            }
        }

        //Now that we know the clob length we insert a clob of zeros to overwrite it in memory
        String zeroClob = StringUtils.repeat("0", clobLength);
        String zeroQuery =
                "UPDATE s2dr.Documents " +
                "SET contents = (?) " +
                "WHERE documentID = (?)";

        PreparedStatement ps2 = null;
        try {
            ps2 = conn.prepareStatement(zeroQuery);

            ps2.setString(1, zeroClob);
            ps2.setInt(2, documentId);
            ps2.executeUpdate();

        } finally {
            if (ps2 != null) {
                ps2.close();
            }
        }

        //Finally we delete the document securely since the file content was overwritten with zeroes
        String query =
                "DELETE " +
                "FROM s2dr.Documents " +
                "WHERE documentId = (?)";

        PreparedStatement ps3 = null;
        try {
            ps3 = conn.prepareStatement(query);
            ps3.setInt(1, documentId);

            ps3.executeUpdate();
        } finally {
            if (ps3 != null) {
                ps3.close();
            }
        }
    }
}