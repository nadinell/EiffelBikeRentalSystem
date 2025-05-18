package com.eiffelbikerental.service;

import com.eiffelbikerental.model.User;

public class PaymentService {
    public boolean processPayment(User user, double amount, String paymentMethod) {
        System.out.println("Processing payment of €" + amount + " for user " + user.getUsername() + " using " + paymentMethod);
        return true; 
    }
}
