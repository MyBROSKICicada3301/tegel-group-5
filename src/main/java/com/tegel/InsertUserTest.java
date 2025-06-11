package com.tegel;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.tegel.dao.DatabaseManager;

public class InsertUserTest {
    public static void main(String[] args) {
        Connection conn = null;
        try {
            conn = DatabaseManager.getConnection();

            String sql = "INSERT INTO \"mod4db\".\"users\" " +
                "(email, passwordhash, phonenumber, dateofbirth, dietres, role, full_name, nick_name) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, "TEGEl5@test.com");
            stmt.setString(2, "aVery$trongP@ssword");
            stmt.setString(3, "11223344");
            stmt.setDate(4, java.sql.Date.valueOf("2006-05-16"));
            stmt.setString(5, "non-veg");
            stmt.setString(6, "user");
            stmt.setString(7, "Tegel User");
            stmt.setString(8, "Teg");

            int rowsInserted = stmt.executeUpdate();
            if (rowsInserted > 0) {
                System.out.println("Test user inserted successfully!");
            }

            stmt.close();
        } catch (SQLException e) {
            System.out.println("Insert failed: " + e.getMessage());
        } finally {
            DatabaseManager.closeConnection(conn);
        }
    }
}