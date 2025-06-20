package com.tegel.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.tegel.dao.UserDAO;
import com.tegel.model.User;
import com.tegel.util.LocalDateAdapter;
import com.tegel.util.LocalDateTimeAdapter;

/**
 * Servlet that provides user information from the session
 * Used to sync client-side localStorage with server session data
 */
@WebServlet("/user-info")
public class UserInfoServlet extends HttpServlet {

    private UserDAO userDAO = new UserDAO();

    // Configure Gson with adapters for proper date/time serialization
    private Gson gson = new GsonBuilder()
        .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
        .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
        .create();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        System.out.println("UserInfoServlet: doGet called");

        HttpSession session = request.getSession(false);
        PrintWriter out = response.getWriter();

        if (session != null && session.getAttribute("userId") != null) {
            // User is logged in, return complete user data
            int userId = (Integer) session.getAttribute("userId");
            System.out.println("UserInfoServlet: User ID from session: " + userId);

            // Get user data from session or database
            User user = (User) session.getAttribute("user");

            // If user object is not in session or is incomplete, fetch from database
            if (user == null || user.getFullName() == null) {
                System.out.println("UserInfoServlet: Fetching user from database");
                user = userDAO.getUserById(userId);

                // Store the complete user object in session for future requests
                if (user != null) {
                    System.out.println("UserInfoServlet: User found in database: " + user.getEmail());
                    session.setAttribute("user", user);
                } else {
                    System.out.println("UserInfoServlet: User not found in database for ID: " + userId);
                }
            } else {
                System.out.println("UserInfoServlet: Using user from session: " + user.getEmail());
            }

            // If we have a valid user object, convert it to JSON
            if (user != null) {
                // For security, set password hash to null before serializing
                String passwordHash = user.getPasswordHash();
                user.setPasswordHash(null);
                String userJson = gson.toJson(user);
                System.out.println("UserInfoServlet: Returning user JSON: " + userJson);
                out.print(userJson);
                // Restore the password hash in the session object
                user.setPasswordHash(passwordHash);
            } else {
                // User ID is valid but we couldn't retrieve user details
                String errorJson = "{\"userId\": " + userId + ", \"loggedIn\": true, \"error\": \"Could not retrieve complete user details\"}";
                System.out.println("UserInfoServlet: Error getting user details: " + errorJson);
                out.print(errorJson);
            }
        } else {
            // User is not logged in
            System.out.println("UserInfoServlet: User not logged in or session is null");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            out.print("{\"loggedIn\": false}");
        }

        out.flush();
    }
}
