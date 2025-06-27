package com.tegel.dao;

import com.tegel.model.Image;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * ImageDAO is responsible for interacting with the database to perform CRUD operations on images.
 * It provides methods to save an image, retrieve an image by its ID, and get metadata of all images
 */
public class ImageDAO {

    /**
     * Saves an image to the database.
     *
     * @param image The image to save.
     * @return true if the image was saved successfully, false otherwise.
     */
    public boolean saveImage(Image image) {
        String sql = "INSERT INTO images (name, content_type, image) VALUES (?, ?, ?)";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, image.getName());
            statement.setString(2, image.getContentType());
            statement.setBytes(3, image.getData());

            int rowsInserted = statement.executeUpdate();
            // true if success
            return rowsInserted > 0;
        } catch (SQLException e) {
            e.printStackTrace(); // Log the exception
            return false; // Return false if an error occurs
        }
    }

    /**
     * Retrieves an image object by its ID.
     *
     * @param id id of the image to retrieve
     * @return the image object if found, null otherwise
     */
    public Image getImageById(int id) {
        String sql = "SELECT * FROM images WHERE id = ?";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                Image image = new Image();
                image.setId(resultSet.getInt("id"));
                image.setName(resultSet.getString("name"));
                image.setContentType(resultSet.getString("content_type"));
                image.setData(resultSet.getBytes("image"));
                image.setTimestamp(resultSet.getTimestamp("upload_date"));
                return image;
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Log the exception
        }

        return null; // Return null if no image found with the given ID
    }
}
