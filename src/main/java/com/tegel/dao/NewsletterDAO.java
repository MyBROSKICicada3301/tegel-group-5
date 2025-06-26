package com.tegel.dao;

import com.tegel.model.Newsletter;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data Access Object for Newsletter entities
 */
public class NewsletterDAO {
    private static final Logger logger = Logger.getLogger(NewsletterDAO.class.getName());

    /**
     * Create a new newsletter
     * @param newsletter The newsletter to create
     * @return The created newsletter with ID, or null if creation failed
     */
    public Newsletter createNewsletter(Newsletter newsletter) {
        String sql = "INSERT INTO mod4db.newsletter (title, content, created_by, is_published) " +
                     "VALUES (?, ?, ?, ?) RETURNING newsletter_id";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, newsletter.getTitle());
            stmt.setString(2, newsletter.getContent());
            stmt.setInt(3, newsletter.getCreatedBy());
            stmt.setBoolean(4, newsletter.isPublished());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    newsletter.setId(rs.getInt(1));
                    // If published, set the published_at timestamp
                    if (newsletter.isPublished()) {
                        updatePublishedAt(newsletter.getId());
                    }
                    logger.info("Created newsletter with ID: " + newsletter.getId());
                    return newsletter;
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error creating newsletter", e);
        }
        return null;
    }

    /**
     * Update an existing newsletter
     * @param newsletter The newsletter with updated information
     * @return true if successful, false otherwise
     */
    public boolean updateNewsletter(Newsletter newsletter) {
        String sql = "UPDATE mod4db.newsletter SET title = ?, content = ?, is_published = ? " +
                     "WHERE newsletter_id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, newsletter.getTitle());
            stmt.setString(2, newsletter.getContent());
            stmt.setBoolean(3, newsletter.isPublished());
            stmt.setInt(4, newsletter.getId());

            int rowsAffected = stmt.executeUpdate();

            // If newsletter is being published for the first time, set published_at
            if (newsletter.isPublished()) {
                boolean updated = updatePublishedAtIfNeeded(newsletter.getId());
                logger.info("Newsletter published status updated: " + updated);
            }

            logger.info("Updated newsletter ID " + newsletter.getId() + ", rows affected: " + rowsAffected);
            return rowsAffected > 0;

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error updating newsletter ID " + newsletter.getId(), e);
            return false;
        }
    }

    /**
     * Delete a newsletter
     * @param newsletterId The ID of the newsletter to delete
     * @return true if successful, false otherwise
     */
    public boolean deleteNewsletter(int newsletterId) {
        String sql = "DELETE FROM mod4db.newsletter WHERE newsletter_id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, newsletterId);
            int rowsAffected = stmt.executeUpdate();

            logger.info("Deleted newsletter ID " + newsletterId + ", rows affected: " + rowsAffected);
            return rowsAffected > 0;

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error deleting newsletter ID " + newsletterId, e);
            return false;
        }
    }

    /**
     * Get a newsletter by ID
     * @param id The newsletter ID
     * @return The newsletter object or null if not found
     */
    public Newsletter getNewsletterById(int id) {
        String sql = "SELECT * FROM mod4db.newsletter WHERE newsletter_id = ?";

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
     * Get all published newsletters
     * @return List of published newsletters
     */
    public List<Newsletter> getAllPublishedNewsletters() {
        return getNewslettersByQuery(
            "SELECT * FROM mod4db.newsletter WHERE is_published = TRUE ORDER BY published_at DESC"
        );
    }

    /**
     * Get latest published newsletters with limit
     * @param limit Maximum number of newsletters to return
     * @return List of latest published newsletters
     */
    public List<Newsletter> getLatestNewsletters(int limit) {
        return getNewslettersByQuery(
            "SELECT * FROM mod4db.newsletter WHERE is_published = TRUE ORDER BY published_at DESC LIMIT ?",
            limit
        );
    }

    /**
     * Get all newsletters (including unpublished) - should only be used by admins
     * @return List of all newsletters
     */
    public List<Newsletter> getAllNewsletters() {
        return getNewslettersByQuery(
            "SELECT * FROM mod4db.newsletter ORDER BY created_at DESC"
        );
    }

    /**
     * Get newsletters created by a specific user
     * @param userId The user ID
     * @return List of newsletters created by the user
     */
    public List<Newsletter> getNewslettersByUser(int userId) {
        return getNewslettersByQuery(
            "SELECT * FROM mod4db.newsletter WHERE created_by = ? ORDER BY created_at DESC",
            userId
        );
    }

    /**
     * Search newsletters by title or content
     * @param searchTerm The search term
     * @param publishedOnly Whether to include only published newsletters
     * @return List of matching newsletters
     */
    public List<Newsletter> searchNewsletters(String searchTerm, boolean publishedOnly) {
        String sql;
        if (publishedOnly) {
            sql = "SELECT * FROM mod4db.newsletter WHERE is_published = TRUE AND " +
                  "(title ILIKE ? OR content ILIKE ?) ORDER BY published_at DESC";
        } else {
            sql = "SELECT * FROM mod4db.newsletter WHERE " +
                  "(title ILIKE ? OR content ILIKE ?) ORDER BY created_at DESC";
        }

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
                logger.info("Found " + newsletters.size() + " newsletters matching search term: " + searchTerm);
                return newsletters;
            }

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error searching newsletters for term: " + searchTerm, e);
        }

        return new ArrayList<>();
    }

    /**
     * Update the published_at timestamp for a newsletter if it's not already set
     * @param newsletterId The newsletter ID
     * @return true if successful, false otherwise
     */
    private boolean updatePublishedAtIfNeeded(int newsletterId) {
        String checkSql = "SELECT published_at FROM mod4db.newsletter WHERE newsletter_id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {

            checkStmt.setInt(1, newsletterId);

            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next() && rs.getTimestamp("published_at") == null) {
                    // Published_at not set, update it
                    return updatePublishedAt(newsletterId);
                }
            }

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error checking published_at for newsletter ID " + newsletterId, e);
        }

        return false;
    }

    /**
     * Update the published_at timestamp for a newsletter
     * @param newsletterId The newsletter ID
     * @return true if successful, false otherwise
     */
    private boolean updatePublishedAt(int newsletterId) {
        String updateSql = "UPDATE mod4db.newsletter SET published_at = CURRENT_TIMESTAMP " +
                           "WHERE newsletter_id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {

            updateStmt.setInt(1, newsletterId);
            int rowsAffected = updateStmt.executeUpdate();

            logger.info("Updated published_at for newsletter ID " + newsletterId +
                       ", rows affected: " + rowsAffected);
            return rowsAffected > 0;

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error updating published_at for newsletter ID " + newsletterId, e);
            return false;
        }
    }

    /**
     * Generic method to get newsletters by SQL query
     * @param sql The SQL query
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
     * Extract newsletter data from a ResultSet
     * @param rs The ResultSet positioned at the correct row
     * @return Newsletter object populated with data
     * @throws SQLException if there's an error accessing the data
     */
    private Newsletter extractNewsletterFromResultSet(ResultSet rs) throws SQLException {
        Newsletter newsletter = new Newsletter();
        newsletter.setId(rs.getInt("newsletter_id"));
        newsletter.setTitle(rs.getString("title"));
        newsletter.setContent(rs.getString("content"));
        newsletter.setCreatedBy(rs.getInt("created_by"));

        // Handle timestamps
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            newsletter.setCreatedAt(createdAt.toLocalDateTime());
        }

        Timestamp publishedAt = rs.getTimestamp("published_at");
        if (publishedAt != null) {
            newsletter.setPublishedAt(publishedAt.toLocalDateTime());
        }

        newsletter.setPublished(rs.getBoolean("is_published"));

        return newsletter;
    }
}
