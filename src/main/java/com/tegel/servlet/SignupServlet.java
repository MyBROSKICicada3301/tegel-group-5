package com.tegel.servlet;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.tegel.dao.UserDAO;
import com.tegel.model.User;
import com.tegel.util.SecurityUtils;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/signup")
public class SignupServlet extends HttpServlet {
    private static final Logger logger = Logger.getLogger(SignupServlet.class.getName());
    private UserDAO userDAO = new UserDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        try {
            // Get parameters with null checks
            String fullName = request.getParameter("fullName");
            String nickname = request.getParameter("nickname");
            String email = request.getParameter("emailAddress");
            String password = request.getParameter("password");
            String dobString = request.getParameter("dob");
            String phone = request.getParameter("phone");
            String dietary = request.getParameter("dietary");
            
            // Validate required fields
            if (!isValidRequiredFields(fullName, email, password, dobString, phone)) {
                logger.warning("Missing required fields in signup attempt");
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"error\":\"Missing required fields\"}");
                return;
            }
            
            // Sanitize inputs (except password which will be hashed)
            try {
                fullName = SecurityUtils.sanitizeInput(fullName);
                nickname = nickname != null ? SecurityUtils.sanitizeInput(nickname) : null;
                email = SecurityUtils.sanitizeInput(email).toLowerCase().trim();
                phone = SecurityUtils.sanitizeInput(phone);
                dietary = dietary != null ? SecurityUtils.sanitizeTextArea(dietary) : null;
            } catch (SecurityException e) {
                logger.warning("Security violation in signup data: " + e.getMessage());
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"error\":\"Invalid input data detected\"}");
                return;
            }
            
            // Validate input formats
            if (!SecurityUtils.isValidEmail(email)) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"error\":\"Invalid email format\"}");
                return;
            }
            
            if (!SecurityUtils.isValidName(fullName)) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"error\":\"Invalid name format\"}");
                return;
            }
            
            if (!SecurityUtils.isValidPassword(password)) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"error\":\"Password must be at least 8 characters with uppercase, lowercase, number and special character\"}");
                return;
            }
            
            if (!SecurityUtils.isValidPhone(phone)) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"error\":\"Invalid phone number format\"}");
                return;
            }
            
            // Check if user already exists
            if (userDAO.getUserByEmail(email) != null) {
                logger.info("Signup attempt with existing email: " + email);
                response.setStatus(HttpServletResponse.SC_CONFLICT);
                response.getWriter().write("{\"error\":\"User already exists\"}");
                return;
            }
            
            // Parse and validate date of birth
            LocalDate dateOfBirth;
            try {
                dateOfBirth = LocalDate.parse(dobString);
                
                // Validate age (must be at least 13 years old)
                if (dateOfBirth.isAfter(LocalDate.now().minusYears(13))) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    response.getWriter().write("{\"error\":\"Must be at least 13 years old\"}");
                    return;
                }
                
                // Validate reasonable birth year (not too old)
                if (dateOfBirth.isBefore(LocalDate.now().minusYears(120))) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    response.getWriter().write("{\"error\":\"Invalid birth date\"}");
                    return;
                }
            } catch (DateTimeParseException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"error\":\"Invalid date format\"}");
                return;
            }
            
            // Hash password using Argon2
            String hashedPassword;
            try {
                hashedPassword = SecurityUtils.hashPassword(password);
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Password hashing failed", e);
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().write("{\"error\":\"Registration failed\"}");
                return;
            }
            
            // Create user object
            User user = new User(email, hashedPassword, phone, dateOfBirth, 
                               dietary, fullName, nickname);
            
            // Save to database
            if (userDAO.createUser(user)) {
                logger.info("User created successfully: " + email);
                response.sendRedirect("login.html?success=registered");
            } else {
                logger.severe("Failed to create user in database: " + email);
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().write("{\"error\":\"Registration failed\"}");
            }
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Unexpected error during user registration", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\":\"Server error occurred\"}");
        }
    }
    
    private boolean isValidRequiredFields(String fullName, String email, String password, 
                                        String dob, String phone) {
        return fullName != null && !fullName.trim().isEmpty() &&
               email != null && !email.trim().isEmpty() &&
               password != null && !password.isEmpty() &&
               dob != null && !dob.trim().isEmpty() &&
               phone != null && !phone.trim().isEmpty();
    }
}
