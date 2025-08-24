package com.bakeryshop.service;

import com.bakeryshop.dto.CartItemDTO;

import java.util.List;

public interface CartService {
    void addToCart(Long userId, Long productId, Integer quantity);
    
    void updateCartItem(Long userId, Long cartItemId, Integer quantity);
    
    void removeFromCart(Long userId, Long cartItemId);
    
    void clearCart(Long userId);
    
    List<CartItemDTO> getCartItems(Long userId);
    
    Integer getCartItemCount(Long userId);
    
    Double calculateTotal(Long userId);
} 