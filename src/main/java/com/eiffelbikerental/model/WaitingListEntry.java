package com.eiffelbikerental.model;

import java.sql.Date;
import java.sql.Timestamp;

public class WaitingListEntry {

    private int id;
    private User user;
    private Bike bike;
    private String status;
    private Timestamp dateAdded;



    public WaitingListEntry() {

	}

	public WaitingListEntry(int id, User user, Bike bike, String status, Timestamp dateAdded) {
        this.id = id;
        this.user = user;
        this.bike = bike;
        this.status = status;
        this.dateAdded = dateAdded;
    }

    public WaitingListEntry(User user2, Date dateAdded2) {
    	this.user = user;
    	this.dateAdded = dateAdded;
    	}

	// Getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Bike getBike() {
        return bike;
    }

    public void setBike(Bike bike) {
        this.bike = bike;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Timestamp getDateAdded() {
        return dateAdded;
    }

    public void setDateAdded(Timestamp dateAdded) {
        this.dateAdded = dateAdded;
    }

   
}


