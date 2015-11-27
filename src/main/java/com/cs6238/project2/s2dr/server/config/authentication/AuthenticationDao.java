package com.cs6238.project2.s2dr.server.config.authentication;

import com.cs6238.project2.s2dr.server.app.exceptions.NoQueryResultsException;
import com.cs6238.project2.s2dr.server.app.exceptions.TooManyQueryResultsException;
import com.cs6238.project2.s2dr.server.app.objects.User;
import com.cs6238.project2.s2dr.server.config.GuiceServletConfig;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AuthenticationDao {

    private static final Connection connection = GuiceServletConfig.injector.getInstance(Connection.class);

    public static User getUser(X509Token token)
            throws SQLException, NoQueryResultsException, TooManyQueryResultsException {

        String query =
                "SELECT userName" +
                "  FROM s2dr.Users" +
                " WHERE userName = (?)" +
                "   AND signature = (?)";

        PreparedStatement ps = null;
        try {
            ps = connection.prepareStatement(query);

            ps.setString(1, token.getSubjectCommonName());
            ps.setBytes(2, token.getSignature());

            ResultSet rs = ps.executeQuery();

            if (!rs.next()) {
                throw new NoQueryResultsException("No username/signature combination matches the provided credentials");
            }

            User user = User.builder()
                    .setUserName(rs.getString(1))
                    .build();

            if (rs.next()) {
                throw new TooManyQueryResultsException(
                        "Two users configured with the same username/signature combination");
            }

            return user;
        } finally {
            if (ps != null) {
                ps.close();
            }
        }
    }
}
