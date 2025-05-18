package com.gustavebikeservice.service;

public class WishlistCheckResponse {
    private boolean isInWishlist;

    public WishlistCheckResponse(boolean isInWishlist) {
        this.isInWishlist = isInWishlist;
    }

    public boolean isInWishlist() {
        return isInWishlist;
    }

    public void setInWishlist(boolean inWishlist) {
        isInWishlist = inWishlist;
    }
}
