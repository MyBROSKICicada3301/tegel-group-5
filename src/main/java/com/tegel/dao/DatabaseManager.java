package com.tegel.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseManager {
    private static final String URL = "jdbc:postgresql://bronto.ewi.utwente.nl:5432/dab_dda2425-2b_120";
    private static final String USERNAME = "dab_dda2425-2b_120";
    private static final String PASSWORD = "tx9Y6pzQGoe65Brw"; // 
    
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USERNAME, PASSWORD);
    }
}