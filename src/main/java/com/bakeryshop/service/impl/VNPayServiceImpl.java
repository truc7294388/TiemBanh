package com.bakeryshop.service.impl;

import com.bakeryshop.entity.Order;
import com.bakeryshop.service.PaymentService;
import com.bakeryshop.util.VNPayUtils;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class VNPayServiceImpl implements PaymentService {
    private final VNPayUtils vnPayUtils;

    public VNPayServiceImpl(VNPayUtils vnPayUtils) {
        this.vnPayUtils = vnPayUtils;
    }

    @Override
    public String createPaymentUrl(Order order) throws Exception {
        return vnPayUtils.createPaymentUrl(
            String.valueOf(order.getId()),
            order.getTotalAmount().longValue()
        );
    }

    @Override
    public boolean validatePaymentResponse(Map<String, String> response) {
        return vnPayUtils.validatePaymentResponse(response);
    }
} 