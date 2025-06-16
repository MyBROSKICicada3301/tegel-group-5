package com.tegel.servlet;

import jakarta.servlet.annotation.MultipartConfig;
import java.io.IOException;

import com.google.gson.Gson;
import com.tegel.dao.EventDAO;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@MultipartConfig
@WebServlet("/events/enroll")
public class EventEnrollmentServlet extends HttpServlet {
    private EventDAO eventDAO = new EventDAO();
    private Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        try {
            int eventId = Integer.parseInt(request.getParameter("eventId"));
            int userId = Integer.parseInt(request.getParameter("userId"));
            
            boolean success = eventDAO.enrollUserInEvent(userId, eventId);
            
            if (success) {
                response.getWriter().write("{\"success\":true}");
            } else {
                response.getWriter().write("{\"success\":false,\"message\":\"Enrollment failed\"}");
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"success\":false,\"message\":\"Server error\"}");
        }
    }
}