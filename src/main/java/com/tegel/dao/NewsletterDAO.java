package com.tegel.dao;

import com.tegel.model.Newsletter;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data Access Object for Newsletter entities.
 */
public class NewsletterDAO {
    private static final Logger logger = Logger.getLogger(NewsletterDAO.class.getName());

    // Add this method to NewsletterDAO.java
    private String stripHtmlTags(String html) {
        if (html == null) return null;
        // Simple HTML tag removal - you might want to use a more robust solution
        return html.replaceAll("<[^>]*>", "").trim();
    }
    /**
     * Create a new newsletter.
     *
     * @param newsletter The newsletter to create
     * @return The created newsletter with ID, or null if creation failed
     */
    public Newsletter createNewsletter(Newsletter newsletter) {
        String sql = "INSERT INTO mod4db.newsletter (title, content, postedby, postedat) VALUES (?, ?, ?, ?) RETURNING letter_id";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, newsletter.getTitle());
            stmt.setString(2, stripHtmlTags(newsletter.getContent()));
            stmt.setInt(3, newsletter.getCreatedBy());
            stmt.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int generatedId = rs.getInt(1);
                    newsletter.setId(generatedId);
                    newsletter.setCreatedAt(LocalDateTime.now());
                    newsletter.setPublished(true);
                    logger.info("Created newsletter with ID: " + generatedId);
                    return newsletter;
                }
            }

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error creating newsletter", e);
        }
        return null;
    }

    /**
     * Update an existing newsletter.
     *
     * @param newsletter The newsletter with updated information
     * @return true if successful, false otherwise
     */
    public boolean updateNewsletter(Newsletter newsletter) {
        String sql = "UPDATE mod4db.newsletter SET title = ?, content = ? WHERE letter_id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, newsletter.getTitle());
            stmt.setString(2, newsletter.getContent());
            stmt.setInt(3, newsletter.getId());

            int rowsAffected = stmt.executeUpdate();
            boolean success = rowsAffected > 0;

            logger.info("Updated newsletter ID " + newsletter.getId() + ", success: " + success);
            return success;

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error updating newsletter ID " + newsletter.getId(), e);
            return false;
        }
    }

    /**
     * Delete a newsletter.
     *
     * @param newsletterId The ID of the newsletter to delete
     * @return true if successful, false otherwise
     */
    public boolean deleteNewsletter(int newsletterId) {
        String sql = "DELETE FROM mod4db.newsletter WHERE letter_id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, newsletterId);
            int rowsAffected = stmt.executeUpdate();

            logger.info(
                    "Deleted newsletter ID " + newsletterId + ", rows affected: " + rowsAffected);
            return rowsAffected > 0;

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error deleting newsletter ID " + newsletterId, e);
            return false;
        }
    }

    /**
     * Get a newsletter by ID.
     *
     * @param id The newsletter ID
     * @return The newsletter object or null if not found
     */
    public Newsletter getNewsletterById(int id) {
        String sql = "SELECT * FROM mod4db.newsletter WHERE letter_id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Newsletter newsletter = extractNewsletterFromResultSet(rs);
                    logger.info("Retrieved newsletter ID: " + id);
                    return newsletter;
                }
            }

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error getting newsletter ID " + id, e);
        }

        logger.info("Newsletter ID " + id + " not found");
        return null;
    }

    /**
     * Get all published newsletters (all newsletters are considered published in this schema).
     *
     * @return List of published newsletters
     */
    public List<Newsletter> getAllPublishedNewsletters() {
        return getNewslettersByQuery("SELECT * FROM mod4db.newsletter ORDER BY postedat DESC");
    }

    /**
     * Get latest published newsletters with limit.
     *
     * @param limit Maximum number of newsletters to return
     * @return List of latest published newsletters
     */
    public List<Newsletter> getLatestNewsletters(int limit) {
        return getNewslettersByQuery(
                "SELECT * FROM mod4db.newsletter ORDER BY postedat DESC LIMIT ?", limit);
    }

    /**
     * Get all newsletters (including unpublished) - should only be used by admins.
     *
     * @return List of all newsletters
     */
    public List<Newsletter> getAllNewsletters() {
        return getNewslettersByQuery("SELECT * FROM mod4db.newsletter ORDER BY postedat DESC");
    }

    /**
     * Search newsletters by title or content.
     *
     * @param searchTerm    The search term
     * @param publishedOnly Whether to include only published newsletters (ignored since all are published)
     * @return List of matching newsletters
     */
    public List<Newsletter> searchNewsletters(String searchTerm, boolean publishedOnly) {
        String sql = "SELECT * FROM mod4db.newsletter WHERE " +
                "(title ILIKE ? OR content ILIKE ?) ORDER BY postedat DESC";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            String searchPattern = "%" + searchTerm + "%";
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);

            try (ResultSet rs = stmt.executeQuery()) {
                List<Newsletter> newsletters = new ArrayList<>();
                while (rs.next()) {
                    newsletters.add(extractNewsletterFromResultSet(rs));
                }
                logger.info("Found " + newsletters.size() + " newsletters matching search term: " +
                                    searchTerm);
                return newsletters;
            }

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error searching newsletters for term: " + searchTerm, e);
        }

        return new ArrayList<>();
    }

    /**
     * Generic method to get newsletters by SQL query.
     *
     * @param sql    The SQL query
     * @param params Optional parameters for the query
     * @return List of newsletters matching the query
     */
    private List<Newsletter> getNewslettersByQuery(String sql, Object... params) {
        List<Newsletter> newsletters = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Set parameters if any
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    newsletters.add(extractNewsletterFromResultSet(rs));
                }
            }

            logger.info("Retrieved " + newsletters.size() + " newsletters with query: " + sql);

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error executing newsletter query: " + sql, e);
        }

        return newsletters;
    }

    /**
     * Extract newsletter data from a ResultSet.
     *
     * @param rs The ResultSet positioned at the correct row
     * @return Newsletter object populated with data
     * @throws SQLException if there's an error accessing the data
     */
    private Newsletter extractNewsletterFromResultSet(ResultSet rs) throws SQLException {
        Newsletter newsletter = new Newsletter();
        newsletter.setId(rs.getInt("letter_id"));
        newsletter.setTitle(rs.getString("title"));
        newsletter.setContent(rs.getString("content"));
        newsletter.setCreatedBy(rs.getInt("postedby"));

        // Handle timestamp
        Timestamp postedAt = rs.getTimestamp("postedat");
        if (postedAt != null) {
            newsletter.setCreatedAt(postedAt.toLocalDateTime());
            newsletter.setPublishedAt(postedAt.toLocalDateTime()); // Use same timestamp for both
        }

        // All newsletters are considered published in this schema
        newsletter.setPublished(true);

        return newsletter;
    }
}