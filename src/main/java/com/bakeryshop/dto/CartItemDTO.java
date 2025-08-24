package com.bakeryshop.dto;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class CartItemDTO {
    private Long id;

    @NotNull(message = "Product ID is required")
    private Long productId;

    private String productName;

    private String productImage;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    private BigDecimal price;

    private BigDecimal subTotal;
} 