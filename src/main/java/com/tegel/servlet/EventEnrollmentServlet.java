package com.tegel.servlet;

import jakarta.servlet.annotation.MultipartConfig;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.tegel.dao.EventDAO;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@MultipartConfig
@WebServlet("/events/enroll")
public class EventEnrollmentServlet extends HttpServlet {
    private static final Logger logger = Logger.getLogger(EventEnrollmentServlet.class.getName());
    private EventDAO eventDAO = new EventDAO();
    private Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            // Parse JSON data from request body
            StringBuilder buffer = new StringBuilder();
            BufferedReader reader = request.getReader();
            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
            }

            String data = buffer.toString();
            JsonObject jsonObject = gson.fromJson(data, JsonObject.class);

            // Extract event ID and user ID
            int eventId = jsonObject.get("eventId").getAsInt();
            int userId = jsonObject.get("userId").getAsInt();

            // Extract food preference data
            boolean wantsFoodOption = false;
            String dietaryRestrictions = null;
            String specialRequests = null;

            if (jsonObject.has("wantsFoodOption")) {
                wantsFoodOption = jsonObject.get("wantsFoodOption").getAsBoolean();
            }

            if (jsonObject.has("dietaryRestrictions") && !jsonObject.get("dietaryRestrictions").isJsonNull()) {
                dietaryRestrictions = jsonObject.get("dietaryRestrictions").getAsString();
            }

            if (jsonObject.has("specialRequests") && !jsonObject.get("specialRequests").isJsonNull()) {
                specialRequests = jsonObject.get("specialRequests").getAsString();
            }

            logger.info("Enrollment request received for user " + userId + " in event " + eventId);

            // Create registration data string to store dietary restrictions
            String registrationData = null;
            if (wantsFoodOption && dietaryRestrictions != null && !dietaryRestrictions.isEmpty()) {
                registrationData = "Dietary restrictions: " + dietaryRestrictions;
            }

            // Attempt to enroll user with all details
            boolean success = eventDAO.enrollUserInEventWithDetails(
                    userId,
                    eventId,
                    registrationData,
                    "confirmed",
                    specialRequests,
                    wantsFoodOption
            );

            // Return appropriate response
            if (success) {
                response.getWriter().write("{\"success\":true,\"message\":\"Enrollment successful\"}");
                logger.info("Enrollment successful for user " + userId + " in event " + eventId);
            } else {
                response.getWriter().write("{\"success\":false,\"message\":\"Enrollment failed. You may already be enrolled or the event is full.\"}");
                logger.warning("Enrollment failed for user " + userId + " in event " + eventId);
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error processing enrollment request", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"success\":false,\"message\":\"Server error occurred during enrollment\"}");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            // Parse JSON data from request body
            StringBuilder buffer = new StringBuilder();
            BufferedReader reader = request.getReader();
            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
            }

            String data = buffer.toString();
            JsonObject jsonObject = gson.fromJson(data, JsonObject.class);

            // Extract event ID and user ID
            int eventId = jsonObject.get("eventId").getAsInt();
            int userId = jsonObject.get("userId").getAsInt();

            logger.info("De-enrollment request received for user " + userId + " in event " + eventId);

            // Attempt to de-enroll user from event
            boolean success = eventDAO.deEnrollUserFromEvent(userId, eventId);

            // Return appropriate response
            if (success) {
                response.getWriter().write("{\"success\":true,\"message\":\"De-enrollment successful\"}");
                logger.info("De-enrollment successful for user " + userId + " from event " + eventId);
            } else {
                response.getWriter().write("{\"success\":false,\"message\":\"De-enrollment failed. You may not be enrolled in this event.\"}");
                logger.warning("De-enrollment failed for user " + userId + " from event " + eventId);
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error processing de-enrollment request", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"success\":false,\"message\":\"Server error occurred during de-enrollment\"}");
        }
    }
}