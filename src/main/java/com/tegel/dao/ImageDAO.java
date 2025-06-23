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

        try (Connection conncetion = DatabaseManager.getConnection();
             PreparedStatement statement = conncetion.prepareStatement(sql)) {

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

    // retrieves a list of all the images metadata from the database (everything except the image)
    public List<Image> getImageMetadata() {
        String sql = "SELECT id, name, content_type, upload_date FROM images";
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
}
