package com.tegel.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import com.tegel.model.Event;

public class EventDAO {
    
    public boolean createEvent(Event event) {
        String sql = "INSERT INTO mod4db.event (title, description, date, location, image, maxparticipants, createdby, isactive) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, event.getTitle());
            stmt.setString(2, event.getDescription());
            stmt.setDate(3, Date.valueOf(event.getDate()));
            stmt.setString(4, event.getLocation());
            stmt.setString(5, event.getImage());
            stmt.setInt(6, event.getMaxParticipants());
            stmt.setInt(7, event.getCreatedBy());
            stmt.setBoolean(8, event.isActive());
            
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public List<Event> getAllActiveEvents() {
        List<Event> events = new ArrayList<>();
        String sql = "SELECT * FROM mod4db.event WHERE isactive = true ORDER BY date";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                events.add(mapResultSetToEvent(rs));
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
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
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return events;
    }
    
    private Event mapResultSetToEvent(ResultSet rs) throws SQLException {
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
    }
}
