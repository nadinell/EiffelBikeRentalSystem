package com.gustavebikeservice.api;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.gustavebikeservice.model.BikeForSale;
import com.gustavebikeservice.service.BasketService;
import com.gustavebikeservice.service.BikeSaleService;

@Path("/basket")
public class BasketAPI {


    private BasketService basketService = new BasketService();
    private BikeSaleService bikeService = new BikeSaleService();


    // Endpoint to retrieve a user's basket with PENDING items
   /* @GET
    @Path("/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getBasketByUser(@PathParam("userId") int userId) {
        List<Basket> basketList = basketService.getBasketByUser(userId);

        if (basketList.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("No items found in the user's basket.")
                    .build();
        }

        return Response.status(Response.Status.OK)
                .entity(basketList)
                .build();
    }*/

    @GET
    @Path("/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getBasketByUser(@PathParam("userId") int userId) {
        List<Map<String, Object>> basketList = basketService.getBasketWithBikeDetails(userId);

        if (basketList.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("No items found in the user's basket.")
                    .build();
        }

        return Response.status(Response.Status.OK)
                .entity(basketList)
                .build();
    }



    @POST
    @Path("/add/{userId}/{bikeId}/{convertedPrice}/{currency}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addToBasket(
            @PathParam("userId") int userId,
            @PathParam("bikeId") int bikeId,
            @PathParam("convertedPrice") double convertedPrice,
            @PathParam("currency") String currency) {

        System.out.println("Debugging userId: " + userId);
        System.out.println("Debugging bikeId: " + bikeId);
        System.out.println("Debugging convertedPrice: " + convertedPrice);
        System.out.println("Debugging currency: " + currency);

        // Get the bike details by its ID
        BikeForSale bike = bikeService.getBikeForSaleById(bikeId);
        if (bike == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Bike not found.")
                    .build();
        }

        // Add the bike to the basket
        boolean isAdded = basketService.addToBasket1(userId, bikeId, convertedPrice, currency);
        if (!isAdded) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Failed to add item to the basket.")
                    .build();
        }

        return Response.status(Response.Status.CREATED)
                .entity("Item added to the basket successfully.")
                .build();
    }




   /* /for Payment / Endpoint to update the status of an item in the basket
    @PUT
    @Path("/updateStatus/{basketId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateBasketStatus(@PathParam("basketId") int basketId,
                                        @QueryParam("newStatus") String newStatus) {
        try {
            Basket.Status status = Basket.Status.valueOf(newStatus.toUpperCase());
            boolean isUpdated = basketService.updateBasketStatus(basketId, status);

            if (!isUpdated) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("Failed to update the basket status.")
                        .build();
            }

            return Response.status(Response.Status.OK)
                    .entity("Basket status updated successfully.")
                    .build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Invalid status provided. Use PENDING, COMPLETED, or CANCELED.")
                    .build();
        }
    }*/

    // Endpoint to clear the basket (remove all PENDING items)
    @DELETE
    @Path("/clear/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response clearBasket(@PathParam("userId") int userId) {
        boolean isCleared = basketService.clearBasket(userId);

        if (!isCleared) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Failed to clear the basket.")
                    .build();
        }

        return Response.status(Response.Status.OK)
                .entity("Basket cleared successfully.")
                .build();
    }

 // Endpoint to remove an item from the user's basket
    @DELETE
    @Path("/remove/{basketId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response removeItemFromBasket(@PathParam("basketId") int basketId) {
        boolean isRemoved = basketService.removeItemFromBasket(basketId);

        if (!isRemoved) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Failed to remove item from the basket.")
                    .build();
        }

        return Response.status(Response.Status.OK)
                .entity("Item removed from the basket successfully.")
                .build();
    }



        @GET
        @Path("/contains/{userId}/{bikeId}")
        @Produces(MediaType.APPLICATION_JSON)
        public Response isBikeInBasket(@PathParam("userId") int userId, @PathParam("bikeId") int bikeId) {
            boolean isInBasket = basketService.isBikeInBasket(userId, bikeId);
            return Response.ok(Collections.singletonMap("isInBasket", isInBasket)).build();
        }

        @GET
        @Path("/{userId}/items")
        public Response getCartItems(@PathParam("userId") int userId) {
            try {
                List<Integer> bikeIds = basketService.getBikeIdsInCart(userId);
                if (bikeIds.isEmpty()) {
                    return Response.status(Response.Status.NOT_FOUND)
                            .entity("Cart is empty for user ID: " + userId)
                            .build();
                }
                return Response.ok(bikeIds).build();
            } catch (Exception e) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity("Error fetching cart items: " + e.getMessage())
                        .build();
            }
        }


    }


