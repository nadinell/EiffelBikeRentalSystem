package com.gustavebikeservice.api;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import com.utils.DatabaseUtils;
import java.sql.*;
import java.util.*;

@Path("/purchase-history")
public class PurchaseHistoryAPI {

    @GET
    @Path("/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPurchaseHistory(@PathParam("userId") int userId) {
        List<Purchase> purchases = new ArrayList<>();

        // Updated SQL query to include the image column from the bikes table
        String sql = "SELECT p.id, p.bikeId, p.userId, p.status, p.created_at, p.currency, p.amount, " +
                     "b.name, b.model, b.image " +
                     "FROM payment p JOIN bikes b ON p.bikeId = b.id " +
                     "WHERE p.userId = ? ORDER BY p.created_at DESC";

        try (Connection conn = DatabaseUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            // Iterate through results and build a list of purchases
            while (rs.next()) {
                Purchase purchase = new Purchase(
                    rs.getInt("id"),
                    rs.getInt("bikeId"),
                    rs.getInt("userId"),
                    rs.getString("status"),
                    rs.getTimestamp("created_at"),
                    rs.getString("currency"),
                    rs.getDouble("amount"),
                    rs.getString("name"),
                    rs.getString("model"),
                    rs.getString("image") // Adding image field
                );
                purchases.add(purchase);
            }
        } catch (SQLException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Database error").build();
        }

        return Response.ok(purchases).build();
    }

    // Updated Purchase class with the new image field
    public static class Purchase {
        private int id;
        private int bikeId;
        private int userId;
        private String status;
        private Timestamp createdAt;
        private String currency;
        private double amount;
        private String bikeName;
        private String bikeModel;
        private String image; // New field for the bike image

        public Purchase(int id, int bikeId, int userId, String status, Timestamp createdAt, 
                        String currency, double amount, String bikeName, String bikeModel, String image) {
            this.id = id;
            this.bikeId = bikeId;
            this.userId = userId;
            this.status = status;
            this.createdAt = createdAt;
            this.currency = currency;
            this.amount = amount;
            this.bikeName = bikeName;
            this.bikeModel = bikeModel;
            this.image = image; // Initialize the image field
        }

        // Getters for JSON response
        public int getId() { return id; }
        public int getBikeId() { return bikeId; }
        public int getUserId() { return userId; }
        public String getStatus() { return status; }
        public Timestamp getCreatedAt() { return createdAt; }
        public String getCurrency() { return currency; }
        public double getAmount() { return amount; }
        public String getBikeName() { return bikeName; }
        public String getBikeModel() { return bikeModel; }
        public String getImage() { return image; } // Getter for the image
    }
}
