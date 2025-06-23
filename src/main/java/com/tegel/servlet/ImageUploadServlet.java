package com.tegel.servlet;

import com.tegel.dao.ImageDAO;
import com.tegel.model.Image;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;

@WebServlet("/upload-image")
@MultipartConfig(fileSizeThreshold = 1024 * 1024, // 1 MB
        maxFileSize = 10 * 1024 * 1024, // 10 MB
        maxRequestSize = 50 * 1024 * 1024 // 50 MB
)

// this servlet handles uploading images (used by an admin or user)
public class ImageUploadServlet extends HttpServlet {
    // create an imageDAO to use for this servlet
    private ImageDAO imageDAO = new ImageDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Part filePart = request.getPart("image");
        String fileName = filePart.getSubmittedFileName();
        String contentType = filePart.getContentType();

        try (InputStream filecontent = filePart.getInputStream()) {
            byte[] imageData = filecontent.readAllBytes();

            // create a new image and set the properties
            Image image = new Image();
            image.setName(fileName);
            image.setContentType(contentType);
            image.setData(imageData);

            boolean isSaved = imageDAO.saveImage(image);

            if (isSaved) {
                response.setContentType("application/json");
                response.getWriter()
                        .write("{\success\": true, \"message\": \"Image uploaded successfully.\"}");
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter()
                        .write("{\success\": false, \"message\": \"Image upload failed.\"}");
            }
        }

    }

}
