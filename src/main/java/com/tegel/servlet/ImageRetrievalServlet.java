package com.tegel.servlet;

import com.tegel.dao.ImageDAO;
import com.tegel.model.Image;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/get-image")
public class ImageRetrievalServlet extends HttpServlet {

    // create imageDAO to use for this servlet
    private ImageDAO imageDAO = new ImageDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String idParam = request.getParameter("id");

        if (idParam != null && !idParam.trim().isEmpty()) {
            try {
                int id = Integer.parseInt(idParam);
                Image image = imageDAO.getImageById(id);

                if (image != null) {
                    response.setContentType(image.getContentType());
                    response.setContentLength(image.getData().length);
                    response.getOutputStream().write(image.getData());
                } else {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND, "Image not found");
                }
            } catch (NumberFormatException e) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid image ID");
            }
        } else {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Image ID is required");
        }
    }
}
