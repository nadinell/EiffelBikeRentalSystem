package com.eiffelbikerental.api;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.eiffelbikerental.model.User;
import com.eiffelbikerental.model.WaitingListEntry;
import com.eiffelbikerental.service.WaitingListService;
import com.utils.DatabaseUtils;
import com.utils.EmailService;
import com.utils.NotificationUtils;

@Path("/rentals")
public class Rentals {

    @GET
    @Path("/all")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getRentalHistory() {
        List<Rental> rentals = new ArrayList<>();
        try (Connection conn = DatabaseUtils.getConnection()) {
            String query = "SELECT * FROM rentals";
            try (Statement stmt = conn.createStatement()) {
                ResultSet rs = stmt.executeQuery(query);
                while (rs.next()) {
                    Rental rental = new Rental();
                    rental.setBikeId(rs.getInt("bike_id")); // Using int for bikeId
                    rental.setStartDate(rs.getTimestamp("start_date").toLocalDateTime().toString());
                    rental.setEndDate(rs.getTimestamp("end_date").toLocalDateTime().toString());
                    rental.setRentalCost(rs.getDouble("rental_cost"));
                    rental.setStatus(rs.getString("status"));
                    rental.setUserId(rs.getInt("user_id"));
                    rentals.add(rental);
                }
            }
        } catch (SQLException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity("{\"error\": \"Error fetching rental history: " + e.getMessage() + "\"}")
                           .build();
        }

        return Response.ok(rentals).build();
    }

  /*  @GET
    @Path("all/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getRentalsByUser(@PathParam("userId") int userId) {
        List<Rental> rentals = new ArrayList<>();
        try (Connection conn = DatabaseUtils.getConnection()) {
            String query = "SELECT * FROM rentals WHERE user_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, userId);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    Rental rental = new Rental();
                    rental.setBikeId(rs.getInt("bike_id"));
                    rental.setStartDate(rs.getTimestamp("start_date").toLocalDateTime().toString());
                    rental.setEndDate(rs.getTimestamp("end_date").toLocalDateTime().toString());
                    rental.setRentalCost(rs.getDouble("rental_cost"));
                    rental.setStatus(rs.getString("status"));
                    rental.setUserId(rs.getInt("user_id"));
                    rentals.add(rental);
                }
            }
        } catch (SQLException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity("{\"error\": \"Error fetching user rentals: " + e.getMessage() + "\"}")
                           .build();
        }

        if (rentals.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND)
                           .entity("{\"error\": \"No rentals found for the user\"}")
                           .build();
        }

        return Response.ok(rentals).build();
    }*/

    @GET
    @Path("all/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getRentalHistoryByUser(@PathParam("userId") int userId) {
        List<Rental> rentals = new ArrayList<>();

        String query = "SELECT rentals.bike_id, rentals.start_date, rentals.end_date, rentals.rental_cost, " +
                       "rentals.status, rentals.user_id, bikes.name as name " +
                       "FROM rentals " +
                       "INNER JOIN bikes ON rentals.bike_id = bikes.id " +
                       "WHERE rentals.user_id = ?";

        try (Connection conn = DatabaseUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, userId); // Set the userId parameter
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Rental rental = new Rental();
                rental.setBikeId(rs.getInt("bike_id"));
                rental.setBikeTitle(rs.getString("name")); // Fetch bike title
                rental.setStartDate(rs.getTimestamp("start_date").toLocalDateTime().toString());
                rental.setEndDate(rs.getTimestamp("end_date").toLocalDateTime().toString());
                rental.setRentalCost(rs.getDouble("rental_cost"));
                rental.setStatus(rs.getString("status"));
                rental.setUserId(rs.getInt("user_id"));

                // Calculate rental duration in hours
                long durationInMillis = rs.getTimestamp("end_date").getTime() - rs.getTimestamp("start_date").getTime();
                long durationInHours = durationInMillis / (1000 * 60 * 60);
                rental.setDuration(durationInHours);

                rentals.add(rental);
            }
        } catch (SQLException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity("{\"error\": \"Error fetching rental history: " + e.getMessage() + "\"}")
                           .build();
        }

        return Response.ok(rentals).build();
    }


    @POST
    @Path("/save")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createRental(Rental rental) {
        if (rental.getBikeId() <= 0 || rental.getStartDate() == null || rental.getEndDate() == null || rental.getRentalCost() <= 0 || rental.getUserId() <= 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                           .entity("{\"error\": \"Invalid input data\"}")
                           .build();
        }

        try (Connection conn = DatabaseUtils.getConnection()) {
            conn.setAutoCommit(false);

            String rentalQuery = "INSERT INTO rentals (bike_id, user_id, start_date, end_date, rental_cost, status, created_at) " +
                                 "VALUES (?, ?, ?, ?, ?, 'active', CURRENT_TIMESTAMP())";
            try (PreparedStatement pstmt = conn.prepareStatement(rentalQuery, Statement.RETURN_GENERATED_KEYS)) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
                Timestamp startDate = Timestamp.valueOf(LocalDateTime.parse(rental.getStartDate(), formatter));
                Timestamp endDate = Timestamp.valueOf(LocalDateTime.parse(rental.getEndDate(), formatter));

                pstmt.setInt(1, rental.getBikeId());
                pstmt.setInt(2, rental.getUserId());
                pstmt.setTimestamp(3, startDate);
                pstmt.setTimestamp(4, endDate);
                pstmt.setDouble(5, rental.getRentalCost());

                int affectedRows = pstmt.executeUpdate();
                if (affectedRows > 0) {
                    String updateBikeQuery = "UPDATE bikes SET isAvailable = 0, rentalCount = rentalCount + 1 WHERE id = ?";
                    try (PreparedStatement updatePstmt = conn.prepareStatement(updateBikeQuery)) {
                        updatePstmt.setInt(1, rental.getBikeId());
                        int rowsUpdated = updatePstmt.executeUpdate();
                        if (rowsUpdated > 0) {
                            conn.commit();

                            // Fetch the user details based on userId
                            String userQuery = "SELECT * FROM users WHERE id = ?";
                            try (PreparedStatement userStmt = conn.prepareStatement(userQuery)) {
                                userStmt.setInt(1, rental.getUserId());
                                ResultSet rs = userStmt.executeQuery();
                                if (rs.next()) {
                                    User user = new User();
                                    user.setId(rs.getInt("id"));
                                    user.setUsername(rs.getString("username"));
                                    user.setEmail(rs.getString("email"));
                                    user.setPhone(rs.getString("phone"));

                                    // Send the notification to the user
                                    String message = "Rental for bike " + rental.getBikeId() + " by user " + user.getUsername() + " has been successfully created.";
                                  //  NotificationUtils.sendNotification(user, message);
                                    return Response.ok("{\"success\": true}").build();
                                    
                                } else {
                                    conn.rollback();
                                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                                                   .entity("{\"error\": \"User not found\"}")
                                                   .build();
                                }
                            }
                        } else {
                            conn.rollback();
                            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                                           .entity("{\"error\": \"Failed to update bike availability and rental count\"}")
                                           .build();
                        }
                    }
                } else {
                    conn.rollback();
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                                   .entity("{\"error\": \"Failed to save rental details\"}")
                                   .build();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity("{\"error\": \"Error saving rental details: " + e.getMessage() + "\"}")
                           .build();
        }
    }


    /*@POST
    @Path("/return")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response returnBike(@QueryParam("bikeId") int bikeId, @QueryParam("userId") int userId) {
        if (bikeId <= 0 || userId <= 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                           .entity("{\"error\": \"Invalid bikeId or userId\"}")
                           .build();
        }

        try (Connection conn = DatabaseUtils.getConnection()) {
            // Check if the bike is rented by the user
            String checkQuery = "SELECT status FROM rentals WHERE bike_id = ? AND user_id = ? AND status = 'active'";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
                checkStmt.setInt(1, bikeId);
                checkStmt.setInt(2, userId);
                ResultSet rs = checkStmt.executeQuery();

                if (!rs.next()) {
                    return Response.status(Response.Status.BAD_REQUEST)
                                   .entity("{\"error\": \"Failed to return the bike. Please check if the bike is rented by the user.\"}")
                                   .build();
                }
            }

            // Start a transaction
            conn.setAutoCommit(false);

            // Update the bike's availability
            String bikeQuery = "UPDATE bikes SET isAvailable = true WHERE id = ?";
            try (PreparedStatement bikeStmt = conn.prepareStatement(bikeQuery)) {
                bikeStmt.setInt(1, bikeId);
                int bikeUpdated = bikeStmt.executeUpdate();

                if (bikeUpdated == 0) {
                    conn.rollback();
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                                   .entity("{\"error\": \"Failed to update bike availability.\"}")
                                   .build();
                }
            }

            // Update the rental status
            String rentalQuery = "UPDATE rentals SET status = 'completed' WHERE bike_id = ? AND user_id = ?";
            try (PreparedStatement rentalStmt = conn.prepareStatement(rentalQuery)) {
                rentalStmt.setInt(1, bikeId);
                rentalStmt.setInt(2, userId);
                int rentalUpdated = rentalStmt.executeUpdate();

                if (rentalUpdated == 0) {
                    conn.rollback();
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                                   .entity("{\"error\": \"Failed to update rental status.\"}")
                                   .build();
                }
            }

            // Commit the transaction
            conn.commit();
            return Response.ok("{\"success\": \"Bike returned successfully.\"}").build();

        } catch (SQLException e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity("{\"error\": \"Error while returning the bike: " + e.getMessage() + "\"}")
                           .build();
        }
    }*/

    public class BikeRentalService {

        @POST
        @Path("/return")
        @Consumes(MediaType.APPLICATION_JSON)
        @Produces(MediaType.APPLICATION_JSON)
        public Response returnBike(@QueryParam("bikeId") int bikeId, @QueryParam("userId") int userId) {
            if (bikeId <= 0 || userId <= 0) {
                return Response.status(Response.Status.BAD_REQUEST)
                               .entity("{\"error\": \"Invalid bikeId or userId\"}")
                               .build();
            }

            try (Connection conn = DatabaseUtils.getConnection()) {
                // Check if the bike is rented by the user
                String checkQuery = "SELECT status FROM rentals WHERE bike_id = ? AND user_id = ? AND status = 'active'";
                try (PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
                    checkStmt.setInt(1, bikeId);
                    checkStmt.setInt(2, userId);
                    try (ResultSet rs = checkStmt.executeQuery()) {
                        if (!rs.next()) {
                            return Response.status(Response.Status.BAD_REQUEST)
                                           .entity("{\"error\": \"Failed to return the bike. Please check if the bike is rented by the user.\"}")
                                           .build();
                        }
                    }
                }

                // Start a transaction
                conn.setAutoCommit(false);

                // Update the bike's availability
                String bikeQuery = "UPDATE bikes SET isAvailable = true WHERE id = ?";
                try (PreparedStatement bikeStmt = conn.prepareStatement(bikeQuery)) {
                    bikeStmt.setInt(1, bikeId);
                    int bikeUpdated = bikeStmt.executeUpdate();

                    if (bikeUpdated == 0) {
                        conn.rollback();
                        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                                       .entity("{\"error\": \"Failed to update bike availability.\"}")
                                       .build();
                    }
                }

                // Update the rental status
                String rentalQuery = "UPDATE rentals SET status = 'completed' WHERE bike_id = ? AND user_id = ?";
                try (PreparedStatement rentalStmt = conn.prepareStatement(rentalQuery)) {
                    rentalStmt.setInt(1, bikeId);
                    rentalStmt.setInt(2, userId);
                    int rentalUpdated = rentalStmt.executeUpdate();

                    if (rentalUpdated == 0) {
                        conn.rollback();
                        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                                       .entity("{\"error\": \"Failed to update rental status.\"}")
                                       .build();
                    }
                }

                // Commit the transaction
                conn.commit();

                // After commit, handle waiting list logic (out of transaction scope)
                handleWaitingList(bikeId);

                return Response.ok("{\"success\": \"Bike returned successfully.\"}").build();

            } catch (SQLException e) {
                e.printStackTrace();
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                               .entity("{\"error\": \"Error while returning the bike: " + e.getMessage() + "\"}")
                               .build();
            }
            //notifyWaitingListIfBikeAvailable(bikeId);

        }

        private void handleWaitingList(int bikeId) {
            WaitingListService waitingListService = new WaitingListService();
            WaitingListEntry nextUser = waitingListService.getNextInLine(bikeId);

            if (nextUser != null) {
                // Send an email notification to the first user in the waiting list
                String message = "The bike " + nextUser.getBike().getName() + " is now available. Confirm within 24 hours.";
                // Send the confirmation email to the first user on the waiting list
				String subject = "Bike Availability Notification";
				String userEmail = nextUser.getUser().getEmail();
				confirmRental(userEmail, subject, message);  // Call confirmRental to send the email
				System.out.println("Email sent successfully to: " + userEmail);

                // Update waiting list status to marked as notified
                waitingListService.markAsNotified(nextUser);

                // Start a timer for 24 hours using a separate thread
                new Thread(() -> {
                    try {
                        Thread.sleep(TimeUnit.HOURS.toMillis(24));  // Wait for 24 hours
                        waitingListService.handleTimeout(nextUser); // Handle timeout after 24 hours
                    } catch (InterruptedException e) {
                        System.err.println("Error during waiting list timeout: " + e.getMessage());
                    }
                }).start();
            } else {
                System.out.println("No users in the waiting list for bike ID: " + bikeId);
            }
        }

        // Modified confirmRental to accept subject and message text
        public void confirmRental(String userEmail, String subject, String messageText) {
            // Business logic to confirm the bike rental
            System.out.println("Bike rental confirmed for: " + userEmail);

            // Send a confirmation email after rental confirmation
            EmailService.sendConfirmationEmail(userEmail, subject, messageText);
        }
    }





    @POST
    @Path("/cancel")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response cancelBike(@QueryParam("bikeId") int bikeId, @QueryParam("userId") int userId) {
        if (bikeId <= 0 || userId <= 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                           .entity("{\"error\": \"Invalid bikeId or userId\"}")
                           .build();
        }

        try (Connection conn = DatabaseUtils.getConnection()) {
            // Check if the bike is rented by the user
            String checkQuery = "SELECT status FROM rentals WHERE bike_id = ? AND user_id = ? AND status = 'active'";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
                checkStmt.setInt(1, bikeId);
                checkStmt.setInt(2, userId);
                ResultSet rs = checkStmt.executeQuery();

                if (!rs.next()) {
                    return Response.status(Response.Status.BAD_REQUEST)
                                   .entity("{\"error\": \"Failed to return the bike. Please check if the bike is rented by the user.\"}")
                                   .build();
                }
            }

            // Start a transaction
            conn.setAutoCommit(false);

            // Update the bike's availability
            String bikeQuery = "UPDATE bikes SET isAvailable = true WHERE id = ?";
            try (PreparedStatement bikeStmt = conn.prepareStatement(bikeQuery)) {
                bikeStmt.setInt(1, bikeId);
                int bikeUpdated = bikeStmt.executeUpdate();

                if (bikeUpdated == 0) {
                    conn.rollback();
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                                   .entity("{\"error\": \"Failed to update bike availability.\"}")
                                   .build();
                }
            }

            // Update the rental status
            String rentalQuery = "UPDATE rentals SET status = 'cancelled' WHERE bike_id = ? AND user_id = ?";
            try (PreparedStatement rentalStmt = conn.prepareStatement(rentalQuery)) {
                rentalStmt.setInt(1, bikeId);
                rentalStmt.setInt(2, userId);
                int rentalUpdated = rentalStmt.executeUpdate();

                if (rentalUpdated == 0) {
                    conn.rollback();
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                                   .entity("{\"error\": \"Failed to update rental status.\"}")
                                   .build();
                }
            }

            // Commit the transaction
            conn.commit();
            return Response.ok("{\"success\": \"Bike returned successfully.\"}").build();

        } catch (SQLException e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity("{\"error\": \"Error while returning the bike: " + e.getMessage() + "\"}")
                           .build();
        }
    }


        // Endpoint to calculate and return today's, last week's, and this quarter's rental costs
        @GET
        @Path("/costs")
        @Produces(MediaType.APPLICATION_JSON)
        public Response getRentalCosts() {
            double todayCost = getTotalCostForToday();
            double lastWeekCost = getTotalCostForLastWeek();
            double thisQuarterCost = getTotalCostForThisQuarter();

            // Create a response map to return the results
            Map<String, Double> costs = new HashMap<>();
            costs.put("today_cost", todayCost);
            costs.put("last_week_cost", lastWeekCost);
            costs.put("this_quarter_cost", thisQuarterCost);

            return Response.ok(costs).build();
        }

        // Helper method to calculate today's rental cost
        private double getTotalCostForToday() {
            String query = "SELECT SUM(rental_cost) AS today_cost FROM rentals WHERE start_date = CURRENT_DATE";
            return getTotalRentalCost(query);
        }

        // Helper method to calculate last week's rental cost
        private double getTotalCostForLastWeek() {
            String query = "SELECT SUM(rental_cost) AS last_week_cost FROM rentals WHERE start_date BETWEEN CURDATE() - INTERVAL 7 DAY AND CURDATE()";
            return getTotalRentalCost(query);
        }

        // Helper method to calculate this quarter's rental cost
        private double getTotalCostForThisQuarter() {
            String query = "SELECT SUM(rental_cost) AS this_quarter_cost " +
                           "FROM rentals WHERE start_date >= " +
                           "CASE " +
                           "WHEN MONTH(CURRENT_DATE) BETWEEN 1 AND 3 THEN MAKEDATE(YEAR(CURRENT_DATE), 1) " +
                           "WHEN MONTH(CURRENT_DATE) BETWEEN 4 AND 6 THEN MAKEDATE(YEAR(CURRENT_DATE), 1) + INTERVAL 3 MONTH " +
                           "WHEN MONTH(CURRENT_DATE) BETWEEN 7 AND 9 THEN MAKEDATE(YEAR(CURRENT_DATE), 1) + INTERVAL 6 MONTH " +
                           "WHEN MONTH(CURRENT_DATE) BETWEEN 10 AND 12 THEN MAKEDATE(YEAR(CURRENT_DATE), 1) + INTERVAL 9 MONTH " +
                           "END";
            return getTotalRentalCost(query);
        }

        // Helper method to execute the query and return the total cost
        private double getTotalRentalCost(String query) {
            double totalCost = 0;
            try (Connection conn = DatabaseUtils.getConnection()) {
                try (Statement stmt = conn.createStatement()) {
                    ResultSet rs = stmt.executeQuery(query);
                    if (rs.next()) {
                        totalCost = rs.getDouble(1);  // Get the sum of rental_cost
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace(); // Handle the exception as needed
            }
            return totalCost;
        }




    public static class Rental {
        private int bikeId;
        private String bikeTitle; // Add bike title
        private String startDate;
        private String endDate;
        private double rentalCost;
        private int userId;
        private String status;
        private long duration; // Add rental duration

        public Rental() {

		}

		// Getters and setters
        public int getBikeId() {
            return bikeId;
        }

        public void setBikeId(int bikeId) {
            this.bikeId = bikeId;
        }

        public String getBikeTitle() {
            return bikeTitle;
        }

        public void setBikeTitle(String bikeTitle) {
            this.bikeTitle = bikeTitle;
        }

        public String getStartDate() {
            return startDate;
        }

        public void setStartDate(String startDate) {
            this.startDate = startDate;
        }

        public String getEndDate() {
            return endDate;
        }

        public void setEndDate(String endDate) {
            this.endDate = endDate;
        }

        public double getRentalCost() {
            return rentalCost;
        }

        public void setRentalCost(double rentalCost) {
            this.rentalCost = rentalCost;
        }

        public int getUserId() {
            return userId;
        }

        public void setUserId(int userId) {
            this.userId = userId;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public long getDuration() {
            return duration;
        }

        public void setDuration(long duration) {
            this.duration = duration;
        }
    }
}
