package com.gustavebikeservice.service;

import com.stripe.Stripe;
import com.stripe.model.PaymentIntent;
import com.stripe.exception.StripeException;
import com.utils.DatabaseUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PaymentService {

	 static {
	        Stripe.apiKey = "";
	    }

	    public double getAvailableFunds(String userId) {
	        String query = "SELECT balance FROM users WHERE id = ?";
	        try (Connection connection = DatabaseUtils.getConnection();
	             PreparedStatement stmt = connection.prepareStatement(query)) {

	            stmt.setString(1, userId);
	            ResultSet rs = stmt.executeQuery();

	            if (rs.next()) {
	                return rs.getDouble("balance");
	            } else {
	                return -1;
	            }
	        } catch (SQLException e) {
	            e.printStackTrace();
	            return -1;
	        }
	    }
	    
	    
	    public String createPaymentIntent(String userId, double amount, String currency) {
	        try {
	            // Log the currency for debugging
	            System.out.println("Currency: " + currency); // Ensure this is a valid Stripe currency.

	            long amountInCents = (long) (amount * 100);

	            Map<String, Object> params = new HashMap<>();
	            params.put("amount", amountInCents);
	            params.put("currency", currency);

	            PaymentIntent paymentIntent = PaymentIntent.create(params);
	            return paymentIntent.getClientSecret();
	        } catch (StripeException e) {
	            e.printStackTrace();
	            return null;
	        }
	    }

	    public void savePaymentDetails(String userId, List<Integer> bikeIds, String paymentStatus, double amount, String currency) throws SQLException {
	        String updateBalanceSQL = "UPDATE users SET balance = balance - ? WHERE id = ?";
	        String updateBikesSQL = "UPDATE bikes SET is_sold = 1 WHERE id = ?";
	        String insertPaymentSQL = "INSERT INTO payment (userId, bikeId, status, amount, currency) VALUES (?, ?, ?, ?, ?)";
	        String deleteBasketSQL = "DELETE FROM basket WHERE userId = ?"; // SQL to delete the items from the basket

	        try (Connection connection = DatabaseUtils.getConnection()) {
	            // Start transaction
	            connection.setAutoCommit(false);

	            // Update user's balance
	            try (PreparedStatement balanceStmt = connection.prepareStatement(updateBalanceSQL)) {
	                balanceStmt.setDouble(1, amount);
	                balanceStmt.setString(2, userId);
	                balanceStmt.executeUpdate();
	            }

	            // Mark bikes as sold and save payment details
	            try (PreparedStatement bikeStmt = connection.prepareStatement(updateBikesSQL);
	                 PreparedStatement paymentStmt = connection.prepareStatement(insertPaymentSQL);
	                 PreparedStatement deleteBasketStmt = connection.prepareStatement(deleteBasketSQL)) {

	                // Delete items from the basket first (before updating bikes)
	                deleteBasketStmt.setString(1, userId);
	                deleteBasketStmt.executeUpdate();

	                for (int bikeId : bikeIds) {
	                    // Update bike as sold
	                    bikeStmt.setInt(1, bikeId);
	                    bikeStmt.executeUpdate();

	                    // Insert payment record
	                    paymentStmt.setString(1, userId);
	                    paymentStmt.setInt(2, bikeId);
	                    paymentStmt.setString(3, paymentStatus);
	                    paymentStmt.setDouble(4, amount);
	                    paymentStmt.setString(5, currency);
	                    paymentStmt.executeUpdate();
	                }
	            }

	            // Commit transaction
	            connection.commit();
	        } catch (SQLException e) {
	            e.printStackTrace();
	            throw new SQLException("Error while processing payment and updating data.");
	        }
	    }

	    
	  






}
