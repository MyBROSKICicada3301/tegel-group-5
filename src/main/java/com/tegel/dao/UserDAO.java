package com.tegel.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.tegel.model.User;
import com.tegel.util.SecurityUtils;

public class UserDAO {
    private static final Logger logger = Logger.getLogger(UserDAO.class.getName());
    
    public boolean createUser(User user) {
        // Input validation before database operation
        if (user == null) {
            logger.warning("Attempted to create null user");
            return false;
        }
        
        if (!SecurityUtils.isValidEmail(user.getEmail()) ||
            !SecurityUtils.isValidName(user.getFullName()) ||
            user.getPasswordHash() == null || user.getPasswordHash().isEmpty()) {
            logger.warning("Invalid user data provided for creation");
            return false;
        }
        
        String sql = "SELECT mod4db.create_user(?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            // Sanitize inputs before database call
            stmt.setString(1, SecurityUtils.sanitizeInput(user.getEmail().toLowerCase().trim()));
            stmt.setString(2, user.getPasswordHash()); // Already hashed, don't sanitize
            stmt.setString(3, user.getPhoneNumber() != null ? SecurityUtils.sanitizeInput(user.getPhoneNumber()) : null);
            stmt.setDate(4, user.getDateOfBirth() != null ? Date.valueOf(user.getDateOfBirth()) : null);
            stmt.setString(5, user.getDietRes() != null ? SecurityUtils.sanitizeTextArea(user.getDietRes()) : null);
            stmt.setString(6, SecurityUtils.sanitizeInput(user.getFullName()));
            stmt.setString(7, user.getNickName() != null ? SecurityUtils.sanitizeInput(user.getNickName()) : null);
            
            stmt.execute();
            logger.info("User created successfully in database");
            return true;
            
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Database error creating user", e);
            
            // Handle specific SQL errors without exposing details
            if (e.getSQLState() != null) {
                switch (e.getSQLState()) {
                    case "23505": // Unique violation
                        logger.warning("Unique constraint violation - user may already exist");
                        break;
                    case "23514": // Check constraint violation
                        logger.warning("Check constraint violation - invalid data format");
                        break;
                    case "23502": // Not null violation
                        logger.warning("Required field missing");
                        break;
                    default:
                        logger.severe("Unexpected SQL error: " + e.getSQLState());
                }
            }
            return false;
        } catch (SecurityException e) {
            logger.severe("Security violation during user creation: " + e.getMessage());
            return false;
        }
    }
    
    public User getUserByEmail(String email) {
        // Input validation
        if (!SecurityUtils.isValidEmail(email)) {
            logger.warning("Invalid email format provided: " + email);
            return null;
        }
        
        String sql = "SELECT * FROM mod4db.users WHERE email = ?";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            // Sanitize and normalize email
            String sanitizedEmail = SecurityUtils.sanitizeInput(email.toLowerCase().trim());
            stmt.setString(1, sanitizedEmail);
            
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToUser(rs);
            }
            
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Database error retrieving user", e);
        } catch (SecurityException e) {
            logger.severe("Security violation during user retrieval: " + e.getMessage());
        }
        
        return null;
    }
    
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM mod4db.users ORDER BY full_name";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
            
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Database error retrieving all users", e);
        }
        
        return users;
    }
    
    public boolean updateUserRole(int userId, String newRole) {
        // Validate inputs
        if (!SecurityUtils.isValidUserId(userId) || !SecurityUtils.isValidRole(newRole)) {
            logger.warning("Invalid userId or role provided for update");
            return false;
        }
        
        String sql = "SELECT mod4db.update_role(?, ?)";
        
        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false); // Start transaction
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, userId);
                stmt.setString(2, SecurityUtils.sanitizeInput(newRole));
                
                stmt.execute();
                conn.commit();
                
                logger.info("User role updated successfully for userId: " + userId);
                return true;
                
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
            
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Database error updating user role", e);
            return false;
        } catch (SecurityException e) {
            logger.severe("Security violation during role update: " + e.getMessage());
            return false;
        }
    }
    
    public boolean deleteUser(int userId) {
        // Validate user ID
        if (!SecurityUtils.isValidUserId(userId)) {
            logger.warning("Invalid userId provided for deletion: " + userId);
            return false;
        }
        
        String sql = "DELETE FROM mod4db.users WHERE user_id = ?";
        
        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false); // Start transaction
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, userId);
                
                int rowsAffected = stmt.executeUpdate();
                
                if (rowsAffected > 0) {
                    conn.commit();
                    logger.info("User deleted successfully: " + userId);
                    return true;
                } else {
                    conn.rollback();
                    logger.warning("No user found with userId: " + userId);
                    return false;
                }
                
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
            
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Database error deleting user", e);
            return false;
        }
    }
    
    public String getPasswordHash(String email) {
        // Validate email
        if (!SecurityUtils.isValidEmail(email)) {
            logger.warning("Invalid email format for password retrieval");
            return null;
        }
        
        String sql = "SELECT mod4db.read_passwordhash(?)";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            String sanitizedEmail = SecurityUtils.sanitizeInput(email.toLowerCase().trim());
            stmt.setString(1, sanitizedEmail);
            
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getString(1);
            }
            
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Database error retrieving password hash", e);
        } catch (SecurityException e) {
            logger.severe("Security violation during password hash retrieval: " + e.getMessage());
        }
        
        return null;
    }
    
    // Get user by ID
    public User getUserById(int userId) {
        // Validate user ID
        if (!SecurityUtils.isValidUserId(userId)) {
            logger.warning("Invalid userId provided: " + userId);
            return null;
        }

        String sql = "SELECT * FROM mod4db.users WHERE user_id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToUser(rs);
            } else {
                logger.warning("No user found with ID: " + userId);
                return null;
            }

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Database error retrieving user by ID: " + userId, e);
            return null;
        }
    }

    // Update existing user
    public boolean updateUser(User user) {
        // Input validation before database operation
        if (user == null || !SecurityUtils.isValidUserId(user.getUserId())) {
            logger.warning("Invalid user data for update");
            return false;
        }

        String sql = "UPDATE mod4db.users SET " +
                    "email = ?, " +
                    "passwordhash = ?, " +
                    "phonenumber = ?, " +
                    "dateofbirth = ?, " +
                    "dietres = ?, " +
                    "full_name = ?, " +
                    "nick_name = ? " +
                    "WHERE user_id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Sanitize inputs before database call
            stmt.setString(1, SecurityUtils.sanitizeInput(user.getEmail().toLowerCase().trim()));
            stmt.setString(2, user.getPasswordHash()); // Already hashed, don't sanitize
            stmt.setString(3, user.getPhoneNumber() != null ?
                SecurityUtils.sanitizeInput(user.getPhoneNumber()) : null);
            stmt.setDate(4, user.getDateOfBirth() != null ?
                Date.valueOf(user.getDateOfBirth()) : null);
            stmt.setString(5, user.getDietRes() != null ?
                SecurityUtils.sanitizeTextArea(user.getDietRes()) : null);
            stmt.setString(6, SecurityUtils.sanitizeInput(user.getFullName()));
            stmt.setString(7, user.getNickName() != null ?
                SecurityUtils.sanitizeInput(user.getNickName()) : null);
            stmt.setInt(8, user.getUserId());

            int rowsUpdated = stmt.executeUpdate();
            boolean success = rowsUpdated > 0;

            if (success) {
                logger.info("User updated successfully: " + user.getUserId());
            } else {
                logger.warning("No user updated with ID: " + user.getUserId());
            }

            return success;

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Database error updating user: " + user.getUserId(), e);
            return false;
        }
    }

    // Helper method to map ResultSet to User object
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setUserId(rs.getInt("user_id"));
        user.setEmail(rs.getString("email"));
        user.setPasswordHash(rs.getString("passwordhash"));
        user.setPhoneNumber(rs.getString("phonenumber"));

        Date dateOfBirth = rs.getDate("dateofbirth");
        if (dateOfBirth != null) {
            user.setDateOfBirth(dateOfBirth.toLocalDate());
        }

        Timestamp createDate = rs.getTimestamp("createdate");
        if (createDate != null) {
            user.setCreateDate(createDate.toLocalDateTime());
        }

        user.setDietRes(rs.getString("dietres"));
        user.setRole(rs.getString("role"));
        user.setFullName(rs.getString("full_name"));
        user.setNickName(rs.getString("nick_name"));

        return user;
    }
}
