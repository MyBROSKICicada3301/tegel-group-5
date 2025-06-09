package com.tegel.util;

import java.time.LocalDate;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;

public class SecurityUtils {
    private static final Logger logger = Logger.getLogger(SecurityUtils.class.getName());
    private static final Argon2 argon2 = Argon2Factory.create();
    
    // Input validation patterns
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );
    
    private static final Pattern PHONE_PATTERN = Pattern.compile(
        "^[+]?[0-9\\s\\-()]{8,15}$"
    );
    
    private static final Pattern NAME_PATTERN = Pattern.compile(
        "^[A-Za-z\\s\\-'àáâäèéêëìíîïòóôöùúûüçñ]{2,100}$", Pattern.CASE_INSENSITIVE
    );
    
    // Strong password requirements
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
        "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$"
    );
    
    // SQL injection prevention patterns
    private static final Pattern SQL_INJECTION_PATTERN = Pattern.compile(
        "(?i)(\\b(SELECT|INSERT|UPDATE|DELETE|DROP|CREATE|ALTER|EXEC|UNION|SCRIPT|JAVASCRIPT|VBSCRIPT)\\b|['\";\\-\\-]|/\\*|\\*/)"
    );
    
    /**
     * Hash password using Argon2id (recommended variant)
     */
    public static String hashPassword(String password) {
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }
        
        try {
            // Using Argon2id with recommended parameters
            // iterations=3, memory=65536 KB (64 MB), parallelism=1
            String hash = argon2.hash(3, 65536, 1, password.toCharArray());
            logger.info("Password hashed successfully using Argon2");
            return hash;
        } catch (Exception e) {
            logger.severe("Failed to hash password: " + e.getMessage());
            throw new RuntimeException("Password hashing failed", e);
        }
    }
    
    /**
     * Verify password against Argon2 hash
     */
    public static boolean verifyPassword(String password, String hash) {
        if (password == null || hash == null) {
            return false;
        }
        
        try {
            boolean isValid = argon2.verify(hash, password.toCharArray());
            logger.info("Password verification completed");
            return isValid;
        } catch (Exception e) {
            logger.severe("Password verification failed: " + e.getMessage());
            return false;
        } finally {
            // Clear password from memory for security
            argon2.wipeArray(password.toCharArray());
        }
    }
    
    /**
     * Validate email format
     */
    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email.trim()).matches();
    }
    
    /**
     * Validate phone number format
     */
    public static boolean isValidPhone(String phone) {
        return phone != null && PHONE_PATTERN.matcher(phone.trim()).matches();
    }
    
    /**
     * Validate name format (supports international characters)
     */
    public static boolean isValidName(String name) {
        return name != null && NAME_PATTERN.matcher(name.trim()).matches();
    }
    
    /**
     * Validate password strength
     */
    public static boolean isValidPassword(String password) {
        return password != null && PASSWORD_PATTERN.matcher(password).matches();
    }
    
    /**
     * Sanitize input to prevent XSS and basic injection attempts
     */
    public static String sanitizeInput(String input) {
        if (input == null) return null;
        
        // Check for SQL injection patterns
        if (SQL_INJECTION_PATTERN.matcher(input).find()) {
            logger.warning("Potential SQL injection attempt detected in input: " + input.substring(0, Math.min(input.length(), 50)));
            throw new SecurityException("Invalid input detected");
        }
        
        return input.trim()
                   .replaceAll("[<>\"'&]", "") // Remove XSS characters
                   .substring(0, Math.min(input.length(), 255)); // Limit length
    }
    
    /**
     * Sanitize text area input (allows more characters, longer length)
     */
    public static String sanitizeTextArea(String input) {
        if (input == null) return null;
        
        // Check for SQL injection patterns
        if (SQL_INJECTION_PATTERN.matcher(input).find()) {
            logger.warning("Potential SQL injection attempt detected in text area");
            throw new SecurityException("Invalid input detected");
        }
        
        return input.trim()
                   .replaceAll("[<>\"'&]", "")
                   .substring(0, Math.min(input.length(), 2000));
    }
    
    /**
     * Validate user role
     */
    public static boolean isValidRole(String role) {
        return role != null && (role.equals("admin") || role.equals("member") || role.equals("user"));
    }
    
    /**
     * Validate user ID (positive integer)
     */
    public static boolean isValidUserId(int userId) {
        return userId > 0;
    }
    
    /**
     * Validate event title format
     */
    public static boolean isValidEventTitle(String title) {
        return title != null && 
               !title.trim().isEmpty() && 
               title.length() <= 100 &&
               !SQL_INJECTION_PATTERN.matcher(title).find();
    }
    
    /**
     * Validate location format
     */
    public static boolean isValidLocation(String location) {
        if (location == null) return true; // Optional field
        return location.length() <= 100 &&
               !SQL_INJECTION_PATTERN.matcher(location).find();
    }
    
    /**
     * Validate image URL/path format
     */
    public static boolean isValidImagePath(String imagePath) {
        if (imagePath == null) return true; // Optional field
        
        // Basic URL/path validation pattern
        Pattern imagePattern = Pattern.compile(
            "^(https?://[^\\s/$.?#].[^\\s]*\\.(jpg|jpeg|png|gif|webp)|[a-zA-Z0-9_/.-]+\\.(jpg|jpeg|png|gif|webp))$",
            Pattern.CASE_INSENSITIVE
        );
        
        return imagePath.length() <= 255 &&
               imagePattern.matcher(imagePath).matches() &&
               !SQL_INJECTION_PATTERN.matcher(imagePath).find();
    }
    
    /**
     * Validate event date
     */
    public static boolean isValidEventDate(LocalDate eventDate) {
        if (eventDate == null) return false;
        
        // Event cannot be more than 2 years in the future
        LocalDate maxFutureDate = LocalDate.now().plusYears(2);
        
        // Event cannot be more than 1 day in the past (for updates)
        LocalDate minPastDate = LocalDate.now().minusDays(1);
        
        return eventDate.isAfter(minPastDate) && eventDate.isBefore(maxFutureDate);
    }
    
    /**
     * Validate max participants count
     */
    public static boolean isValidMaxParticipants(int maxParticipants) {
        return maxParticipants >= 0 && maxParticipants <= 10000; // Reasonable upper limit
    }
    
    /**
     * Clean up Argon2 resources
     */
    public static void cleanup() {
        // using wipeArray with explicit type cast to resolve ambiguity in case of cleanup
        argon2.wipeArray((char[]) null);
    }
}
