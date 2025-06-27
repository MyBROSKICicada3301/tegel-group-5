package com.tegel.servlet;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.tegel.dao.UserDAO;
import com.tegel.util.SecurityUtils;

/**
 * Servlet to handle admin password reset for users.
 */
@WebServlet("/admin/users/password/*")
public class AdminPasswordResetServlet extends HttpServlet {
    private static final Logger logger =
            Logger.getLogger(AdminPasswordResetServlet.class.getName());
    private UserDAO userDAO = new UserDAO();
    private Gson gson = new Gson();

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        HttpSession session = request.getSession(false);

        try {
            // Check if user is logged in and is an admin
            if (session == null || session.getAttribute("userId") == null ||
                    !"admin".equals(session.getAttribute("role"))) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                out.print("{\"success\": false, \"error\": \"Admin privileges required\"}");
                return;
            }

            // Extract userId from URL path
            String pathInfo = request.getPathInfo();
            if (pathInfo == null || pathInfo.equals("/")) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"success\": false, \"error\": \"User ID is required\"}");
                return;
            }

            int userId;
            try {
                userId = Integer.parseInt(pathInfo.substring(1));
            } catch (NumberFormatException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"success\": false, \"error\": \"Invalid user ID\"}");
                return;
            }

            // Parse request JSON for new password
            StringBuilder buffer = new StringBuilder();
            BufferedReader reader = request.getReader();
            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
            }

            String data = buffer.toString();
            JsonObject jsonObject = gson.fromJson(data, JsonObject.class);

            if (!jsonObject.has("password") || jsonObject.get("password").getAsString().isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"success\": false, \"error\": \"Password is required\"}");
                return;
            }

            String newPassword = jsonObject.get("password").getAsString();

            // Hash the password
            String passwordHash = SecurityUtils.hashPassword(newPassword);

            // Update the password
            boolean success = userDAO.updateUserPassword(userId, passwordHash);

            if (success) {
                out.print("{\"success\": true, \"message\": \"Password updated successfully\"}");
                logger.info("Password updated for user ID: " + userId + " by admin");
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.print("{\"success\": false, \"error\": \"Failed to update password\"}");
                logger.warning("Password update failed for user ID: " + userId);
            }

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error updating password", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"success\": false, \"error\": \"Server error occurred\"}");
        }

        out.flush();
    }
}