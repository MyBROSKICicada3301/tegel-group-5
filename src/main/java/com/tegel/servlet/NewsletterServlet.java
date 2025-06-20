package com.tegel.servlet;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.tegel.dao.NewsletterDAO;
import com.tegel.model.Newsletter;
import com.tegel.util.LocalDateTimeAdapter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NewsletterServlet extends HttpServlet {
    private static final Logger logger = Logger.getLogger(NewsletterServlet.class.getName());
    private final NewsletterDAO newsletterDAO = new NewsletterDAO();

    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .setPrettyPrinting()
            .create();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        logger.info("NewsletterServlet receiving request with pathInfo: " + pathInfo);

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        try {
            // Get a specific newsletter by ID
            if (pathInfo != null && !pathInfo.equals("/")) {
                String idStr = pathInfo.substring(1); // Remove the leading '/'
                logger.info("Fetching newsletter with ID: " + idStr);

                try {
                    int newsletterId = Integer.parseInt(idStr);
                    Newsletter newsletter = newsletterDAO.getNewsletterById(newsletterId);

                    if (newsletter != null) {
                        logger.info("Found newsletter: " + newsletter.getTitle());
                        out.print(gson.toJson(newsletter));
                    } else {
                        logger.warning("Newsletter not found with ID: " + newsletterId);
                        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                        Map<String, String> errorResponse = new HashMap<>();
                        errorResponse.put("error", "Newsletter not found");
                        out.print(gson.toJson(errorResponse));
                    }
                } catch (NumberFormatException e) {
                    logger.warning("Invalid newsletter ID format: " + idStr);
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    Map<String, String> errorResponse = new HashMap<>();
                    errorResponse.put("error", "Invalid newsletter ID format");
                    out.print(gson.toJson(errorResponse));
                }
            }
            // Get latest newsletters
            else {
                int limit = 10; // Change default limit to show more newsletters
                String limitParam = request.getParameter("limit");
                if (limitParam != null && !limitParam.isEmpty()) {
                    try {
                        limit = Integer.parseInt(limitParam);
                        logger.info("Using custom limit: " + limit);
                    } catch (NumberFormatException e) {
                        logger.warning("Invalid limit parameter: " + limitParam + ". Using default: " + limit);
                    }
                }

                logger.info("Fetching latest " + limit + " newsletters");
                List<Newsletter> newsletters = newsletterDAO.getLatestNewsletters(limit);
                logger.info("Found " + newsletters.size() + " newsletters");

                Map<String, Object> responseData = new HashMap<>();
                responseData.put("newsletters", newsletters);
                out.print(gson.toJson(responseData));
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error processing newsletter request", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Internal server error");
            errorResponse.put("message", e.getMessage());
            out.print(gson.toJson(errorResponse));
        }
    }
}
