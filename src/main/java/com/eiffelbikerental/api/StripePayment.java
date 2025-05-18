package com.eiffelbikerental.api;

import com.stripe.Stripe;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import com.utils.DatabaseUtils;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Path("/stripe")
public class StripePayment {

    static {
        Stripe.apiKey = ""; // secret key
    }

    @POST
    @Path("/create-checkout-session")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createCheckoutSession(PaymentRequest paymentRequest) {
        try {
            long rentalCostInCents = (long) (paymentRequest.getRentalCost() * 100);

            SessionCreateParams params = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setSuccessUrl("http://localhost:8080/EiffelBikeRentalSystem/bikes.oceanwp.org/product/laoreet-nisi-bibendum/Rentals.html?payment_status=success")
                    .setCancelUrl("http://localhost:8080/EiffelBikeRentalSystem/cancel.html")
                    .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
                    .addLineItem(
                        SessionCreateParams.LineItem.builder()
                            .setQuantity(1L)
                            .setPriceData(
                                SessionCreateParams.LineItem.PriceData.builder()
                                    .setCurrency("eur")
                                    .setUnitAmount(rentalCostInCents)
                                    .setProductData(
                                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                            .setName("Bike Rental")
                                            .build()
                                    )
                                    .build()
                            )
                            .build()
                    )
                    .build();

            Session session = Session.create(params);

            return Response.ok("{\"id\": \"" + session.getId() + "\"}").build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Failed to create checkout session: " + e.getMessage() + "\"}")
                    .build();
        }
    }





    public static class PaymentRequest {
        private int bikeId;
        private int userId;
        private double rentalCost;

        public int getBikeId() {
            return bikeId;
        }

        public void setBikeId(int bikeId) {
            this.bikeId = bikeId;
        }

        public double getRentalCost() {
            return rentalCost;
        }

        public void setRentalCost(double rentalCost) {
            this.rentalCost = rentalCost;
        }
    }
}
