package com.gustavebikeservice.api;

import java.util.Collections;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.gustavebikeservice.model.BikeForSale;
import com.gustavebikeservice.service.BikeSaleService;


@Path("/bikesSale")
public class BikeSaleAPI {

    private BikeSaleService bikeSaleService = new BikeSaleService();

    // Endpoint to retrieve all bikes for sale
    @GET
    @Path("/forSale")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllBikesForSale() {
        List<BikeForSale> bikesForSale = bikeSaleService.getAllBikesForSale();

        if (bikesForSale.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("No bikes available for sale.")
                    .build();
        }

        return Response.status(Response.Status.OK)
                .entity(bikesForSale)
                .build();
    }

    // Endpoint to retrieve a bike for sale by ID
    @GET
    @Path("/forSale/{bikeId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getBikeForSaleById(@PathParam("bikeId") int bikeId) {
        BikeForSale bikeForSale = bikeSaleService.getBikeForSaleById(bikeId);

        if (bikeForSale == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Bike not found or not available for sale.")
                    .build();
        }

        return Response.status(Response.Status.OK)
                .entity(bikeForSale)
                .build();
    }

    // Endpoint to mark a bike as sold
    @PUT
    @Path("/markAsSold/{bikeId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response markBikeAsSold(@PathParam("bikeId") int bikeId) {
        boolean isMarkedAsSold = bikeSaleService.markBikeAsSold(bikeId);

        if (!isMarkedAsSold) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Failed to mark the bike as sold.")
                    .build();
        }

        return Response.status(Response.Status.OK)
                .entity("Bike marked as sold successfully.")
                .build();
    }

    // Endpoint to update the sale price and owner information
    @PUT
    @Path("/updateSaleInfo/{bikeId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateSaleInfo(@PathParam("bikeId") int bikeId, BikeForSale updatedBikeForSale) {
        boolean isUpdated = bikeSaleService.updateSaleInfo(bikeId, updatedBikeForSale.getSalePrice(), updatedBikeForSale.getOwner());

        if (!isUpdated) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Failed to update bike sale information.")
                    .build();
        }

        return Response.status(Response.Status.OK)
                .entity("Bike sale information updated successfully.")
                .build();
    }

 // Fetch bike prices in the requested currency
    @GET
    @Path("/saleCurrency")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getBikesForSaleInCurrency(@QueryParam("currency") String currency) {
        try {
            if (currency == null || currency.isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("Currency parameter is required.")
                        .build();
            }

            List<BikeForSale> bikes = bikeSaleService.getBikesForSaleInCurrency(currency);
            return Response.status(Response.Status.OK)
                    .entity(bikes)
                    .build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(e.getMessage())
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("An error occurred while fetching bike prices.")
                    .build();
        }
    }


    @GET
    @Path("/isAvailable/{bikeId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response isBikeAvailable(@PathParam("bikeId") int bikeId) {
        boolean isAvailable = bikeSaleService.isBikeAvailable(bikeId);
        return Response.ok(Collections.singletonMap("isAvailable", isAvailable)).build();
    }
    }



