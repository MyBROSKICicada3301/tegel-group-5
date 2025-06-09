package com.tegel.servlet;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSerializer;
import com.tegel.dao.EventDAO;
import com.tegel.model.Event;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/events")
public class EventServlet extends HttpServlet {
    private static final Logger logger = Logger.getLogger(EventServlet.class.getName());
    private EventDAO eventDAO = new EventDAO();
    private Gson gson;
    
    public EventServlet() {
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
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        try {
            String filter = request.getParameter("filter");
            List<Event> events;
            
            if ("upcoming".equals(filter)) {
                events = eventDAO.getUpcomingEvents();
                logger.info("Retrieved upcoming events for public view");
            } else {
                events = eventDAO.getAllActiveEvents();
                logger.info("Retrieved all active events for public view");
            }
            
            response.getWriter().write(gson.toJson(events));
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error retrieving public events", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\":\"Unable to load events\"}");
        }
    }
}
