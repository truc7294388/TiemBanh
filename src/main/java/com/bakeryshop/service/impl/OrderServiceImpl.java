package com.bakeryshop.service.impl;

import com.bakeryshop.dto.OrderDTO;
import com.bakeryshop.dto.OrderItemDTO;
import com.bakeryshop.entity.Order;
import com.bakeryshop.entity.OrderItem;
import com.bakeryshop.entity.Product;
import com.bakeryshop.entity.User;
import com.bakeryshop.exception.ResourceNotFoundException;
import com.bakeryshop.repository.OrderRepository;
import com.bakeryshop.repository.ProductRepository;
import com.bakeryshop.repository.UserRepository;
import com.bakeryshop.service.CartService;
import com.bakeryshop.service.EmailService;
import com.bakeryshop.service.OrderService;
import com.bakeryshop.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final ProductService productService;
    private final CartService cartService;
    private final EmailService emailService;

    public OrderServiceImpl(OrderRepository orderRepository,
                          UserRepository userRepository,
                          ProductRepository productRepository,
                          ProductService productService,
                          CartService cartService,
                          EmailService emailService) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.productService = productService;
        this.cartService = cartService;
        this.emailService = emailService;
    }

    @Override
    @Transactional
    public OrderDTO createOrder(Long userId, OrderDTO orderDTO) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Validate order items
        if (orderDTO.getOrderItems() == null || orderDTO.getOrderItems().isEmpty()) {
            throw new RuntimeException("Order must have at least one item");
        }

        // Create new order
        Order order = new Order();
        order.setUser(user);
        order.setStatus(Order.OrderStatus.PENDING);
        order.setPaymentStatus(Order.PaymentStatus.PENDING);
        order.setPaymentMethod(orderDTO.getPaymentMethod());
        order.setShippingAddress(orderDTO.getShippingAddress());
        order.setShippingPhone(orderDTO.getShippingPhone());
        order.setShippingName(orderDTO.getShippingName());
        order.setNote(orderDTO.getNote());
        order.setCreatedAt(LocalDateTime.now());

        // Calculate total and validate stock
        BigDecimal total = BigDecimal.ZERO;
        for (OrderItemDTO itemDTO : orderDTO.getOrderItems()) {
            Product product = productRepository.findById(itemDTO.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found: " + itemDTO.getProductId()));

            // Check stock
            if (product.getStock() < itemDTO.getQuantity()) {
                throw new RuntimeException("Insufficient stock for product: " + product.getName() 
                    + " (Available: " + product.getStock() + ", Required: " + itemDTO.getQuantity() + ")");
            }

            // Create order item
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(itemDTO.getQuantity());
            orderItem.setPrice(product.getPrice());
            orderItem.setSubTotal(product.getPrice().multiply(BigDecimal.valueOf(itemDTO.getQuantity())));
            order.getItems().add(orderItem);

            // Update total
            total = total.add(orderItem.getSubTotal());

            // Update stock
            product.setStock(product.getStock() - itemDTO.getQuantity());
            productRepository.save(product);
        }

        // Set total amount
        order.setTotalAmount(total);

        // Save order
        Order savedOrder = orderRepository.save(order);

        // Clear cart
        cartService.clearCart(userId);

        // Send confirmation email
        try {
            emailService.sendOrderConfirmationEmail(savedOrder);
        } catch (Exception e) {
            // Log error but don't fail the order
            e.printStackTrace();
        }

        // If payment method is VNPAY, set payment status to PENDING
        if (Order.PaymentMethod.VNPAY.equals(savedOrder.getPaymentMethod())) {
            savedOrder.setPaymentStatus(Order.PaymentStatus.PENDING);
            orderRepository.save(savedOrder);
        }

        return convertToDTO(savedOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public Order findById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
    }

    @Override
    @Transactional(readOnly = true)
    public OrderDTO getOrderDTOById(Long orderId) {
        return convertToDTO(findById(orderId));
    }

    @Override
    @Transactional
    public void updateOrderStatus(Long orderId, Order.OrderStatus status, String note) {
        Order order = findById(orderId);
        String oldStatus = order.getStatus().toString();
        order.setStatus(status);
        order.setNote(note);
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);
        
        // Send notification email
        emailService.sendOrderStatusUpdateEmail(order, oldStatus, status.toString());
    }

    @Override
    @Transactional
    public void updatePaymentStatus(Long orderId, Order.PaymentStatus status, String note) {
        Order order = findById(orderId);
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
    public void updatePaymentMethod(Long orderId, Order.PaymentMethod method) {
        Order order = findById(orderId);
        order.setPaymentMethod(method);
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);
    }

    @Override
    @Transactional
    public void updateTransactionNo(Long orderId, String transactionNo) {
        Order order = findById(orderId);
        order.setTransactionNo(transactionNo);
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);
    }

    @Override
    @Transactional
    public void cancelOrder(Long orderId, String reason) {
        Order order = findById(orderId);
        String oldStatus = order.getStatus().toString();
        order.setStatus(Order.OrderStatus.CANCELLED);
        order.setNote(reason);
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);
        
        // Send cancellation email
        emailService.sendOrderStatusUpdateEmail(order, oldStatus, Order.OrderStatus.CANCELLED.toString());
        
        // Return products to stock
        order.getItems().forEach(item -> {
            Product product = item.getProduct();
            product.setStock(product.getStock() + item.getQuantity());
            productRepository.save(product);
        });
    }

    @Override
    @Transactional
    public void deleteOrder(Long orderId) {
        Order order = findById(orderId);
        orderRepository.delete(order);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderDTO> getUserOrders(Long userId, Pageable pageable) {
        return orderRepository.findByUserId(userId, pageable)
                .map(this::convertToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderDTO> getAllOrders(Pageable pageable) {
        return orderRepository.findAll(pageable)
                .map(this::convertToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderDTO> searchOrders(Long userId, String keyword, 
            Order.OrderStatus status, Order.PaymentStatus paymentStatus, 
            Order.PaymentMethod paymentMethod, Pageable pageable) {
        
        Specification<Order> spec = Specification.where(null);
        
        if (userId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("user").get("id"), userId));
        }
        
        if (keyword != null && !keyword.trim().isEmpty()) {
            spec = spec.and((root, query, cb) -> 
                cb.or(
                    cb.like(cb.lower(root.get("user").get("email")), "%" + keyword.toLowerCase() + "%"),
                    cb.like(cb.lower(root.get("user").get("phone")), "%" + keyword.toLowerCase() + "%")
                )
            );
        }
        
        if (status != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        }
        
        if (paymentStatus != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("paymentStatus"), paymentStatus));
        }
        
        if (paymentMethod != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("paymentMethod"), paymentMethod));
        }
        
        return orderRepository.findAll(spec, pageable)
                .map(this::convertToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderDTO> getOrdersByStatus(String status, Pageable pageable) {
        if (status == null || status.trim().isEmpty()) {
            return getAllOrders(pageable);
        }
        return orderRepository.findByStatus(Order.OrderStatus.valueOf(status.toUpperCase()), pageable)
                .map(this::convertToDTO);
    }

    @Override
    public byte[] generateOrderInvoice(Long orderId) {
        Order order = findById(orderId);
        // Implementation for generating PDF invoice
        return new byte[0]; // Placeholder
    }

    @Override
    public long countTotalOrders() {
        return orderRepository.count();
    }

    @Override
    public double calculateTotalRevenue() {
        return orderRepository.findByStatusAndPaymentStatus(Order.OrderStatus.CONFIRMED,Order.PaymentStatus.PAID)
                .stream()
                .mapToDouble(order -> order.getTotalAmount().doubleValue())
                .sum();
    }

    @Override
    public List<Order> getRecentOrders() {
        return orderRepository.findTop10ByOrderByCreatedAtDesc();
    }

    @Override
    public List<Order> getOrdersByStatus(Order.OrderStatus status) {
        return orderRepository.findByStatus(status);
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