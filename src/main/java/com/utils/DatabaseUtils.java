package com.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseUtils {

	private static final String URL = "jdbc:mysql://localhost:3306/bike_rental";

    private static final String USER = "root";
    private static final String PASSWORD = "";
    private static Connection connection = null;
    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try { 
                Class.forName("com.mysql.cj.jdbc.Driver"); 
                
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
            } catch (SQLException | ClassNotFoundException e) {
                throw new SQLException("Failed to connect to the database: " + e.getMessage(), e);
            }
        }
        return connection;
    }

}
