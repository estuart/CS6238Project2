package com.cs6238.project2.s2dr.server.app;

import com.cs6238.project2.s2dr.server.config.authentication.X509Token;

import javax.inject.Inject;
import java.security.spec.RSAPublicKeySpec;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class LoginDao {

    private final Connection connection;

    @Inject
    public LoginDao(Connection connection) {
        this.connection = connection;
    }

    public void addNewUser(X509Token token, RSAPublicKeySpec publicKeySpec) throws SQLException {
        String query =
                "INSERT" +
                "  INTO s2dr.Users (userName, signature, pubKeyModulus, pubKeyExponent)" +
                "VALUES (?, ?, ?, ?)";

        PreparedStatement ps = null;
        try {
            ps = connection.prepareStatement(query);

            ps.setString(1, token.getSubjectCommonName());
            ps.setBytes(2, token.getSignature());
            ps.setBytes(3, publicKeySpec.getModulus().toByteArray());
            ps.setBytes(4, publicKeySpec.getPublicExponent().toByteArray());

            ps.executeUpdate();
        } finally {
            if (ps != null) {
                ps.close();
            }
        }
    }
}
