package com.gustavebikeservice.model;

public class Basket {
    private int id;             // Unique identifier for the basket entry
    private int userId;         // ID of the user
    private int itemId;         // ID of the item (e.g., bike ID)
    private double price;       // Price of the item
    private String currency;    // Currency for the price (e.g., USD, EUR)


    // Constructors
    public Basket() {
    }

    public Basket(int id, int userId, int itemId, double price,  String currency) {
        this.id = id;
        this.userId = userId;
        this.itemId = itemId;
        this.price = price;
        this.currency = currency;
    }

    public Basket(int userId, int itemId, double price, String currency) {
        this.userId = userId;
        this.itemId = itemId;
        this.price = price;
        this.currency = currency;
    }

    public Basket(int userId, int itemId, double price) {
        this.userId = userId;
        this.itemId = itemId;
        this.price = price;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }


    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    // toString method for debugging and logging
    @Override
    public String toString() {
        return "Basket{" +
                "id=" + id +
                ", userId=" + userId +
                ", itemId=" + itemId +
                ", price=" + price +
                ", currency='" + currency + '\'' +
                '}';
    }
}
