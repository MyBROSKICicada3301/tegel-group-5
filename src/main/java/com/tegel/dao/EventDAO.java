package com.tegel.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.tegel.model.Event;
import com.tegel.util.SecurityUtils;

public class EventDAO {
    private static final Logger logger = Logger.getLogger(EventDAO.class.getName());
    
    public boolean createEvent(Event event) {
        // Input validation before database operation
        if (event == null) {
            logger.warning("Attempted to create null event");
            return false;
        }
        
        // Validate required fields and format
        if (!isValidEventData(event)) {
            logger.warning("Invalid event data provided for creation");
            return false;
        }
        
        String sql = "INSERT INTO mod4db.event (title, description, date, location, image, maxparticipants, createdby, isactive) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false); // Start transaction
            
            try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                
                // Sanitize and validate inputs before database call
                stmt.setString(1, SecurityUtils.sanitizeInput(event.getTitle()));
                stmt.setString(2, event.getDescription() != null ? 
                    SecurityUtils.sanitizeTextArea(event.getDescription()) : null);
                stmt.setDate(3, event.getDate() != null ? Date.valueOf(event.getDate()) : null);
                stmt.setString(4, event.getLocation() != null ? 
                    SecurityUtils.sanitizeInput(event.getLocation()) : null);
                stmt.setString(5, event.getImage() != null ? 
                    SecurityUtils.sanitizeInput(event.getImage()) : null);
                stmt.setInt(6, event.getMaxParticipants());
                stmt.setInt(7, event.getCreatedBy());
                stmt.setBoolean(8, event.isActive());
                
                int rowsAffected = stmt.executeUpdate();
                
                if (rowsAffected > 0) {
                    conn.commit();
                    
                    // Get generated event ID
                    ResultSet generatedKeys = stmt.getGeneratedKeys();
                    if (generatedKeys.next()) {
                        event.setEventId(generatedKeys.getInt(1));
                    }
                    
                    logger.info("Event created successfully in database");
                    return true;
                } else {
                    conn.rollback();
                    logger.warning("No rows affected during event creation");
                    return false;
                }
                
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
            
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Database error creating event", e);
            
            // Handle specific SQL errors without exposing details
            if (e.getSQLState() != null) {
                switch (e.getSQLState()) {
                    case "23502": // Not null violation
                        logger.warning("Required field missing in event creation");
                        break;
                    case "23514": // Check constraint violation
                        logger.warning("Check constraint violation - invalid event data format");
                        break;
                    case "23503": // Foreign key violation
                        logger.warning("Foreign key violation - invalid user reference");
                        break;
                    default:
                        logger.severe("Unexpected SQL error: " + e.getSQLState());
                }
            }
            return false;
        } catch (SecurityException e) {
            logger.severe("Security violation during event creation: " + e.getMessage());
            return false;
        }
    }
    
    public List<Event> getAllActiveEvents() {
        List<Event> events = new ArrayList<>();
        String sql = "SELECT e.*, COUNT(er.user_id) as current_participants " +
                    "FROM mod4db.event e LEFT JOIN mod4db.eventregistration er ON e.event_id = er.event_id " +
                    "WHERE e.isactive = true GROUP BY e.event_id ORDER BY e.date";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Event event = mapResultSetToEvent(rs);
                events.add(event);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error getting active events", e);
        }
        
        return events;
    }
    
    public List<Event> getAllEvents() {
        List<Event> events = new ArrayList<>();
        String sql = "SELECT * FROM mod4db.event ORDER BY date";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                events.add(mapResultSetToEvent(rs));
            }
            
            logger.info("Retrieved " + events.size() + " total events");
            
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Database error retrieving all events", e);
        }
        
        return events;
    }
    
    public Event getEventById(int eventId) {
        // Validate event ID
        if (!SecurityUtils.isValidUserId(eventId)) { // Reusing validation for positive integers
            logger.warning("Invalid eventId provided: " + eventId);
            return null;
        }
        
        String sql = "SELECT e.*, COUNT(er.user_id) as current_participants " +
                    "FROM mod4db.event e LEFT JOIN mod4db.eventregistration er ON e.event_id = er.event_id " +
                    "WHERE e.event_id = ? GROUP BY e.event_id";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, eventId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToEvent(rs);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error getting event by ID: " + eventId, e);
        }
        
        return null;
    }
    
    public boolean updateEvent(Event event) {
        // Input validation
        if (event == null || !SecurityUtils.isValidUserId(event.getEventId())) {
            logger.warning("Invalid event data for update");
            return false;
        }
        
        if (!isValidEventData(event)) {
            logger.warning("Invalid event data provided for update");
            return false;
        }
        
        String sql = "UPDATE mod4db.event SET title = ?, description = ?, date = ?, location = ?, image = ?, maxparticipants = ?, isactive = ? WHERE event_id = ?";
        
        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false); // Start transaction
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                
                // Sanitize inputs
                stmt.setString(1, SecurityUtils.sanitizeInput(event.getTitle()));
                stmt.setString(2, event.getDescription() != null ? 
                    SecurityUtils.sanitizeTextArea(event.getDescription()) : null);
                stmt.setDate(3, event.getDate() != null ? Date.valueOf(event.getDate()) : null);
                stmt.setString(4, event.getLocation() != null ? 
                    SecurityUtils.sanitizeInput(event.getLocation()) : null);
                stmt.setString(5, event.getImage() != null ? 
                    SecurityUtils.sanitizeInput(event.getImage()) : null);
                stmt.setInt(6, event.getMaxParticipants());
                stmt.setBoolean(7, event.isActive());
                stmt.setInt(8, event.getEventId());
                
                int rowsAffected = stmt.executeUpdate();
                
                if (rowsAffected > 0) {
                    conn.commit();
                    logger.info("Event updated successfully: " + event.getEventId());
                    return true;
                } else {
                    conn.rollback();
                    logger.warning("No event found with ID: " + event.getEventId());
                    return false;
                }
                
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
            
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Database error updating event", e);
            return false;
        } catch (SecurityException e) {
            logger.severe("Security violation during event update: " + e.getMessage());
            return false;
        }
    }
    
    public boolean deleteEvent(int eventId) {
        // Validate event ID
        if (!SecurityUtils.isValidUserId(eventId)) {
            logger.warning("Invalid eventId provided for deletion: " + eventId);
            return false;
        }
        
        String sql = "DELETE FROM mod4db.event WHERE event_id = ?";
        
        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false); // Start transaction
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, eventId);
                
                int rowsAffected = stmt.executeUpdate();
                
                if (rowsAffected > 0) {
                    conn.commit();
                    logger.info("Event deleted successfully: " + eventId);
                    return true;
                } else {
                    conn.rollback();
                    logger.warning("No event found with ID: " + eventId);
                    return false;
                }
                
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
            
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Database error deleting event", e);
            return false;
        }
    }
    
    public List<Event> getEventsByCreator(int creatorId) {
        // Validate creator ID
        if (!SecurityUtils.isValidUserId(creatorId)) {
            logger.warning("Invalid creatorId provided: " + creatorId);
            return new ArrayList<>();
        }
        
        List<Event> events = new ArrayList<>();
        String sql = "SELECT * FROM mod4db.event WHERE createdby = ? ORDER BY date DESC";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, creatorId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                events.add(mapResultSetToEvent(rs));
            }
            
            logger.info("Retrieved " + events.size() + " events for creator: " + creatorId);
            
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Database error retrieving events by creator", e);
        }
        
        return events;
    }
    
    public List<Event> getUpcomingEvents() {
        List<Event> events = new ArrayList<>();
        String sql = "SELECT * FROM mod4db.event WHERE isactive = ? AND date >= ? ORDER BY date";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setBoolean(1, true);
            stmt.setDate(2, Date.valueOf(LocalDate.now()));
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                events.add(mapResultSetToEvent(rs));
            }
            
            logger.info("Retrieved " + events.size() + " upcoming events");
            
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Database error retrieving upcoming events", e);
        }
        
        return events;
    }
    
    public int getEventParticipantCount(int eventId) {
        // Validate event ID
        if (!SecurityUtils.isValidUserId(eventId)) {
            logger.warning("Invalid eventId for participant count: " + eventId);
            return -1;
        }
        
        String sql = "SELECT COUNT(*) FROM mod4db.eventregistration WHERE event_id = ? AND status = ?";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, eventId);
            stmt.setString(2, "confirmed"); // Assuming confirmed status for active participants
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1);
            }
            
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Database error getting participant count", e);
        }
        
        return -1;
    }
    
    private boolean isValidEventData(Event event) {
        // Validate required fields
        if (event.getTitle() == null || event.getTitle().trim().isEmpty()) {
            logger.warning("Event title is required");
            return false;
        }
        
        // Validate title length
        if (event.getTitle().length() > 100) {
            logger.warning("Event title too long");
            return false;
        }
        
        // Validate date is not in the past (except for updates)
        if (event.getDate() != null && event.getDate().isBefore(LocalDate.now().minusDays(1))) {
            logger.warning("Event date cannot be in the past");
            return false;
        }
        
        // Validate max participants
        if (event.getMaxParticipants() < 0) {
            logger.warning("Max participants cannot be negative");
            return false;
        }
        
        // Validate creator ID
        if (!SecurityUtils.isValidUserId(event.getCreatedBy())) {
            logger.warning("Invalid creator ID");
            return false;
        }
        
        // Validate location length if provided
        if (event.getLocation() != null && event.getLocation().length() > 100) {
            logger.warning("Event location too long");
            return false;
        }
        
        // Validate description length if provided
        if (event.getDescription() != null && event.getDescription().length() > 2000) {
            logger.warning("Event description too long");
            return false;
        }
        
        return true;
    }
    
    private Event mapResultSetToEvent(ResultSet rs) throws SQLException {
        try {
            Event event = new Event();
            event.setEventId(rs.getInt("event_id"));
            event.setTitle(rs.getString("title"));
            event.setDescription(rs.getString("description"));
            
            Date date = rs.getDate("date");
            if (date != null) {
                event.setDate(date.toLocalDate());
            }
            
            event.setLocation(rs.getString("location"));
            event.setImage(rs.getString("image"));
            event.setMaxParticipants(rs.getInt("maxparticipants"));
            event.setCreatedBy(rs.getInt("createdby"));
            
            Timestamp createdAt = rs.getTimestamp("createdat");
            if (createdAt != null) {
                event.setCreatedAt(createdAt.toLocalDateTime());
            }
            
            event.setActive(rs.getBoolean("isactive"));
            
            return event;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error mapping ResultSet to Event", e);
            throw new SQLException("Error mapping event data", e);
        }
    }

    public boolean enrollUserInEvent(int userId, int eventId) {
    // Implementation to enroll a user in an event
    // Connect to database, execute SQL, etc.
    try {
        // Example implementation:
        // connection = getConnection();
        // String sql = "INSERT INTO event_enrollments (user_id, event_id) VALUES (?, ?)";
        // PreparedStatement statement = connection.prepareStatement(sql);
        // statement.setInt(1, userId);
        // statement.setInt(2, eventId);
        // int rowsInserted = statement.executeUpdate();
        // return rowsInserted > 0;
        
        // Placeholder return until implemented
        return true;
    } catch (Exception e) {
        e.printStackTrace();
        return false;
    }
}
}
