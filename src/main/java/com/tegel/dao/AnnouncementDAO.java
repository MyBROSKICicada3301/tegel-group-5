package com.tegel.dao;

import com.tegel.model.Announcement;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data Access Object for handling operations on the mod4db.announcement table
 */
public class AnnouncementDAO {
    private static final Logger logger = Logger.getLogger(AnnouncementDAO.class.getName());

    /**
     * Get all public announcements (for non-authenticated users)
     * @return List of public announcements ordered by latest first
     */
    public List<Announcement> getPublicAnnouncements() {
        List<Announcement> announcements = new ArrayList<>();
        String sql = "SELECT * FROM mod4db.announcement WHERE ispublic = TRUE ORDER BY postedat DESC";

        logger.info("AnnouncementDAO.getPublicAnnouncements() - START");
        logger.info("SQL: " + sql);

        try (Connection conn = DatabaseManager.getConnection()) {
            logger.info("Database connection obtained successfully");

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                logger.info("Executing query...");

                try (ResultSet rs = stmt.executeQuery()) {
                    logger.info("Query executed successfully, processing results...");
                    int count = 0;

                    while (rs.next()) {
                        count++;
                        Announcement announcement = mapResultSetToAnnouncement(rs);
                        announcements.add(announcement);
                        logger.info("Processed announcement: ID=" + announcement.getAnnouncementId() +
                                   ", Title=" + announcement.getTitle());
                    }

                    logger.info("Total announcements found: " + count);
                } catch (SQLException e) {
                    logger.log(Level.SEVERE, "Error executing query", e);
                    throw e;
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Database error in getPublicAnnouncements", e);
        }

        logger.info("AnnouncementDAO.getPublicAnnouncements() - END - returning " + announcements.size() + " announcements");
        return announcements;
    }

    /**
     * Get all announcements (both public and private) for authenticated users
     * @return List of all announcements ordered by latest first
     */
    public List<Announcement> getAllAnnouncements() {
        List<Announcement> announcements = new ArrayList<>();
        String sql = "SELECT * FROM mod4db.announcement ORDER BY postedat DESC";

        logger.info("AnnouncementDAO.getAllAnnouncements() - START");
        logger.info("SQL: " + sql);

        try (Connection conn = DatabaseManager.getConnection()) {
            logger.info("Database connection obtained successfully");

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                logger.info("Executing query...");

                try (ResultSet rs = stmt.executeQuery()) {
                    logger.info("Query executed successfully, processing results...");
                    int count = 0;

                    while (rs.next()) {
                        count++;
                        Announcement announcement = mapResultSetToAnnouncement(rs);
                        announcements.add(announcement);
                        logger.info("Processed announcement: ID=" + announcement.getAnnouncementId() +
                                   ", Title=" + announcement.getTitle());
                    }

                    logger.info("Total announcements found: " + count);
                } catch (SQLException e) {
                    logger.log(Level.SEVERE, "Error executing query", e);
                    throw e;
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Database error in getAllAnnouncements", e);
        }

        logger.info("AnnouncementDAO.getAllAnnouncements() - END - returning " + announcements.size() + " announcements");
        return announcements;
    }

    /**
     * Get a specific announcement by ID
     * @param announcementId the ID of the announcement to retrieve
     * @return the announcement, or null if not found
     */
    public Announcement getAnnouncementById(int announcementId) {
        String sql = "SELECT * FROM mod4db.announcement WHERE announcement_id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, announcementId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToAnnouncement(rs);
                }
            }

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error retrieving announcement with ID " + announcementId, e);
        }

        return null;
    }

    /**
     * Create a new announcement
     * @param announcement the announcement to create
     * @return true if successful, false otherwise
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
     * @param announcement the announcement with updated data
     * @return true if successful, false otherwise
     */
    public boolean updateAnnouncement(Announcement announcement) {
        String sql = "UPDATE mod4db.announcement SET title = ?, content = ?, ispublic = ? WHERE announcement_id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, announcement.getTitle());
            stmt.setString(2, announcement.getContent());
            stmt.setBoolean(3, announcement.isPublic());
            stmt.setInt(4, announcement.getAnnouncementId());

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error updating announcement with ID " + announcement.getAnnouncementId(), e);
            return false;
        }
    }

    /**
     * Delete an announcement by ID
     * @param announcementId the ID of the announcement to delete
     * @return true if successful, false otherwise
     */
    public boolean deleteAnnouncement(int announcementId) {
        String sql = "DELETE FROM mod4db.announcement WHERE announcement_id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, announcementId);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error deleting announcement with ID " + announcementId, e);
            return false;
        }
    }

    /**
     * Search announcements by keyword in title or content
     * @param keyword the search term
     * @param authenticatedUser whether the user is authenticated
     * @return list of matching announcements
     */
    public List<Announcement> searchAnnouncements(String keyword, boolean authenticatedUser) {
        List<Announcement> announcements = new ArrayList<>();
        String sql;

        if (authenticatedUser) {
            // Authenticated users can search all announcements
            sql = "SELECT * FROM mod4db.announcement WHERE title LIKE ? OR content LIKE ? ORDER BY postedat DESC";
        } else {
            // Non-authenticated users can only search public announcements
            sql = "SELECT * FROM mod4db.announcement WHERE ispublic = TRUE AND (title LIKE ? OR content LIKE ?) ORDER BY postedat DESC";
        }

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            String searchPattern = "%" + keyword + "%";
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    announcements.add(mapResultSetToAnnouncement(rs));
                }
            }

            logger.info("Found " + announcements.size() + " announcements matching search: " + keyword);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error searching announcements for: " + keyword, e);
        }

        return announcements;
    }

    /**
     * Search announcements by date
     * @param date the date to search for
     * @param authenticatedUser whether the user is authenticated
     * @return list of matching announcements
     */
    public List<Announcement> searchAnnouncementsByDate(LocalDateTime date, boolean authenticatedUser) {
        List<Announcement> announcements = new ArrayList<>();
        String sql;

        if (authenticatedUser) {
            // Authenticated users can search all announcements
            sql = "SELECT * FROM mod4db.announcement WHERE DATE(postedat) = DATE(?) ORDER BY postedat DESC";
        } else {
            // Non-authenticated users can only search public announcements
            sql = "SELECT * FROM mod4db.announcement WHERE ispublic = TRUE AND DATE(postedat) = DATE(?) ORDER BY postedat DESC";
        }

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setTimestamp(1, Timestamp.valueOf(date));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    announcements.add(mapResultSetToAnnouncement(rs));
                }
            }

            logger.info("Found " + announcements.size() + " announcements on date: " + date.toLocalDate());
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error searching announcements by date: " + date.toLocalDate(), e);
        }

        return announcements;
    }

    /**
     * Helper method to map a ResultSet row to an Announcement object
     * @param rs the ResultSet positioned at the current row
     * @return a new Announcement object with data from the ResultSet
     * @throws SQLException if a database error occurs
     */
    private Announcement mapResultSetToAnnouncement(ResultSet rs) throws SQLException {
        Announcement announcement = new Announcement();
        announcement.setAnnouncementId(rs.getInt("announcement_id"));
        announcement.setTitle(rs.getString("title"));
        announcement.setContent(rs.getString("content"));
        announcement.setPostedBy(rs.getInt("postedby"));

        Timestamp timestamp = rs.getTimestamp("postedat");
        if (timestamp != null) {
            announcement.setPostedAt(timestamp.toLocalDateTime());
        }

        announcement.setPublic(rs.getBoolean("ispublic"));

        return announcement;
    }
}
