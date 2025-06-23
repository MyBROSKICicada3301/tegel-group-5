package com.tegel.servlet;

import com.tegel.dao.EventDAO;
import com.tegel.model.Event;
import com.tegel.util.SecurityUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSerializer;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;

@MultipartConfig
@WebServlet("/admin/events/*")
public class EventManagementServlet extends HttpServlet {
    private static final Logger logger = Logger.getLogger(EventManagementServlet.class.getName());
    private EventDAO eventDAO = new EventDAO();
    private Gson gson;
    
    public EventManagementServlet() {

        // Configure Gson with custom serializers for LocalDate and LocalDateTime
        this.gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, (JsonSerializer<LocalDate>) (src, typeOfSrc, context) -> 
                context.serialize(src.format(DateTimeFormatter.ISO_LOCAL_DATE)))
            .registerTypeAdapter(LocalDateTime.class, (JsonSerializer<LocalDateTime>) (src, typeOfSrc, context) -> 
                context.serialize(src.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
            .create();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        if (!isAuthorized(request, response)) return;
        
        String pathInfo = request.getPathInfo();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        try {
            if (pathInfo == null || "/".equals(pathInfo) || "/all".equals(pathInfo)) {
                // Get all events
                List<Event> events = eventDAO.getAllEvents();
                response.getWriter().write(gson.toJson(events));
                logger.info("Retrieved all events for admin view");
                
            } else if ("/active".equals(pathInfo)) {
                // Get only active events
                List<Event> events = eventDAO.getAllActiveEvents();
                response.getWriter().write(gson.toJson(events));
                logger.info("Retrieved active events for admin view");
                
            } else if ("/upcoming".equals(pathInfo)) {
                // Get upcoming events
                List<Event> events = eventDAO.getUpcomingEvents();
                response.getWriter().write(gson.toJson(events));
                logger.info("Retrieved upcoming events for admin view");
                
            } else if (pathInfo.startsWith("/") && pathInfo.length() > 1) {
                // Get specific event by ID
                try {
                    String eventIdStr = pathInfo.substring(1);
                    int eventId = Integer.parseInt(eventIdStr);
                    
                    if (!SecurityUtils.isValidUserId(eventId)) {
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        response.getWriter().write("{\"error\":\"Invalid event ID\"}");
                        return;
                    }
                    
                    Event event = eventDAO.getEventById(eventId);
                    if (event != null) {
                        response.getWriter().write(gson.toJson(event));
                        logger.info("Retrieved event: " + eventId);
                    } else {
                        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                        response.getWriter().write("{\"error\":\"Event not found\"}");
                    }
                } catch (NumberFormatException e) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    response.getWriter().write("{\"error\":\"Invalid event ID format\"}");
                }
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"error\":\"Invalid request path\"}");
            }
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error retrieving events", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\":\"Server error occurred\"}");
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {

        if (!isAuthorized(request, response)) return;
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        try {
            Event event = createEventFromRequest(request);
            if (event == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"error\":\"Invalid event data provided\"}");
                return;
            }
            
            // Set creator from session
            HttpSession session = request.getSession();
            Integer userId = (Integer) session.getAttribute("userId");
            if (userId == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("{\"error\":\"User session invalid\"}");
                return;
            }
            
            event.setCreatedBy(userId);
            event.setActive(true);
            
            if (eventDAO.createEvent(event)) {
                logger.info("Event created successfully by user: " + userId);
                response.setStatus(HttpServletResponse.SC_CREATED);
                response.getWriter().write(gson.toJson(event));
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().write("{\"error\":\"Failed to create event\"}");
            }
            
        } catch (SecurityException e) {
            logger.warning("Security violation in event creation: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\":\"Invalid input data detected\"}");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error creating event", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\":\"Server error occurred\"}");
        }
    }
    
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        if (!isAuthorized(request, response)) return;
        
        String pathInfo = request.getPathInfo();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        try {
            if (pathInfo == null || !pathInfo.startsWith("/") || pathInfo.length() <= 1) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"error\":\"Event ID required for update\"}");
                return;
            }
            
            // Extract event ID from path
            String eventIdStr = pathInfo.substring(1);
            int eventId = Integer.parseInt(eventIdStr);
            
            if (!SecurityUtils.isValidUserId(eventId)) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"error\":\"Invalid event ID\"}");
                return;
            }
            
            // Check if event exists
            Event existingEvent = eventDAO.getEventById(eventId);
            if (existingEvent == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write("{\"error\":\"Event not found\"}");
                return;
            }
            
            // Create updated event from request
            Event updatedEvent = createEventFromRequest(request);
            if (updatedEvent == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"error\":\"Invalid event data provided\"}");
                return;
            }
            
            // Preserve original creation info
            updatedEvent.setEventId(eventId);
            updatedEvent.setCreatedBy(existingEvent.getCreatedBy());
            updatedEvent.setCreatedAt(existingEvent.getCreatedAt());
            
            if (eventDAO.updateEvent(updatedEvent)) {
                logger.info("Event updated successfully: " + eventId);
                response.getWriter().write(gson.toJson(updatedEvent));
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().write("{\"error\":\"Failed to update event\"}");
            }
            
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\":\"Invalid event ID format\"}");
        } catch (SecurityException e) {
            logger.warning("Security violation in event update: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\":\"Invalid input data detected\"}");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error updating event", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\":\"Server error occurred\"}");
        }
    }
    
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        if (!isAuthorized(request, response)) return;
        
        String pathInfo = request.getPathInfo();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        try {
            if (pathInfo == null || !pathInfo.startsWith("/") || pathInfo.length() <= 1) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"error\":\"Event ID required for deletion\"}");
                return;
            }
            
            String eventIdStr = pathInfo.substring(1);
            int eventId = Integer.parseInt(eventIdStr);
            
            if (!SecurityUtils.isValidUserId(eventId)) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"error\":\"Invalid event ID\"}");
                return;
            }
            
            if (eventDAO.deleteEvent(eventId)) {
                logger.info("Event deleted successfully: " + eventId);
                response.getWriter().write("{\"success\":true,\"message\":\"Event deleted successfully\"}");
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write("{\"error\":\"Event not found or could not be deleted\"}");
            }
            
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\":\"Invalid event ID format\"}");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error deleting event", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\":\"Server error occurred\"}");
        }
    }
    
    private Event createEventFromRequest(HttpServletRequest request) {
        try {
            // Extract parameters
            String title = request.getParameter("title");
            String description = request.getParameter("description");
            String dateStr = request.getParameter("date");
            String location = request.getParameter("location");
            String image = request.getParameter("image");
            String maxParticipantsStr = request.getParameter("maxParticipants");
            String isActiveStr = request.getParameter("isActive");
            String priceStr = request.getParameter("price");
            String hasFoodOptionStr = request.getParameter("hasFoodOption");

            // parse the price
            double price = 0.0;
            if (priceStr != null && !priceStr.trim().isEmpty()) {
                try {
                    price = Double.parseDouble(priceStr);
                    if (price < 0 || price > 10000) {
                        throw new SecurityException("Invalid price value (0-10000)");
                    }
                } catch (NumberFormatException e) {
                    logger.warning("Invalid price format: " + priceStr);
                    return null;
                }
            }

            // parse the hasFoodOption
            boolean hasFoodOption = false; // Default to false
            if (hasFoodOptionStr != null) {
                hasFoodOption = Boolean.parseBoolean(hasFoodOptionStr);
            }

            
            // Validate required fields
            if (title == null || title.trim().isEmpty()) {
                logger.warning("Event title is required");
                return null;
            }
            
            if (dateStr == null || dateStr.trim().isEmpty()) {
                logger.warning("Event date is required");
                return null;
            }
            
            // Sanitize inputs
            try {
                title = SecurityUtils.sanitizeInput(title);
                description = description != null ? SecurityUtils.sanitizeTextArea(description) : null;
                location = location != null ? SecurityUtils.sanitizeInput(location) : null;
                image = image != null ? SecurityUtils.sanitizeInput(image) : null;
            } catch (SecurityException e) {
                logger.warning("Input sanitization failed: " + e.getMessage());
                throw e;
            }
            
            // Validate formats
            if (title.length() > 100) {
                throw new SecurityException("Title too long (max 100 characters)");
            }
            
            if (location != null && location.length() > 100) {
                throw new SecurityException("Location too long (max 100 characters)");
            }
            
            if (description != null && description.length() > 2000) {
                throw new SecurityException("Description too long (max 2000 characters)");
            }
            
            // Parse and validate date
            LocalDate eventDate;
            try {
                eventDate = LocalDate.parse(dateStr);
            } catch (DateTimeParseException e) {
                logger.warning("Invalid date format: " + dateStr);
                return null;
            }
            
            // Validate date is not too far in the past or future
            LocalDate now = LocalDate.now();
            if (eventDate.isBefore(now.minusDays(1))) {
                throw new SecurityException("Event date cannot be in the past");
            }
            
            if (eventDate.isAfter(now.plusYears(2))) {
                throw new SecurityException("Event date cannot be more than 2 years in the future");
            }
            
            // Parse and validate max participants
            int maxParticipants = 0;
            if (maxParticipantsStr != null && !maxParticipantsStr.trim().isEmpty()) {
                try {
                    maxParticipants = Integer.parseInt(maxParticipantsStr);
                    if (maxParticipants < 0 || maxParticipants > 10000) {
                        throw new SecurityException("Invalid max participants count (0-10000)");
                    }
                } catch (NumberFormatException e) {
                    logger.warning("Invalid max participants format: " + maxParticipantsStr);
                    return null;
                }
            }
            
            // Parse active status
            boolean isActive = true; // Default to active
            if (isActiveStr != null) {
                isActive = Boolean.parseBoolean(isActiveStr);
            }
            
            // Create and populate event object
            Event event = new Event();
            event.setTitle(title);
            event.setDescription(description);
            event.setDate(eventDate);
            event.setLocation(location);
            event.setImage(image);
            event.setMaxParticipants(maxParticipants);
            event.setActive(isActive);
            event.setHasFoodOption(hasFoodOption);
            event.setPrice(price);
            
            return event;
            
        } catch (SecurityException e) {
            // Re-throw security exceptions
            throw e;
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error parsing event data from request", e);
            return null;
        }
    }
    
    private boolean isAuthorized(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        
        HttpSession session = request.getSession(false);
        if (session == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\":\"No active session\"}");
            return false;
        }
        
        String userRole = (String) session.getAttribute("userRole");
        Integer userId = (Integer) session.getAttribute("userId");
        
        if (!"admin".equals(userRole) || userId == null) {
            logger.warning("Unauthorized access attempt to event management by user: " + userId);
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write("{\"error\":\"Admin access required\"}");
            return false;
        }
        
        return true;
    }
}