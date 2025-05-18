package com.eiffelbikerental.service;

import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import com.utils.NotificationUtils;
import com.eiffelbikerental.model.Bike;
import com.eiffelbikerental.model.User;
import com.eiffelbikerental.model.WaitingListEntry;
import com.utils.DatabaseUtils;
import com.utils.NotificationUtils;

public class BikeRentalService {

    private Map<Integer, Bike> rentedBikes;
    private Map<Integer, LocalDateTime> notifiedUsers = new ConcurrentHashMap<>();
    private Map<Integer, Queue<User>> waitingLists = new ConcurrentHashMap<>();

    public BikeRentalService() {
        rentedBikes = new HashMap<>();
        waitingLists = new HashMap<>();
    }





    // Add a new bike to the inventory
    public void addBike(Bike bike) {
        String query = "INSERT INTO bikes (name, model, image, price, isAvailable, description, owner) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = DatabaseUtils.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, bike.getName());
            statement.setString(2, bike.getModel());
            statement.setString(3, bike.getImage());
            statement.setDouble(4, bike.getPrice());
            statement.setBoolean(5, bike.isAvailable());
            statement.setString(6, bike.getDescription());
            statement.setString(7, bike.getOwner());
            statement.executeUpdate();
            System.out.println("Bike added: " + bike);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }





    // Get a bike by its ID
    public Bike getBikeById(int bikeId) {
        String query = "SELECT * FROM bikes WHERE id = ?";
        try (Connection connection = DatabaseUtils.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, bikeId);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return new Bike(
                        resultSet.getInt("id"),
                        resultSet.getString("name"),
                        resultSet.getString("model"),
                        resultSet.getString("image"),
                        resultSet.getDouble("price"),
                        resultSet.getBoolean("isAvailable"),
                        resultSet.getString("description"),
                        resultSet.getString("owner")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }


 // Method to get a list of all bikes for rental
    public List<Bike> getBikes() {
        List<Bike> availableBikes = new ArrayList<>();
        String query = "SELECT * FROM bikes where is_sold = 0";

        try (Connection connection = DatabaseUtils.getConnection();  // Ensure this method properly connects to the DB
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                // Map ResultSet data to Bike object
                Bike bike = new Bike(
                        resultSet.getInt("id"),
                        resultSet.getString("name"),
                        resultSet.getString("model"),
                        resultSet.getString("image"),
                        resultSet.getDouble("price"),
                        resultSet.getBoolean("isAvailable"),
                        resultSet.getString("description"),
                        resultSet.getString("owner")
                );
                availableBikes.add(bike);
            }
        } catch (SQLException e) {
            // Use proper logging or custom exception handling
            System.err.println("SQL Exception while fetching bikes: " + e.getMessage());
            // Log the exception or rethrow as custom exception if needed
        }
        return availableBikes;
    }


 // Method to get a list of all available bikes for rental
    public List<Bike> getAvailableBikes() {
        List<Bike> availableBikes = new ArrayList<>();
        String query = "SELECT * FROM bikes WHERE isAvailable = true AND is_sold = 0";;

        try (Connection connection = DatabaseUtils.getConnection();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                Bike bike = new Bike(
                        resultSet.getInt("id"),
                        resultSet.getString("name"),
                        resultSet.getString("model"),
                        resultSet.getString("image"),
                        resultSet.getDouble("price"),
                        resultSet.getBoolean("isAvailable"),
                        resultSet.getString("description"),
                        resultSet.getString("owner")
                );
                availableBikes.add(bike);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return availableBikes;
    }

    // List all currently rented bikes
    public List<Bike> listRentedBikes() {
        List<Bike> rentedBikeList = new ArrayList<>();
        String query = "SELECT * FROM bikes WHERE isAvailable = false";

        try (Connection connection = DatabaseUtils.getConnection();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                Bike bike = new Bike(
                        resultSet.getInt("id"),
                        resultSet.getString("name"),
                        resultSet.getString("model"),
                        resultSet.getString("image"),
                        resultSet.getDouble("price"),
                        resultSet.getBoolean("isAvailable"),
                        resultSet.getString("description"),
                        resultSet.getString("owner")
                );
                rentedBikeList.add(bike);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rentedBikeList;
    }

 // Method to calculate rental duration in hours
    public int calculateRentalDuration(String startDateStr, String endDateStr) {
        // Define the date-time format (assuming the format is "yyyy-MM-dd'T'HH:mm")
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

        // Parse the start and end date strings to LocalDateTime objects
        LocalDateTime startDate = LocalDateTime.parse(startDateStr, formatter);
        LocalDateTime endDate = LocalDateTime.parse(endDateStr, formatter);

        // Calculate the duration between the start and end dates
        Duration duration = Duration.between(startDate, endDate);

        // Return the duration in hours
        return (int) duration.toHours();
    }

 // Calculate rental cost based on bike and rental duration (in hours)
    public double calculateRentalCost(Bike bike, String startDateStr, String endDateStr) {
        if (bike == null || startDateStr == null || endDateStr == null) {
            throw new IllegalArgumentException("Invalid bike or rental duration.");
        }

        // Calculate rental duration in hours using the provided start and end date strings
        int rentalDurationHours = calculateRentalDuration(startDateStr, endDateStr);

        if (rentalDurationHours <= 0) {
            throw new IllegalArgumentException("Rental duration must be positive.");
        }

        // Base cost: price per hour * rental duration
        double cost = bike.getPrice() * rentalDurationHours;

        // Apply discount for longer rental durations (e.g., 10% discount for rentals longer than 24 hours)
        if (rentalDurationHours > 24) {
            cost *= 0.9;  // 10% discount
        }

        return cost;
    }



    public boolean isBikeAvailable(int bikeId, String startDateStr, String endDateStr) {
        // Modify to match the new format yyyy-MM-ddTHH:mm
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
        LocalDateTime startDate = LocalDateTime.parse(startDateStr, formatter);
        LocalDateTime endDate = LocalDateTime.parse(endDateStr, formatter);

        // Check if startDate is before the current date and time
        LocalDateTime currentDate = LocalDateTime.now();
        if (startDate.isBefore(currentDate)) {
            System.out.println("Start date cannot be before the current date.");
            return false; // Not available if startDate is in the past
        }

        // Convert to Timestamp for database query
        Timestamp startTimestamp = Timestamp.valueOf(startDate.atZone(ZoneId.systemDefault()).toLocalDateTime());
        Timestamp endTimestamp = Timestamp.valueOf(endDate.atZone(ZoneId.systemDefault()).toLocalDateTime());

        String query = "SELECT COUNT(*) AS overlapCount FROM rentals " +
                       "WHERE bike_id = ? AND (" +
                       "(start_date <= ? AND end_date >= ?) OR " +  // Check if new rental starts before current rental ends
                       "(start_date BETWEEN ? AND ?) OR " +        // Check if new rental ends during an existing rental period
                       "(end_date BETWEEN ? AND ?))";              // Check if current rental ends during new rental period

        try (Connection connection = DatabaseUtils.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            // Set the parameters for the prepared statement (the order should match the query placeholders)
            statement.setInt(1, bikeId);
            statement.setTimestamp(2, endTimestamp);   // endDate check for overlap
            statement.setTimestamp(3, startTimestamp); // startDate check for overlap
            statement.setTimestamp(4, startTimestamp); // Additional check for new rental starting within the period
            statement.setTimestamp(5, endTimestamp);   // Additional check for new rental ending within the period
            statement.setTimestamp(6, startTimestamp); // Additional check for current rental starting within the period
            statement.setTimestamp(7, endTimestamp);   // Additional check for current rental ending within the period

            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                int overlapCount = resultSet.getInt("overlapCount");
                return overlapCount == 0; // Available if no overlap
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public String getBikeStatus(int bikeId) {
        Bike bike = getBikeById(bikeId);
        if (bike != null) {
            return bike.isAvailable() ? "available" : "rented";
        }
        return null;
    }
    
    
 // Method to get the waiting list
    public List<WaitingListEntry> getWaitingList(int bikeId) {
        List<WaitingListEntry> waitingList = new ArrayList<>();
        String query = "SELECT * FROM waiting_list WHERE bike_id = ? ORDER BY date_added ASC";

        try (Connection connection = DatabaseUtils.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, bikeId);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    int userId = rs.getInt("user_id");
                    Date dateAdded = rs.getDate("date_added");
                    User user = getUserById(userId);
                    WaitingListEntry entry = new WaitingListEntry(user, dateAdded);
                    waitingList.add(entry);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return waitingList;
    }

    // Method to get the user by userId
    private User getUserById(int userId) {
        User user = null;
        String query = "SELECT * FROM users WHERE id = ?";
        try (Connection conn = DatabaseUtils.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String userEmail = rs.getString("email");
                    user = new User(userId, userEmail);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return user;
    }


    public void notifyWaitingListIfBikeAvailable(int bikeId) throws IOException {
        // Check if the bike is available
        Bike bike = getBikeById(bikeId);
        if (bike != null && bike.isAvailable()) {
            // Get the waiting list for the bike
            List<WaitingListEntry> waitingList = getWaitingList(bikeId);

            if (!waitingList.isEmpty()) {
                // Get the first user on the waiting list (first-come, first-served)
                WaitingListEntry firstUserEntry = waitingList.get(0);
                User user = firstUserEntry.getUser();

                // Check if the user has already been notified
                if (!notifiedUsers.containsKey(user.getId())) {
                    // Send email notification to the first user
                    String subject = "Bike Available for Rental";
                    String message = "Dear " + user.getEmail() + ",\n\n" +
                                     "The bike you're interested in is now available for rental. " +
                                     "Please visit the rental service to complete your booking.\n\n" +
                                     "Best regards,\nEiffel Bike Rental Team";

                    NotificationUtils.sendEmail(user.getEmail(), subject, message);
                    
                    // Mark the user as notified
                    notifiedUsers.put(user.getId(), LocalDateTime.now());

                    // removeUserFromWaitingList(bikeId, user);
                }
            }
        }
    }



}
