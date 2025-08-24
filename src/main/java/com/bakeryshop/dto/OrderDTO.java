package com.bakeryshop.dto;

import com.bakeryshop.entity.Order;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderDTO {
    private Long id;

    private Long userId;

    @NotEmpty(message = "Order items cannot be empty")
    private List<OrderItemDTO> orderItems;

    private BigDecimal totalAmount;

    private Order.OrderStatus status;

    private Order.PaymentMethod paymentMethod;

    private Order.PaymentStatus paymentStatus;

    private String paymentId;

    @NotBlank(message = "Shipping address is required")
    private String shippingAddress;

    @NotBlank(message = "Shipping phone is required")
    private String shippingPhone;

    @NotBlank(message = "Shipping name is required")
    private String shippingName;

    private String note;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
} 