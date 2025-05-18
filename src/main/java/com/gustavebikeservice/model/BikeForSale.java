package com.gustavebikeservice.model;

import com.eiffelbikerental.model.Bike;

public class BikeForSale extends Bike {
    private double salePrice;   // Price at which the bike is being sold
    private boolean isSold;     // Boolean to track if the bike is sold

 // Constructor for BikeForSale class
    public BikeForSale(int id, String name, String model, String image, double salePrice, boolean isAvailable, String description, String owner) {
        super(id, name, model, image, salePrice, isAvailable, description, owner);  // Call to Bike constructor
        this.salePrice = salePrice;
        this.isSold = false;
    }

    // Constructor for BikeForSale class
    public BikeForSale(int id, String name, String model, String image, double salePrice, boolean isAvailable, String description, String owner, String notes) {
        super(id, name, model, image, salePrice, isAvailable, description, owner,  notes);  // Call to Bike constructor
        this.salePrice = salePrice;
        this.isSold = false;
    }

    // Getters and Setters
    public double getSalePrice() {
        return salePrice;
    }

    public void setSalePrice(double salePrice) {
        this.salePrice = salePrice;
    }

    public boolean isSold() {
        return isSold;
    }

    public void setSold(boolean sold) {
        isSold = sold;
    }


 // Method to mark the bike as sold
    public void processSale() {
        if (!isSold) {
            isSold = true;
            System.out.println("Bike " + getName() + " sold!");
        } else {
            System.out.println("This bike is already sold.");
        }
    }

    @Override
    public String toString() {
        return super.toString() + " [Sale Price=" + salePrice +  ", Is Sold=" + isSold + "]";
    }
}
