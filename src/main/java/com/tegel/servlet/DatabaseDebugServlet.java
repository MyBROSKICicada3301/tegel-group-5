package com.tegel.servlet;

import java.sql.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

import com.tegel.dao.DatabaseManager;

/**
 * Servlet to provide debug information about the database connection and structure.
 * Accessible at /debug.
 */
@WebServlet("/debug")
public class DatabaseDebugServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/plain");
        PrintWriter out = response.getWriter();

        out.println("Database Debug Information");
        out.println("-------------------------");

        try (Connection conn = DatabaseManager.getConnection()) {

            out.println("Connection successful!");
            out.println("Connection details:");
            out.println("- Auto-commit: " + conn.getAutoCommit());
            out.println("- Catalog: " + conn.getCatalog());
            out.println("- Schema: " + conn.getSchema());

            DatabaseMetaData metaData = conn.getMetaData();
            out.println("Database product: " + metaData.getDatabaseProductName() + " " +
                                metaData.getDatabaseProductVersion());
            out.println("Driver: " + metaData.getDriverName() + " " + metaData.getDriverVersion());
            out.println("URL: " + metaData.getURL());

            out.println("\nAvailable tables:");
            try (ResultSet tables = metaData.getTables(null, null, "%", new String[]{"TABLE"})) {
                while (tables.next()) {
                    String tableName = tables.getString("TABLE_NAME");
                    out.println("- " + tableName);
                }
            }

            out.println("\nTesting newsletter table:");
            try (Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM newsletter")) {
                    if (rs.next()) {
                        out.println("Newsletter count: " + rs.getInt(1));
                    }
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                } catch (RuntimeException e) {
                    out.println("Error querying newsletter table: " + e.getMessage());

                    try (ResultSet rs = stmt.executeQuery(
                            "SELECT COUNT(*) FROM mod4db.newsletter")) {
                        if (rs.next()) {
                            out.println("mod4db.newsletter count: " + rs.getInt(1));
                        }
                    } catch (SQLException e1) {
                        throw new RuntimeException(e1);
                    } catch (RuntimeException e2) {
                        out.println("Error querying mod4db.newsletter table: " + e2.getMessage());
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (RuntimeException e) {
            out.println("ERROR: " + e.getMessage());
            e.printStackTrace(out);
        }
    }
}