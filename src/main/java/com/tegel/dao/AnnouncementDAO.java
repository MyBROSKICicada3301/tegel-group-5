package com.tegel.dao;

import com.tegel.model.Announcement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data Access Object for Announcement entities
 */
public class AnnouncementDAO {
    private static final Logger logger = Logger.getLogger(AnnouncementDAO.class.getName());

    /**
     * Get all announcements from the database, ordered by postedat (newest first)
     * Only returns public announcements
     *
     * @return List of announcements
     */
    public List<Announcement> getAllAnnouncements() {
        List<Announcement> announcements = new ArrayList<>();
        String sql = "SELECT * FROM mod4db.announcement WHERE ispublic = TRUE ORDER BY postedat DESC";

        try (Connection conn = DatabaseManager.getConnection()) {
            logger.info("Database connection obtained, preparing SQL statement");

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                logger.info("Executing query: " + sql);

                try (ResultSet rs = stmt.executeQuery()) {
                    logger.info("Query executed successfully, processing results");

                    while (rs.next()) {
                        Announcement announcement = extractAnnouncementFromResultSet(rs);
                        announcements.add(announcement);
                    }

                    logger.info("Retrieved " + announcements.size() + " announcements successfully");
                } catch (SQLException e) {
                    logger.log(Level.SEVERE, "Error executing query: " + e.getMessage(), e);
                    throw e;
                }
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Error preparing statement: " + e.getMessage(), e);
                throw e;
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Database connection error: " + e.getMessage(), e);
            // Log more detailed information about the error
            logger.log(Level.SEVERE, "SQL State: " + e.getSQLState());
            logger.log(Level.SEVERE, "Error Code: " + e.getErrorCode());
            logger.log(Level.SEVERE, "Full Error: ", e);
        }

        return announcements;
    }

    /**
     * Get a specific announcement by ID
     *
     * @param id The announcement ID
     * @return The announcement object or null if not found
     */
    public Announcement getAnnouncementById(int id) {
        String sql = "SELECT * FROM mod4db.announcement WHERE announcement_id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extractAnnouncementFromResultSet(rs);
                }
            }

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error retrieving announcement with ID " + id, e);
        }

        return null;
    }

    /**
     * Create a new announcement
     *
     * @param announcement The announcement to create
     * @return True if successful, false otherwise
     */
    public boolean createAnnouncement(Announcement announcement) {
        String sql = "INSERT INTO mod4db.announcement (title, content, postedby, ispublic) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, announcement.getTitle());
            stmt.setString(2, announcement.getContent());
            stmt.setInt(3, announcement.getPostedBy());
            stmt.setBoolean(4, announcement.isPublic());

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error creating announcement", e);
            return false;
        }
    }

    /**
     * Update an existing announcement
     *
     * @param announcement The announcement to update
     * @return True if successful, false otherwise
     */
    public boolean updateAnnouncement(Announcement announcement) {
        String sql = "UPDATE mod4db.announcement SET title = ?, content = ?, ispublic = ? WHERE announcement_id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, announcement.getTitle());
            stmt.setString(2, announcement.getContent());
            stmt.setBoolean(3, announcement.isPublic());
            stmt.setInt(4, announcement.getId());

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error updating announcement with ID " + announcement.getId(), e);
            return false;
        }
    }

    /**
     * Delete an announcement
     *
     * @param id The ID of the announcement to delete
     * @return True if successful, false otherwise
     */
    public boolean deleteAnnouncement(int id) {
        String sql = "DELETE FROM mod4db.announcement WHERE announcement_id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error deleting announcement with ID " + id, e);
            return false;
        }
    }

    /**
     * Search for announcements by title or content
     * Only returns public announcements
     *
     * @param searchTerm The search term
     * @return List of matching announcements
     */
    public List<Announcement> searchAnnouncements(String searchTerm) {
        List<Announcement> announcements = new ArrayList<>();
        String sql = "SELECT * FROM mod4db.announcement WHERE ispublic = TRUE AND (title LIKE ? OR content LIKE ?) ORDER BY postedat DESC";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            String searchPattern = "%" + searchTerm + "%";
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Announcement announcement = extractAnnouncementFromResultSet(rs);
                    announcements.add(announcement);
                }
            }

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error searching for announcements with term: " + searchTerm, e);
        }

        return announcements;
    }

    /**
     * Search for announcements by date
     * Only returns public announcements
     *
     * @param date The date to search for (any announcements on that date)
     * @return List of matching announcements
     */
    public List<Announcement> searchAnnouncementsByDate(LocalDateTime date) {
        List<Announcement> announcements = new ArrayList<>();
        String sql = "SELECT * FROM mod4db.announcement WHERE ispublic = TRUE AND DATE(postedat) = DATE(?) ORDER BY postedat DESC";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setTimestamp(1, Timestamp.valueOf(date));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Announcement announcement = extractAnnouncementFromResultSet(rs);
                    announcements.add(announcement);
                }
            }

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error searching for announcements by date: " + date, e);
        }

        return announcements;
    }

    /**
     * Extract announcement data from a ResultSet row
     *
     * @param rs The ResultSet positioned at the correct row
     * @return Announcement object populated with data from ResultSet
     * @throws SQLException if there is an error accessing ResultSet data
     */
    private Announcement extractAnnouncementFromResultSet(ResultSet rs) throws SQLException {
        Announcement announcement = new Announcement();
        announcement.setId(rs.getInt("announcement_id"));
        announcement.setTitle(rs.getString("title"));
        announcement.setContent(rs.getString("content"));

        // Convert SQL timestamp to LocalDateTime
        Timestamp timestamp = rs.getTimestamp("postedat");
        if (timestamp != null) {
            announcement.setCreatedAt(timestamp.toLocalDateTime());
        }

        announcement.setPostedBy(rs.getInt("postedby"));
        announcement.setPublic(rs.getBoolean("ispublic"));

        return announcement;
    }
}
