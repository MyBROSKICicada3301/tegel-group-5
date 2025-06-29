package com.tegel.servlet;

import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

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

/**
 * Servlet to handle event-related operations such as fetching events and their details.
 */
@MultipartConfig
@WebServlet({"/events/*", "/api/events/*"})
public class EventServlet extends HttpServlet {
    private static final Logger logger = Logger.getLogger(EventServlet.class.getName());
    private final EventDAO eventDAO = new EventDAO();

    // Configure Gson with adapters for proper date/time serialization
    private final Gson gson =
            new GsonBuilder().registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
                    .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter()).create();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        logger.info("EventServlet: Request received for path: " + request.getRequestURI());

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String pathInfo = request.getPathInfo();
        logger.info("EventServlet: Path info: " + pathInfo);

        try {
            String upcomingParam = request.getParameter("upcoming");
            String limitParam = request.getParameter("limit");
            boolean upcoming = upcomingParam != null && upcomingParam.equalsIgnoreCase("true");
            int limit = 0;
            if (limitParam != null) {
                try {
                    limit = Integer.parseInt(limitParam);
                } catch (NumberFormatException e) {
                    limit = 0;
                }
            }

            if (pathInfo == null || pathInfo.equals("/")) {
                List<Event> events;
                if (upcoming) {
                    // Fetch only upcoming events, with optional limit
                    if (limit > 0) {
                        events = eventDAO.getUpcomingEvents(limit);
                    } else {
                        events = eventDAO.getUpcomingEvents();
                    }
                } else {
                    // Get all active events for public view
                    events = eventDAO.getAllActiveEvents();
                }
                logger.info("EventServlet: Retrieved " + events.size() + " events");

                // Only show event titles to unauthenticated users
                boolean isAuthenticated = request.getSession(false) != null && request.getSession(false).getAttribute("user") != null;
                if (!isAuthenticated) {
                    // Return only event titles (and date for sorting)
                    var simpleEvents = events.stream().map(e -> new Object() {
                        String title = e.getTitle();
                        LocalDate date = e.getDate();
                    }).collect(Collectors.toList());
                    String jsonEvents = gson.toJson(simpleEvents);
                    response.getWriter().write(jsonEvents);
                    logger.info("EventServlet: Sent only event titles for unauthenticated user");
                    return;
                }

                // Update current participant count for each event
                for (Event event : events) {
                    try {
                        int count = eventDAO.getEventParticipantCount(event.getEventId());
                        if (count >= 0) {
                            event.setCurrentParticipants(count);
                        }

                        // If imageId is null but the image column contains an integer value,
                        // try to convert it and set as imageId
                        if (event.getImageId() == null && event.getImage() != null) {
                            try {
                                int imageId = Integer.parseInt(event.getImage());
                                event.setImageId(imageId);
                                logger.info(
                                        "Converted image string to ID: " + imageId + " for event " +
                                                event.getEventId());
                            } catch (NumberFormatException e) {
                                // Not a number, leave as is (probably a legacy URL)
                                logger.fine("Unable to parse image as ID for event " +
                                                    event.getEventId() + ": " + event.getImage());
                            }
                        }
                    } catch (RuntimeException e) {
                        logger.warning(
                                "EventServlet: Error processing event " + event.getEventId() +
                                        ": " + e.getMessage());
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

                        // If imageId is null but the image column contains an integer value,
                        // try to convert it and set as imageId
                        if (event.getImageId() == null && event.getImage() != null) {
                            try {
                                int imageId = Integer.parseInt(event.getImage());
                                event.setImageId(imageId);
                                logger.info(
                                        "Converted image string to ID: " + imageId + " for event " +
                                                event.getEventId());
                            } catch (NumberFormatException e) {
                                // Not a number, leave as is (probably a legacy URL)
                                logger.fine("Unable to parse image as ID for event " +
                                                    event.getEventId() + ": " + event.getImage());
                            }
                        }

                        String jsonEvent = gson.toJson(event);
                        logger.info("EventServlet: JSON response for single event: " + jsonEvent);
                        response.getWriter().write(jsonEvent);
                        logger.info("EventServlet: Response sent successfully for event ID: " +
                                            eventId);
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
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (RuntimeException e) {
            logger.log(Level.SEVERE, "EventServlet: Error processing request", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\":\"Internal server error\"}");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // Check if user is logged in
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\":\"Please login to create events\"}");
            return;
        }

        // Get user role - support different possible session attribute names
        String userRole = (String) session.getAttribute("userRole");
        String role = (String) session.getAttribute("role");

        // Allow both admin and member roles
        boolean isAuthorized = ("admin".equalsIgnoreCase(userRole) || "member".equalsIgnoreCase(userRole) ||
                "admin".equalsIgnoreCase(role) || "member".equalsIgnoreCase(role) ||
                "ADMIN".equals(userRole) || "MEMBER".equals(userRole) ||
                "ADMIN".equals(role) || "MEMBER".equals(role));

        if (!isAuthorized) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write("{\"error\":\"Admin or member access required\"}");
            return;
        }

        // Event creation logic copied from AdminServlet.handleEventCreation
        try {
            String title = request.getParameter("title");
            String description = request.getParameter("description");
            String dateStr = request.getParameter("date");
            String location = request.getParameter("location");
            String maxParticipantsStr = request.getParameter("maxParticipants");
            String image = request.getParameter("image");
            String isActiveStr = request.getParameter("isActive");
            String priceStr = request.getParameter("price");
            String hasFoodOptionStr = request.getParameter("hasFoodOption");
            String isPublicStr = request.getParameter("isPublic");

            logger.info("Received parameters: isActive=" + isActiveStr + ", price=" + priceStr +
                                ", hasFoodOption=" + hasFoodOptionStr + ", isPublic=" + isPublicStr);

            // Validate required fields
            if (title == null || title.trim().isEmpty() || dateStr == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"error\":\"Missing required fields\"}");
                return;
            }

            Event event = new Event();
            event.setTitle(title.trim());
            event.setDescription(description != null ? description.trim() : "");
            event.setDate(java.time.LocalDate.parse(dateStr));
            event.setLocation(location != null ? location.trim() : "");

            if (maxParticipantsStr != null && !maxParticipantsStr.trim().isEmpty()) {
                event.setMaxParticipants(Integer.parseInt(maxParticipantsStr));
            } else {
                event.setMaxParticipants(0); // 0 = unlimited
            }

            if (image != null && !image.trim().isEmpty()) {
                event.setImage(String.valueOf(Integer.parseInt(image)));
            } else {
                // Set a default value or null
                event.setImage("0"); // Default image ID
            }
            event.setActive("true".equalsIgnoreCase(isActiveStr) || "on".equals(isActiveStr));
            event.setPublic("true".equalsIgnoreCase(isPublicStr) || "on".equals(isPublicStr));

            // Set price, default to 0
            if (priceStr != null && !priceStr.trim().isEmpty()) {
                try {
                    double price = Double.parseDouble(priceStr);
                    event.setPrice(price);
                } catch (NumberFormatException e) {
                    event.setPrice(0.0);
                }
            } else {
                event.setPrice(0.0);
            }

            event.setHasFoodOption("true".equalsIgnoreCase(hasFoodOptionStr) || "on".equals(hasFoodOptionStr));
            event.setCreatedBy((Integer) session.getAttribute("userId"));

            boolean success = eventDAO.createEvent(event);

            if (success) {
                response.getWriter()
                        .write("{\"eventId\":" + event.getEventId() + ",\"success\":true}");
            } else {
                response.getWriter().write("{\"error\":\"Failed to create event\"}");
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error creating event", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\":\"Server error: " + e.getMessage() + "\"}");
        }
    }
}
