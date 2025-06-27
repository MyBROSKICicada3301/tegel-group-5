package com.tegel.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DatabaseManager is responsible for managing database connections.
 * It provides methods to establish and close connections to a PostgreSQL database.
 */
public class DatabaseManager {
    private static final Logger logger = Logger.getLogger(DatabaseManager.class.getName());
    // Use environment variables for sensitive data in production
    private static final String URL =
            "jdbc:postgresql://bronto.ewi.utwente.nl:5432/dab_dda2425-2b_120";
    private static final String USERNAME = "dab_dda2425-2b_120";
    private static final String PASSWORD = "tx9Y6pzQGoe65Brw";

    static {
        try {
            Class.forName("org.postgresql.Driver");
            logger.info("PostgreSQL driver loaded successfully");
        } catch (ClassNotFoundException e) {
            logger.severe("PostgreSQL JDBC Driver not found: " + e.getMessage());
            throw new RuntimeException("PostgreSQL JDBC Driver not found", e);
        }
    }

    /**
     * Establishes a connection to the PostgreSQL database.
     *
     * @return a Connection object
     * @throws SQLException if a database access error occurs
     */
    public static Connection getConnection() throws SQLException {
        try {
            Connection conn = DriverManager.getConnection(URL, USERNAME, PASSWORD);

            // Set connection properties for security
            conn.setAutoCommit(true);

            logger.info("Database connection established successfully");
            return conn;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to connect to database", e);
            throw e;
        }
    }

    /**
     * Closes the provided database connection.
     *
     * @param conn the Connection object to close
     */
    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
                logger.info("Database connection closed successfully");
            } catch (SQLException e) {
                logger.log(Level.WARNING, "Error closing database connection", e);
            }
        }
    }
}