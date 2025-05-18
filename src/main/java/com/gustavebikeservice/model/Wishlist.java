package com.gustavebikeservice.model;

public class Wishlist {
    private int id;
    private int userId;
    private int bikeId;

    public Wishlist() {}

    public Wishlist(int id, int userId, int bikeId) {
        this.id = id;
        this.userId = userId;
        this.bikeId = bikeId;
    }

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

    public int getBikeId() {
        return bikeId;
    }

    public void setBikeId(int bikeId) {
        this.bikeId = bikeId;
    }
}
