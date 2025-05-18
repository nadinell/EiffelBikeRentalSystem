package com.gustavebikeservice.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.gustavebikeservice.model.BikeForSale;
import com.utils.CurrencyConverter;
import com.utils.DatabaseUtils;

public class BikeSaleService {

	private List<BikeForSale> bikesForSale; 

    // Method to retrieve all bikes for sale
    public List<BikeForSale> getAllBikesForSale() {
        List<BikeForSale> bikesForSale = new ArrayList<>();
        String query = "SELECT * FROM bikes WHERE rentalCount > 0 AND is_sold = 0"; // only bikes that have been rented at least once are for sale 

        try (Connection conn = DatabaseUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                String model = rs.getString("model");
                String image = rs.getString("image");
                double salePrice = rs.getDouble("sale_price");
                boolean isAvailable = rs.getBoolean("isAvailable");
                String description = rs.getString("description");
                String owner = rs.getString("owner");

                BikeForSale bikeForSale = new BikeForSale(id, name, model, image, salePrice, isAvailable, description, owner);
                bikesForSale.add(bikeForSale);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return bikesForSale;
    }

    // Method to mark a bike as sold
    public boolean markBikeAsSold(int bikeId) {
        String query = "UPDATE bikes SET is_sold = 1 WHERE id = ?";

        try (Connection conn = DatabaseUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, bikeId);
            int rowsUpdated = stmt.executeUpdate();

            return rowsUpdated > 0; // return true if the bike was marked as sold
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Method to update the sale price and owner information
    public boolean updateSaleInfo(int bikeId, double salePrice, String owner) {
        String query = "UPDATE bikes SET sale_price = ?, owner = ? WHERE id = ?";

        try (Connection conn = DatabaseUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setDouble(1, salePrice);
            stmt.setString(2, owner);
            stmt.setInt(3, bikeId);

            int rowsUpdated = stmt.executeUpdate();
            return rowsUpdated > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Method to retrieve a bike for sale by ID
    public BikeForSale getBikeForSaleById(int bikeId) {
        //String query = "SELECT * FROM bikes WHERE id = ? AND rentalCount > 0 AND is_sold = 0";
        String query = "SELECT * FROM bikes WHERE id = ?";// Only bikes rented at least once and unsold
        BikeForSale bikeForSale = null;

        try (Connection conn = DatabaseUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, bikeId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                String model = rs.getString("model");
                String image = rs.getString("image");
                double salePrice = rs.getDouble("sale_price");
                boolean isAvailable = rs.getBoolean("isAvailable");
                String description = rs.getString("description");
                String owner = rs.getString("owner");
                String notes = rs.getString("notes");
                bikeForSale = new BikeForSale(id, name, model, image, salePrice, isAvailable, description, owner,notes);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return bikeForSale;
    }



 // Fetch all bikes for sale in the requested currency
    public List<BikeForSale> getBikesForSaleInCurrency(String currency) {
        return bikesForSale.stream().map(bike -> {
            double convertedPrice = CurrencyConverter.convertToCurrency(currency, bike.getSalePrice());
            bike.setSalePrice(convertedPrice); // Update the price to the converted value
            return bike;
        }).collect(Collectors.toList());
    }

    public boolean isBikeAvailable(int bikeId) {
        String query = "SELECT isAvailable FROM bikes WHERE id = ?";
        try (Connection conn = DatabaseUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, bikeId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getBoolean("isAvailable"); // Return the availability status of the bike
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false; // Default to false if the bike doesn't exist or any error occurs
    }

}
