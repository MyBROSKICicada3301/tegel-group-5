package com.tegel.servlet;

import com.tegel.dao.EventDAO;
import com.tegel.model.Event;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Servlet to handle event approval operations.
 * Only administrators can approve events.
 */
@WebServlet("/admin/events/approve/*")
public class EventApprovalServlet extends HttpServlet {
    private static final Logger logger = Logger.getLogger(EventApprovalServlet.class.getName());
    private final EventDAO eventDAO = new EventDAO();

    /**
     * Handles POST requests to approve an event.
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

        String pathInfo = request.getPathInfo();

        try {
            if (pathInfo == null || !pathInfo.startsWith("/") || pathInfo.length() == 1) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"error\":\"Event ID required for approval\"}");
                return;
            }

            // Extract event ID from path
            String eventIdStr = pathInfo.substring(1);
            int eventId = Integer.parseInt(eventIdStr);

            // Check if event exists
            Event event = eventDAO.getEventById(eventId);
            if (event == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write("{\"error\":\"Event not found\"}");
                return;
            }

            // Update event approval status
            boolean success = eventDAO.approveEvent(eventId);

            if (success) {
                logger.info("Event approved successfully: " + eventId);
                response.getWriter().write("{\"success\":true,\"message\":\"Event approved successfully\"}");
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().write("{\"error\":\"Failed to approve event\"}");
            }

        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\":\"Invalid event ID format\"}");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error approving event", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\":\"Server error occurred: " + e.getMessage() + "\"}");
        }
    }

    /**
     * Checks if the user is authorized to access event approval features.
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

        if (!"admin".equals(userRole) || userId == null) {
            logger.warning("Unauthorized access attempt to event approval by user: " + userId);
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write("{\"error\":\"Admin access required\"}");
            return false;
        }

        return true;
    }
}