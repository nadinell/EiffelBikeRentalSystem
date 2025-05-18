package com.eiffelbikerental.service;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;

import com.utils.DatabaseUtils;

public class BikeFeedbackService {

    // Add Feedback dynamically (rating or note, or both)
    public void addFeedback(int bikeId, int userId, Integer rating, String note) throws SQLException {
        String query = "INSERT INTO bike_feedback (bike_id, user_id, rating, note) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, bikeId);
            stmt.setInt(2, userId);
            stmt.setObject(3, rating, Types.INTEGER); // Dynamically handle rating (nullable)
            stmt.setObject(4, note, Types.VARCHAR);   // Dynamically handle note (nullable)

            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new SQLException("Error while adding feedback", e);
        }
    }

    // Fetch feedback dynamically for a specific bike
    public List<Map<String, Object>> getBikeFeedback(int bikeId) throws SQLException {
        List<Map<String, Object>> feedbackList = new ArrayList<>();
        String query = "SELECT user_id, rating, note, date_added FROM bike_feedback WHERE bike_id = ?";
        try (Connection conn = DatabaseUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, bikeId);
            ResultSet rs = stmt.executeQuery();

            SimpleDateFormat sdf = new SimpleDateFormat("d MMM yyyy"); // Date format for output

            while (rs.next()) {
                Map<String, Object> feedback = processFeedbackRow(rs, sdf);
                feedbackList.add(feedback);
            }
        } catch (SQLException e) {
            throw new SQLException("Error while fetching bike feedback", e);
        }

        return feedbackList;
    }

    // Helper method to process each row dynamically
    private Map<String, Object> processFeedbackRow(ResultSet rs, SimpleDateFormat sdf) throws SQLException {
        Map<String, Object> feedback = new HashMap<>();

        // Dynamically fetch and handle nullable columns
        Integer userId = rs.getObject("user_id") != null ? rs.getInt("user_id") : null;
        Integer rating = rs.getObject("rating") != null ? rs.getInt("rating") : null;
        String note = rs.getString("note");
        Timestamp dateAdded = rs.getTimestamp("date_added");

        feedback.put("user_id", userId);
        feedback.put("rating", rating);
        feedback.put("note", note != null && !note.trim().isEmpty() ? note : "No note provided");
        feedback.put("dateAdded", dateAdded != null ? sdf.format(dateAdded) : "Unknown Date");

        return feedback;
    }

    // Optional: Fetch all feedback dynamically for all bikes
    public Map<Integer, List<Map<String, Object>>> getAllFeedback() throws SQLException {
        Map<Integer, List<Map<String, Object>>> allFeedback = new HashMap<>();
        String query = "SELECT bike_id, user_id, rating, note, date_added FROM bike_feedback";

        try (Connection conn = DatabaseUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            SimpleDateFormat sdf = new SimpleDateFormat("d MMM yyyy");

            while (rs.next()) {
                int bikeId = rs.getInt("bike_id");
                Map<String, Object> feedback = processFeedbackRow(rs, sdf);

                allFeedback.computeIfAbsent(bikeId, k -> new ArrayList<>()).add(feedback);
            }
        } catch (SQLException e) {
            throw new SQLException("Error while fetching all feedback", e);
        }

        return allFeedback;
    }
    
    
 // Assuming DatabaseUtils provides a connection to the database
    private Connection getConnection() throws SQLException {
        return DatabaseUtils.getConnection();
    }

 // Method to get the count of each rating (1-5) for a specific bike
    public Map<Integer, Integer> getRatingCounts(int bikeId) throws SQLException {
        Map<Integer, Integer> ratingCounts = new HashMap<>();

        String query = "SELECT rating, COUNT(*) AS count FROM bike_feedback WHERE bike_id = ? GROUP BY rating";
        try (Connection connection = getConnection(); PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, bikeId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int rating = rs.getInt("rating");
                    int count = rs.getInt("count");
                    ratingCounts.put(rating, count); // Store the count for each rating
                }
            }
        }
        return ratingCounts;
    }


    // Method to get the average rating for a specific bike
    public double getAverageRating(int bikeId) throws SQLException {
        String query = "SELECT AVG(rating) AS avg_rating FROM bike_feedback WHERE bike_id = ?";
        try (Connection connection = getConnection(); PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, bikeId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("avg_rating"); // Return the calculated average
                }
            }
        }
        return 0.0;  // Default if no ratings exist
    }
}
