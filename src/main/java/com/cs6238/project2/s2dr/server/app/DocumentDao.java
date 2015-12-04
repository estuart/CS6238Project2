package com.cs6238.project2.s2dr.server.app;

import com.cs6238.project2.s2dr.server.app.exceptions.DocumentNotFoundException;
import com.cs6238.project2.s2dr.server.app.exceptions.NoQueryResultsException;
import com.cs6238.project2.s2dr.server.app.exceptions.TooManyQueryResultsException;
import com.cs6238.project2.s2dr.server.app.exceptions.UnexpectedQueryResultsException;
import com.cs6238.project2.s2dr.server.app.objects.CurrentUser;
import com.cs6238.project2.s2dr.server.app.objects.DelegatePermissionParams;
import com.cs6238.project2.s2dr.server.app.objects.DocumentDownload;
import com.cs6238.project2.s2dr.server.app.objects.DocumentPermission;
import com.cs6238.project2.s2dr.server.app.objects.SecurityFlag;
import com.google.common.collect.ImmutableSet;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.util.ByteSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.FileNotFoundException;
import java.math.BigInteger;
import java.security.spec.RSAPublicKeySpec;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class DocumentDao {

    private static final Logger LOG = LoggerFactory.getLogger(DocumentDao.class);

    private final Connection conn;
    private final CurrentUser currentUser;

    @Inject
    public DocumentDao(
            Connection conn,
            CurrentUser currentUser) {

        this.conn = conn;
        this.currentUser = currentUser;
    }

    public boolean documentExists(String documentName) throws SQLException {

        String query =
                "SELECT documentName\n" +
                "  FROM s2dr.documents\n" +
                " WHERE documentName = ? ";

        LOG.debug("Query:\n{}", query);

        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(query);

            ps.setString(1, documentName);

            ResultSet rs = ps.executeQuery();

            // PMD doesn't like `return rs.next();` so we have to do this
            if (rs.next()) {
                return true;
            }
            return false;
        } finally {
            if (ps != null) {
                ps.close();
            }
        }

    }

    public void uploadDocument(String documentName,
                               ByteSource contents,
                               Optional<byte[]> encryptionKey,
                               Optional<byte[]> signature)
            throws SQLException, FileNotFoundException, UnexpectedQueryResultsException {

        String query =
                "INSERT INTO s2dr.Documents (documentName, contents, uploadUser, encryptionKey, signature)\n" +
                "VALUES (?, ?, ?, ?, ?)";

        LOG.debug("Query:\n{}", query);

        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(query);

            ps.setString(1, documentName);
            ps.setBytes(2, contents.getBytes());
            ps.setString(3, currentUser.getUserName());
            ps.setBytes(4, encryptionKey.orElse(null)); // this is a nullable field
            ps.setBytes(5, signature.orElse(null)); // nullable field

            ps.executeUpdate();

        } finally {
            if (ps != null) {
                ps.close();
            }
        }
    }

    public void overwriteDocument(String documentName,
                                  ByteSource contents,
                                  Optional<byte[]> encryptionKey,
                                  Optional<byte[]> signature) throws SQLException, FileNotFoundException {

        String query =
                "UPDATE s2dr.Documents\n" +
                "   SET contents = (?),\n" +
                "       uploadUser = (?),\n" +
                "       encryptionKey = (?),\n" +
                "       signature = (?)\n" +
                " WHERE documentName = (?)";

        LOG.debug("Query:\n{}", query);

        PreparedStatement ps = null;
        try {
            ps = conn.prepareCall(query);

            ps.setBytes(1, contents.getBytes());
            ps.setString(2, currentUser.getUserName());
            ps.setBytes(3, encryptionKey.orElse(null)); // this is a nullable field
            ps.setBytes(4, signature.orElse(null)); // nullable field
            ps.setString(5, documentName);

            ps.executeUpdate();
        } finally {
            if (ps != null) {
                ps.close();
            }
        }
    }

    public void setDocumentSecurity(String documentName, SecurityFlag securityFlag) throws SQLException {

        String query =
                "INSERT INTO s2dr.DocumentSecurity (documentName, securityFlag)\n" +
                "VALUES (?, ?)";

        LOG.debug("Query:\n{}", query);

        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(query);

            ps.setString(1, documentName);
            ps.setString(2, securityFlag.name());

            ps.executeUpdate();
        } finally {
            if (ps != null) {
                ps.close();
            }
        }
    }

    public void clearDocumentSecurity(String documentName) throws SQLException {

        String query =
                "DELETE\n" +
                "  FROM s2dr.DocumentSecurity\n" +
                " WHERE documentName = (?)";

        LOG.debug("Query:\n{}", query);

        deleteByDocumentName(documentName, query);
    }

    public EnumSet<SecurityFlag> getDocumentSecurity(String documentName) throws SQLException {

        String query =
                "SELECT securityFlag\n" +
                "  FROM s2dr.DocumentSecurity\n" +
                " WHERE documentName = (?)";

        LOG.debug("Query:\n{}", query);

        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(query);

            ps.setString(1, documentName);

            ResultSet rs = ps.executeQuery();

            Set<SecurityFlag> flags = new HashSet<>();
            while (rs.next()) {
                flags.add(SecurityFlag.valueOf(rs.getString("securityFlag")));
            }
            return EnumSet.copyOf(flags);
        } finally {
            if (ps != null) {
                ps.close();
            }
        }
    }

    public DocumentDownload downloadDocument(String documentName)
            throws SQLException, DocumentNotFoundException, UnexpectedQueryResultsException {
        String query =
                "SELECT documentName,\n" +
                "       uploadUser,\n" +
                "       contents,\n" +
                "       encryptionKey,\n" +
                "       signature\n" +
                "  FROM s2dr.Documents\n" +
                " WHERE documentName = ?";

        LOG.debug("Query:\n{}", query);

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
                    .setUploadUserName(rs.getString("uploadUser"))
                    .setContents(rs.getBlob("contents").getBinaryStream())
                    .setEncryptionKey(Optional.ofNullable(rs.getBytes("encryptionKey")))
                    .setSignature(Optional.ofNullable(rs.getBytes("signature")))
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

    public void delegatePermissions(
            String documentName, DelegatePermissionParams params, Optional<Long> maxTime) throws SQLException {
        String query =
                "INSERT INTO s2dr.DocumentPermissions\n" +
                "   (documentName, userName, permission, timeLimit, canPropogate)\n" +
                "VALUES (?, ?, ?, ?, ?)";

        LOG.debug("Query:\n{}", query);

        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(query);

            ps.setString(1, documentName);
            ps.setString(2, params.getUserName());
            ps.setString(3, params.getPermission().toString());

            Timestamp timeLimit = null;
            // if neither maxTime nor params.timeLimitMillis is present, then we just just set
            // null for the time limit
            if (maxTime.isPresent() || params.getTimeLimitMillis().isPresent()) {
                // maxTime takes precedence over the params.timeLimitMillis because params.timeLimitMillis
                // is the value selected by the user, but maxTime is the value selected by the system,
                // which may be less than what is selected by the user.
                Long timeLimitMillis = maxTime.orElse(params.getTimeLimitMillis().orElse(null));
                timeLimit = new Timestamp(System.currentTimeMillis() + timeLimitMillis);
            }
            ps.setTimestamp(4, timeLimit);

            ps.setBoolean(5, params.getCanPropogate());

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
                "SELECT *\n" +
                "FROM s2dr.Documents\n" +
                "WHERE documentName = (?)";

        LOG.debug("Query:\n{}", sizeQuery);

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
                "UPDATE s2dr.Documents\n" +
                "SET contents = (?)\n" +
                "WHERE documentName = (?)";

        LOG.debug("Query:\n{}", zeroClob);

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
                "DELETE\n" +
                "FROM s2dr.Documents\n" +
                "WHERE documentName = (?)";

        LOG.debug("Query:\n{}", query);

        deleteByDocumentName(documentName, query);
    }

    public void deleteAllDocumentPermissions(String documentName) throws SQLException {
        String query =
                "DELETE\n" +
                "  FROM s2dr.DocumentPermissions\n" +
                " WHERE documentName = (?)";

        LOG.debug("Query:\n{}", query);

        deleteByDocumentName(documentName, query);
    }

    private void deleteByDocumentName(String documentName, String query) throws SQLException {
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

    public Set<DocumentPermission> getDocPermsForCurrentUser(String documentName) throws SQLException {
        ImmutableSet.Builder<DocumentPermission> permissionBuilder = ImmutableSet.builder();

        String query =
                "SELECT permission\n" +
                "  FROM s2dr.DocumentPermissions\n" +
                " WHERE documentName = (?)\n" +
                "   AND (userName = (?)\n" +
                "          OR userName = 'ALL')\n" +
                "   AND (timeLimit IS NULL\n" +
                "          OR timeLimit > (?))";

        LOG.debug("Query:\n{}", query);

        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(query);

            ps.setString(1, documentName);
            ps.setString(2, currentUser.getUserName());
            ps.setTimestamp(3, new Timestamp(System.currentTimeMillis()));

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                permissionBuilder.add(DocumentPermission.valueOf(rs.getString("permission")));
            }
        } finally {
            if (ps != null) {
                ps.close();
            }
        }

        Set<DocumentPermission> permissions = permissionBuilder.build();
        LOG.info("User \"{}\" possesses the following permissions for document \"{}\": {}",
                currentUser.getUserName(), documentName, permissions);

        return permissions;
    }

    public boolean userCanDelegate(String documentName, EnumSet<DocumentPermission> permissions)
            throws SQLException {

        LOG.debug("Checking for \"{}\"", permissions);

        String query =
                "SELECT permission\n" +
                "  FROM s2dr.DocumentPermissions\n" +
                appendPermissionInClause(permissions.size()) +
                "   AND documentName = (?)\n" +
                "   AND userName = (?)\n" +
                "   AND canPropogate = 'TRUE'\n" +
                "   AND (timeLimit IS NULL\n" +
                "          OR timeLimit > NOW())";

        LOG.debug("Query:\n{}", query);

        PreparedStatement ps = null;
        try {
            int bindIndex = 0;
            ps = conn.prepareStatement(query);

            bindIndex = bindQueryPermissions(ps, permissions, bindIndex);

            ps.setString(++bindIndex, documentName);
            ps.setString(++bindIndex, currentUser.getUserName());

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return true;
            }
            return false;
        } finally {
            if (ps != null) {
                ps.close();
            }
        }
    }


    public Optional<Long> getMaxDelegationTime(String documentName, EnumSet<DocumentPermission> permissions)
            throws SQLException, NoQueryResultsException {

        String query =
                "  SELECT timeLimit\n" +
                "    FROM s2dr.DocumentPermissions\n" +
                appendPermissionInClause(permissions.size()) +
                "     AND documentName = (?)\n" +
                "     AND userName = (?)\n" +
                "     AND (timeLimit > NOW()\n" +
                "            OR timeLimit IS NULL)\n" +
                "ORDER BY timeLimit ASC";

        LOG.debug("Query:\n{}", query);

        PreparedStatement ps = null;
        try {
            int bindIndex = 0;
            ps = conn.prepareStatement(query);

            bindIndex = bindQueryPermissions(ps, permissions, bindIndex);

            ps.setString(++bindIndex, documentName);
            ps.setString(++bindIndex, currentUser.getUserName());

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                Timestamp timeLimit = rs.getTimestamp("timeLimit");

                if (timeLimit == null) {
                    return Optional.empty();
                }
                return Optional.of(timeLimit.getTime() - System.currentTimeMillis());
            }

            throw new NoQueryResultsException("Expected at least one result from the query");
        } finally {
            if (ps != null) {
                ps.close();
            }
        }
    }

    private String appendPermissionInClause(int permissionsLength) {
        // crappy approach but I don't want to put more effort into a school project
        if (permissionsLength == 0) {
            throw new IllegalArgumentException("Must provide at least one permission to check");
        }

        if (permissionsLength == 1) {
            return "   WHERE permission IN (?)\n";
        }

        if (permissionsLength == 2) {
            return "   WHERE permission IN (?, ?)\n";
        }

        if (permissionsLength == 3) {
            return "   WHERE permission IN (?, ?, ?)\n";
        }

        throw new IllegalArgumentException("This method is set up to handle no more than three permissions");
    }

    private int bindQueryPermissions(
            PreparedStatement ps, EnumSet<DocumentPermission> permissions, int bindIndex) throws SQLException {

        for (DocumentPermission permission: permissions) {
            ps.setString(++bindIndex, permission.name());
        }
        return bindIndex;
    }

    // This method really doesn't belong in this DAO, but I don't feel like adding another
    // DAO just for this query since this is only a school project
    public RSAPublicKeySpec getUserPubKeySpec(String userName)
            throws SQLException, NoQueryResultsException {

        String query =
                "SELECT pubKeyModulus,\n" +
                "       pubKeyExponent\n" +
                "  FROM s2dr.Users" +
                " WHERE userName = (?)";

        PreparedStatement ps = null;

        try {
            ps = conn.prepareStatement(query);

            ps.setString(1, userName);

            ResultSet rs = ps.executeQuery();

            if (!rs.next()) {
                throw new NoQueryResultsException(String.format("Could not find public key info for %s", userName));
            }

            return new RSAPublicKeySpec(
                    new BigInteger(rs.getBytes("pubKeyModulus")),
                    new BigInteger(rs.getBytes("pubKeyExponent")));
        } finally {
            if (ps != null) {
                ps.close();
            }
        }
    }
}
