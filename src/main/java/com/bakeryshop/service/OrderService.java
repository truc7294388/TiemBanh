package com.bakeryshop.service;

import com.bakeryshop.dto.OrderDTO;
import com.bakeryshop.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface OrderService {
    // Basic CRUD operations
    Order findById(Long orderId);
    OrderDTO getOrderDTOById(Long orderId);
    OrderDTO createOrder(Long userId, OrderDTO orderDTO);
    void deleteOrder(Long orderId);
    
    // Order management
    void updateOrderStatus(Long orderId, Order.OrderStatus status, String note);
    void updatePaymentStatus(Long orderId, Order.PaymentStatus status, String note);
    void updatePaymentMethod(Long orderId, Order.PaymentMethod method);
    void updateTransactionNo(Long orderId, String transactionNo);
    void cancelOrder(Long orderId, String reason);
    
    // Order queries
    Page<OrderDTO> getUserOrders(Long userId, Pageable pageable);
    Page<OrderDTO> getAllOrders(Pageable pageable);
    Page<OrderDTO> searchOrders(Long userId, String keyword, 
                              Order.OrderStatus status,
                              Order.PaymentStatus paymentStatus, 
                              Order.PaymentMethod paymentMethod,
                              Pageable pageable);
    Page<OrderDTO> getOrdersByStatus(String status, Pageable pageable);
    
    // Order processing
    byte[] generateOrderInvoice(Long orderId);
    
    // Statistics & Dashboard
    long countTotalOrders();
    double calculateTotalRevenue();
    List<Order> getRecentOrders();
    List<Order> getOrdersByStatus(Order.OrderStatus status);
} 