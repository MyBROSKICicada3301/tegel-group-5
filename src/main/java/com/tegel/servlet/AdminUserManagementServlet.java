package com.tegel.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.tegel.dao.UserDAO;
import com.tegel.model.User;
import com.tegel.util.LocalDateAdapter;
import com.tegel.util.LocalDateTimeAdapter;

/**
 * Servlet that provides admin functionality for user management
 * Handles retrieving all users, updating user roles, and other admin-specific user operations
 */
public class AdminUserManagementServlet extends HttpServlet {

    private UserDAO userDAO = new UserDAO();

    // Configure Gson with adapters for proper date/time serialization
    private Gson gson = new GsonBuilder()
        .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
        .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
        .create();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        System.out.println("AdminUserManagementServlet: doGet called");
        System.out.println("Request URI: " + request.getRequestURI());
        System.out.println("Request Path Info: " + request.getPathInfo());

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        // Check if user is logged in and has admin role
        HttpSession session = request.getSession(false);
        Integer userId = null;
        String userRole = null;

        if (session != null) {
            userId = (Integer) session.getAttribute("userId");
            userRole = (String) session.getAttribute("role");
            System.out.println("Session found. UserId: " + userId + ", Role: " + userRole);
        } else {
            System.out.println("No session found");
        }

        if (session == null || userId == null) {
            System.out.println("Unauthorized access: No session or userId");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            out.print("{\"error\": \"You must be logged in to access this resource\"}");
            out.flush();
            return;
        }

        // Check if user has admin role
        if (userRole == null || !userRole.equalsIgnoreCase("admin")) {
            System.out.println("Forbidden access: Not an admin role, role value: '" + userRole + "'");
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            out.print("{\"error\": \"Admin privileges required to access user data\"}");
            out.flush();
            return;
        }

        // Path info used to determine the specific action
        String pathInfo = request.getPathInfo();
        System.out.println("Processing request with pathInfo: " + pathInfo);

        try {
            // Default action is to get all users
            if (pathInfo == null || pathInfo.equals("/") || pathInfo.equals("/all")) {
                System.out.println("Getting all users");
                List<User> allUsers = userDAO.getAllUsers();
                System.out.println("Found " + allUsers.size() + " users");

                // For security, remove password hashes before sending to client
                allUsers.forEach(user -> user.setPasswordHash(null));

                String usersJson = gson.toJson(allUsers);
                System.out.println("Sending user data");
                out.print(usersJson);
            }
            // Get specific user by ID
            else if (pathInfo.startsWith("/id/")) {
                String idStr = pathInfo.substring(4); // Remove "/id/"
                try {
                    int targetUserId = Integer.parseInt(idStr);
                    User user = userDAO.getUserById(targetUserId);

                    if (user != null) {
                        user.setPasswordHash(null); // Remove sensitive data
                        out.print(gson.toJson(user));
                    } else {
                        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                        out.print("{\"error\": \"User not found\"}");
                    }
                } catch (NumberFormatException e) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"error\": \"Invalid user ID format\"}");
                }
            } else {
                System.out.println("Invalid request path: " + pathInfo);
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\": \"Invalid request path\"}");
            }
        } catch (Exception e) {
            System.out.println("Error processing request: " + e.getMessage());
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\": \"Server error: " + e.getMessage() + "\"}");
        }

        out.flush();
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        // Check admin permissions
        HttpSession session = request.getSession(false);
        String userRole = session != null ? (String) session.getAttribute("role") : null;
        if (session == null || session.getAttribute("userId") == null ||
            userRole == null || !userRole.equalsIgnoreCase("admin")) {
            System.out.println("Forbidden access in doPut: Not an admin role, role value: '" + userRole + "'");
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            out.print("{\"error\": \"Admin privileges required\"}");
            out.flush();
            return;
        }

        String pathInfo = request.getPathInfo();

        try {
            // Update user role
            if (pathInfo != null && pathInfo.startsWith("/role/")) {
                String idStr = pathInfo.substring(6); // Remove "/role/"
                try {
                    int userId = Integer.parseInt(idStr);

                    // Read request body to get new role
                    StringBuilder requestBody = new StringBuilder();
                    String line;
                    while ((line = request.getReader().readLine()) != null) {
                        requestBody.append(line);
                    }

                    // Parse JSON from request body
                    User updatedUser = gson.fromJson(requestBody.toString(), User.class);

                    if (updatedUser != null && updatedUser.getRole() != null) {
                        boolean success = userDAO.updateUserRole(userId, updatedUser.getRole());

                        if (success) {
                            out.print("{\"success\": true, \"message\": \"User role updated successfully\"}");
                        } else {
                            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                            out.print("{\"success\": false, \"error\": \"User not found or role update failed\"}");
                        }
                    } else {
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        out.print("{\"success\": false, \"error\": \"Missing role information\"}");
                    }
                } catch (NumberFormatException e) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"success\": false, \"error\": \"Invalid user ID format\"}");
                }
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"success\": false, \"error\": \"Invalid request path\"}");
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"success\": false, \"error\": \"Server error: " + e.getMessage() + "\"}");
        }

        out.flush();
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        // Check admin permissions
        HttpSession session = request.getSession(false);
        String userRole = session != null ? (String) session.getAttribute("role") : null;
        if (session == null || session.getAttribute("userId") == null ||
            userRole == null || !userRole.equalsIgnoreCase("admin")) {
            System.out.println("Forbidden access in doDelete: Not an admin role, role value: '" + userRole + "'");
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            out.print("{\"error\": \"Admin privileges required\"}");
            out.flush();
            return;
        }

        String pathInfo = request.getPathInfo();

        try {
            // Delete user by ID
            if (pathInfo != null && pathInfo.length() > 1) {
                String idStr = pathInfo.substring(1); // Remove leading "/"
                try {
                    int userId = Integer.parseInt(idStr);

                    // Prevent admins from deleting themselves
                    Integer currentUserId = (Integer) session.getAttribute("userId");
                    if (currentUserId != null && currentUserId == userId) {
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        out.print("{\"success\": false, \"error\": \"You cannot delete your own admin account\"}");
                        out.flush();
                        return;
                    }

                    boolean success = userDAO.deleteUser(userId);

                    if (success) {
                        out.print("{\"success\": true, \"message\": \"User deleted successfully\"}");
                    } else {
                        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                        out.print("{\"success\": false, \"error\": \"User not found or delete operation failed\"}");
                    }
                } catch (NumberFormatException e) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"success\": false, \"error\": \"Invalid user ID format\"}");
                }
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"success\": false, \"error\": \"User ID is required for deletion\"}");
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"success\": false, \"error\": \"Server error: " + e.getMessage() + "\"}");
        }
        out.flush();
    }
}