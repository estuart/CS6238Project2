package com.cs6238.project2.s2dr.server.dao;

import com.cs6238.project2.s2dr.server.exceptions.DocumentNotFoundException;
import com.cs6238.project2.s2dr.server.pojos.DocumentDownload;

import javax.inject.Inject;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Dao {

    private final Connection conn;

    @Inject
    public Dao(Connection conn) {
        this.conn = conn;
    }

    public String getUserName(int userId) throws SQLException {
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

            rs.next();

            firstName = rs.getString("firstName");
            lastName = rs.getString("lastName");

            return firstName + " " + lastName;
        } finally {
            if (preparedStatement != null) {
                preparedStatement.close();
            }
        }
    }

    public int uploadDocument(File document, String documentName) throws SQLException, FileNotFoundException {
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

            rs.next();

            return rs.getInt(1);

        } finally {
            if (ps != null) {
                ps.close();
            }
        }
    }

    public DocumentDownload downloadDocument(int documentId) throws SQLException, DocumentNotFoundException {
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

            return DocumentDownload.builder()
                    .setDocumentId(rs.getInt("documentId"))
                    .setDocumentName(rs.getString("documentName"))
                    .setContents(rs.getClob("contents").getAsciiStream())
                    .build();

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
