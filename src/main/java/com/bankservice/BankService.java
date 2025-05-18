package com.bankservice;

import com.utils.DatabaseUtils;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class BankService {

    // Get the available funds for the user
    public double getAvailableFunds(String userId) {
        String query = "SELECT balance FROM users WHERE id = ?";
        try (Connection connection = DatabaseUtils.getConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {

            stmt.setString(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getDouble("balance");
            } else {
                return -1; // User not found
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return -1; // Handle database error
        }
    }
}
