package com.eiffelbikerental.api;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.eiffelbikerental.model.Bike;
import com.eiffelbikerental.service.BikeRentalService;
import com.eiffelbikerental.service.UserService;
import com.eiffelbikerental.service.PaymentService;


@Path("/bikerental")
public class BikeRentalAPI {

    private BikeRentalService bikeRentalService;
    private UserService userService;
    private PaymentService paymentService;


    public BikeRentalAPI() {
        bikeRentalService = new BikeRentalService();
        userService = new UserService();
    }

    // Endpoint to add a new bike to the inventory
    @POST
    @Path("/bikes")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addBike(Bike bike) {
        bikeRentalService.addBike(bike);
        return Response.status(Response.Status.CREATED).entity("Bike added successfully.").build();
    }


    // Endpoint to get details of a specific bike by ID
    @GET
    @Path("/bikes/{bikeId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getBikeById(@PathParam("bikeId") int bikeId) {
        Bike bike = bikeRentalService.getBikeById(bikeId);
        if (bike != null) {
            return Response.ok(bike).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).entity("Bike not found.").build();
        }
    }


 // Endpoint to get a list of all available bikes for rental
    @GET
    @Path("/all")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllBikes() {
        List<Bike> availableBikes = bikeRentalService.getBikes();  // Fetch bikes from the service

        if (availableBikes == null || availableBikes.isEmpty()) {  // Check if the list is empty or null
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("No bikes available for rental.")
                    .build();  // Return NOT_FOUND status with a message
        }

        return Response.ok(availableBikes).build();  // Return OK status with the list of bikes
    }


 // Endpoint to get a list of all available bikes for rental
    @GET
    @Path("/available")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAvailableBikes() {
        List<Bike> availableBikes = bikeRentalService.getAvailableBikes();
        if (availableBikes.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("No bikes available for rental.").build();
        }
        return Response.ok(availableBikes).build();
    }

 // Endpoint to list all rented bikes
    @GET
    @Path("/rented")
    @Produces(MediaType.APPLICATION_JSON)
    public Response listRentedBikes() {
        List<Bike> rentedBikes = bikeRentalService.listRentedBikes();
        if (rentedBikes.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("No bikes are currently rented.").build();
        }
        return Response.ok(rentedBikes).build();
    }

    @GET
    @Path("/availability")
    @Produces(MediaType.APPLICATION_JSON)
    public Response checkBikeAvailability(
            @QueryParam("bikeId") int bikeId,
            @QueryParam("startDate") String startDate,
            @QueryParam("endDate") String endDate) {
        if (bikeId <= 0 || startDate == null || endDate == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Collections.singletonMap("error", "bikeId, startDate, and endDate must be provided."))
                    .build();
        }

        try {
            boolean isAvailable = bikeRentalService.isBikeAvailable(bikeId, startDate, endDate);
            return Response.ok(Collections.singletonMap("available", isAvailable)).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Collections.singletonMap("error", "Error checking availability: " + e.getMessage()))
                    .build();
        }
    }



    @GET
    @Path("/rental/cost")
    @Produces(MediaType.APPLICATION_JSON)
    public Response calculateRentalCost(@QueryParam("bikeId") int bikeId,
                                         @QueryParam("startDate") String startDateStr,
                                         @QueryParam("endDate") String endDateStr) {
        if (bikeId <= 0 || startDateStr == null || endDateStr == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Collections.singletonMap("error", "BikeId, startDate, and endDate must be provided."))
                    .build();
        }

        // Retrieve bike by ID
        Bike bike = bikeRentalService.getBikeById(bikeId);
        if (bike == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Collections.singletonMap("error", "Bike not found."))
                    .build();
        }

        try {
            // Calculate rental cost using start and end dates
            double cost = bikeRentalService.calculateRentalCost(bike, startDateStr, endDateStr);
            return Response.ok(Collections.singletonMap("cost", cost)).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Collections.singletonMap("error", e.getMessage()))
                    .build();
        }
    }


 // Endpoint to get the rental status of a specific bike
    @GET
    @Path("/status/{bikeId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getBikeStatus(@PathParam("bikeId") int bikeId) {
        String status = bikeRentalService.getBikeStatus(bikeId);
        if (status == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Bike not found.").build();
        }
        return Response.ok(Collections.singletonMap("status", status)).build();
    }

    @GET
    @Path("/bikes/availability/{bikeId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getBikeAvailability(@PathParam("bikeId") int bikeId) {
        Bike bike = bikeRentalService.getBikeById(bikeId);
        if (bike == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Collections.singletonMap("error", "Bike not found."))
                    .build();
        }
        Map<String, Object> response = new HashMap<>();
        response.put("bikeId", bikeId);
        response.put("isAvailable", bike.isAvailable());
        return Response.ok(response).build();
    }

/*    @GET
    @Path("/notifyFirstUser/{bikeId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response notifyFirstUser(@PathParam("bikeId") int bikeId) {
        // Call the service method to notify the first user when the bike is available
        String resultMessage = bikeRentalService.notifyFirstUserWhenBikeIsAvailable(bikeId);

        // Return the result as a response
        return Response.status(Response.Status.OK)
                       .entity(resultMessage)
                       .build();
    }*/
}
