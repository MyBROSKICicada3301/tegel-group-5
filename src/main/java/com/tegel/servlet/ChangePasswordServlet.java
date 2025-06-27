package com.tegel.servlet;

import com.google.gson.JsonSyntaxException;
import jakarta.servlet.ServletException;
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
import com.tegel.model.User;
import com.tegel.util.SecurityUtils;

/**
 * Servlet to handle password changes.
 */
@WebServlet("/change-password")
public class ChangePasswordServlet extends HttpServlet {
    private static final Logger logger = Logger.getLogger(ChangePasswordServlet.class.getName());
    private final UserDAO userDAO = new UserDAO();
    private final Gson gson = new Gson();

    /**
     * Handles POST requests to change the user's password.
     * Expects current and new passwords in the request body as JSON.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        HttpSession session = request.getSession(false);

        try {
            if (session == null || session.getAttribute("userId") == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                out.print("{\"success\": false, \"message\": \"Not authenticated\"}");
                return;
            }

            int userId = (Integer) session.getAttribute("userId");
            User user = userDAO.getUserById(userId);

            if (user == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print("{\"success\": false, \"message\": \"User not found\"}");
                return;
            }

            // Parse request data
            StringBuilder buffer = new StringBuilder();
            BufferedReader reader = request.getReader();
            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
            }

            String data = buffer.toString();
            JsonObject jsonObject = gson.fromJson(data, JsonObject.class);

            String currentPassword = jsonObject.get("currentPassword").getAsString();
            String newPassword = jsonObject.get("newPassword").getAsString();

            // Verify current password
            if (!SecurityUtils.verifyPassword(currentPassword, user.getPasswordHash())) {
                out.print("{\"success\": false, \"message\": \"Current password is incorrect\"}");
                return;
            }

            // Validate new password
            if (newPassword.length() < 8) {
                out.print(
                        "{\"success\": false, \"message\": \"New password must be at least 8 characters long\"}");
                return;
            }

            if (!newPassword.matches(".*[A-Za-z].*") || !newPassword.matches(".*[0-9].*")) {
                out.print(
                        "{\"success\": false, \"message\": \"New password must include at least one letter and one number\"}");
                return;
            }

            // Generate new password hash
            String newPasswordHash = SecurityUtils.hashPassword(newPassword);
            user.setPasswordHash(newPasswordHash);

            // Update user in database
            boolean success = userDAO.updateUser(user);

            if (success) {
                out.print("{\"success\": true, \"message\": \"Password changed successfully\"}");
                logger.info("Password changed for user ID: " + userId);
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.print("{\"success\": false, \"message\": \"Failed to change password\"}");
                logger.warning("Password change failed for user ID: " + userId);
            }

        } catch (JsonSyntaxException | IOException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error changing password", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"success\": false, \"message\": \"Server error occurred\"}");
        }

        out.flush();
    }
}
