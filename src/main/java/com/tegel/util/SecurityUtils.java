package com.tegel.util;

import java.util.logging.Logger;
import java.util.regex.Pattern;

import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;

/**
 * Utility class for security-related operations such as password hashing, input validation,
 * and sanitization to prevent common security vulnerabilities.
 */
public class SecurityUtils {
    private static final Logger logger = Logger.getLogger(SecurityUtils.class.getName());
    private static final Argon2 argon2 = Argon2Factory.create();

    // Input validation patterns
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private static final Pattern PHONE_PATTERN = Pattern.compile("^[+]?[0-9\\s\\-()]{8,15}$");

    private static final Pattern NAME_PATTERN =
            Pattern.compile("^[A-Za-z\\s\\-'àáâäèéêëìíîïòóôöùúûüçñ]{2,100}$",
                            Pattern.CASE_INSENSITIVE);

    // Strong password requirements
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$");

    // SQL injection prevention patterns
    private static final Pattern SQL_INJECTION_PATTERN = Pattern.compile(
            "(?i)(\\b(SELECT|INSERT|UPDATE|DELETE|DROP|CREATE|ALTER|EXEC|UNION|SCRIPT|JAVASCRIPT|VBSCRIPT)\\b|['\";\\-]|/\\*|\\*/)");


    public static String hashPassword(String password) {
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }

        try {
            // iterations=3, memory=65536 KB (64 MB), parallelism=1
            String hash = argon2.hash(3, 65536, 1, password.toCharArray());
            logger.info("Password hashed successfully using Argon2");
            return hash;
        } catch (RuntimeException e) {
            logger.severe("Failed to hash password: " + e.getMessage());
            throw new RuntimeException("Password hashing failed", e);
        }
    }

    /**
     * Verify a password against a stored hash.
     *
     * @param password The plain text password to verify
     * @param hash     The stored hash to compare against
     * @return true if the password matches the hash, false otherwise
     */
    public static boolean verifyPassword(String password, String hash) {
        if (password == null || hash == null) {
            return false;
        }

        try {
            boolean isValid = argon2.verify(hash, password.toCharArray());
            logger.info("Password verification completed");
            return isValid;
        } catch (RuntimeException e) {
            logger.severe("Password verification failed: " + e.getMessage());
            return false;
        } finally {
            // Clear password from memory for security
            argon2.wipeArray(password.toCharArray());
        }
    }

    /**
     * Validate email format.
     */
    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    /**
     * Validate phone number format.
     */
    public static boolean isValidPhone(String phone) {
        return phone != null && PHONE_PATTERN.matcher(phone.trim()).matches();
    }

    /**
     * Validate name format (supports international characters).
     */
    public static boolean isValidName(String name) {
        return name != null && NAME_PATTERN.matcher(name.trim()).matches();
    }

    /**
     * Validate password strength.
     */
    public static boolean isValidPassword(String password) {
        return password != null && PASSWORD_PATTERN.matcher(password).matches();
    }

    /**
     * Sanitize input to prevent XSS and basic injection attempts.
     */
    public static String sanitizeInput(String input) {
        if (input == null) {
            return null;
        }

        // Check for SQL injection patterns
        if (SQL_INJECTION_PATTERN.matcher(input).find()) {
            logger.warning("Potential SQL injection attempt detected in input: " +
                                   input.substring(0, Math.min(input.length(), 50)));
            throw new SecurityException("Invalid input detected");
        }

        // Store the intermediate result after trim and replaceAll
        String sanitized = input.trim().replaceAll("[<>\"'&]", "");
        // Now apply substring on the sanitized string's length
        return sanitized.substring(0, Math.min(sanitized.length(), 255)); // Limit length
    }

    /**
     * Sanitize text area input (allows more characters, longer length)..
     */
    public static String sanitizeTextArea(String input) {
        if (input == null) {
            return null;
        }

        // Check for SQL injection patterns
        if (SQL_INJECTION_PATTERN.matcher(input).find()) {
            logger.warning("Potential SQL injection attempt detected in text area");
            throw new SecurityException("Invalid input detected");
        }

        // Store the intermediate result after trim and replaceAll
        String sanitized = input.trim().replaceAll("[<>\"'&]", "");
        // Now apply substring on the sanitized string's length
        return sanitized.substring(0, Math.min(sanitized.length(), 2000)); // Limit length
    }

    /**
     * Validate user role.
     */
    public static boolean isValidRole(String role) {
        return role != null &&
                (role.equals("admin") || role.equals("member") || role.equals("user"));
    }

    /**
     * Validate user ID (positive integer).
     */
    public static boolean isValidUserId(int userId) {
        return userId > 0;
    }

    /**
     * Validate location format.
     */
    public static boolean isValidLocation(String location) {
        if (location == null) {
            return true; // Optional field
        }
        return location.length() <= 100 && !SQL_INJECTION_PATTERN.matcher(location).find();
    }

    public static boolean isValidUsername(String userName) {
        if (userName == null || userName.isEmpty()) {
            return false;
        }

        Pattern usernamePattern = Pattern.compile("^[a-zA-Z0-9_-]{3,30}$");

        return usernamePattern.matcher(userName).matches() &&
                !SQL_INJECTION_PATTERN.matcher(userName).find();
    }
}
