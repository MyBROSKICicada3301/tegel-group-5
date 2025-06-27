package com.tegel.dao;

import com.tegel.model.Image;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

// handles database action related to images
public class ImageDAO {

    /**
     * Saves an image to the database.
     *
     * @param image The image to save.
     * @return The ID of the newly inserted image, or -1 if the operation failed.
     */
    public int saveImage(Image image) {
        String sql = "INSERT INTO mod4db.images (name, content_type, data) VALUES (?, ?, ?) RETURNING id";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, image.getName());
            statement.setString(2, image.getContentType());
            statement.setBytes(3, image.getData());

            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                return rs.getInt(1); // Return the generated image ID
            }
            return -1;
        } catch (SQLException e) {
            e.printStackTrace(); // Log the exception
            return -1; // Return -1 if an error occurs
        }
    }

    /**
     * Saves an image associated with an event to the database.
     *
     * @param image The image to save.
     * @param eventId The ID of the event to associate with this image.
     * @return The ID of the saved image if successful, -1 otherwise.
     */
    public int saveEventImage(Image image, int eventId) {
        String sql = "INSERT INTO mod4db.images (name, content_type, data, event_id) VALUES (?, ?, ?, ?) RETURNING id";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, image.getName());
            statement.setString(2, image.getContentType());
            statement.setBytes(3, image.getData());
            statement.setInt(4, eventId);

            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                return rs.getInt(1); // Return the generated image ID
            }
            return -1;
        } catch (SQLException e) {
            e.printStackTrace(); // Log the exception
            return -1; // Return -1 if an error occurs
        }
    }

    /**
     * Retrieves an image object by its ID.
     *
     * @param id id of the image to retrieve
     * @return the image object if found, null otherwise
     */
    public Image getImageById(int id) {
        String sql = "SELECT * FROM mod4db.images WHERE id = ?";

        try (Connection conncetion = DatabaseManager.getConnection();
             PreparedStatement statement = conncetion.prepareStatement(sql)) {

            statement.setInt(1, id);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                Image image = new Image();
                image.setId(resultSet.getInt("id"));
                image.setName(resultSet.getString("name"));
                image.setContentType(resultSet.getString("content_type"));
                image.setData(resultSet.getBytes("data")); // Using "data" instead of "image"
                image.setTimestamp(resultSet.getTimestamp("upload_date"));
                return image;
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Log the exception
        }

        return null; // Return null if no image found with the given ID
    }

    // retrieves a list of all the images metadata from the database (everything except the image)
    public List<Image> getImageMetadata() {
        String sql = "SELECT id, name, content_type, upload_date FROM mod4db.images";
        List<Image> images = new ArrayList<>();

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                Image image = new Image();
                image.setId(resultSet.getInt("id"));
                image.setName(resultSet.getString("name"));
                image.setContentType(resultSet.getString("content_type"));
                image.setTimestamp(resultSet.getTimestamp("upload_date"));

                images.add(image);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return images;
    }

    /**
     * Gets all images associated with a specific event
     *
     * @param eventId The ID of the event
     * @return A list of images for the event
     */
    public List<Image> getImagesByEventId(int eventId) {
        String sql = "SELECT * FROM mod4db.images WHERE event_id = ?";
        List<Image> images = new ArrayList<>();

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, eventId);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                Image image = new Image();
                image.setId(resultSet.getInt("id"));
                image.setName(resultSet.getString("name"));
                image.setContentType(resultSet.getString("content_type"));
                image.setData(resultSet.getBytes("data"));
                image.setTimestamp(resultSet.getTimestamp("upload_date"));

                images.add(image);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return images;
    }

    /**
     * Updates an image record to associate it with an event
     *
     * @param imageId The ID of the image to update
     * @param eventId The ID of the event to associate with the image
     * @return true if the update was successful, false otherwise
     */
    public boolean updateEventIdForImage(int imageId, int eventId) {
        String sql = "UPDATE mod4db.images SET event_id = ? WHERE id = ?";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, eventId);
            statement.setInt(2, imageId);

            int rowsUpdated = statement.executeUpdate();
            return rowsUpdated > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
