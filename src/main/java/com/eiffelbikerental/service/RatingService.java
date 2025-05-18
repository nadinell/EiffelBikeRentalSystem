package com.eiffelbikerental.service;

import java.util.HashMap;
import java.util.Map;

import com.eiffelbikerental.model.Bike;
import com.eiffelbikerental.model.User;

public class RatingService {

    // Map to store ratings for bikes (bike ID -> rating)
    private Map<Bike, Double> bikeRatings;
    // Map to store condition notes for bikes (bike ID -> condition notes list)
    private Map<Bike, String> bikeConditionNotes;

    // Constructor to initialize the rating and condition notes service
    public RatingService() {
        bikeRatings = new HashMap<>();
        bikeConditionNotes = new HashMap<>();
    }

    // Method to rate a bike
    public void rateBike(Bike bike, User user, double rating) {
        if (bike == null || user == null) {
            System.out.println("Invalid bike or user.");
            return;
        }

        if (rating < 1.0 || rating > 5.0) {
            System.out.println("Rating must be between 1 and 5.");
            return;
        }

        // Store the rating for the bike
        bikeRatings.put(bike, rating);
        System.out.println(user.getUsername() + " rated bike " + bike.getName() + " with a rating of " + rating);
    }

    // Method to add a condition note to a bike
    public void addConditionNote(Bike bike, User user, String note) {
        if (bike == null || user == null || note == null || note.isEmpty()) {
            System.out.println("Invalid bike, user, or note.");
            return;
        }

        // Add condition note to the bike (if no note exists, initialize an empty string)
        bikeConditionNotes.put(bike, note);
        System.out.println(user.getUsername() + " added a condition note to bike " + bike.getName() + ": " + note);
    }

    // Method to get the rating of a bike
    public double getBikeRating(Bike bike) {
        if (bike == null) {
            System.out.println("Invalid bike.");
            return -1;  // Return an invalid rating if the bike doesn't exist
        }

        // Return the rating if it exists
        return bikeRatings.getOrDefault(bike, -1.0);  // Default -1 if no rating exists for the bike
    }

    // Method to get the condition note for a bike
    public String getBikeConditionNote(Bike bike) {
        if (bike == null) {
            System.out.println("Invalid bike.");
            return null;  // Return null if the bike doesn't exist
        }

        // Return the condition note if it exists
        return bikeConditionNotes.getOrDefault(bike, "No condition notes available.");
    }

    // Method to print out all ratings and notes for a bike (for internal use or reporting)
    public void listBikeRatingsAndNotes(Bike bike) {
        if (bike == null) {
            System.out.println("Invalid bike.");
            return;
        }

        double rating = getBikeRating(bike);
        String conditionNote = getBikeConditionNote(bike);

        System.out.println("Bike: " + bike.getName());
        System.out.println("Rating: " + (rating == -1 ? "No rating yet" : rating));
        System.out.println("Condition Notes: " + conditionNote);
    }
}
