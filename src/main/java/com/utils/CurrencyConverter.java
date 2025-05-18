package com.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

public class CurrencyConverter {

    private static final String API_URL = "";
    private static final String API_KEY = "";
    private static final String BASE_CURRENCY = "EUR";

    /**
     *
     * @param toCurrency The target currency (e.g., "USD", "GBP").
     * @param amount     The amount in Euros.
     * @return Converted amount in the target currency.
     */
    public static double convertToCurrency(String toCurrency, double amount) {
        try {
            // Build the API URL with  API key and base currency
            String urlString = API_URL + API_KEY + "/latest/" + BASE_CURRENCY;
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();

            // Check for successful response
            int responseCode = connection.getResponseCode();
            if (responseCode != 200) {
                throw new RuntimeException("Failed to fetch exchange rates. Response Code: " + responseCode);
            }

            // Read response
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            // Parse JSON response
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> jsonResponse = mapper.readValue(response.toString(), Map.class);

            // Get exchange rates and calculate conversion
            Map<String, Double> rates = (Map<String, Double>) jsonResponse.get("conversion_rates");
            Double exchangeRate = rates.get(toCurrency.toUpperCase());
            if (exchangeRate == null) {
                throw new IllegalArgumentException("Unsupported currency: " + toCurrency);
            }

            return amount * exchangeRate;

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error fetching exchange rates: " + e.getMessage());
        }
    }
}
