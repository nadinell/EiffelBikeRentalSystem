package com.gustavebikeservice.service;

import com.eiffelbikerental.model.Bike;
import com.gustavebikeservice.model.Wishlist;
import com.utils.DatabaseUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class WishlistService {


    public boolean addToWishlist(int userId, int bikeId) {
        String query = "INSERT INTO wishlist (user_id, bike_id) VALUES (?, ?)";
        try (Connection connection = DatabaseUtils.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, userId);
            statement.setInt(2, bikeId);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean removeFromWishlist(int userId, int bikeId) {
        String query = "DELETE FROM wishlist WHERE user_id = ? AND bike_id = ?";
        try (Connection connection = DatabaseUtils.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, userId);
            statement.setInt(2, bikeId);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Integer> getWishlist(int userId) {
        String query = "SELECT bike_id FROM wishlist WHERE user_id = ?";
        List<Integer> bikeIds = new ArrayList<>();
        try (Connection connection = DatabaseUtils.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, userId);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                bikeIds.add(resultSet.getInt("bike_id"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return bikeIds;
    }
   
        
 // Method to get the wishlist by userId
    public List<Bike> getWishlistByUserId(int userId) throws SQLException {
        List<Bike> wishlist = new ArrayList<>();
        Connection connection = DatabaseUtils.getConnection();
        String query = "SELECT b.id, b.name, b.model, b.image, b.sale_price, b.isAvailable " +
                       "FROM bikes b " +
                       "JOIN wishlist w ON b.id = w.bike_id " +
                       "WHERE w.user_id = ? AND b.is_sold = 0"; // Added condition for is_sold = 0
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                int bikeId = rs.getInt("id");
                String name = rs.getString("name");
                String model = rs.getString("model");
                String imageUrl = rs.getString("image");
                double price = rs.getDouble("sale_price");
                boolean available = rs.getBoolean("isAvailable");
                
                Bike bike = new Bike(bikeId, name, model, imageUrl, price, available);
                wishlist.add(bike);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return wishlist;
    }

 // Check if a bike is in the user's wishlist
    public boolean isBikeInWishlist(String userId, String bikeId) {
        String query = "SELECT COUNT(*) FROM wishlist WHERE user_id = ? AND bike_id = ?";

        try (Connection connection = DatabaseUtils.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            // Set query parameters
            preparedStatement.setString(1, userId);
            preparedStatement.setString(2, bikeId);

            // Execute query
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    // Return true if count > 0 (item exists in wishlist)
                    return resultSet.getInt(1) > 0;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // If there's an exception or no result, return false
        return false;
    }

    }



