package com.bakeryshop.admin.service.impl;

import com.bakeryshop.admin.service.AdminOrderService;
import com.bakeryshop.dto.OrderDTO;
import com.bakeryshop.dto.OrderItemDTO;
import com.bakeryshop.entity.Order;
import com.bakeryshop.entity.OrderItem;
import com.bakeryshop.entity.Product;
import com.bakeryshop.exception.ResourceNotFoundException;
import com.bakeryshop.repository.OrderRepository;
import com.bakeryshop.repository.ProductRepository;
import com.bakeryshop.service.EmailService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminOrderServiceImpl implements AdminOrderService {
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final EmailService emailService;

    public AdminOrderServiceImpl(OrderRepository orderRepository,
                               ProductRepository productRepository,
                               EmailService emailService) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.emailService = emailService;
    }

    @Override
    @Transactional(readOnly = true)
    public OrderDTO getOrderById(Long id) {
        return orderRepository.findById(id)
                .map(this::convertToDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderDTO> getAllOrders(String keyword, Pageable pageable) {
        if (keyword != null && !keyword.isEmpty()) {
            return orderRepository.findByUserEmailContainingOrUserPhoneContaining(keyword, keyword, pageable)
                    .map(this::convertToDTO);
        }
        return orderRepository.findAll(pageable).map(this::convertToDTO);
    }

    @Override
    @Transactional
    public void updateOrderStatus(Long id, Order.OrderStatus status, String note) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", id));
        
        String oldStatus = order.getStatus().toString();
        order.setStatus(status);
        order.setNote(note);
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);

        // If order is cancelled, return products to stock
        if (Order.OrderStatus.CANCELLED.equals(status)) {
            order.getItems().forEach(item -> {
                Product product = item.getProduct();
                product.setStock(product.getStock() + item.getQuantity());
                productRepository.save(product);
            });
        }

        // Send notification email
        try {
            emailService.sendOrderStatusUpdateEmail(order, oldStatus, status.toString());
        } catch (Exception e) {
            // Log error but don't fail the transaction
            e.printStackTrace();
        }
    }

    @Override
    @Transactional
    public void updatePaymentStatus(Long id, Order.PaymentStatus status, String note) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", id));
        
        String oldStatus = order.getPaymentStatus().toString();
        order.setPaymentStatus(status);
        order.setNote(note);
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);

        // Send notification email
        try {
            emailService.sendPaymentStatusUpdateEmail(order, oldStatus, status.toString());
        } catch (Exception e) {
            // Log error but don't fail the transaction
            e.printStackTrace();
        }
    }

    @Override
    @Transactional
    public void deleteOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", id));
        
        // Return products to stock before deleting
        order.getItems().forEach(item -> {
            Product product = item.getProduct();
            product.setStock(product.getStock() + item.getQuantity());
            productRepository.save(product);
        });
        
        orderRepository.delete(order);
    }

    @Override
    public long getTotalOrders() {
        return orderRepository.count();
    }

    @Override
    public long getOrdersByStatus(Order.OrderStatus status) {
        return orderRepository.countByStatus(status);
    }

    @Override
    public long getOrdersByPaymentStatus(Order.PaymentStatus status) {
        return orderRepository.countByPaymentStatus(status);
    }

    @Override
    public double getTotalRevenue() {
        return orderRepository.findAll().stream()
                .filter(order -> order.getStatus() == Order.OrderStatus.DELIVERED)
                .mapToDouble(order -> order.getTotalAmount().doubleValue())
                .sum();
    }

    @Override
    public double getRevenueByStatus(Order.OrderStatus status) {
        return orderRepository.findByStatus(status).stream()
                .mapToDouble(order -> order.getTotalAmount().doubleValue())
                .sum();
    }

    @Override
    public double getRevenueByPaymentStatus(Order.PaymentStatus status) {
        return orderRepository.findByPaymentStatus(status).stream()
                .mapToDouble(order -> order.getTotalAmount().doubleValue())
                .sum();
    }

    @Override
    public double getAverageOrderValue() {
        List<Order> deliveredOrders = orderRepository.findByStatus(Order.OrderStatus.DELIVERED);
        if (deliveredOrders.isEmpty()) {
            return 0.0;
        }
        double totalRevenue = deliveredOrders.stream()
                .mapToDouble(order -> order.getTotalAmount().doubleValue())
                .sum();
        return totalRevenue / deliveredOrders.size();
    }

    private OrderDTO convertToDTO(Order order) {
        OrderDTO dto = new OrderDTO();
        dto.setId(order.getId());
        dto.setUserId(order.getUser().getId());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setStatus(order.getStatus());
        dto.setPaymentMethod(order.getPaymentMethod());
        dto.setPaymentStatus(order.getPaymentStatus());
        dto.setPaymentId(order.getPaymentId());
        dto.setShippingAddress(order.getShippingAddress());
        dto.setShippingPhone(order.getShippingPhone());
        dto.setShippingName(order.getShippingName());
        dto.setNote(order.getNote());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setUpdatedAt(order.getUpdatedAt());

        List<OrderItemDTO> itemDTOs = order.getItems().stream()
                .map(item -> {
                    OrderItemDTO itemDTO = new OrderItemDTO();
                    itemDTO.setId(item.getId());
                    itemDTO.setProductId(item.getProduct().getId());
                    itemDTO.setProductName(item.getProduct().getName());
                    itemDTO.setProductImage(item.getProduct().getImageUrl());
                    itemDTO.setProductCategoryName(item.getProduct().getCategory().getName());
                    itemDTO.setQuantity(item.getQuantity());
                    itemDTO.setPrice(item.getPrice());
                    itemDTO.setSubTotal(item.getPrice()
                            .multiply(BigDecimal.valueOf(item.getQuantity())));
                    return itemDTO;
                })
                .collect(Collectors.toList());
        dto.setOrderItems(itemDTOs);

        return dto;
    }
} 