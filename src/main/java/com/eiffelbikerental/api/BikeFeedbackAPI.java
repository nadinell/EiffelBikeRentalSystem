package com.eiffelbikerental.api;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.eiffelbikerental.model.BikeFeedback;
import com.eiffelbikerental.service.BikeFeedbackService;

@Path("/bike-feedback")
public class BikeFeedbackAPI {

    private BikeFeedbackService bikeFeedbackService = new BikeFeedbackService();

    // Endpoint to add feedback (either rating or condition note)
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addBikeFeedback(BikeFeedback bikeFeedback) {
        try {
            // Call service layer to add feedback
            bikeFeedbackService.addFeedback(
                bikeFeedback.getBikeId(),
                bikeFeedback.getUserId(),
                bikeFeedback.getRating(),
                bikeFeedback.getNote()
            );

            return Response.status(Response.Status.CREATED).entity(bikeFeedback).build();
        } catch (SQLException e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error while adding feedback").build();
        }
    }

    // Endpoint to get all feedback for a specific bike
    @GET
    @Path("/{bikeId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getBikeFeedback(@PathParam("bikeId") int bikeId) {
        try {
            List<Map<String, Object>> feedbackList = bikeFeedbackService.getBikeFeedback(bikeId);

            if (feedbackList.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND)
                               .entity("{\"message\": \"No feedback available for this bike.\"}")
                               .build();
            }

            return Response.ok(feedbackList).build();

        } catch (SQLException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity("{\"error\": \"Error fetching bike feedback: " + e.getMessage() + "\"}")
                           .build();
        }
    }

    // Endpoint to get the average rating for a specific bike
    @GET
    @Path("/average-rating/{bikeId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAverageRating(@PathParam("bikeId") int bikeId) {
        try {
            double averageRating = bikeFeedbackService.getAverageRating(bikeId);

            // If there is no feedback, return 0.0
            if (averageRating == 0.0) {
                return Response.status(Response.Status.NOT_FOUND)
                               .entity("{\"message\": \"No ratings available for this bike.\"}")
                               .build();
            }

            return Response.ok("{\"averageRating\": " + averageRating + "}").build();

        } catch (SQLException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity("{\"error\": \"Error fetching average rating: " + e.getMessage() + "\"}")
                           .build();
        }
    }

    // Endpoint to update existing feedback
   /* @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateBikeFeedback(@PathParam("id") int id, BikeFeedback bikeFeedback) {
        try {
            // Assuming you add an `updateFeedback` method in your service
            boolean isUpdated = bikeFeedbackService.updateFeedback(
                id,
                bikeFeedback.getBikeId(),
                bikeFeedback.getUserId(),
                bikeFeedback.getRating(),
                bikeFeedback.getNote()
            );

            if (isUpdated) {
                return Response.ok("{\"message\": \"Feedback updated successfully.\"}").build();
            } else {
                return Response.status(Response.Status.NOT_FOUND)
                               .entity("{\"message\": \"Feedback not found.\"}")
                               .build();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity("{\"error\": \"Error updating feedback: " + e.getMessage() + "\"}")
                           .build();
        }
    }*/

    // Endpoint to delete feedback by ID
   /* @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteBikeFeedback(@PathParam("id") int id) {
        try {
            // Assuming you add a `deleteFeedback` method in your service
            boolean isDeleted = bikeFeedbackService.deleteFeedback(id);

            if (isDeleted) {
                return Response.ok("{\"message\": \"Feedback deleted successfully.\"}").build();
            } else {
                return Response.status(Response.Status.NOT_FOUND)
                               .entity("{\"message\": \"Feedback not found.\"}")
                               .build();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity("{\"error\": \"Error deleting feedback: " + e.getMessage() + "\"}")
                           .build();
        }
    }*/
    
    // Endpoint to get the rating breakdown for a specific bike (ratings 1-5 count)
    @GET
    @Path("/rating-breakdown/{bikeId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getRatingBreakdown(@PathParam("bikeId") int bikeId) {
        try {
            Map<Integer, Integer> ratingCounts = bikeFeedbackService.getRatingCounts(bikeId);

            // If no ratings are found for this bike, return a message
            if (ratingCounts.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND)
                               .entity("{\"message\": \"No ratings available for this bike.\"}")
                               .build();
            }

            return Response.ok(ratingCounts).build();
        } catch (SQLException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity("{\"error\": \"Error fetching rating breakdown: " + e.getMessage() + "\"}")
                           .build();
        }
    }
}
