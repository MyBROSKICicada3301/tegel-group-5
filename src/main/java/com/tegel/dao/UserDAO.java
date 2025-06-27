package com.tegel.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.tegel.model.User;
import com.tegel.util.SecurityUtils;

/**
 * UserDAO is responsible for interacting with the database to perform CRUD operations
 * on User entities.
 * It provides methods to create, retrieve, update, and delete users, as well as manage user roles.
 */
public class UserDAO {
    private static final Logger logger = Logger.getLogger(UserDAO.class.getName());

    /**
     * Creates a new user in the database.
     *
     * @param user The User object containing user details.
     * @return true if the user was created successfully, false otherwise.
     */
    public boolean createUser(User user) {
        // Input validation before database operation
        if (user == null) {
            logger.warning("Attempted to create null user");
            return false;
        }

        if (!SecurityUtils.isValidEmail(user.getEmail()) ||
                !SecurityUtils.isValidName(user.getFullName()) || user.getPasswordHash() == null ||
                user.getPasswordHash().isEmpty()) {
            logger.warning("Invalid user data provided for creation");
            return false;
        }

        String sql = "SELECT mod4db.create_user(?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Sanitize inputs before database call
            stmt.setString(1, SecurityUtils.sanitizeInput(user.getEmail().toLowerCase().trim()));
            stmt.setString(2, user.getPasswordHash()); // Already hashed, don't sanitize
            stmt.setString(3, user.getPhoneNumber() != null ?
                    SecurityUtils.sanitizeInput(user.getPhoneNumber()) : null);
            stmt.setDate(4, user.getDateOfBirth() != null ? Date.valueOf(user.getDateOfBirth()) :
                    null);
            stmt.setString(5, user.getDietRes() != null ?
                    SecurityUtils.sanitizeTextArea(user.getDietRes()) : null);
            stmt.setString(6, SecurityUtils.sanitizeInput(user.getFullName()));
            stmt.setString(7, user.getNickName() != null ?
                    SecurityUtils.sanitizeInput(user.getNickName()) : null);

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

    /**
     * Retrieves a user by their email address.
     *
     * @param email The email address of the user to retrieve.
     * @return The User object if found, null otherwise.
     */
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

    /**
     * Retrieves all users from the database.
     *
     * @return A list of User objects representing all users.
     */
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

    /**
     * Updates the role of a user in the database.
     *
     * @param userId  The ID of the user whose role is to be updated.
     * @param newRole The new role to assign to the user.
     * @return true if the role was updated successfully, false otherwise.
     */
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

    /**
     * Deletes a user from the database.
     *
     * @param userId The ID of the user to delete.
     * @return true if the user was deleted successfully, false otherwise.
     */
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

    /**
     * Retrieves a user by their ID.
     *
     * @param userId The ID of the user to retrieve.
     * @return The User object if found, null otherwise.
     */
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

    /**
     * Updates the details of an existing user.
     *
     * @param user The User object containing updated user details.
     * @return true if the user was updated successfully, false otherwise.
     */
    public boolean updateUser(User user) {
        // Input validation before database operation
        if (user == null || !SecurityUtils.isValidUserId(user.getUserId())) {
            logger.warning("Invalid user data for update");
            return false;
        }

        String sql = "SELECT mod4db.update_user(?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Set parameters
            stmt.setInt(1, user.getUserId());
            stmt.setString(2, user.getEmail());
            stmt.setString(3, user.getPasswordHash());
            stmt.setString(4, user.getPhoneNumber());
            stmt.setDate(5, user.getDateOfBirth() != null ?
                    Date.valueOf(user.getDateOfBirth()) : null);
            stmt.setString(6, user.getDietRes());
            stmt.setString(7, user.getFullName());
            stmt.setString(8, user.getNickName());

            // Execute and get result
            ResultSet rs = stmt.executeQuery();
            boolean success = false;
            if (rs.next()) {
                success = rs.getBoolean(1);
            }

            return success;

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Database error updating user: " + user.getUserId(), e);
            return false;
        }
    }

    /**
     * Maps a ResultSet to a User object.
     *
     * @param rs The ResultSet containing user data.
     * @return A User object populated with data from the ResultSet.
     * @throws SQLException if an error occurs while accessing the ResultSet.
     */
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

    /**
     * Updates a user's password.
     *
     * @param userId The ID of the user whose password is to be updated.
     * @param newPasswordHash The new password hash to set.
     * @return true if the password was updated successfully, false otherwise.
     */
    public boolean updateUserPassword(int userId, String newPasswordHash) {
        // Validate inputs
        if (!SecurityUtils.isValidUserId(userId) || newPasswordHash == null || newPasswordHash.isEmpty()) {
            logger.warning("Invalid userId or password provided for update");
            return false;
        }

        String sql = "UPDATE mod4db.users SET passwordhash = ? WHERE user_id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            conn.setAutoCommit(false); // Start transaction

            stmt.setString(1, newPasswordHash);
            stmt.setInt(2, userId);

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                conn.commit();
                logger.info("Password updated successfully for userId: " + userId);
                return true;
            } else {
                conn.rollback();
                logger.warning("No user found with userId: " + userId);
                return false;
            }

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Database error updating user password", e);
            return false;
        }
    }
}
