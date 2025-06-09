package com.tegel.servlet;

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

@WebServlet("/login")
public class LoginServlet extends HttpServlet {
    private static final Logger logger = Logger.getLogger(LoginServlet.class.getName());
    private UserDAO userDAO = new UserDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        
        try {
            // Validate inputs
            if (email == null || password == null || email.trim().isEmpty() || password.isEmpty()) {
                logger.warning("Login attempt with missing credentials");
                response.sendRedirect("login.html?error=invalid");
                return;
            }
            
            // Sanitize email
            email = SecurityUtils.sanitizeInput(email).toLowerCase().trim();
            
            if (!SecurityUtils.isValidEmail(email)) {
                logger.warning("Login attempt with invalid email format: " + email);
                response.sendRedirect("login.html?error=invalid");
                return;
            }
            
            // Get user from database
            User user = userDAO.getUserByEmail(email);
            
            if (user != null && SecurityUtils.verifyPassword(password, user.getPasswordHash())) {
                // Successful login
                HttpSession session = request.getSession();
                session.setAttribute("user", user);
                session.setAttribute("userId", user.getUserId());
                session.setAttribute("userRole", user.getRole());
                
                // Set session timeout (30 minutes)
                session.setMaxInactiveInterval(1800);
                
                logger.info("Successful login for user: " + email);
                
                // Redirect based on role
                if ("admin".equals(user.getRole())) {
                    response.sendRedirect("adminindex.html");
                } else {
                    response.sendRedirect("index.html");
                }
            } else {
                logger.warning("Failed login attempt for email: " + email);
                response.sendRedirect("login.html?error=invalid");
            }
            
        } catch (SecurityException e) {
            logger.severe("Security violation during login: " + e.getMessage());
            response.sendRedirect("login.html?error=invalid");
        } catch (Exception e) {
            logger.severe("Error during login: " + e.getMessage());
            response.sendRedirect("login.html?error=server");
        }
    }
}
