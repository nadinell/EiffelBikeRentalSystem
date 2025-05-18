package com.eiffelbikerental.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.logging.Logger;

import org.mindrot.jbcrypt.BCrypt;

import com.eiffelbikerental.model.User;
import com.utils.DatabaseUtils;

public class UserService {

    private static final Logger logger = Logger.getLogger(UserService.class.getName());

    // Register a new user
  /*  public boolean registerUser(String username, String email, String password, User.UserType userType) {
        if (username == null || email == null || password == null || userType == null) {
            logger.warning("Invalid registration information.");
            return false;
        }

        if (!email.endsWith("@univ-eiffel.fr")) {
            logger.warning("Invalid email address. Only Gustave Eiffel University emails are allowed.");
            return false;
        }

        if (!isPasswordStrong(password)) {
            logger.warning("Weak password.");
            return false;
        }

        String hashedPassword = hashPassword(password);

        try (Connection conn = DatabaseUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO users (username, email, password, userType) VALUES (?, ?, ?, ?)"
             )) {

            stmt.setString(1, username);
            stmt.setString(2, email);
            stmt.setString(3, hashedPassword);
            stmt.setString(4, userType.name());
            stmt.executeUpdate();

            logger.info("User registered successfully.");
            return true;

        } catch (SQLIntegrityConstraintViolationException e) {
            logger.warning("Username or email already exists.");
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }*/

    public boolean registerUser(String username, String email, String password, User.UserType userType) {
        // Check for null values in registration information
        if (username == null || email == null || password == null || userType == null) {
            logger.warning("Invalid registration information.");
            return false;
        }

        // Email validation based on userType
        if (userType == User.UserType.STUDENT || userType == User.UserType.EMPLOYEE) {
            // Validate email for STUDENT and EMPLOYEE (must end with @univ-eiffel.fr)
            if (!email.endsWith("@univ-eiffel.fr")) {
                logger.warning("Invalid email address. Only Gustave Eiffel University emails are allowed for STUDENT and EMPLOYEE.");
                return false;
            }
        } else if (userType == User.UserType.CUSTOMER) {
            // Validate email for CUSTOMER (must end with @gmail.com)
            if (!email.endsWith("@gmail.com")) {
                logger.warning("Invalid email address. Only Gmail addresses are allowed for CUSTOMER.");
                return false;
            }
        }

        // Validate password strength
        if (!isPasswordStrong(password)) {
            logger.warning("Weak password. Password must be at least 8 characters long.");
            return false;
        }

        // Hash the password before storing it
        String hashedPassword = hashPassword(password);

        // Perform database operation to insert the new user
        try (Connection conn = DatabaseUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO users (username, email, password, userType) VALUES (?, ?, ?, ?)"
             )) {

            stmt.setString(1, username);
            stmt.setString(2, email);
            stmt.setString(3, hashedPassword);
            stmt.setString(4, userType.name());

            stmt.executeUpdate();
            logger.info("User registered successfully.");

            return true;

        } catch (SQLIntegrityConstraintViolationException e) {
            // This exception is thrown if the username or email already exists
            logger.warning("Username or email already exists.");
            return false;
        } catch (SQLException e) {
            // Handle other SQL exceptions
            e.printStackTrace();
            return false;
        }
    }


    // Login user
    public boolean loginUser(String username, String password) {
        try (Connection conn = DatabaseUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT password FROM users WHERE username = ?")) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String storedHash = rs.getString("password");
                if (checkPassword(password, storedHash)) {
                    return true;  // Login successful
                } else {
                    logger.warning("Incorrect password for user: " + username);
                    return false;  // Invalid password
                }
            } else {
                logger.warning("User not found: " + username);
                return false;  // User not found
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;  // Database error
        }
    }

    public int getUserIdByUsername(String username) {
        try (Connection conn = DatabaseUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT id FROM users WHERE username = ?")) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;  // Return a default value in case of failure (e.g., invalid user)
    }




 // Fetch user details by ID
    public User getUser(int userId) {
        try (Connection conn = DatabaseUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT id, username, email, userType, phone FROM users WHERE id = ?"
             )) {

            stmt.setInt(1, userId); // Use userId to query the database
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("email"),
                        null, // Do not return the password
                        User.UserType.valueOf(rs.getString("userType")), // Correctly fetching userType
                        rs.getInt("phone") // Include the phone number in the returned User object
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }


    // Helper methods for password handling
    private String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    public boolean checkPassword(String password, String storedHash) {
        return BCrypt.checkpw(password, storedHash);
    }

    public User getUserById(int userId) {
        if (userId <= 0) {
            logger.warning("Invalid user ID.");
            return null;
        }

        // Prepare the SQL query to fetch user by userId
        String query = "SELECT * FROM users WHERE id = ?";

        try (Connection conn = DatabaseUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, userId); // Set the userId parameter

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    // Populate the User object with the data from the result set
                    User user = new User();
                    user.setId(rs.getInt("id"));
                    user.setUsername(rs.getString("username"));
                    user.setEmail(rs.getString("email"));
                    user.setPassword(rs.getString("password"));
                    user.setUserType(User.UserType.valueOf(rs.getString("userType")));
                    user.setPhone(rs.getString("phone"));
                    user.setAddress(rs.getString("address"));
                    user.setCountry(rs.getString("country"));

                    return user;
                } else {
                    logger.warning("User not found for ID: " + userId);
                    return null;
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            logger.severe("Failed to fetch user by ID: " + e.getMessage());
            return null;
        }
    }






    public boolean isPasswordStrong(String password) {
        String passwordPattern = "^(?=.*[A-Z])(?=.*[a-z])(?=.*[0-9])(?=.*[!@#$%^&*(),.?\":{}|<>]).{8,}$";
        return password != null && password.matches(passwordPattern);
    }


    public String getUserTypeByUsername(String username) {
        String userType = null;  // Default value if user type is not found

        try (Connection conn = DatabaseUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT userType FROM users WHERE username = ?")) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                userType = rs.getString("userType");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            logger.warning("Failed to retrieve userType for username: " + username);
        }

        return userType;  // Return the user type or null if not found
    }

    public boolean changePassword(int userId, String currentPassword, String newPassword, String confirmPassword) {
        // Validate userId
        if (userId <= 0) {
            logger.warning("Invalid user ID.");
            return false;
        }

        // Check if the new password and confirmation password match
        if (!newPassword.equals(confirmPassword)) {
            logger.warning("New password and confirmation password do not match.");
            return false;
        }

        // Check if the new password meets strength requirements
        if (!isPasswordStrong(newPassword)) {
            logger.warning("New password is not strong enough.");
            return false;
        }

        try (Connection conn = DatabaseUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT password FROM users WHERE id = ?")) {

            stmt.setInt(1, userId);  // Use userId to query the database
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String storedHash = rs.getString("password");

                // Verify the current password against the stored hash
                if (checkPassword(currentPassword, storedHash)) {
                    // Hash the new password
                    String hashedNewPassword = hashPassword(newPassword);

                    // Update the password in the database
                    try (PreparedStatement updateStmt = conn.prepareStatement("UPDATE users SET password = ? WHERE id = ?")) {
                        updateStmt.setString(1, hashedNewPassword);
                        updateStmt.setInt(2, userId);
                        updateStmt.executeUpdate();
                        logger.info("Password updated successfully.");
                        return true;
                    }

                } else {
                    logger.warning("Current password is incorrect.");
                    return false;  // Current password doesn't match
                }
            } else {
                logger.warning("User not found.");
                return false;  // User doesn't exist
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;  // Database error
        }
    }





//update credentials
    public boolean updateUserById(int userId, String newUsername, String newEmail, String newPassword, User.UserType newUserType, String newPhone, String newAddress, String newCountry) {
        if (userId <= 0) {
            logger.warning("Invalid user ID.");
            return false;
        }

        // Validate email for student, employee, or customer
        if (newEmail != null) {
            if (newUserType == User.UserType.STUDENT || newUserType == User.UserType.EMPLOYEE) {
                if (!newEmail.endsWith("@univ-eiffel.fr")) {
                    logger.warning("Invalid email address for STUDENT/EMPLOYEE.");
                    return false;
                }
            } else if (newUserType == User.UserType.CUSTOMER) {
                if (!newEmail.endsWith("@gmail.com")) {
                    logger.warning("Invalid email address for CUSTOMER.");
                    return false;
                }
            }
        }

        // Validate new password strength if it's provided
        if (newPassword != null && !isPasswordStrong(newPassword)) {
            logger.warning("Weak password.");
            return false;
        }

        // Hash the new password if it's provided
        String hashedPassword = (newPassword != null) ? hashPassword(newPassword) : null;

        // Build the dynamic SQL query for updating only the changed fields
        StringBuilder query = new StringBuilder("UPDATE users SET ");
        boolean hasPreviousField = false;

        if (newUsername != null) {
            query.append("username = ?, ");
            hasPreviousField = true;
        }
        if (newEmail != null) {
            query.append("email = ?, ");
            hasPreviousField = true;
        }
        if (hashedPassword != null) {
            query.append("password = ?, ");
            hasPreviousField = true;
        }
        if (newUserType != null) {
            query.append("userType = ?, ");
            hasPreviousField = true;
        }
        if (newPhone != null) {
            query.append("phone = ?, ");
            hasPreviousField = true;
        }
        if (newAddress != null) {
            query.append("address = ?, ");
            hasPreviousField = true;
        }
        if (newCountry != null) {
            query.append("country = ?, ");
            hasPreviousField = true;
        }

        // Remove the last comma and space if there are fields to update
        if (hasPreviousField) {
            query.delete(query.length() - 2, query.length());
        } else {
            logger.warning("No fields to update.");
            return false;
        }

        // Add the WHERE condition
        query.append(" WHERE id = ?");

        // Prepare the statement with the dynamic query
        try (Connection conn = DatabaseUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query.toString())) {

            int index = 1;

            // Set the parameters based on which fields were provided
            if (newUsername != null) {
				stmt.setString(index++, newUsername);
			}
            if (newEmail != null) {
				stmt.setString(index++, newEmail);
			}
            if (hashedPassword != null) {
				stmt.setString(index++, hashedPassword);
			}
            if (newUserType != null) {
				stmt.setString(index++, newUserType.name());
			}
            if (newPhone != null) {
				stmt.setString(index++, newPhone);
			}
            if (newAddress != null) {
				stmt.setString(index++, newAddress);
			}
            if (newCountry != null) {
				stmt.setString(index++, newCountry);
			}

            stmt.setInt(index, userId);  // Set the userId at the end

            int rowsUpdated = stmt.executeUpdate();
            if (rowsUpdated > 0) {
                logger.info("User updated successfully.");
                return true;
            } else {
                logger.warning("User not found for update: id=" + userId);
                return false;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            logger.severe("Failed to update user: " + e.getMessage());
            return false;
        }
    }


    public int getRentalsCount(int userId) {
        String query = "SELECT COUNT(*) FROM rentals WHERE user_id = ?";

        try (Connection conn = DatabaseUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, userId);  // Set the userId parameter

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);  // Return the count of rentals
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;  // Return 0 if there is an issue with the database or no rentals are found
    }

    public int getWaitinglistCount(int userId) {
        String query = "SELECT COUNT(*) FROM waiting_list WHERE user_id = ?";

        try (Connection conn = DatabaseUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, userId);  // Set the userId parameter

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);  // Return the count of rentals
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }
    

   





}
