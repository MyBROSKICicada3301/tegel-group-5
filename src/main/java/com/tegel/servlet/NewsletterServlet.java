package com.tegel.servlet;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.tegel.dao.NewsletterDAO;
import com.tegel.dao.UserDAO;
import com.tegel.model.Newsletter;
import com.tegel.model.User;
import com.tegel.util.LocalDateTimeAdapter;
import com.tegel.util.Logger;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Servlet for handling newsletter operations
 */
@WebServlet("/api/newsletters/*")
public class NewsletterServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final NewsletterDAO newsletterDAO = new NewsletterDAO();
    private final UserDAO userDAO = new UserDAO();
    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .create();


    /**
     * Handle GET requests - retrieve newsletters
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        response.setContentType("application/json");

        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                // Get query parameters
                String searchTerm = request.getParameter("q");
                String limitStr = request.getParameter("limit");
                boolean onlyPublished = "true".equals(request.getParameter("published"));

                List<Newsletter> newsletters;

                if (searchTerm != null && !searchTerm.trim().isEmpty()) {
                    // Search newsletters - non-admins can only search published newsletters
                    User user = (User) request.getSession().getAttribute("user");
                    boolean isAdmin = user != null && "admin".equals(user.getRole());

                    // If not admin, force onlyPublished to true
                    if (!isAdmin) {
                        onlyPublished = true;
                    }

                    newsletters = newsletterDAO.searchNewsletters(searchTerm, onlyPublished);
                } else if (limitStr != null) {
                    // Get limited number of newsletters (always published ones for public access)
                    int limit = Integer.parseInt(limitStr);
                    newsletters = newsletterDAO.getLatestNewsletters(limit);
                } else if (onlyPublished) {
                    // Get all published newsletters - accessible to everyone
                    newsletters = newsletterDAO.getAllPublishedNewsletters();
                } else {
                    // Get all newsletters (admin only)
                    User user = (User) request.getSession().getAttribute("user");
                    if (user != null && "admin".equals(user.getRole())) {
                        newsletters = newsletterDAO.getAllNewsletters();
                    } else {
                        // Non-admins can only see published newsletters
                        newsletters = newsletterDAO.getAllPublishedNewsletters();
                    }
                }

                response.getWriter().write(gson.toJson(newsletters));

            } else {
                // Get newsletter by ID
                try {
                    int id = Integer.parseInt(pathInfo.substring(1));
                    Newsletter newsletter = newsletterDAO.getNewsletterById(id);

                    if (newsletter != null) {
                        // Non-admins can only view published newsletters
                        User user = (User) request.getSession().getAttribute("user");
                        boolean isAdmin = user != null && "admin".equals(user.getRole());

                        if (!newsletter.isPublished() && !isAdmin) {
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            response.getWriter().write(gson.toJson(Map.of("error", "Newsletter not available")));
                            return;
                        }

                        response.getWriter().write(gson.toJson(newsletter));
                    } else {
                        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                        response.getWriter().write(gson.toJson(Map.of("error", "Newsletter not found")));
                    }
                } catch (NumberFormatException e) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    response.getWriter().write(gson.toJson(Map.of("error", "Invalid newsletter ID")));
                }
            }
        } catch (Exception e) {
            Logger.error("NewsletterServlet", "Error processing GET request", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(gson.toJson(Map.of("error", "Server error processing newsletter request")));
        }
    }

    /**
     * Handle POST requests - create new newsletter
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");

        // Check if user is admin
        User user = (User) request.getSession().getAttribute("user");
        if (user == null || !"admin".equals(user.getRole())) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write(gson.toJson(Map.of("error", "Only admins can create newsletters")));
            return;
        }

        try {
            // Parse request body
            String requestBody = request.getReader().lines().collect(Collectors.joining());
            Newsletter newsletter = gson.fromJson(requestBody, Newsletter.class);

            // Set creator
            newsletter.setCreatedBy(user.getUserId());

            // Create newsletter
            Newsletter createdNewsletter = newsletterDAO.createNewsletter(newsletter);

            if (createdNewsletter != null) {
                response.setStatus(HttpServletResponse.SC_CREATED);
                response.getWriter().write(gson.toJson(createdNewsletter));
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write(gson.toJson(Map.of("error", "Failed to create newsletter")));
            }
        } catch (Exception e) {
            Logger.error("NewsletterServlet", "Error creating newsletter", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(gson.toJson(Map.of("error", "Server error creating newsletter")));
        }
    }

    /**
     * Handle PUT requests - update existing newsletter
     */
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");

        // Check if user is admin
        User user = (User) request.getSession().getAttribute("user");
        if (user == null || !"admin".equals(user.getRole())) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write(gson.toJson(Map.of("error", "Only admins can update newsletters")));
            return;
        }

        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(gson.toJson(Map.of("error", "Newsletter ID is required")));
            return;
        }

        try {
            int id = Integer.parseInt(pathInfo.substring(1));

            // Check if newsletter exists
            Newsletter existingNewsletter = newsletterDAO.getNewsletterById(id);
            if (existingNewsletter == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write(gson.toJson(Map.of("error", "Newsletter not found")));
                return;
            }

            // Parse request body
            String requestBody = request.getReader().lines().collect(Collectors.joining());
            Newsletter updatedNewsletter = gson.fromJson(requestBody, Newsletter.class);
            updatedNewsletter.setId(id);

            // Preserve created_by field
            updatedNewsletter.setCreatedBy(existingNewsletter.getCreatedBy());

            // Update newsletter
            boolean success = newsletterDAO.updateNewsletter(updatedNewsletter);

            if (success) {
                Newsletter newsletter = newsletterDAO.getNewsletterById(id);
                response.getWriter().write(gson.toJson(newsletter));
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write(gson.toJson(Map.of("error", "Failed to update newsletter")));
            }
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(gson.toJson(Map.of("error", "Invalid newsletter ID")));
        } catch (Exception e) {
            Logger.error("NewsletterServlet", "Error updating newsletter", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(gson.toJson(Map.of("error", "Server error updating newsletter")));
        }
    }

    /**
     * Handle DELETE requests - delete newsletter
     */
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");

        // Check if user is admin
        User user = (User) request.getSession().getAttribute("user");
        if (user == null || !"admin".equals(user.getRole())) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write(gson.toJson(Map.of("error", "Only admins can delete newsletters")));
            return;
        }

        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(gson.toJson(Map.of("error", "Newsletter ID is required")));
            return;
        }

        try {
            int id = Integer.parseInt(pathInfo.substring(1));

            // Delete newsletter
            boolean success = newsletterDAO.deleteNewsletter(id);

            if (success) {
                response.getWriter().write(gson.toJson(Map.of("message", "Newsletter deleted successfully")));
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write(gson.toJson(Map.of("error", "Newsletter not found or could not be deleted")));
            }
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(gson.toJson(Map.of("error", "Invalid newsletter ID")));
        } catch (Exception e) {
            Logger.error("NewsletterServlet", "Error deleting newsletter", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(gson.toJson(Map.of("error", "Server error deleting newsletter")));
        }
    }
}
