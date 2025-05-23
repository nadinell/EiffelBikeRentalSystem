package com.eiffelbikerental.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.stream.Collectors;

import com.eiffelbikerental.model.Bike;
import com.eiffelbikerental.model.User;
import com.eiffelbikerental.model.WaitingListEntry;
import com.utils.DatabaseUtils;
import com.utils.NotificationUtils;
import com.utils.SendGridEmailService;

public class WaitingListService {

    // Queue to store users waiting for a particular bike
    private Queue<WaitingListEntry> waitingList;

    // Constructor to initialize the waiting list service
    public WaitingListService() {
        waitingList = new LinkedList<>();
        //loadWaitingListFromDatabase();

    }

 // Method to add a user to the waiting list for a bike and save it in the database
    public void addToWaitingList(Bike bike, User user) {
        if (bike == null || user == null) {
            System.out.println("Invalid bike or user.");
            return;
        }

        // Create a new waiting list entry with status 'waiting' and the current timestamp
        WaitingListEntry entry = new WaitingListEntry(
            0,  // ID will be generated by the database
            user,
            bike,
            "waiting",
            new Timestamp(System.currentTimeMillis())  // Current timestamp
        );

        // Save the entry to the database
        saveToDatabase(entry);

        System.out.println(user.getUsername() + " added to the waiting list for bike " + bike.getName());
    }

    // Save the waiting list entry to the database
    private void saveToDatabase(WaitingListEntry entry) {
    	String query = "INSERT INTO waiting_list (user_id, bike_id, status, date_added) VALUES (?, ?, ?, ?)";

    	try (Connection connection = DatabaseUtils.getConnection();
    	     PreparedStatement preparedStatement = connection.prepareStatement(query)) {

    	    preparedStatement.setInt(1, entry.getUser().getId());
    	    preparedStatement.setInt(2, entry.getBike().getId());
    	    preparedStatement.setString(3, entry.getStatus());
    	    preparedStatement.setTimestamp(4, entry.getDateAdded());

    	    int rowsAffected = preparedStatement.executeUpdate();
    	    if (rowsAffected > 0) {
    	        System.out.println("Entry added successfully.");
    	    } else {
    	        System.out.println("Failed to add entry.");
    	    }
    	} catch (SQLException e) {
    	    e.printStackTrace();
    	    throw new RuntimeException("Database error: " + e.getMessage());
    	}
    }

    // Method to remove a user from the waiting list (when they get the bike)
    public void removeFromWaitingList(Bike bike, User user) {
        if (bike == null || user == null) {
            System.out.println("Invalid bike or user.");
            return;
        }

        // Remove the user from the waiting list (if they are there)
        WaitingListEntry entryToRemove = null;
        for (WaitingListEntry entry : waitingList) {
            if (entry.getBike().equals(bike) && entry.getUser().equals(user)) {
                entryToRemove = entry;
                break;
            }
        }

        if (entryToRemove != null) {
            waitingList.remove(entryToRemove);
            System.out.println(user.getUsername() + " removed from the waiting list for bike " + bike.getName());
        } else {
            System.out.println(user.getUsername() + " is not on the waiting list for bike " + bike.getName());
        }
    }

    // Method to notify the first user on the waiting list that the bike is available
    public void notifyUser(Bike bike) {
        if (bike == null) {
            System.out.println("Invalid bike.");
            return;
        }

        // Find the first user waiting for this bike
        WaitingListEntry entryToNotify = null;
        for (WaitingListEntry entry : waitingList) {
            if (entry.getBike().equals(bike)) {
                entryToNotify = entry;
                break;
            }
        }

        if (entryToNotify != null) {
            User nextUser = entryToNotify.getUser();
            entryToNotify.setStatus("notified"); // Change status to "notified"
            waitingList.remove(entryToNotify); // Remove user from list
            System.out.println("Notifying " + nextUser.getUsername() + " that bike " + bike.getName() + " is now available.");
        } else {
            System.out.println("No one is waiting for bike " + bike.getName());
        }
    }

    private boolean updateStatusInDatabase(int entryId, String status) {
        String query = "UPDATE waiting_list SET status = ? WHERE id = ?";
        try (Connection connection = DatabaseUtils.getConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, status);
            stmt.setInt(2, entryId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }


 // Notify the next user on the waiting list when a bike becomes available
    public void notifyNextUser(Bike bike) {
        WaitingListEntry nextEntry = getNextInLine(bike.getId());
        if (nextEntry != null) {
            User user = nextEntry.getUser();
            String message = "Bike " + bike.getName() + " is now available.";
            NotificationUtils.sendNotification(user, message);

            if (updateStatusInDatabase(nextEntry.getId(), "notified")) {
                System.out.println("User " + user.getUsername() + " notified about bike " + bike.getName());
            }
        } else {
            System.out.println("No users waiting for bike " + bike.getName());
        }
    }

    public WaitingListEntry getNextInLine(int bikeId) {
        String query = "SELECT * FROM waiting_list WHERE bike_id = ? AND status = 'waiting' ORDER BY date_added ASC LIMIT 1";
        try (Connection conn = DatabaseUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, bikeId);  // Set the bikeId parameter in the query
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {  // Check if there are results in the ResultSet
                    // Extract data into local variables
                    int userId = rs.getInt("user_id");
                    int bikeIdFromDb = rs.getInt("bike_id");
                    String status = rs.getString("status");
                    Timestamp dateAdded = rs.getTimestamp("date_added");

                    // Fetch user and bike details using the extracted data
                    User user = getUserById(userId);
                    Bike bike = getBikeById(bikeIdFromDb);

                    // Return a new WaitingListEntry with the extracted data
                    return new WaitingListEntry(rs.getInt("id"), user, bike, status, dateAdded);
                }
            } catch (SQLException e) {
                System.err.println("Error executing query for bike ID " + bikeId + ": " + e.getMessage());
                e.printStackTrace();
            }
        } catch (SQLException e) {
            System.err.println("Error establishing connection or preparing statement: " + e.getMessage());
            e.printStackTrace();
        }

        // If no entry was found, return null
        return null;
    }



    // Method to get the number of users waiting for a particular bike
 // In WaitingListService class
    public int getWaitingListSize(int bikeId) {
        List<WaitingListEntry> entries = getWaitingList(bikeId); // Get the waiting list for the bike
        return entries != null ? entries.size() : 0; // Return the size, or 0 if there are no entries
    }


    // Method to list all the users in the waiting list for a specific bike
    public List<WaitingListEntry> getWaitingList(int bikeId) {
        // Filter the waiting list for entries with the given bike ID
        return waitingList.stream()
                          .filter(entry -> entry.getBike().getId() == bikeId)
                          .collect(Collectors.toList());
    }
   

    // Method to print out the users in the waiting list (for internal use or reporting)
    public void listWaitingList() {
        if (waitingList.isEmpty()) {
            System.out.println("The waiting list is empty.");
        } else {
            System.out.println("Waiting list:");
            for (WaitingListEntry entry : waitingList) {
                System.out.println(entry);
            }
        }
    }


 // Method to retrieve all waiting list entries for a specific user
    public List<WaitingListEntry> getUserWaitingList(int userId) {
        List<WaitingListEntry> waitingListEntries = new ArrayList<>();
        String query = "SELECT wl.id, wl.status, wl.date_added, b.id AS bike_id, b.name, b.model, b.image, b.price, "
                     + "u.id AS user_id, u.username "
                     + "FROM waiting_list wl "
                     + "JOIN bikes b ON wl.bike_id = b.id "
                     + "JOIN users u ON wl.user_id = u.id "
                     + "WHERE wl.user_id = ?";

        try (Connection conn = DatabaseUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("id");
                    String status = rs.getString("status");
                    Timestamp dateAdded = rs.getTimestamp("date_added");

                    // Create Bike object
                    Bike bike = new Bike(
                        rs.getInt("bike_id"),
                        rs.getString("name"),
                        rs.getString("model"),
                        rs.getString("image"),
                        rs.getDouble("price")
                    );

                    // Create User object
                    User user = new User(
                        rs.getInt("user_id"),
                        rs.getString("username")
                    );

                    // Create WaitingListEntry and add to the list
                    waitingListEntries.add(new WaitingListEntry(id, user, bike, status, dateAdded));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return waitingListEntries;
    }




 // Method to retrieve a Bike from the database
    private Bike getBikeById(int bikeId) {
        // Assuming you have a bikes table with the required columns
        String query = "SELECT * FROM bikes WHERE id = ?";
        try (Connection conn = DatabaseUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, bikeId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String name = rs.getString("name");
                    String model = rs.getString("model");
                    String image = rs.getString("image");
                    double price = rs.getDouble("price");

                    // Return the bike object with all the required fields
                    return new Bike(bikeId, name,  model, image, price);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; // Return null if no bike is found
    }



    // Method to retrieve a User from the database
    private User getUserById(int userId) {
        // Assuming you have a user table with an id and username
        String query = "SELECT * FROM users WHERE id = ?";
        try (Connection conn = DatabaseUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String username = rs.getString("username");
                    return new User(userId, username); // Assuming the User constructor takes an ID and username
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; // Return null if no user is found
    }

    // Cancel a bike request by entry ID
    public boolean cancelBikeRequest(int entryId) {
        // Initialize database connection
        try (Connection conn = DatabaseUtils.getConnection()) {
            // First, we need to remove the entry from the database
            String deleteQuery = "DELETE FROM waiting_list WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(deleteQuery)) {
                stmt.setLong(1, entryId);

                int affectedRows = stmt.executeUpdate();

                // If the entry is deleted from the database, proceed to remove it from the queue
                if (affectedRows > 0) {
                    // Iterate over the queue and remove the corresponding entry
                    WaitingListEntry entryToRemove = null;
                    for (WaitingListEntry entry : waitingList) {
                        if (entry.getId() == entryId) {
                            entryToRemove = entry;
                            break;
                        }
                    }

                    if (entryToRemove != null) {
                        waitingList.remove(entryToRemove);  // Remove the entry from the queue
                        return true;
                    }
                }
            } catch (SQLException e) {
                System.err.println("Error executing delete query: " + e.getMessage());
                return false;
            }
        } catch (SQLException e) {
            System.err.println("Database connection error: " + e.getMessage());
            return false;
        }

        return false;  // If no entry was deleted
    }

	public boolean cancelBike(Long bikeId, Long userId) {
        // Find and remove the bike entry based on bikeId and userId
        for (WaitingListEntry entry : waitingList) {
            if (entry.getBike().equals(bikeId) && entry.getUser().equals(userId)) {
                waitingList.remove(entry);
                return true; // Successfully removed
            }
        }
        return false; // Not found
    }





	// Mark an entry as notified
	public void markAsNotified(WaitingListEntry entry) {
	    String query = "UPDATE waiting_list SET status = 'notified' WHERE id = ?";
	    try (Connection conn = DatabaseUtils.getConnection();
	         PreparedStatement stmt = conn.prepareStatement(query)) {
	        stmt.setInt(1, entry.getId());
	        int rowsUpdated = stmt.executeUpdate();
	        
	        // Check if the entry was updated
	        if (rowsUpdated == 0) {
	            System.err.println("No entry found with ID " + entry.getId());
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	        System.err.println("Error marking entry as notified for ID " + entry.getId());
	    }
	}

	// Handle timeout for a waiting list entry
	public void handleTimeout(WaitingListEntry entry) {
	    String query = "UPDATE waiting_list SET status = 'expired' WHERE id = ?";
	    try (Connection conn = DatabaseUtils.getConnection();
	         PreparedStatement stmt = conn.prepareStatement(query)) {
	        stmt.setInt(1, entry.getId());
	        int rowsUpdated = stmt.executeUpdate();

	        // Check if the entry was updated
	        if (rowsUpdated == 0) {
	            System.err.println("No entry found with ID " + entry.getId());
	        }

	        // Notify the next user in line
	        WaitingListEntry nextUser = getNextInLine(entry.getBike().getId());
	        if (nextUser != null) {
	            notifyUser(nextUser.getUser(), "The bike " + nextUser.getBike().getName() + " is now available. Please confirm within 24 hours.");
	        }

	    } catch (SQLException e) {
	        e.printStackTrace();
	        System.err.println("Error handling timeout for entry ID " + entry.getId());
	    }
	}

	// Notify a user via email
	private void notifyUser(User user, String message) {
	    if (user.getEmail() != null && !user.getEmail().isEmpty()) {
	        try {
	            // Send an email notification
	            NotificationUtils.sendEmail(user.getEmail(), "Bike Rental Notification", message);
	            System.out.println("Notification sent to " + user.getEmail());
	        } catch (Exception e) {
	            System.err.println("Failed to notify user " + user.getUsername() + " via email: " + e.getMessage());
	        }
	    } else {
	        System.out.println("No valid email for user " + user.getUsername());
	    }
	}



	public List<WaitingListEntry> getWaitingList1(int bikeId) {
        List<WaitingListEntry> waitingListEntries = new ArrayList<>();
        
        String query = "SELECT wl.id AS waiting_list_id, " +
                "wl.status, " +
                "wl.date_added, " +
                "u.id AS user_id, " +
                "u.email AS user_email, " +
                "b.id AS bike_id, " +
                "b.model AS bike_model " + // Space at the end
                "FROM waiting_list wl " + // Space after wl
                "JOIN users u ON wl.user_id = u.id " + // Space before JOIN
                "JOIN bikes b ON wl.bike_id = b.id " + // Space before JOIN
                "WHERE wl.bike_id = ? AND wl.status = 'waiting' " + // Space before WHERE
                "ORDER BY wl.date_added ASC;"; // Ends correctly


        
        
        
        try (Connection conn = DatabaseUtils.getConnection(); // Assuming DatabaseUtils is a utility class that handles DB connections
             PreparedStatement stmt = conn.prepareStatement(query)) {
            // Set the bikeId parameter in the query
            stmt.setInt(1, bikeId);
            // Execute the query
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    System.out.println("user_id: " + rs.getInt("user_id"));
                    System.out.println("user_email: " + rs.getString("user_email"));
                    System.out.println("bike_id: " + rs.getInt("bike_id"));
                    System.out.println("bike_model: " + rs.getString("bike_model"));
                    
                    WaitingListEntry entry = new WaitingListEntry();
                    entry.setId(rs.getInt("waiting_list_id"));
                    entry.setStatus(rs.getString("status"));
                    entry.setDateAdded(rs.getTimestamp("date_added"));

                    User user = new User();
                    user.setId(rs.getInt("user_id"));
                    user.setEmail(rs.getString("user_email"));
                    
                    entry.setUser(user);
                    System.out.println("iii"+ entry);
                    System.out.println( entry);  

                    
                    Bike bike = new Bike();
                    bike.setId(rs.getInt("bike_id"));
                    bike.setModel(rs.getString("bike_model"));
                    entry.setBike(bike);

                    waitingListEntries.add(entry);
                }
            }
        } catch (SQLException e) {
            // Handle SQL exception
            e.printStackTrace();
        }
        System.out.println("waitingListEntries"+waitingListEntries);
        return waitingListEntries;
	}
	
        public boolean isBikeAvailable1(int bikeId) {
            String query = "SELECT * FROM bikes WHERE id=? and isAvailable = 1 ";
            
            try (Connection conn = DatabaseUtils.getConnection(); // Assuming DatabaseUtils is a utility class that handles DB connections
                 PreparedStatement stmt = conn.prepareStatement(query)) {
                System.out.println("here");

                // Set the bikeId parameter in the query
                stmt.setInt(1, bikeId);
                
                try (ResultSet rs = stmt.executeQuery()) {
                	System.out.println("ouuuuhhhhh ");
                	
                    // If no active rental found, the bike is available
                    return rs.next();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            
            return false; // In case of an error, consider the bike unavailable
        }
    	
    	
    	
        public void notifyFirstUserWhenBikeIsAvailable(int bikeId) {
        	System.out.println("ooooooo"+isBikeAvailable1(bikeId));
            // Step 1: Check if the bike is available
            if (isBikeAvailable1(bikeId)) {
                // Step 2: Get the first user in the waiting list based on earliest added date
            	List<WaitingListEntry> waitingList = getWaitingList1(bikeId);
            	System.out.println("aaaaaaaaaaaaaaaaaaaaaaaaa"+waitingList);
            	WaitingListEntry firstEntry = null;
            	if (!waitingList.isEmpty()) {
            	    firstEntry = waitingList.get(0);
            	    for (WaitingListEntry entry : waitingList) {
            	        if (entry.getDateAdded().before(firstEntry.getDateAdded())) {
            	            firstEntry = entry;
            	        }
            	    }
            	}
            	System.out.println("fiiiiiiiiiiiiiiiiii"+firstEntry);

                if (firstEntry != null) {
                    String userEmail = firstEntry.getUser().getEmail(); // Assuming User class has getEmail method
                    String subject = "Bike Available Now!";
                    String body = "Dear userrrrrrr" + ",\n\n" +
                                  "The bike you requested is now available for rental. Please visit the website to reserve it.\n\n" +
                                  "Best regards,\n" +
                                  "The Bike Rental Team";
                    
                    // Send the email
                    SendGridEmailService.sendEmail(userEmail, subject, body);
                    System.out.println("Notification sent to: " + userEmail);
                } else {
                    System.out.println("No users in the waiting list for bike ID: " + bikeId);
                }
            } else {
                System.out.println("Bike is still rented, cannot notify users yet.");
            }
        }
    }




 /*   //*********for dashboard****** // Remove a user from the waiting list
    public boolean removeFromWaitingList(Bike bike, User user) {
        if (bike == null || user == null) {
            System.out.println("Invalid bike or user.");
            return false;
        }

        WaitingListEntry entryToRemove = waitingList.stream()
                .filter(entry -> entry.getBike().equals(bike) && entry.getUser().equals(user))
                .findFirst()
                .orElse(null);

        if (entryToRemove != null && deleteFromDatabase(entryToRemove.getId())) {
            waitingList.remove(entryToRemove); // Remove from queue
            System.out.println(user.getUsername() + " removed from the waiting list for bike " + bike.getName());
            return true;
        }

        System.out.println(user.getUsername() + " is not on the waiting list for bike " + bike.getName());
        return false;
    }

    // Delete an entry from the database
    private boolean deleteFromDatabase(int entryId) {
        String query = "DELETE FROM waiting_list WHERE id = ?";
        try (Connection connection = DatabaseUtils.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, entryId);
            return preparedStatement.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }*/

    /*// Load the waiting list from the database into the queue
    private void loadWaitingListFromDatabase() {
        String query = "SELECT wl.id, wl.status, wl.date_added, b.id AS bike_id, b.name, b.model, b.image, b.price, "
                + "u.id AS user_id, u.username, u.email, u.phone "
                + "FROM waiting_list wl "
                + "JOIN bikes b ON wl.bike_id = b.id "
                + "JOIN users u ON wl.user_id = u.id";

        try (Connection conn = DatabaseUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String status = rs.getString("status");
                Timestamp dateAdded = rs.getTimestamp("date_added");

                Bike bike = new Bike(
                        rs.getInt("bike_id"),
                        rs.getString("name"),
                        rs.getString("model"),
                        rs.getString("image"),
                        rs.getDouble("price")
                );

                User user = new User(
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getString("email"),
                        rs.getString("phone")
                );

                WaitingListEntry entry = new WaitingListEntry(id, user, bike, status, dateAdded);
                waitingList.add(entry);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error loading waiting list from database: " + e.getMessage());
        }
    }

    public void addToWaitingList(Bike bike, User user) {
        if (bike == null || user == null) {
            System.out.println("Invalid bike or user.");
            return;
        }*/


