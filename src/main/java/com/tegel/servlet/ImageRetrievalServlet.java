package com.tegel.servlet;

import com.tegel.dao.ImageDAO;
import com.tegel.model.Image;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Servlet for retrieving images from the database
 * This servlet retrieves an image by its ID or by an event ID and sends it to the client
 * Usage: /tegel_webapp/get-image?id=x (for direct image ID)
 *        /tegel_webapp/get-image?eventId=x (for image associated with an event)
 */
@WebServlet("/get-image")
public class ImageRetrievalServlet extends HttpServlet {
    private static final Logger logger = Logger.getLogger(ImageRetrievalServlet.class.getName());
    private ImageDAO imageDAO = new ImageDAO();

    /**
     * Handles GET requests to retrieve images
     * The image can be retrieved by:
     * - image ID (?id=x)
     * - event ID (?eventId=x)
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // For debugging - just to confirm the servlet is getting called
        logger.info("ImageRetrievalServlet doGet called with request URI: " + request.getRequestURI());

        // Get the image ID from the request
        String imageIdStr = request.getParameter("id");
        String eventIdStr = request.getParameter("eventId");

        logger.info("Image retrieval request received - imageId: " + imageIdStr + ", eventId: " + eventIdStr);

        if (imageIdStr == null && eventIdStr == null) {
            logger.warning("Image retrieval request with no ID or eventId parameter");
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Either image ID or event ID is required");
            return;
        }

        try {
            Image image = null;
            int requestedId = -1;

            // If direct image ID is provided, use it
            if (imageIdStr != null && !imageIdStr.trim().isEmpty()) {
                requestedId = Integer.parseInt(imageIdStr);
                logger.info("Image retrieval request for image ID: " + requestedId);
                image = imageDAO.getImageById(requestedId);

                if (image == null) {
                    logger.warning("Image not found with ID: " + requestedId);
                }
            }
            // If event ID is provided, get the image associated with that event
            else if (eventIdStr != null && !eventIdStr.trim().isEmpty()) {
                int eventId = Integer.parseInt(eventIdStr);
                logger.info("Image retrieval request for event ID: " + eventId);

                List<Image> images = imageDAO.getImagesByEventId(eventId);
                if (images != null && !images.isEmpty()) {
                    // Get the first image associated with the event
                    image = images.get(0);
                    logger.info("Found image ID " + image.getId() + " for event ID: " + eventId);
                } else {
                    logger.warning("No images found for event ID: " + eventId);
                }
            }

            // Check if we have a valid image with data
            if (image == null) {
                logger.warning("No image found for request (imageId: " + imageIdStr + ", eventId: " + eventIdStr + ")");
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Image not found");
                return;
            }

            if (image.getData() == null || image.getData().length == 0) {
                logger.warning("Image found but has no data (imageId: " + (image.getId() == 0 ? requestedId : image.getId()) + ")");
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Image has no data");
                return;
            }

            // Add CORS headers for debugging purposes
            response.setHeader("Access-Control-Allow-Origin", "*");
            response.setHeader("Access-Control-Allow-Methods", "GET");

            // Set response headers to prevent caching issues
            response.setContentType(image.getContentType());
            response.setHeader("Cache-Control", "public, max-age=86400"); // Cache for 1 day
            response.setContentLength(image.getData().length);

            // Write the image data to the response
            response.getOutputStream().write(image.getData());
            response.getOutputStream().flush();

            logger.info("Image successfully retrieved and sent: ID=" + image.getId() +
                      ", Type=" + image.getContentType() +
                      ", Size=" + image.getData().length + " bytes");

        } catch (NumberFormatException e) {
            logger.warning("Invalid ID format: " + (imageIdStr != null ? imageIdStr : eventIdStr) + " - " + e.getMessage());
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid ID format");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error retrieving image: " + e.getMessage(), e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error retrieving image: " + e.getMessage());
        }
    }
}
