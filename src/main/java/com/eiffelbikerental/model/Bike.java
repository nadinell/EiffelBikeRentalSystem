package com.eiffelbikerental.model;

import java.util.ArrayList;
import java.util.List;

public class Bike {
    private int id;
    private String name;
    private String model;
    private String image;
    private double price;       // Rental price or sale price
    private boolean isAvailable;
    private String description;
    private String owner;       // Owner of the bike (e.g., "Eiffel Bike Corp" or user ID)
    private int rentalCount;    // The number of times this bike has been rented out
    private List<String> notes; // Notes for the bike, e.g., condition, repair notes



    public Bike() {
	}





	public Bike(int id,String name, String model, String image, double price) {
		this.id = id;
		this.name = name;
		this.model = model;
		this.image = image;
		this.price = price;
	}





	public Bike(int id, String title, String model, String image, double price, boolean isAvailable, String description, String owner) {
        this.id = id;
        this.name = title;
        this.model = model;
        this.image = image;
        this.price = price;
        this.isAvailable = isAvailable;
        this.description = description;
        this.owner = owner;
        this.rentalCount = 0;
        this.notes = new ArrayList<>();
    }

	public Bike(int id, String title, String model, String image, double price, boolean isAvailable, String description, String owner, String notes) {
        this.id = id;
        this.name = title;
        this.model = model;
        this.image = image;
        this.price = price;
        this.isAvailable = isAvailable;
        this.description = description;
        this.owner = owner;
        this.rentalCount = 0;
        this.notes = new ArrayList<>();
    }


    public Bike(int bikeId, String name) {
    	 this.id = id;
         this.name = name;

    	 }





	public Bike(int bikeId,String name, String modelName, String image, double price, boolean isavailable) {
		this.id = bikeId;
        this.name = name;
        this.model = modelName;
        this.image = image;
        this.price = price;
        this.isAvailable = isavailable;

	}





	public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String title) {
        this.name = title;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean available) {
        isAvailable = available;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public int getRentalCount() {
        return rentalCount;
    }

    public void setRentalCount(int rentalCount) {
        this.rentalCount = rentalCount;
    }

    public List<String> getNotes() {
        return notes;
    }

    public void setNotes(List<String> notes) {
        this.notes = notes;
    }

    @Override
	public String toString() {
		return "Bike [id=" + id + ", name=" + name + ", model=" + model + ", image=" + image + ", price=" + price
				+ ", isAvailable=" + isAvailable + ", description=" + description + ", owner=" + owner
				+ ", rentalCount=" + rentalCount + ", notes=" + notes + "]";
	}
}
