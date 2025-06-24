package com.tegel.servlet;

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
public class AdminServlet extends HttpServlet {
    private UserDAO userDAO = new UserDAO();
    private Gson gson = new Gson();
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
        
        String pathInfo = request.getPathInfo();
        
        if (pathInfo != null && pathInfo.equals("/events")) {
            handleEventCreation(request, response);
        } else if (pathInfo != null && pathInfo.equals("/updateRole")) {
            handleRoleUpdate(request, response);
        } else if (pathInfo != null && pathInfo.equals("/users")) {
            handleGetAllUsers(request, response);
        }
    }

    private void handleEventCreation(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        try {
            // Check if user is admin
            HttpSession session = request.getSession(false);
            if (session == null || !"admin".equals(session.getAttribute("userRole"))) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.getWriter().write("{\"error\":\"Admin access required\"}");
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
            
            event.setImage(image != null ? image.trim() : "");
            event.setActive(isActiveStr != null && "on".equals(isActiveStr));
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
            event.setHasFoodOption("on".equals(hasFoodOptionStr));
            logger.info("Event hasFoodOption after setting: " + event.isHasFoodOption());

            event.setCreatedBy((Integer) session.getAttribute("userId"));
            
            com.tegel.dao.EventDAO eventDAO = new com.tegel.dao.EventDAO();
            boolean success = eventDAO.createEvent(event);
            
            if (success) {
                response.getWriter().write("{\"eventId\":" + event.getEventId() + ",\"success\":true}");
            } else {
                response.getWriter().write("{\"error\":\"Failed to create event\"}");
            }
            
        } catch (Exception e) {
            logger.severe("Error creating event: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\":\"Server error: " + e.getMessage() + "\"}");
        }
    }

    private void handleRoleUpdate(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        int userId = Integer.parseInt(request.getParameter("userId"));
        String newRole = request.getParameter("newRole");

        boolean success = userDAO.updateUserRole(userId, newRole);
        response.setContentType("application/json");
        response.getWriter().write("{\"success\":" + success + "}");
    }

    private void handleGetAllUsers(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        try {
            List<User> users = userDAO.getAllUsers();
            response.getWriter().write(gson.toJson(users));
        } catch (Exception e) {
            logger.severe("Error getting users: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\":\"Server error\"}");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

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
