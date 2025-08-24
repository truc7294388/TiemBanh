package com.bakeryshop.service;

import com.bakeryshop.entity.Order;
import com.bakeryshop.entity.User;

public interface EmailService {
    void sendVerificationEmail(User user);
    void sendPasswordResetEmail(User user);
    void sendOrderConfirmationEmail(Order order);
    void sendOrderCancellationEmail(Order order);
    void sendOrderStatusUpdateEmail(Order order, String oldStatus, String newStatus);
    void sendPaymentStatusUpdateEmail(Order order, String oldStatus, String newStatus);
} 