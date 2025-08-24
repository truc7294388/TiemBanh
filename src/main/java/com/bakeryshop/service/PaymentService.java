package com.bakeryshop.service;

import com.bakeryshop.entity.Order;

import java.util.Map;

public interface PaymentService {
    String createPaymentUrl(Order order) throws Exception;
    boolean validatePaymentResponse(Map<String, String> response);
} 