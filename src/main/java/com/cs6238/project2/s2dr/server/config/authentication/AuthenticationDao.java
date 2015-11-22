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

    public static User getUser(String userName, String password)
            throws SQLException, NoQueryResultsException, TooManyQueryResultsException {

        String query =
                "SELECT userId," +
                "       firstName," +
                "       lastName," +
                "       userName" +
                "  FROM s2dr.Users" +
                " WHERE userName = (?)" +
                "   AND password = (?)";

        PreparedStatement ps = null;
        try {
            ps = connection.prepareStatement(query);

            ps.setString(1, userName);
            ps.setString(2, password);

            ResultSet rs = ps.executeQuery();

            if (!rs.next()) {
                throw new NoQueryResultsException("No username/password combination matches the provided credentials");
            }

            User user = User.builder()
                    .setUserId(rs.getInt(1))
                    .setFirstName(rs.getString(2))
                    .setLastName(rs.getString(3))
                    .setUserName(rs.getString(4))
                    .build();

            if (rs.next()) {
                throw new TooManyQueryResultsException("Two users configured with the same username/password combination");
            }

            return user;
        } finally {
            if (ps != null) {
                ps.close();
            }
        }
    }
}
