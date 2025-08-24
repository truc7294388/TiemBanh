package com.bakeryshop.controller;

import com.bakeryshop.dto.CartItemDTO;
import com.bakeryshop.service.CartService;
import com.bakeryshop.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cart")
public class CartController {
    private final CartService cartService;
    private final UserService userService;

    public CartController(CartService cartService, UserService userService) {
        this.cartService = cartService;
        this.userService = userService;
    }

    @PostMapping("/add")
    public ResponseEntity<?> addToCart(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam Long productId,
            @RequestParam(defaultValue = "1") @Min(1) Integer quantity) {
        try {
            Long userId = userService.getUserByEmail(userDetails.getUsername()).getId();
            cartService.addToCart(userId, productId, quantity);
            
            // Return cart item count for UI update
            int cartItemCount = cartService.getCartItemCount(userId);
            Map<String, Object> response = new HashMap<>();
            response.put("cartItemCount", cartItemCount);
            response.put("message", "Sản phẩm đã được thêm vào giỏ hàng!");
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PutMapping("/items/{cartItemId}")
    public ResponseEntity<?> updateCartItem(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long cartItemId,
            @RequestParam @Min(1) Integer quantity) {
        try {
            Long userId = userService.getUserByEmail(userDetails.getUsername()).getId();
            cartService.updateCartItem(userId, cartItemId, quantity);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @DeleteMapping("/items/{cartItemId}")
    public ResponseEntity<?> removeFromCart(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long cartItemId) {
        try {
            Long userId = userService.getUserByEmail(userDetails.getUsername()).getId();
            cartService.removeFromCart(userId, cartItemId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @DeleteMapping("/clear")
    public ResponseEntity<?> clearCart(
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            Long userId = userService.getUserByEmail(userDetails.getUsername()).getId();
            cartService.clearCart(userId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/items")
    public ResponseEntity<?> getCartItems(
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            Long userId = userService.getUserByEmail(userDetails.getUsername()).getId();
            List<CartItemDTO> items = cartService.getCartItems(userId);
            return ResponseEntity.ok(items);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/count")
    public ResponseEntity<?> getCartItemCount(
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            Long userId = userService.getUserByEmail(userDetails.getUsername()).getId();
            return ResponseEntity.ok(cartService.getCartItemCount(userId));
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
} 