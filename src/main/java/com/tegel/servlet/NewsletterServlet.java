package com.tegel.servlet;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.tegel.dao.NewsletterDAO;
import com.tegel.dao.UserDAO;
import com.tegel.model.Newsletter;
import com.tegel.model.User;
import com.tegel.util.LocalDateTimeAdapter;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@WebServlet("/api/newsletters/*")
public class NewsletterServlet extends HttpServlet {
    private NewsletterDAO newsletterDAO;
    private UserDAO userDAO;
    private Gson gson;

    @Override
    public void init() throws ServletException {
        super.init();
        this.newsletterDAO = new NewsletterDAO();
        this.userDAO = new UserDAO();
        this.gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .create();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String pathInfo = request.getPathInfo();

        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                handleGetAllNewsletters(request, response);
            } else {
                String[] pathParts = pathInfo.split("/");
                if (pathParts.length == 2) {
                    int id = Integer.parseInt(pathParts[1]);
                    handleGetNewsletterById(id, response);
                } else {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    response.getWriter().write(gson.toJson(Map.of("error", "Invalid request path")));
                }
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(gson.toJson(Map.of("error", "Internal server error")));
        }
    }

    private void handleGetAllNewsletters(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        String publishedParam = request.getParameter("published");
        String limitParam = request.getParameter("limit");
        String searchTerm = request.getParameter("search");

        List<Newsletter> newsletters;

        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            boolean onlyPublished = "true".equals(publishedParam);
            newsletters = newsletterDAO.searchNewsletters(searchTerm.trim(), onlyPublished);
        } else if ("true".equals(publishedParam)) {
            if (limitParam != null) {
                int limit = Integer.parseInt(limitParam);
                newsletters = newsletterDAO.getLatestNewsletters(limit);
            } else {
                newsletters = newsletterDAO.getAllPublishedNewsletters();
            }
        } else {
            newsletters = newsletterDAO.getAllNewsletters();
        }

        response.getWriter().write(gson.toJson(newsletters));
    }

    private void handleGetNewsletterById(int id, HttpServletResponse response) throws IOException {
        Newsletter newsletter = newsletterDAO.getNewsletterById(id);

        if (newsletter != null) {
            response.getWriter().write(gson.toJson(newsletter));
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write(gson.toJson(Map.of("error", "Newsletter not found")));
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        if (!isUserAdmin(request)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write(gson.toJson(Map.of("error", "Admin access required")));
            return;
        }

        try {
            String requestBody = request.getReader().lines().collect(Collectors.joining());
            Newsletter newsletter = gson.fromJson(requestBody, Newsletter.class);

            HttpSession session = request.getSession(false);
            Integer userId = (Integer) session.getAttribute("userId");
            newsletter.setCreatedBy(userId);

            Newsletter createdNewsletter = newsletterDAO.createNewsletter(newsletter);

            if (createdNewsletter != null) {
                response.getWriter().write(gson.toJson(createdNewsletter));
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().write(gson.toJson(Map.of("error", "Failed to create newsletter")));
            }

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(gson.toJson(Map.of("error", "Invalid request data")));
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        if (!isUserAdmin(request)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write(gson.toJson(Map.of("error", "Admin access required")));
            return;
        }

        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(gson.toJson(Map.of("error", "Newsletter ID required")));
            return;
        }

        try {
            String[] pathParts = pathInfo.split("/");
            int id = Integer.parseInt(pathParts[1]);

            String requestBody = request.getReader().lines().collect(Collectors.joining());
            Newsletter newsletter = gson.fromJson(requestBody, Newsletter.class);
            newsletter.setId(id);

            boolean success = newsletterDAO.updateNewsletter(newsletter);

            if (success) {
                Newsletter updatedNewsletter = newsletterDAO.getNewsletterById(id);
                response.getWriter().write(gson.toJson(updatedNewsletter));
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write(gson.toJson(Map.of("error", "Newsletter not found or update failed")));
            }

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(gson.toJson(Map.of("error", "Invalid request data")));
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        if (!isUserAdmin(request)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write(gson.toJson(Map.of("error", "Admin access required")));
            return;
        }

        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(gson.toJson(Map.of("error", "Newsletter ID required")));
            return;
        }

        try {
            String[] pathParts = pathInfo.split("/");
            int id = Integer.parseInt(pathParts[1]);

            boolean success = newsletterDAO.deleteNewsletter(id);

            if (success) {
                response.getWriter().write(gson.toJson(Map.of("message", "Newsletter deleted successfully")));
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write(gson.toJson(Map.of("error", "Newsletter not found or delete failed")));
            }

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(gson.toJson(Map.of("error", "Invalid request data")));
        }
    }

    private boolean isUserAdmin(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) return false;

        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) return false;

        User user = userDAO.getUserById(userId);
        return user != null && "admin".equals(user.getRole());
    }
}