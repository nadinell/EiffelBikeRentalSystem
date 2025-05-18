package com.eiffelbikerental.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.eiffelbikerental.model.Bike;
import com.eiffelbikerental.model.User;
import com.eiffelbikerental.model.WaitingListEntry;
import com.eiffelbikerental.service.WaitingListService;

@Path("/waitinglist")
public class WaitingListAPI {

    private WaitingListService waitingListService = new WaitingListService();

    // Add a user to the waiting list for a specific bike
    @POST
    @Path("/add")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addToWaitingList(WaitingListEntry entry) {
        if (entry == null || entry.getBike() == null || entry.getUser() == null) {
            System.out.println("Invalid input: " + entry);
            return Response.status(Response.Status.BAD_REQUEST)
                           .entity("Bike or User cannot be null")
                           .build();
        }

        try {
            // Debug log to ensure proper input
            System.out.println("Adding to waiting list: Bike ID = " + entry.getBike().getId() + ", User ID = " + entry.getUser().getId());
            waitingListService.addToWaitingList(entry.getBike(), entry.getUser());

            return Response.status(Response.Status.CREATED)
                           .entity("User added to waiting list.")
                           .build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity("An error occurred: " + e.getMessage())
                           .build();
        }
    }



    // Remove a user from the waiting list when the bike becomes available
    @POST
    @Path("/remove")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response removeFromWaitingList(WaitingListEntry entry) {
        Bike bike = entry.getBike();
        User user = entry.getUser();

        if (bike == null || user == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                           .entity("Bike or User cannot be null")
                           .build();
        }

        waitingListService.removeFromWaitingList(bike, user);
        return Response.status(Response.Status.OK)
                       .entity("User " + user.getUsername() + " removed from waiting list for bike " + bike.getName())
                       .build();
    }

    // Notify the first user in the waiting list that the bike is available
    @POST
    @Path("/notify/{bikeId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response notifyUser(Bike bike) {
        if (bike == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                           .entity("Bike cannot be null")
                           .build();
        }

        waitingListService.notifyNextUser(bike);
        return Response.status(Response.Status.OK)
                       .entity("User notified that bike " + bike.getName() + " is now available")
                       .build();
    }

    // Get the number of users currently waiting for a specific bike
   /* @GET
    @Path("/size/{bikeId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getWaitingListSize(@PathParam("bikeId") int bikeId) {
        int size = waitingListService.getWaitingListSize(bikeId);  // Pass bikeId to the service
        return Response.status(Response.Status.OK)
                       .entity("Number of users waiting for bike ID " + bikeId + ": " + size)
                       .build();
    }*/

    

    // List all the users in the waiting list for a specific bike
    @GET
    @Path("/list/{bikeId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response listWaitingList(@PathParam("bikeId") int bikeId) {
        // This will list all users waiting for a bike
        List<WaitingListEntry> entries = waitingListService.getWaitingList(bikeId);

        if (entries == null || entries.isEmpty()) {
            return Response.status(Response.Status.NO_CONTENT)
                           .entity("No users are waiting for bike ID " + bikeId)
                           .build();
        }

        return Response.status(Response.Status.OK)
                       .entity(entries)
                       .build();
    }

 // Endpoint to get the status of a user's waiting list entry for a bike
    @GET
    @Path("/status/{bikeId}/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getWaitingListStatus(@PathParam("bikeId") int bikeId, @PathParam("userId") int userId) {
        List<WaitingListEntry> entries = waitingListService.getWaitingList(bikeId);
        if (entries.isEmpty()) {
            return Response.status(Response.Status.NO_CONTENT)
                           .entity("No users waiting for bike ID " + bikeId)
                           .build();
        }

        // Check if the user is in the waiting list
        for (WaitingListEntry entry : entries) {
            if (entry.getUser().getId() == userId) {
            	return Response.ok(new HashMap<String, Object>() {{
            	    put("status", entry.getStatus());
            	}}).build();

            }
        }

        return Response.status(Response.Status.NOT_FOUND)
                       .entity("User ID " + userId + " is not on the waiting list for bike ID " + bikeId)
                       .build();
    }

 // Endpoint to retrieve the waiting list entries for a specific user
    @GET
    @Path("/user/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUserWaitingList(@PathParam("userId") int userId) {
        // Retrieve all waiting list entries for the user
        List<WaitingListEntry> userWaitingList = waitingListService.getUserWaitingList(userId);

        // If the waiting list is empty, return an empty JSON array
        if (userWaitingList.isEmpty()) {
            return Response.status(Response.Status.OK)  // Change to 200 OK
                           .entity(new ArrayList<>())  // Return an empty list
                           .build();
        }

        // Return the list of waiting list entries as JSON
        return Response.status(Response.Status.OK)
                       .entity(userWaitingList)
                       .build();
    }


    // Delete a bike request from the waiting list
    @DELETE
    @Path("/cancel/{entryId}")
    public Response cancelBikeRequest(@PathParam("entryId") int entryId) {
        boolean isDeleted = waitingListService.cancelBikeRequest(entryId);

        if (isDeleted) {
            return Response.ok("Bike request canceled successfully.").build();  // Ensure this response is sent back
        } else {
            return Response.status(Response.Status.NOT_FOUND)
                           .entity("Bike request not found.")
                           .build();  // This will send a 404 response
        }
    }

    @DELETE
    @Path("/cancel/{bikeId}/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response cancelBikeFromWaitingList(@PathParam("bikeId") Long bikeId, @PathParam("userId") Long userId) {
        boolean isCanceled = waitingListService.cancelBike(bikeId, userId);
        if (isCanceled) {
            return Response.ok(Collections.singletonMap("message", "Bike request successfully canceled.")).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND)
                           .entity(Collections.singletonMap("message", "Bike request not found."))
                           .build();
        }
    }

 // Get the number of users currently waiting for a specific bike
    @GET
    @Path("/size/{bikeId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getWaitingListSize(@PathParam("bikeId") int bikeId) {
        int size = waitingListService.getWaitingListSize(bikeId);  // Pass bikeId to the service
        
        if (size == 0) {
            return Response.status(Response.Status.OK)
                           .entity("No users are waiting for bike ID " + bikeId)
                           .build();
        }

        return Response.status(Response.Status.OK)
                       .entity("Number of users waiting for bike ID " + bikeId + ": " + size)
                       .build();
    }


}
