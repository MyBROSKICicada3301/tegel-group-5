package com.tegel.servlet;

import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import com.google.gson.Gson;
import com.tegel.dao.EventDAO;
import com.tegel.model.Event;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@MultipartConfig
@WebServlet("/events/*")
public class EventServlet extends HttpServlet {
    private static final Logger logger = Logger.getLogger(EventServlet.class.getName());
    private EventDAO eventDAO = new EventDAO();
    private Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session != null) {
            System.out.println("Session received with ID: " + session.getId());
            System.out.println("Session user attribute: " + session.getAttribute("user"));
        } else {
            System.out.println("No session received in this request.");
        }


        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        String pathInfo = request.getPathInfo();
        
        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                // Get all active events for public view
                List<Event> events = eventDAO.getAllActiveEvents();
                response.getWriter().write(gson.toJson(events));
                logger.info("Retrieved " + events.size() + " active events for public view");
            } else {
                // Get specific event by ID
                String eventIdStr = pathInfo.substring(1);
                int eventId = Integer.parseInt(eventIdStr);
                Event event = eventDAO.getEventById(eventId);
                
                if (event != null) {
                    response.getWriter().write(gson.toJson(event));
                    logger.info("Retrieved event details for ID: " + eventId);
                } else {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    response.getWriter().write("{\"error\":\"Event not found\"}");
                }
            }
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\":\"Invalid event ID format\"}");
        } catch (Exception e) {
            logger.severe("Error retrieving events: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\":\"Server error occurred\"}");
        }
    }
}
