package com.tegel.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.tegel.dao.UserDAO;
import com.tegel.model.User;
import com.tegel.util.LocalDateAdapter;
import com.tegel.util.LocalDateTimeAdapter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Servlet to provide detailed user information for the account page.
 */
@WebServlet("/user-details")
public class UserDetailsServlet extends HttpServlet {
    private static final Logger logger = Logger.getLogger(UserDetailsServlet.class.getName());
    private final UserDAO userDAO = new UserDAO();

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
                User user = userDAO.getUserById(userId);

                if (user != null) {
                    // Remove sensitive info before sending to client
                    user.setPasswordHash(null);

                    String userJson = gson.toJson(user);
                    out.print(userJson);
                    logger.info("User details sent for user ID: " + userId);
                } else {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    out.print("{\"error\": \"User not found\"}");
                    logger.warning("User not found for ID: " + userId);
                }
            } else {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                out.print("{\"error\": \"Not authenticated\"}");
                logger.info("Unauthenticated user details request");
            }
        } catch (RuntimeException e) {
            logger.log(Level.SEVERE, "Error retrieving user details", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\": \"Server error occurred\"}");
        }

        out.flush();
    }
}
