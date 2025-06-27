package com.tegel.servlet;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.tegel.dao.AnnouncementDAO;
import com.tegel.dao.UserDAO;
import com.tegel.model.Announcement;
import com.tegel.util.LocalDateTimeAdapter;

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
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Servlet that handles announcement-related operations
 */
@WebServlet("/announcement")
public class AnnouncementServlet extends HttpServlet {
    private static final Logger logger = Logger.getLogger(AnnouncementServlet.class.getName());
    private final AnnouncementDAO announcementDAO = new AnnouncementDAO();
    private final UserDAO userDAO = new UserDAO();
    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .create();

    /**
     * Handles GET requests to retrieve announcements
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        PrintWriter out = response.getWriter();
        HttpSession session = request.getSession(false);
        boolean isAuthenticated = (session != null && session.getAttribute("userId") != null);

        logger.info("AnnouncementServlet.doGet() - START - isAuthenticated: " + isAuthenticated);
        logger.info("Request URI: " + request.getRequestURI());
        logger.info("Request URL: " + request.getRequestURL());
        logger.info("Context Path: " + request.getContextPath());
        logger.info("Servlet Path: " + request.getServletPath());

        try {
            // Check for specific announcement ID
            String idParam = request.getParameter("id");
            if (idParam != null && !idParam.isEmpty()) {
                logger.info("Getting single announcement with ID: " + idParam);
                handleGetSingleAnnouncement(idParam, isAuthenticated, response, out);
                return;
            }

            // Check for search term
            String searchTerm = request.getParameter("search");
            if (searchTerm != null && !searchTerm.isEmpty()) {
                logger.info("Searching announcements with term: " + searchTerm);
                List<Announcement> announcements = announcementDAO.searchAnnouncements(searchTerm, isAuthenticated);
                logger.info("Found " + announcements.size() + " announcements matching search");
                out.write(gson.toJson(announcements));
                return;
            }

            // Check for date filter
            String dateParam = request.getParameter("date");
            if (dateParam != null && !dateParam.isEmpty()) {
                logger.info("Filtering announcements by date: " + dateParam);
                handleDateSearch(dateParam, isAuthenticated, response, out);
                return;
            }

            // Get all announcements based on authentication status
            logger.info("Getting all announcements for " + (isAuthenticated ? "authenticated" : "non-authenticated") + " user");
            handleGetAllAnnouncements(isAuthenticated, out);

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error processing announcement request", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.write(gson.toJson(createErrorResponse("Server error: " + e.getMessage())));
        } finally {
            logger.info("AnnouncementServlet.doGet() - END");
        }
    }

    /**
     * Helper method to get a single announcement by ID
     */
    private void handleGetSingleAnnouncement(String idParam, boolean isAuthenticated,
                                            HttpServletResponse response, PrintWriter out) {
        try {
            int announcementId = Integer.parseInt(idParam);
            Announcement announcement = announcementDAO.getAnnouncementById(announcementId);

            if (announcement == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.write(gson.toJson(createErrorResponse("Announcement not found.")));
                return;
            }

            // Check if non-authenticated user is trying to access a private announcement
            if (!isAuthenticated && !announcement.isPublic()) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                out.write(gson.toJson(createErrorResponse("You must be logged in to view this announcement.")));
                return;
            }

            out.write(gson.toJson(announcement));
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.write(gson.toJson(createErrorResponse("Invalid announcement ID format.")));
        }
    }

    /**
     * Helper method to handle date search
     */
    private void handleDateSearch(String dateParam, boolean isAuthenticated,
                                  HttpServletResponse response, PrintWriter out) {
        try {
            LocalDate date = LocalDate.parse(dateParam, DateTimeFormatter.ISO_DATE);
            LocalDateTime dateTime = LocalDateTime.of(date, LocalTime.MIDNIGHT);
            List<Announcement> announcements = announcementDAO.searchAnnouncementsByDate(dateTime, isAuthenticated);
            out.write(gson.toJson(announcements));
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.write(gson.toJson(createErrorResponse("Invalid date format. Use YYYY-MM-DD.")));
        }
    }

    /**
     * Helper method to get all announcements
     */
    private void handleGetAllAnnouncements(boolean isAuthenticated, PrintWriter out) {
        List<Announcement> announcements;

        try {
            if (isAuthenticated) {
                logger.info("Calling announcementDAO.getAllAnnouncements()");
                announcements = announcementDAO.getAllAnnouncements();
                logger.info("Retrieved " + announcements.size() + " announcements (public and private)");
            } else {
                logger.info("Calling announcementDAO.getPublicAnnouncements()");
                announcements = announcementDAO.getPublicAnnouncements();
                logger.info("Retrieved " + announcements.size() + " public announcements");
            }

            // Debug the announcement data
            for (Announcement a : announcements) {
                logger.info("Announcement: ID=" + a.getAnnouncementId() +
                           ", Title=" + a.getTitle() +
                           ", PostedBy=" + a.getPostedBy() +
                           ", Public=" + a.isPublic());
            }

            String json = gson.toJson(announcements);
            logger.info("JSON response: " + json);
            out.write(json);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Exception in handleGetAllAnnouncements", e);
            throw e;
        }
    }

    /**
     * Handles POST requests to create new announcements
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        PrintWriter out = response.getWriter();
        HttpSession session = request.getSession(false);

        // Check if user is authenticated
        if (session == null || session.getAttribute("userId") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            out.write(gson.toJson(createErrorResponse("You must be logged in to create announcements.")));
            return;
        }

        // Allow both admin and member users to create announcements
        String role = (String) session.getAttribute("role");
        if (!"admin".equals(role) && !"member".equals(role)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            out.write(gson.toJson(createErrorResponse("Only administrators and members can create announcements.")));
            return;
        }

        int userId = (int) session.getAttribute("userId");

        try {
            // Parse request body into Announcement object
            Announcement announcement = parseRequestBody(request);

            // Validate required fields
            if (announcement.getTitle() == null || announcement.getTitle().trim().isEmpty() ||
                    announcement.getContent() == null || announcement.getContent().trim().isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.write(gson.toJson(createErrorResponse("Title and content are required.")));
                return;
            }

            // Set posted by and current time
            announcement.setPostedBy(userId);
            // The database will set the timestamp using CURRENT_TIMESTAMP default

            // Create the announcement
            boolean success = announcementDAO.createAnnouncement(announcement);

            if (success) {
                Map<String, Object> responseData = new HashMap<>();
                responseData.put("success", true);
                responseData.put("message", "Announcement created successfully.");
                out.write(gson.toJson(responseData));
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.write(gson.toJson(createErrorResponse("Failed to create announcement.")));
            }

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error creating announcement", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.write(gson.toJson(createErrorResponse("Server error: " + e.getMessage())));
        }
    }

    /**
     * Handles PUT requests to update existing announcements
     */
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        PrintWriter out = response.getWriter();
        HttpSession session = request.getSession(false);

        // Check if user is authenticated
        if (session == null || session.getAttribute("userId") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            out.write(gson.toJson(createErrorResponse("You must be logged in to update announcements.")));
            return;
        }

        int userId = (int) session.getAttribute("userId");
        String role = (String) session.getAttribute("role");

        try {
            // Parse request body into Announcement object
            Announcement announcement = parseRequestBody(request);

            // Validate required fields
            if (announcement.getAnnouncementId() <= 0 ||
                    announcement.getTitle() == null || announcement.getTitle().trim().isEmpty() ||
                    announcement.getContent() == null || announcement.getContent().trim().isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.write(gson.toJson(createErrorResponse("ID, title, and content are required.")));
                return;
            }

            // Check if the announcement exists and get its details
            Announcement existingAnnouncement = announcementDAO.getAnnouncementById(announcement.getAnnouncementId());

            if (existingAnnouncement == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.write(gson.toJson(createErrorResponse("Announcement not found.")));
                return;
            }

            // Check permissions: admins can update any announcement, members can only update their own
            if (!"admin".equals(role)) {
                if (!"member".equals(role)) {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    out.write(gson.toJson(createErrorResponse("Only administrators and members can update announcements.")));
                    return;
                }

                // Check if the member is the author of the announcement
                if (existingAnnouncement.getPostedBy() != userId) {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    out.write(gson.toJson(createErrorResponse("You can only update your own announcements.")));
                    return;
                }
            }

            // Update the announcement
            boolean success = announcementDAO.updateAnnouncement(announcement);

            if (success) {
                Map<String, Object> responseData = new HashMap<>();
                responseData.put("success", true);
                responseData.put("message", "Announcement updated successfully.");
                out.write(gson.toJson(responseData));
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.write(gson.toJson(createErrorResponse("Failed to update announcement.")));
            }

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error updating announcement", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.write(gson.toJson(createErrorResponse("Server error: " + e.getMessage())));
        }
    }

    /**
     * Handles DELETE requests to remove announcements
     */
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        PrintWriter out = response.getWriter();
        HttpSession session = request.getSession(false);

        // Check if user is authenticated
        if (session == null || session.getAttribute("userId") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            out.write(gson.toJson(createErrorResponse("You must be logged in to delete announcements.")));
            return;
        }

        int userId = (int) session.getAttribute("userId");
        String role = (String) session.getAttribute("role");

        try {
            // Get announcement ID from request parameter
            String idParam = request.getParameter("id");
            if (idParam == null || idParam.trim().isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.write(gson.toJson(createErrorResponse("Announcement ID is required.")));
                return;
            }

            int announcementId = Integer.parseInt(idParam);

            // Check if the announcement exists and get its details
            Announcement existingAnnouncement = announcementDAO.getAnnouncementById(announcementId);

            if (existingAnnouncement == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.write(gson.toJson(createErrorResponse("Announcement not found.")));
                return;
            }

            // Check permissions: admins can delete any announcement, members can only delete their own
            if (!"admin".equals(role)) {
                if (!"member".equals(role)) {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    out.write(gson.toJson(createErrorResponse("Only administrators and members can delete announcements.")));
                    return;
                }

                // Check if the member is the author of the announcement
                if (existingAnnouncement.getPostedBy() != userId) {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    out.write(gson.toJson(createErrorResponse("You can only delete your own announcements.")));
                    return;
                }
            }

            // Delete the announcement
            boolean success = announcementDAO.deleteAnnouncement(announcementId);

            if (success) {
                Map<String, Object> responseData = new HashMap<>();
                responseData.put("success", true);
                responseData.put("message", "Announcement deleted successfully.");
                out.write(gson.toJson(responseData));
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.write(gson.toJson(createErrorResponse("Failed to delete announcement.")));
            }

        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.write(gson.toJson(createErrorResponse("Invalid announcement ID format.")));
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error deleting announcement", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.write(gson.toJson(createErrorResponse("Server error: " + e.getMessage())));
        }
    }

    /**
     * Helper method to parse request body into an Announcement object
     */
    private Announcement parseRequestBody(HttpServletRequest request) throws IOException {
        StringBuilder requestBody = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                requestBody.append(line);
            }
        }

        return gson.fromJson(requestBody.toString(), Announcement.class);
    }

    /**
     * Helper method to create error response maps
     */
    private Map<String, Object> createErrorResponse(String errorMessage) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", errorMessage);
        return errorResponse;
    }
}
