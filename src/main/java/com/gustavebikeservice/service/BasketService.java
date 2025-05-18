package com.gustavebikeservice.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.utils.DatabaseUtils;

public class BasketService {

    // Method to fetch the basket of a user (PENDING items only)
   /* public List<Basket> getBasketByUser(int userId) {
        List<Basket> basketList = new ArrayList<>();
        String query = "SELECT * FROM Basket WHERE userId = ? AND status = 'PENDING'";

        try (Connection conn = DatabaseUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("userId");
                    int itemId = rs.getInt("bikeId");
                    double price = rs.getDouble("priceAtTimeOfAdd");
                    Basket.Status status = Basket.Status.valueOf(rs.getString("status"));

                    Basket basket = new Basket(id, userId, itemId, price, status);
                    basketList.add(basket);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }*/


	 private static final String GET_BIKE_IDS_QUERY = 
	            "SELECT bikeId FROM basket WHERE userId = ?";

	    public List<Integer> getBikeIdsInCart(int userId) throws Exception {
	        List<Integer> bikeIds = new ArrayList<>();

	        try (Connection connection = DatabaseUtils.getConnection();
	             PreparedStatement preparedStatement = connection.prepareStatement(GET_BIKE_IDS_QUERY)) {

	            preparedStatement.setInt(1, userId);
	            ResultSet resultSet = preparedStatement.executeQuery();

	            while (resultSet.next()) {
	                bikeIds.add(resultSet.getInt("bikeId"));
	            }
	        }

	        return bikeIds;
	    }

	// Method to fetch the basket of a user (PENDING items only)
	public List<Map<String, Object>> getBasketWithBikeDetails(int userId) {
	    List<Map<String, Object>> result = new ArrayList<>();
	    String query = "SELECT ba.basketId, ba.userId, ba.bikeId, ba.priceAtTimeOfAdd," +
	                   "ba.currency, " + 
	                   "b.name AS bikeTitle, b.model AS bikeModel, b.image AS bikeImage " +
	                   "FROM basket ba " +
	                   "JOIN bikes b ON ba.bikeId = b.id " +
	                   "WHERE ba.userId = ?";

	    try (Connection conn = DatabaseUtils.getConnection();
	         PreparedStatement stmt = conn.prepareStatement(query)) {

	        stmt.setInt(1, userId);

	        try (ResultSet rs = stmt.executeQuery()) {
	            while (rs.next()) {
	                Map<String, Object> entry = new HashMap<>();
	                entry.put("basketId", rs.getInt("basketId"));
	                entry.put("userId", rs.getInt("userId"));
	                entry.put("bikeId", rs.getInt("bikeId"));
	                entry.put("priceAtTimeOfAdd", rs.getDouble("priceAtTimeOfAdd"));
	                entry.put("bikeTitle", rs.getString("bikeTitle"));
	                entry.put("bikeModel", rs.getString("bikeModel"));
	                entry.put("bikeImage", rs.getString("bikeImage"));
	                entry.put("currency", rs.getString("currency"));  // Adding the currency to the result map

	                result.add(entry);
	            }
	        }

	    } catch (SQLException e) {
	        e.printStackTrace();
	    }

	    return result;
	}


    // Method to add an item to a user's basket
    public boolean addToBasket1(int userId, int itemId, double price, String currency) {
        String query = "INSERT INTO basket (userId, bikeId, priceAtTimeOfAdd, currency) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, userId);
            stmt.setInt(2, itemId);
            stmt.setDouble(3, price);
            stmt.setString(4, currency);     // Set currency


            int rowsInserted = stmt.executeUpdate();
            return rowsInserted > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    /*// Method to update the status of an item in the basket (e.g., COMPLETED, CANCELED)
    public boolean updateBasketStatus(int basketId, Basket.Status newStatus) {
        String query = "UPDATE Basket SET status = ? WHERE id = ?";

        try (Connection conn = DatabaseUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, newStatus.toString());
            stmt.setInt(2, basketId);

            int rowsUpdated = stmt.executeUpdate();
            return rowsUpdated > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }*/

    // Method to remove all  items for a user (e.g., clearing the basket)
    public boolean clearBasket(int userId) {
        String query = "DELETE FROM Basket WHERE userId = ?";

        try (Connection conn = DatabaseUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, userId);
            int rowsDeleted = stmt.executeUpdate();
            return rowsDeleted > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean isBikeInBasket(int userId, int bikeId) {
        String query = "SELECT COUNT(*) FROM Basket WHERE userId = ? AND bikeId = ?";
        try (Connection conn = DatabaseUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, userId);
            stmt.setInt(2, bikeId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0; // If the count is greater than 0, the bike is in the basket
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false; // Return false if the bike isn't found or any error occurs
    }

 // Method to remove an item from a user's basket
    public boolean removeItemFromBasket(int basketId) {
        String query = "DELETE FROM Basket WHERE basketId = ? ";

        try (Connection conn = DatabaseUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, basketId);
            int rowsDeleted = stmt.executeUpdate();
            return rowsDeleted > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }


  


}