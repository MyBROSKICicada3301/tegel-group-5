package com.tegel.servlet;

import com.tegel.util.JwtUtil;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import com.tegel.dao.UserDAO;
import com.tegel.model.User;
import com.tegel.util.SecurityUtils;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;

@MultipartConfig
@WebServlet("/login")
public class LoginServlet extends HttpServlet {
    private static final Logger logger = Logger.getLogger(LoginServlet.class.getName());
    private UserDAO userDAO = new UserDAO();

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
                // create JWT based session
                // turn the information that will be stored into claims
                Map<String, Object> claims = new HashMap<>();
                claims.put("email", email);
                claims.put("role", user.getRole());

                // create token
                String token = JwtUtil.generateToken(Integer.toString(user.getUserId()), claims, JwtUtil.EXPIRATION_TIME);

                // create and add a cookie
                Cookie cookie = new Cookie("token", token);
                cookie.setHttpOnly(true);
                cookie.setPath("/");
                cookie.setMaxAge((int)(JwtUtil.EXPIRATION_TIME / 1000)); // in seconds
                response.addCookie(cookie);
                
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
