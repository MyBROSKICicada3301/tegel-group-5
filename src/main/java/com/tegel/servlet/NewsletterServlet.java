package com.tegel.servlet;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.tegel.dao.NewsletterDAO;
import com.tegel.model.Newsletter;
import com.tegel.util.LocalDateTimeAdapter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
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

@WebServlet("/newsletter")
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

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        try {
            // Check for a specific newsletter ID
            String idStr = request.getParameter("id");

            if (idStr != null && !idStr.isEmpty()) {
                try {
                    int id = Integer.parseInt(idStr);
                    logger.info("Fetching newsletter with ID: " + id);

                    Newsletter newsletter = newsletterDAO.getNewsletterById(id);

                    if (newsletter != null) {
                        Map<String, Object> responseData = new HashMap<>();
                        responseData.put("newsletter", newsletter);
                        out.print(gson.toJson(responseData));
                    } else {
                        logger.warning("Newsletter not found with ID: " + id);
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
            } else {
                // Get latest newsletters
                int limit = 10; // Default limit
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