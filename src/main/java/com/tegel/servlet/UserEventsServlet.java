package com.tegel.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.tegel.dao.EventDAO;
import com.tegel.model.Event;
import com.tegel.util.LocalDateAdapter;
import com.tegel.util.LocalDateTimeAdapter;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Servlet to provide a list of events the user has enrolled in.
 */
@WebServlet("/user-events")
public class UserEventsServlet extends HttpServlet {
    private static final Logger logger = Logger.getLogger(UserEventsServlet.class.getName());
    private final EventDAO eventDAO = new EventDAO();

    // Configure Gson with adapters for proper date/time serialization
    private final Gson gson =
            new GsonBuilder().registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
                    .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter()).create();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        HttpSession session = request.getSession(false);

        try {
            if (session != null && session.getAttribute("userId") != null) {
                int userId = (Integer) session.getAttribute("userId");

                // Get events the user has enrolled in
                List<Event> enrolledEvents = eventDAO.getEnrolledEventsByUserId(userId);

                // Send response with events
                out.print("{\"events\":" + gson.toJson(enrolledEvents) + "}");
                logger.info("Sent " + enrolledEvents.size() + " enrolled events for user ID: " +
                                    userId);
            } else {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                out.print("{\"error\": \"Not authenticated\"}");
                logger.info("Unauthenticated user events request");
            }
        } catch (RuntimeException e) {
            logger.log(Level.SEVERE, "Error retrieving user events", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\": \"Server error occurred\"}");
        }

        out.flush();
    }
}
