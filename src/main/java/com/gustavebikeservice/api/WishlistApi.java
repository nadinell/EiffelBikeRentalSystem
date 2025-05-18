package com.gustavebikeservice.api;

import com.eiffelbikerental.model.Bike;
import com.gustavebikeservice.service.WishlistCheckResponse;
import com.gustavebikeservice.service.WishlistService;

import java.sql.SQLException;
import java.util.List;
import javax.ws.rs.*;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/wishlist")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class WishlistApi {

    private WishlistService wishlistService = new WishlistService();

    @POST
    @Path("/add")
    public Response addToWishlist(@QueryParam("userId") int userId, @QueryParam("bikeId") int bikeId) {
        // Ensure the userId and bikeId are valid
        if (userId <= 0 || bikeId <= 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Invalid userId or bikeId")
                    .build();
        }

        // Call the service to add the bike to the user's wishlist
        boolean success = wishlistService.addToWishlist(userId, bikeId);

        if (success) {
            return Response.ok("Bike added to wishlist").build();
        } else {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Failed to add bike to wishlist")
                    .build();
        }
    }


    @DELETE
    @Path("/remove")
    public Response removeFromWishlist(@QueryParam("userId") int userId, @QueryParam("bikeId") int bikeId) {
     boolean success = wishlistService.removeFromWishlist(userId, bikeId);
        if (success) {
            return Response.ok("Bike removed from wishlist").build();
        } else {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Failed to remove bike from wishlist")
                    .build();
        }
    }

    @GET
    @Path("/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getWishlist(@PathParam("userId") int userId) throws SQLException {
        List<Bike> wishlist = wishlistService.getWishlistByUserId(userId);
        
        if (wishlist.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("No bikes found in wishlist for user with ID " + userId)
                    .build();
        }
        
        return Response.ok(wishlist).build();
    }
 // Check if the bike is in the user's wishlist
    @GET
    @Path("/check")
    @Produces("application/json")
    public Response checkWishlist(
        @QueryParam("userId") String userId,
        @QueryParam("bikeId") String bikeId
    ) {
        if (userId == null || bikeId == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("User ID and Bike ID are required.")
                    .build();
        }

        // Check if the bike is in the wishlist for the given user
        boolean isInWishlist = wishlistService.isBikeInWishlist(userId, bikeId);

        // Create a response object
        WishlistCheckResponse response = new WishlistCheckResponse(isInWishlist);

        return Response.ok(response).build();
    }


}
