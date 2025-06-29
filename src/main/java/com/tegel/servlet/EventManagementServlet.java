package com.tegel.servlet;
/**
 * Servelt for managing event-related operations in the admin panel.
 */

import com.tegel.dao.EventDAO;
import com.tegel.dao.ImageDAO;
import com.tegel.dao.UserDAO;
import com.tegel.model.Event;
import com.tegel.model.Image;
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
import jakarta.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;

@MultipartConfig(fileSizeThreshold = 1024 * 1024,    // 1 MB
        maxFileSize = 10 * 1024 * 1024,     // 10 MB
        maxRequestSize = 50 * 1024 * 1024   // 50 MB
)

@WebServlet({"/admin/events/*", "/members/events/*"})
public class EventManagementServlet extends HttpServlet {
    private static final Logger logger = Logger.getLogger(EventManagementServlet.class.getName());
    private final EventDAO eventDAO = new EventDAO();
    private final ImageDAO imageDAO = new ImageDAO();
    private final UserDAO userDAO = new UserDAO();
    private final Gson gson;

    /**
     * Default constructor initializes Gson with custom serializers for LocalDate and LocalDateTime.
     */
    public EventManagementServlet() {

        // Configure Gson with custom serializers for LocalDate and LocalDateTime
        this.gson = new GsonBuilder().registerTypeAdapter(LocalDate.class,
                                                          (JsonSerializer<LocalDate>) (src, typeOfSrc, context) -> context.serialize(
                                                                  src.format(
                                                                          DateTimeFormatter.ISO_LOCAL_DATE)))
                .registerTypeAdapter(LocalDateTime.class,
                                     (JsonSerializer<LocalDateTime>) (src, typeOfSrc, context) -> context.serialize(
                                             src.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
                .create();
    }

    /**
     * Handles GET requests to retrieve events.
     * Supports fetching all events, active events, upcoming events, or a specific event by ID.
     *
     * @param request  the HttpServletRequest object
     * @param response the HttpServletResponse object
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException      if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!isAuthorized(request, response)) {
            return;
        }

        String pathInfo = request.getPathInfo();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // check if its a member path
        boolean isMemberPath = request.getServletPath().startsWith("/members/");
        HttpSession session = request.getSession(false);
        Integer userId = (Integer) session.getAttribute("userId");

        try {
            if (isMemberPath &&
                    (pathInfo == null || "/".equals(pathInfo) || "/all".equals(pathInfo))) {
                // Get events created by the member
                List<Event> events = eventDAO.getEventsByCreator(userId);
                response.getWriter().write(gson.toJson(events));
                return;
            }

            if (pathInfo != null && pathInfo.matches("/\\d+/participants")) {
                // /admin/events/{eventId}/participants
                int eventId = Integer.parseInt(pathInfo.split("/")[1]);
                if (!SecurityUtils.isValidUserId(eventId)) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    response.getWriter().write("{\"error\":\"Invalid event ID\"}");
                    return;
                }
                var participants = userDAO.getUsersByEventId(eventId);
                response.getWriter().write(gson.toJson(participants));
                return;
            }

            if (pathInfo != null && pathInfo.matches("/\\d+/participants/download")) {
                // /admin/events/{eventId}/participants/download
                int eventId = Integer.parseInt(pathInfo.split("/")[1]);
                if (!SecurityUtils.isValidUserId(eventId)) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    response.getWriter().write("{\"error\":\"Invalid event ID\"}");
                    return;
                }
                var participants = userDAO.getUsersByEventId(eventId);
                // Set headers for CSV download
                response.setContentType("text/csv");
                response.setHeader("Content-Disposition",
                                   "attachment; filename=participants_event_" + eventId + ".csv");
                response.setCharacterEncoding("UTF-8");
                // Write CSV header
                response.getWriter().println("Full Name,Email,Phone Number,Dietary Restrictions");
                // Write participant data
                for (var user : participants) {
                    String line = String.format("\"%s\",\"%s\",\"%s\",\"%s\"",
                                                user.getFullName() != null ?
                                                        user.getFullName().replace("\"", "''") : "",
                                                user.getEmail() != null ?
                                                        user.getEmail().replace("\"", "''") : "",
                                                user.getPhoneNumber() != null ?
                                                        user.getPhoneNumber().replace("\"", "''") :
                                                        "", user.getDietRes() != null ?
                                                        user.getDietRes().replace("\"", "''") : "");
                    response.getWriter().println(line);
                }
                return;
            }

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

        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (RuntimeException e) {
            logger.log(Level.SEVERE, "Error retrieving events", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\":\"Server error occurred\"}");
        }
    }

    /**
     * Handles POST requests to create a new event.
     * Supports image uploads and associates the image with the event.
     *
     * @param request  the HttpServletRequest object
     * @param response the HttpServletResponse object
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException      if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!isAuthorized(request, response)) {
            return;
        }

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            // Process the image upload first to get the image ID for the event
            Part imagePart = request.getPart("eventImage");
            Integer imageId = null;

            // If an image was uploaded, save it first
            if (imagePart != null && imagePart.getSize() > 0) {
                try (InputStream fileContent = imagePart.getInputStream()) {
                    String fileName = imagePart.getSubmittedFileName();
                    String contentType = imagePart.getContentType();

                    // Validate file is an image
                    if (contentType == null || !contentType.startsWith("image/")) {
                        logger.warning("Uploaded file is not an image: " + contentType);
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        response.getWriter().write("{\"error\":\"Only image files are allowed\"}");
                        return;
                    }

                    byte[] imageData = fileContent.readAllBytes();

                    Image imageObj = new Image();
                    imageObj.setName(fileName);
                    imageObj.setContentType(contentType);
                    imageObj.setData(imageData);

                    // Save the image first to get its ID
                    // We'll associate it with the event after the event is created
                    int imgId = imageDAO.saveImage(imageObj);

                    if (imgId <= 0) {
                        logger.warning("Failed to save image for event");
                        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                        response.getWriter().write("{\"error\":\"Failed to save event image\"}");
                        return;
                    }

                    // Store the image ID as an Integer object
                    imageId = imgId;
                    logger.info("Successfully saved event image with ID: " + imageId);
                }
            }

            // Now create the event with the image ID if we have one
            Event event = createEventFromRequest(request);
            if (event == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"error\":\"Invalid event data provided\"}");
                return;
            }

            // Set the image ID if we have one
            if (imageId != null) {
                event.setImageId(imageId);
                logger.info("Setting event image ID to: " + imageId);
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

                // If we have an image ID, update the image record to associate it with the event
                if (imageId != null) {
                    imageDAO.updateEventIdForImage(imageId, event.getEventId());
                    logger.info(
                            "Updated image " + imageId + " with event ID " + event.getEventId());
                }

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
        } catch (ServletException | IOException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error creating event", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\":\"Server error occurred\"}");
        }
    }

    /**
     * Handles PUT requests to update an existing event.
     * Supports image uploads and updates the associated image if provided.
     *
     * @param request  the HttpServletRequest object
     * @param response the HttpServletResponse object
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        String pathInfo = request.getPathInfo();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            if (pathInfo == null || !pathInfo.startsWith("/") || pathInfo.length() == 1) {
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

            // Process the image upload first to get the image ID for the event update
            Part imagePart = request.getPart("eventImage");
            Integer newImageId = null;

            // If an image was uploaded, save it first
            if (imagePart != null && imagePart.getSize() > 0) {
                try (InputStream fileContent = imagePart.getInputStream()) {
                    String fileName = imagePart.getSubmittedFileName();
                    String contentType = imagePart.getContentType();

                    // Validate file is an image
                    if (contentType == null || !contentType.startsWith("image/")) {
                        logger.warning("Uploaded file is not an image: " + contentType);
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        response.getWriter().write("{\"error\":\"Only image files are allowed\"}");
                        return;
                    }

                    byte[] imageData = fileContent.readAllBytes();

                    Image imageObj = new Image();
                    imageObj.setName(fileName);
                    imageObj.setContentType(contentType);
                    imageObj.setData(imageData);

                    // Save the image first to get its ID
                    int imgId = imageDAO.saveImage(imageObj);

                    if (imgId <= 0) {
                        logger.warning("Failed to save image for event update");
                        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                        response.getWriter().write("{\"error\":\"Failed to save event image\"}");
                        return;
                    }

                    // Store the image ID as an Integer object
                    newImageId = imgId;
                    logger.info("Successfully saved new event image with ID: " + newImageId);
                }
            }

            // Create updated event from request
            Event updatedEvent = createEventFromRequest(request);
            if (updatedEvent == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"error\":\"Invalid event data provided\"}");
                return;
            }

            // Set the new image ID if we have one, otherwise preserve the existing one
            if (newImageId != null) {
                updatedEvent.setImageId(newImageId);
                logger.info("Setting updated event image ID to: " + newImageId);
            } else {
                // Preserve existing image ID if no new image was uploaded
                updatedEvent.setImageId(existingEvent.getImageId());
            }

            // Preserve original creation info
            updatedEvent.setEventId(eventId);
            updatedEvent.setCreatedBy(existingEvent.getCreatedBy());
            updatedEvent.setCreatedAt(existingEvent.getCreatedAt());

            if (eventDAO.updateEvent(updatedEvent)) {
                logger.info("Event updated successfully: " + eventId);

                // If we have a new image ID, update the image record to associate it with the event
                if (newImageId != null) {
                    imageDAO.updateEventIdForImage(newImageId, eventId);
                    logger.info("Updated image " + newImageId + " with event ID " + eventId);
                }

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
        } catch (ServletException | IOException e) {
            throw new RuntimeException(e);
        } catch (RuntimeException e) {
            logger.log(Level.SEVERE, "Error updating event", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\":\"Server error occurred\"}");
        }
    }

    /**
     * Handles DELETE requests to remove an existing event.
     * Deletes the event by ID and removes associated image if applicable.
     *
     * @param request  the HttpServletRequest object
     * @param response the HttpServletResponse object
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        if (!isAuthorized(request, response)) {
            return;
        }

        String pathInfo = request.getPathInfo();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            if (pathInfo == null || !pathInfo.startsWith("/") || pathInfo.length() == 1) {
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
                response.getWriter()
                        .write("{\"success\":true,\"message\":\"Event deleted successfully\"}");
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter()
                        .write("{\"error\":\"Event not found or could not be deleted\"}");
            }

        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\":\"Invalid event ID format\"}");
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (RuntimeException e) {
            logger.log(Level.SEVERE, "Error deleting event", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\":\"Server error occurred\"}");
        }
    }

    /**
     * Creates an Event object from the request parameters.
     * Validates and sanitizes input data, handles image uploads, and parses date formats.
     *
     * @param request the HttpServletRequest object
     * @return a populated Event object or null if validation fails
     */
    private Event createEventFromRequest(HttpServletRequest request) {
        try {
            // Extract parameters
            String title = request.getParameter("title");
            String description = request.getParameter("description");
            String dateStr = request.getParameter("date");
            String location = request.getParameter("location");
            String image =
                    request.getParameter("image"); // May be null if file upload is used instead
            String maxParticipantsStr = request.getParameter("maxParticipants");
            String isActiveStr = request.getParameter("isActive");
            String priceStr = request.getParameter("price");
            String hasFoodOptionStr = request.getParameter("hasFoodOption");
            String isPublicStr = request.getParameter("isPublic");

            // Handle image upload if present
            try {
                Part imagePart = request.getPart("eventImage");
                if (imagePart != null && imagePart.getSize() > 0) {
                    String fileName = imagePart.getSubmittedFileName();
                    String contentType = imagePart.getContentType();

                    // Validate file is an image
                    if (contentType != null && !contentType.startsWith("image/")) {
                        logger.warning("Uploaded file is not an image: " + contentType);
                        throw new SecurityException("Only image files are allowed");
                    }

                    // Store the image part for later processing after event creation
                    // This way we can associate it with the event_id
                    request.setAttribute("pendingImageUpload", true);
                    request.setAttribute("pendingImagePart", imagePart);
                    logger.info("Image prepared for upload with event");
                }
            } catch (IOException e) {
                logger.log(Level.WARNING, "Error handling image upload", e);
            } catch (ServletException e) {
                // Part may not be available if not multipart request
                logger.log(Level.FINE, "No image part found in request", e);
            }

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

            // parse the isPublic
            boolean isPublic = false; // Default to false
            if (isPublicStr != null) {
                isPublic = Boolean.parseBoolean(isPublicStr);
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
                description =
                        description != null ? SecurityUtils.sanitizeTextArea(description) : null;
                location = location != null ? SecurityUtils.sanitizeInput(location) : null;
                // Only sanitize image if it's a path/URL, not if it's an image ID
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
            event.setPublic(isPublic);

            return event;

        } catch (SecurityException e) {
            // Re-throw security exceptions
            throw e;
        } catch (RuntimeException e) {
            logger.log(Level.WARNING, "Error parsing event data from request", e);
            return null;
        }
    }

    /**
     * Checks if the user is authorized to access event management features.
     * Validates session and user role.
     *
     * @param request  the HttpServletRequest object
     * @param response the HttpServletResponse object
     * @return true if authorized, false otherwise
     * @throws IOException if an I/O error occurs
     */
    private boolean isAuthorized(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        HttpSession session = request.getSession(false);
        if (session == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\":\"No active session\"}");
            return false;
        }

        String userRole = (String) session.getAttribute("role");
        Integer userId = (Integer) session.getAttribute("userId");

        if (userId == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\":\"User not authenticated\"}");
            return false;
        }

        // Check if it's an admin path or member path
        String servletPath = request.getServletPath();
        boolean isAdminPath = servletPath.startsWith("/admin/");
        boolean isMemberPath = servletPath.startsWith("/member/");

        // Admin can access admin paths
        if (isAdminPath && "admin".equals(userRole)) {
            return true;
        }

        // For member paths, check if they're accessing their own event
        if (isMemberPath) {
            String pathInfo = request.getPathInfo();
            if (pathInfo == null || !pathInfo.startsWith("/") || pathInfo.length() == 1) {
                // For GET requests listing events, allow members to see their own
                if (request.getMethod().equals("GET")) {
                    return true;
                }
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"error\":\"Event ID required\"}");
                return false;
            }

            try {
                // Extract event ID
                String eventIdStr = pathInfo.split("/")[1];
                int eventId = Integer.parseInt(eventIdStr);

                // Get the event to check ownership
                Event event = eventDAO.getEventById(eventId);
                if (event == null) {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    response.getWriter().write("{\"error\":\"Event not found\"}");
                    return false;
                }

                // Allow if user is the creator of the event
                if (event.getCreatedBy() == userId) {
                    return true;
                }

                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.getWriter().write("{\"error\":\"You can only edit your own events\"}");
                return false;
            } catch (NumberFormatException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"error\":\"Invalid event ID\"}");
                return false;
            }
        }

        // If we reach here, access is denied
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.getWriter().write("{\"error\":\"Access denied\"}");
        return false;
    }
}
