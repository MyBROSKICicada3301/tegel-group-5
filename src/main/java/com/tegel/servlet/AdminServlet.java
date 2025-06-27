package com.tegel.servlet;

import jakarta.servlet.annotation.WebServlet;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import com.google.gson.Gson;
import com.tegel.dao.UserDAO;
import com.tegel.model.Event;
import com.tegel.model.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Servlet to handle admin functionalities such as user management and event creation.
 */
@WebServlet("/admin/*")
public class AdminServlet extends HttpServlet {


    private final UserDAO userDAO = new UserDAO();
    private final Gson gson = new Gson();
    private static final Logger logger = Logger.getLogger(AdminServlet.class.getName());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String pathInfo = request.getPathInfo();
        HttpSession session = request.getSession();
        String userRole = (String) session.getAttribute("userRole");

        // Check admin authorization
        if (!"admin".equals(userRole)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        response.setContentType("application/json");

        if ("/users".equals(pathInfo)) {
            List<User> users = userDAO.getAllUsers();
            response.getWriter().write(gson.toJson(users));
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        System.out.println("doPost called in /admin");
        String pathInfo = request.getPathInfo();

        if (pathInfo != null && pathInfo.equals("/events")) {
            handleEventCreation(request, response);
        } else if (pathInfo != null && pathInfo.equals("/updateRole")) {
            handleRoleUpdate(request, response);
        } else if (pathInfo != null && pathInfo.equals("/users")) {
            handleGetAllUsers(request, response);
        }
    }

    /**
     * Handles event creation by an admin.
     *
     * @param request  the HttpServletRequest object
     * @param response the HttpServletResponse object
     * @throws IOException if an I/O error occurs
     */
    private void handleEventCreation(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        try {
            // Check if user is logged in
            HttpSession session = request.getSession(false);
            if (session == null || session.getAttribute("userId") == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("{\"error\":\"Please login to create events\"}");
                return;
            }

            // Get user role - check for both possible attribute names and formats
            String userRole = (String) session.getAttribute("userRole");
            String role = (String) session.getAttribute("role");

            // Support multiple formats of role names (case-insensitive)
            boolean isAuthorized = ("admin".equalsIgnoreCase(userRole) || "member".equalsIgnoreCase(userRole) ||
                    "admin".equalsIgnoreCase(role) || "member".equalsIgnoreCase(role) ||
                    "ADMIN".equals(userRole) || "MEMBER".equals(userRole) ||
                    "ADMIN".equals(role) || "MEMBER".equals(role));

            if (!isAuthorized) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.getWriter().write("{\"error\":\"Admin or member access required\"}");
                return;
            }

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
                                ", hasFoodOption=" + hasFoodOptionStr + ", isPublic=" +
                                isPublicStr);
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

            event.setImage(String.valueOf(Integer.parseInt(image)));
            event.setActive("true".equalsIgnoreCase(isActiveStr) || "on".equals(isActiveStr));

            // set public visibility
            event.setPublic("true".equalsIgnoreCase(isPublicStr) || "on".equals(isPublicStr));

            // set price, default to 0
            if (priceStr != null && !priceStr.trim().isEmpty()) {
                try {
                    double price = Double.parseDouble(priceStr);
                    event.setPrice(price);
                } catch (NumberFormatException e) {
                    event.setPrice(0.0);
                }
            } else {
                event.setPrice(0.0); // Default price
            }
            logger.info("hasFoodOptionStr received: " + hasFoodOptionStr);
            logger.info("hasFoodOption value calculated: " + "on".equals(hasFoodOptionStr));
            event.setHasFoodOption(
                    "true".equalsIgnoreCase(hasFoodOptionStr) || "on".equals(hasFoodOptionStr));
            logger.info("Event hasFoodOption after setting: " + event.isHasFoodOption());

            event.setCreatedBy((Integer) session.getAttribute("userId"));

            com.tegel.dao.EventDAO eventDAO = new com.tegel.dao.EventDAO();
            boolean success = eventDAO.createEvent(event);

            if (success) {
                response.getWriter()
                        .write("{\"eventId\":" + event.getEventId() + ",\"success\":true}");
            } else {
                response.getWriter().write("{\"error\":\"Failed to create event\"}");
            }

        } catch (NumberFormatException | IOException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            logger.severe("Error creating event: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\":\"Server error: " + e.getMessage() + "\"}");
        }
    }

    /**
     * Handles role update for a user by an admin.
     *
     * @param request  the HttpServletRequest object
     * @param response the HttpServletResponse object
     * @throws IOException if an I/O error occurs
     */
    private void handleRoleUpdate(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        int userId = Integer.parseInt(request.getParameter("userId"));
        String newRole = request.getParameter("newRole");

        boolean success = userDAO.updateUserRole(userId, newRole);
        response.setContentType("application/json");
        response.getWriter().write("{\"success\":" + success + "}");
    }

    /**
     * Handles getting all users for admin.
     *
     * @param request  the HttpServletRequest object
     * @param response the HttpServletResponse object
     * @throws IOException if an I/O error occurs
     */
    private void handleGetAllUsers(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        try {
            List<User> users = userDAO.getAllUsers();
            response.getWriter().write(gson.toJson(users));
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (RuntimeException e) {
            logger.severe("Error getting users: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\":\"Server error\"}");
        }
    }

    /**
     * Handles user deletion by an admin.
     *
     * @param request  the HttpServletRequest object
     * @param response the HttpServletResponse object
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        HttpSession session = request.getSession();
        String userRole = (String) session.getAttribute("userRole");

        // Check admin authorization
        if (!"admin".equals(userRole)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        String pathInfo = request.getPathInfo();
        if (pathInfo != null && pathInfo.startsWith("/user/")) {
            int userId = Integer.parseInt(pathInfo.substring(6));
            boolean success = userDAO.deleteUser(userId);

            response.setContentType("application/json");
            response.getWriter().write("{\"success\":" + success + "}");
        }
    }
}
