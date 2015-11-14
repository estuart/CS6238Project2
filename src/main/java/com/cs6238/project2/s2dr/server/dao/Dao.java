package com.cs6238.project2.s2dr.server.dao;

import javax.inject.Inject;
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
}
