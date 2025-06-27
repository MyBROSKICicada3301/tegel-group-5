package com.tegel.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.tegel.dao.UserDAO;
import com.tegel.model.User;
import com.tegel.util.LocalDateAdapter;
import com.tegel.util.LocalDateTimeAdapter;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Servlet to provide basic user information for announcements and other public displays
 * This servlet will return only limited information about any user if the requester is authenticated.
 */
@WebServlet("/userdetails")
public class UserInfoServlet extends HttpServlet {
    private static final Logger logger = Logger.getLogger(UserInfoServlet.class.getName());
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

        // Check if the request is authenticated
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            out.print("{\"error\": \"Not authenticated\"}");
            logger.info("Unauthenticated user details request");
            return;
        }

        // Get the requested user ID
        String userIdStr = request.getParameter("id");
        if (userIdStr == null || userIdStr.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\": \"No user ID provided\"}");
            return;
        }

        try {
            int userId = Integer.parseInt(userIdStr);
            User user = userDAO.getUserById(userId);

            if (user != null) {
                // Create a limited response with only public information
                Map<String, Object> userInfo = new HashMap<>();
                userInfo.put("userId", user.getUserId());
                userInfo.put("fullName", user.getFullName());
                userInfo.put("role", user.getRole());

                String userJson = gson.toJson(userInfo);
                out.print(userJson);
                logger.info("Limited user details sent for user ID: " + userId);
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print("{\"error\": \"User not found\"}");
                logger.warning("User not found for ID: " + userIdStr);
            }
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\": \"Invalid user ID format\"}");
        } catch (RuntimeException e) {
            logger.log(Level.SEVERE, "Error retrieving user details", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\": \"Server error occurred\"}");
        }

        out.flush();
    }
}
