package com.gustavebikeservice.api;

import com.gustavebikeservice.service.BasketService;
import com.gustavebikeservice.service.PaymentService;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import com.utils.DatabaseUtils;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Path("/payment")
public class PaymentAPI {

    private final PaymentService paymentService = new PaymentService();
    private BasketService cartService = new BasketService();


    // Endpoint to check user's available funds
    @GET
    @Path("/users/{userId}/funds")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUserFunds(@PathParam("userId") String userId) {
        try (Connection connection = DatabaseUtils.getConnection()) {
            String query = "SELECT balance FROM users WHERE id = ?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, userId);
            ResultSet resultSet = stmt.executeQuery();

            if (resultSet.next()) {
                double availableFunds = resultSet.getDouble("balance");
                return Response.ok("{\"availableFunds\": " + availableFunds + "}").build();
            } else {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\": \"User not found\"}")
                        .build();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Database error\"}")
                    .build();
        }
    }

    // Endpoint to initiate payment
    @POST
    @Path("/save")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response processPayment(PaymentRequest paymentRequest) {
        try {
            // Step 1: Check user's available funds via BankService
            double availableFunds = paymentService.getAvailableFunds(paymentRequest.getUserId());
            if (availableFunds < paymentRequest.getAmount()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"error\": \"Insufficient funds\"}")
                        .build();
            }

            // Step 2: Create a payment intent with Stripe
            String clientSecret = paymentService.createPaymentIntent(paymentRequest.getUserId(),
                                                                      paymentRequest.getAmount(),
                                                                      paymentRequest.getCurrency());
            if (clientSecret == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"error\": \"Failed to create payment intent\"}")
                        .build();
            }

            // Step 3: Save payment details in the database for each bike
            for (int bikeId : paymentRequest.getBikeIds()) {
            	paymentService.savePaymentDetails(paymentRequest.getUserId(),
                        paymentRequest.getBikeIds(),
                        "COMPLETED",
                        paymentRequest.getAmount(),
                        paymentRequest.getCurrency());

            }

            return Response.ok("{\"clientSecret\": \"" + clientSecret + "\"}").build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Payment processing failed\"}")
                    .build();
        }
    }


 // New endpoint to create a payment intent
    @POST
    @Path("/intent")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createPaymentIntent(PaymentRequest paymentRequest) {
        try {
            // Create the payment intent with Stripe
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount((long) (paymentRequest.getAmount() * 100)) // Stripe expects the amount in cents
                    .setCurrency(paymentRequest.getCurrency()) // Use the correct currency from the request
                    .build();

            PaymentIntent paymentIntent = PaymentIntent.create(params);

            // Return the client secret for the payment
            return Response.ok("{\"clientSecret\": \"" + paymentIntent.getClientSecret() + "\"}").build();
        } catch (StripeException e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Failed to create payment intent\"}")
                    .build();
        }
    }


    @POST
    @Path("/process/{userId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response processPayment(@PathParam("userId") int userId, PaymentRequest paymentRequest) throws Exception {
        try {
            // Retrieve bike IDs from the cart
            List<Integer> bikeIds = cartService.getBikeIdsInCart(userId);

            if (bikeIds.isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"error\": \"Cannot process payment: Cart is empty.\"}")
                        .build();
            }

            // Save payment details and update database
            paymentService.savePaymentDetails(paymentRequest.getUserId(),
                                              bikeIds,
                                              "COMPLETED",
                                              paymentRequest.getAmount(),
                                              paymentRequest.getCurrency());

            return Response.ok("{\"message\": \"Payment processed successfully.\"}").build();
        } catch (SQLException e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Error processing payment: " + e.getMessage() + "\"}")
                    .build();
        }
    }


    // Request model for payment processing
    public static class PaymentRequest {
        private String userId;
        private List<Integer> bikeIds;  // Change to List of integers
        private double amount;
        private String currency;

        // Getter and Setter methods
        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public List<Integer> getBikeIds() {
            return bikeIds;
        }

        public void setBikeIds(List<Integer> bikeIds) {
            this.bikeIds = bikeIds;
        }

        public double getAmount() {
            return amount;
        }

        public void setAmount(double amount) {
            this.amount = amount;
        }

        public String getCurrency() {
            return currency;
        }

        public void setCurrency(String currency) {
            this.currency = currency;
        }
    }

   

    }
