package com.tegel.dao;

import com.tegel.model.Newsletter;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NewsletterDAO {
    private static final Logger logger = Logger.getLogger(NewsletterDAO.class.getName());

    public List<Newsletter> getLatestNewsletters(int limit) {
        List<Newsletter> newsletters = new ArrayList<>();

        // Try both with and without schema qualifier
        String[] queries = {
                "SELECT * FROM newsletter ORDER BY postedat DESC LIMIT ?",
                "SELECT * FROM mod4db.newsletter ORDER BY postedat DESC LIMIT ?"
        };

        for (String sql : queries) {
            try (Connection conn = DatabaseManager.getConnection()) {
                if (conn == null) continue;

                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setInt(1, limit);
                    stmt.setQueryTimeout(5); // Set timeout to 5 seconds

                    try (ResultSet rs = stmt.executeQuery()) {
                        while (rs.next()) {
                            Newsletter newsletter = new Newsletter();
                            newsletter.setNewsletterId(rs.getInt("letter_id"));
                            newsletter.setTitle(rs.getString("title"));
                            newsletter.setContent(rs.getString("content"));

                            // Use the most compatible method for datetime
                            Timestamp ts = rs.getTimestamp("postedat");
                            if (ts != null) {
                                newsletter.setPublishDate(ts.toLocalDateTime());
                            }

                            newsletters.add(newsletter);
                        }

                        if (!newsletters.isEmpty()) {
                            return newsletters; // Return early if we found results
                        }
                    }
                }
            } catch (SQLException e) {
                // Just try the next query if this one fails
            }
        }

        return newsletters;
    }

    public Newsletter getNewsletterById(int newsletterId) {
        String[] queries = {
                "SELECT * FROM newsletter WHERE letter_id = ?",
                "SELECT * FROM mod4db.newsletter WHERE letter_id = ?"
        };

        for (String sql : queries) {
            try (Connection conn = DatabaseManager.getConnection()) {
                if (conn == null) continue;

                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setInt(1, newsletterId);
                    stmt.setQueryTimeout(5); // Set timeout to 5 seconds

                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            Newsletter newsletter = new Newsletter();
                            newsletter.setNewsletterId(rs.getInt("letter_id"));
                            newsletter.setTitle(rs.getString("title"));
                            newsletter.setContent(rs.getString("content"));

                            // Use the most compatible method for datetime
                            Timestamp ts = rs.getTimestamp("postedat");
                            if (ts != null) {
                                newsletter.setPublishDate(ts.toLocalDateTime());
                            }

                            return newsletter;
                        }
                    }
                }
            } catch (SQLException e) {
                // Just try the next query if this one fails
            }
        }

        return null;
    }
}