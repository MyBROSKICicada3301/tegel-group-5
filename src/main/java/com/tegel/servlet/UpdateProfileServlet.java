package com.tegel.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.tegel.dao.UserDAO;
import com.tegel.model.User;
import com.tegel.util.SecurityUtils;

/**
 * Servlet to handle user profile updates
 */
@WebServlet("/update-profile")
public class UpdateProfileServlet extends HttpServlet {
    private static final Logger logger = Logger.getLogger(UpdateProfileServlet.class.getName());
    private UserDAO userDAO = new UserDAO();
    private Gson gson = new Gson();

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
            User existingUser = userDAO.getUserById(userId);

            if (existingUser == null) {
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

            // Update user object with new information
            if (jsonObject.has("fullName")) {
                existingUser.setFullName(SecurityUtils.sanitizeInput(jsonObject.get("fullName").getAsString()));
            }

            if (jsonObject.has("nickName")) {
                existingUser.setNickName(SecurityUtils.sanitizeInput(jsonObject.get("nickName").getAsString()));
            }

            if (jsonObject.has("phoneNumber")) {
                existingUser.setPhoneNumber(SecurityUtils.sanitizeInput(jsonObject.get("phoneNumber").getAsString()));
            }

            if (jsonObject.has("dateOfBirth") && !jsonObject.get("dateOfBirth").getAsString().isEmpty()) {
                try {
                    existingUser.setDateOfBirth(LocalDate.parse(jsonObject.get("dateOfBirth").getAsString()));
                } catch (Exception e) {
                    logger.warning("Invalid date format received: " + jsonObject.get("dateOfBirth").getAsString());
                }
            }

            if (jsonObject.has("dietRes")) {
                existingUser.setDietRes(SecurityUtils.sanitizeInput(jsonObject.get("dietRes").getAsString()));
            }

            // Update user in database
            boolean success = userDAO.updateUser(existingUser);

            if (success) {
                out.print("{\"success\": true, \"message\": \"Profile updated successfully\"}");
                logger.info("Profile updated for user ID: " + userId);
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.print("{\"success\": false, \"message\": \"Failed to update profile\"}");
                logger.warning("Profile update failed for user ID: " + userId);
            }

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error updating profile", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"success\": false, \"message\": \"Server error occurred\"}");
        }

        out.flush();
    }
}
