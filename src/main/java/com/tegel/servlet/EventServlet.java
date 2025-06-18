package com.tegel.servlet;

import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.tegel.dao.EventDAO;
import com.tegel.model.Event;
import com.tegel.util.LocalDateAdapter;
import com.tegel.util.LocalDateTimeAdapter;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;

@MultipartConfig
@WebServlet("/events/*")
public class EventServlet extends HttpServlet {
    private static final Logger logger = Logger.getLogger(EventServlet.class.getName());
    private EventDAO eventDAO = new EventDAO();

    // Configure Gson with adapters for proper date/time serialization
    private Gson gson = new GsonBuilder()
        .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
        .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
        .create();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);

        logger.info("EventServlet: Request received for path: " + request.getRequestURI());

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        String pathInfo = request.getPathInfo();
        logger.info("EventServlet: Path info: " + pathInfo);

        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                // Get all active events for public view
                logger.info("EventServlet: Fetching all active events");
                List<Event> events = eventDAO.getAllActiveEvents();
                logger.info("EventServlet: Retrieved " + events.size() + " events");

                // Update current participant count for each event
                for (Event event : events) {
                    try {
                        int count = eventDAO.getEventParticipantCount(event.getEventId());
                        if (count >= 0) {
                            event.setCurrentParticipants(count);
                        }
                    } catch (Exception e) {
                        logger.warning("EventServlet: Error getting participant count for event " + event.getEventId() + ": " + e.getMessage());
                    }
                }

                String jsonEvents = gson.toJson(events);
                logger.info("EventServlet: JSON response: " + jsonEvents);
                response.getWriter().write(jsonEvents);
                logger.info("EventServlet: Response sent successfully");
            } else {
                // Get specific event by ID
                String eventIdStr = pathInfo.substring(1);
                logger.info("EventServlet: Looking for event with ID: " + eventIdStr);
                try {
                    int eventId = Integer.parseInt(eventIdStr);
                    Event event = eventDAO.getEventById(eventId);

                    if (event != null) {
                        // Update current participant count
                        int count = eventDAO.getEventParticipantCount(event.getEventId());
                        if (count >= 0) {
                            event.setCurrentParticipants(count);
                        }

                        String jsonEvent = gson.toJson(event);
                        logger.info("EventServlet: JSON response for single event: " + jsonEvent);
                        response.getWriter().write(jsonEvent);
                        logger.info("EventServlet: Response sent successfully for event ID: " + eventId);
                    } else {
                        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                        response.getWriter().write("{\"error\":\"Event not found\"}");
                        logger.warning("EventServlet: Event not found with ID: " + eventId);
                    }
                } catch (NumberFormatException e) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    response.getWriter().write("{\"error\":\"Invalid event ID format\"}");
                    logger.warning("EventServlet: Invalid event ID format: " + eventIdStr);
                }
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "EventServlet: Error retrieving events: " + e.getMessage(), e);
            e.printStackTrace(); // Print stack trace for detailed error information
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\":\"Server error occurred: " + e.getMessage() + "\"}");
        }
    }
}
