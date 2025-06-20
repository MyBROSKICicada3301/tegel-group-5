package com.tegel.servlet;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.tegel.dao.AnnouncementDAO;
import com.tegel.model.Announcement;
import com.tegel.util.LocalDateTimeAdapter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Servlet for handling announcement-related operations
 */
@WebServlet("/announcement")
public class AnnouncementServlet extends HttpServlet {
    private static final Logger logger = Logger.getLogger(AnnouncementServlet.class.getName());
    private final AnnouncementDAO announcementDAO = new AnnouncementDAO();
    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .setPrettyPrinting()
            .create();

    /**
     * Handles GET requests for announcements
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        PrintWriter out = response.getWriter();

        try {
            // Check if there's a specific announcement ID requested
            String idParam = request.getParameter("id");

            // Check if there's a search term
            String searchTerm = request.getParameter("search");

            // Check if there's a date filter
            String dateParam = request.getParameter("date");

            List<Announcement> announcements;

            if (idParam != null && !idParam.trim().isEmpty()) {
                // Get specific announcement
                try {
                    int id = Integer.parseInt(idParam);
                    Announcement announcement = announcementDAO.getAnnouncementById(id);

                    if (announcement != null) {
                        out.print(gson.toJson(announcement));
                    } else {
                        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                        out.print("{\"error\": \"Announcement not found\"}");
                    }
                    return;
                } catch (NumberFormatException e) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"error\": \"Invalid announcement ID format\"}");
                    return;
                }
            } else if (searchTerm != null && !searchTerm.trim().isEmpty()) {
                // Search announcements by term
                announcements = announcementDAO.searchAnnouncements(searchTerm);
            } else if (dateParam != null && !dateParam.trim().isEmpty()) {
                // Search announcements by date
                try {
                    LocalDate date = LocalDate.parse(dateParam, DateTimeFormatter.ISO_DATE);
                    // Convert LocalDate to LocalDateTime for searching (start of day)
                    LocalDateTime dateTime = LocalDateTime.of(date, LocalTime.MIDNIGHT);
                    announcements = announcementDAO.searchAnnouncementsByDate(dateTime);
                } catch (Exception e) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"error\": \"Invalid date format. Use YYYY-MM-DD\"}");
                    return;
                }
            } else {
                // Get all announcements
                announcements = announcementDAO.getAllAnnouncements();
            }

            // Return the list of announcements
            out.print(gson.toJson(announcements));

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error processing announcement request", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\": \"Server error occurred while retrieving announcements\"}");
        }
    }

    /**
     * Handles POST requests for creating new announcements (admin-only)
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        PrintWriter out = response.getWriter();

        // TODO: Add authentication check here to ensure only admins can create announcements

        try {
            // Parse the request body to get announcement details
            StringBuilder sb = new StringBuilder();
            String line;

            while ((line = request.getReader().readLine()) != null) {
                sb.append(line);
            }

            // Parse JSON into Announcement object
            Announcement announcement = gson.fromJson(sb.toString(), Announcement.class);

            // Validate required fields
            if (announcement.getTitle() == null || announcement.getTitle().trim().isEmpty() ||
                announcement.getContent() == null || announcement.getContent().trim().isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"success\": false, \"message\": \"Title and content are required\"}");
                return;
            }

            // Set postedBy from the session (assuming user is logged in)
            Integer userId = (Integer) request.getSession().getAttribute("userId");
            if (userId == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                out.print("{\"success\": false, \"message\": \"You must be logged in to create announcements\"}");
                return;
            }

            announcement.setPostedBy(userId);
            announcement.setPublic(true); // Set default visibility to public

            // Create the announcement
            boolean success = announcementDAO.createAnnouncement(announcement);

            if (success) {
                out.print("{\"success\": true, \"message\": \"Announcement created successfully\"}");
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.print("{\"success\": false, \"message\": \"Failed to create announcement\"}");
            }

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error creating announcement", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"success\": false, \"message\": \"Server error occurred\"}");
        }
    }

    /**
     * Handles PUT requests for updating announcements (admin-only)
     */
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        PrintWriter out = response.getWriter();

        // TODO: Add authentication check here to ensure only admins can update announcements

        try {
            // Parse the request body to get announcement details
            StringBuilder sb = new StringBuilder();
            String line;

            while ((line = request.getReader().readLine()) != null) {
                sb.append(line);
            }

            // Parse JSON into Announcement object
            Announcement announcement = gson.fromJson(sb.toString(), Announcement.class);

            // Validate required fields
            if (announcement.getId() <= 0 ||
                announcement.getTitle() == null || announcement.getTitle().trim().isEmpty() ||
                announcement.getContent() == null || announcement.getContent().trim().isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"success\": false, \"message\": \"ID, title, and content are required\"}");
                return;
            }

            // Update the announcement
            boolean success = announcementDAO.updateAnnouncement(announcement);

            if (success) {
                out.print("{\"success\": true, \"message\": \"Announcement updated successfully\"}");
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.print("{\"success\": false, \"message\": \"Failed to update announcement\"}");
            }

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error updating announcement", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"success\": false, \"message\": \"Server error occurred\"}");
        }
    }

    /**
     * Handles DELETE requests for deleting announcements (admin-only)
     */
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        PrintWriter out = response.getWriter();

        // TODO: Add authentication check here to ensure only admins can delete announcements

        try {
            // Get the announcement ID to delete
            String idParam = request.getParameter("id");

            if (idParam == null || idParam.trim().isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"success\": false, \"message\": \"Announcement ID is required\"}");
                return;
            }

            try {
                int id = Integer.parseInt(idParam);

                // Delete the announcement
                boolean success = announcementDAO.deleteAnnouncement(id);

                if (success) {
                    out.print("{\"success\": true, \"message\": \"Announcement deleted successfully\"}");
                } else {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    out.print("{\"success\": false, \"message\": \"Announcement not found or could not be deleted\"}");
                }

            } catch (NumberFormatException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"success\": false, \"message\": \"Invalid announcement ID format\"}");
            }

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error deleting announcement", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"success\": false, \"message\": \"Server error occurred\"}");
        }
    }
}
