package com.bakeryshop.admin.service;

import com.bakeryshop.dto.OrderDTO;
import com.bakeryshop.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminOrderService {
    // Basic CRUD operations
    OrderDTO getOrderById(Long id);
    Page<OrderDTO> getAllOrders(String keyword, Pageable pageable);
    void updateOrderStatus(Long id, Order.OrderStatus status, String note);
    void updatePaymentStatus(Long id, Order.PaymentStatus status, String note);
    void deleteOrder(Long id);

    // Statistics
    long getTotalOrders();
    long getOrdersByStatus(Order.OrderStatus status);
    long getOrdersByPaymentStatus(Order.PaymentStatus status);
    double getTotalRevenue();
    double getRevenueByStatus(Order.OrderStatus status);
    double getRevenueByPaymentStatus(Order.PaymentStatus status);
    double getAverageOrderValue();
} 