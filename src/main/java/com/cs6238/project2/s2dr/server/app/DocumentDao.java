package com.cs6238.project2.s2dr.server.app;

import com.cs6238.project2.s2dr.server.app.exceptions.DocumentNotFoundException;
import com.cs6238.project2.s2dr.server.app.exceptions.NoQueryResultsException;
import com.cs6238.project2.s2dr.server.app.exceptions.TooManyQueryResultsException;
import com.cs6238.project2.s2dr.server.app.exceptions.UnexpectedQueryResultsException;
import com.cs6238.project2.s2dr.server.app.objects.DelegatePermissionParams;
import com.cs6238.project2.s2dr.server.app.objects.DocumentDownload;
import com.cs6238.project2.s2dr.server.app.objects.SecurityFlag;
import org.apache.commons.lang3.StringUtils;
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

    public boolean documentExists(String documentName) throws SQLException {

        String query =
                "SELECT documentName" +
                "  FROM s2dr.documents" +
                " WHERE documentName = ?";

        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(query);

            ps.setString(1, documentName);

            ResultSet rs = ps.executeQuery();

            return rs.next();
        } finally {
            if (ps != null) {
                ps.close();
            }
        }

    }

    public void uploadDocument(File document, String documentName, SecurityFlag securityFlag)
            throws SQLException, FileNotFoundException, UnexpectedQueryResultsException {

        String query =
                "INSERT INTO s2dr.Documents (documentName, contents, securityFlag) " +
                "VALUES (?, ?, ?)";

        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(query);

            ps.setString(1, documentName);
            ps.setClob(2, new FileReader(document));
            ps.setString(3, securityFlag.name());

            ps.executeUpdate();

        } finally {
            if (ps != null) {
                ps.close();
            }
        }
    }

    public void overwriteDocument(String documentName, File document, SecurityFlag securityFlag)
            throws SQLException, FileNotFoundException {

        String query =
                "UPDATE s2dr.Documents" +
                "   SET contents       = ?," +
                "       securityFlag   = ?" +
                " WHERE documentName = ?";

        PreparedStatement ps = null;
        try {
            ps = conn.prepareCall(query);

            ps.setClob(1, new FileReader(document));
            ps.setString(2, securityFlag.name());
            ps.setString(3, documentName);

            ps.executeUpdate();
        } finally {
            if (ps != null) {
                ps.close();
            }
        }
    }

    public DocumentDownload downloadDocument(String documentName)
            throws SQLException, DocumentNotFoundException, UnexpectedQueryResultsException {
        String query =
                "SELECT * " +
                "FROM s2dr.Documents " +
                "WHERE documentName = ?";

        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(query);

            ps.setString(1, documentName);

            ResultSet rs = ps.executeQuery();

            if (!rs.next()) {
                // no documents matched the given document name
                throw new DocumentNotFoundException();
            }

            DocumentDownload download = DocumentDownload.builder()
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

    public void delegatePermissions(String documentName, DelegatePermissionParams delegateParams) throws SQLException {
        String query =
                "INSERT INTO s2dr.DocumentPermissions" +
                "   (documentName, userId, permission, canPropogate)" +
                "VALUES (?, ?, ?, ?)";

        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(query);

            ps.setString(1, documentName);
            ps.setInt(2, delegateParams.getUserId());
            ps.setString(3, delegateParams.getPermission().toString());
            ps.setBoolean(4, delegateParams.getCanPropogate());

            ps.executeUpdate();

        } finally {
            if (ps != null) {
                ps.close();
            }
        }
    }


    public void deleteDocument(String documentName) throws SQLException, NoQueryResultsException {
        Integer clobLength;
        //This query gets the length of the current clob
        String sizeQuery =
                "SELECT * " +
                "FROM s2dr.Documents " +
                "WHERE documentName = (?)";
        PreparedStatement ps1 = null;
        try {
            ps1 = conn.prepareStatement(sizeQuery);
            ps1.setString(1, documentName);
            ResultSet rs = ps1.executeQuery();

            if (!rs.next()) {
                // no documents matched the given documentName
                throw new NoQueryResultsException("No documents matched the given document name");
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
                "WHERE documentName = (?)";

        PreparedStatement ps2 = null;
        try {
            ps2 = conn.prepareStatement(zeroQuery);

            ps2.setString(1, zeroClob);
            ps2.setString(2, documentName);
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
                "WHERE documentName = (?)";

        PreparedStatement ps3 = null;
        try {
            ps3 = conn.prepareStatement(query);
            ps3.setString(1, documentName);

            ps3.executeUpdate();
        } finally {
            if (ps3 != null) {
                ps3.close();
            }
        }
    }

    public void deleteAllDocumentPermissions(String documentName) throws SQLException {
        String query =
                "DELETE" +
                "  FROM s2dr.DocumentPermissions" +
                " WHERE documentName = ?";

        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(query);
            ps.setString(1, documentName);
            ps.executeUpdate();
        } finally {
            if (ps != null) {
                ps.close();
            }
        }
    }
}
