package com.tegel.dao;

import com.tegel.model.Newsletter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NewsletterDAO {
    private static final Logger logger = Logger.getLogger(NewsletterDAO.class.getName());

    /**
     * Retrieves the latest newsletters from the database
     * @param limit the maximum number of newsletters to retrieve
     * @return a list of the latest newsletters
     */
    public List<Newsletter> getLatestNewsletters(int limit) {
        List<Newsletter> newsletters = new ArrayList<>();
        String sql = "SELECT * FROM mod4db.newsletter ORDER BY publish_date DESC LIMIT ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, limit);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Newsletter newsletter = new Newsletter();
                    newsletter.setNewsletterId(rs.getInt("newsletter_id"));
                    newsletter.setTitle(rs.getString("title"));
                    newsletter.setContent(rs.getString("content"));
                    newsletter.setPublishDate(rs.getObject("publish_date", LocalDateTime.class));
                    newsletters.add(newsletter);
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error retrieving latest newsletters", e);
        }

        return newsletters;
    }

    /**
     * Retrieves a specific newsletter by ID
     * @param newsletterId the ID of the newsletter to retrieve
     * @return the newsletter object, or null if not found
     */
    public Newsletter getNewsletterById(int newsletterId) {
        String sql = "SELECT * FROM mod4db.newsletter WHERE newsletter_id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, newsletterId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Newsletter newsletter = new Newsletter();
                    newsletter.setNewsletterId(rs.getInt("newsletter_id"));
                    newsletter.setTitle(rs.getString("title"));
                    newsletter.setContent(rs.getString("content"));
                    newsletter.setPublishDate(rs.getObject("publish_date", LocalDateTime.class));
                    return newsletter;
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error retrieving newsletter with ID: " + newsletterId, e);
        }

        return null;
    }
}
