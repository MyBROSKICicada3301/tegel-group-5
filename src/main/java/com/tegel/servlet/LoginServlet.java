package com.tegel.servlet;

import jakarta.servlet.annotation.MultipartConfig;
import java.io.IOException;
import java.util.logging.Logger;

import com.tegel.dao.UserDAO;
import com.tegel.model.User;
import com.tegel.util.SecurityUtils;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Servlet to handle user login functionality.
 * Validates user credentials and manages session.
 */
@MultipartConfig
@WebServlet("/login")
public class LoginServlet extends HttpServlet {
    private static final Logger logger = Logger.getLogger(LoginServlet.class.getName());
    private final UserDAO userDAO = new UserDAO();

    /**
     * Handles POST requests for user login.
     * Validates email and password, checks against the database,
     * and manages user session.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        System.out.println("doPost called in /login");

        String email = request.getParameter("email");
        String password = request.getParameter("password");

        try {
            // Validate inputs
            if (email == null || password == null || email.trim().isEmpty() || password.isEmpty()) {
                logger.warning("Login attempt with missing credentials");
                response.sendRedirect("login.html?error=missing");
                return;
            }

            // Sanitize email
            email = SecurityUtils.sanitizeInput(email).toLowerCase().trim();

            if (!SecurityUtils.isValidEmail(email)) {
                logger.warning("Login attempt with invalid email format: " + email);
                response.sendRedirect("login.html?error=invalid_email");
                return;
            }

            // Get user from database
            User user = userDAO.getUserByEmail(email);

            if (user == null) {
                // User doesn't exist
                logger.warning("Login attempt with non-existent email: " + email);
                response.sendRedirect("login.html?error=user_not_found");
                return;
            }

            if (!SecurityUtils.verifyPassword(password, user.getPasswordHash())) {
                // Wrong password
                logger.warning("Failed login attempt due to wrong password for email: " + email);
                response.sendRedirect("login.html?error=wrong_password");
                return;
            }

            // Successful login
            HttpSession session = request.getSession();
            session.setAttribute("user", user);
            session.setAttribute("userId", user.getUserId());
            session.setAttribute("role", user.getRole()); // Changed from "userRole" to "role"

            // Set session timeout (2 minutes)
            session.setMaxInactiveInterval(12000); // 12000 seconds = 200 minutes

            logger.info("Successful login for user: " + email);

            // Redirect based on role
            if ("admin".equals(user.getRole())) {
                response.sendRedirect("adminindex.html");
            } else {
                response.sendRedirect("index.html");
            }

        } catch (SecurityException e) {
            logger.severe("Security violation during login: " + e.getMessage());
            response.sendRedirect("login.html?error=security");
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (RuntimeException e) {
            logger.severe("Error during login: " + e.getMessage());
            response.sendRedirect("login.html?error=server");
        }
    }

    /**
     * Handles GET requests by redirecting to the login page.
     * This is to ensure that GET requests do not perform any actions.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Redirect GET requests to the login page
        response.sendRedirect("login.html");
    }
}
