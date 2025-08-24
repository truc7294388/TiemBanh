package com.bakeryshop.controller;

import com.bakeryshop.service.CartService;
import com.bakeryshop.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/cart")
public class WebCartController {
    private final CartService cartService;
    private final UserService userService;

    public WebCartController(CartService cartService, UserService userService) {
        this.cartService = cartService;
        this.userService = userService;
    }

    @GetMapping
    public String viewCart(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails == null) {
            return "redirect:/login?redirect=/cart";
        }
        
        try {
            Long userId = userService.getUserByEmail(userDetails.getUsername()).getId();
            model.addAttribute("cartItems", cartService.getCartItems(userId));
            model.addAttribute("totalAmount", cartService.calculateTotal(userId));
            return "cart/cart";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "cart/cart";
        }
    }

    @PostMapping("/add")
    public String addToCart(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam Long productId,
            @RequestParam(defaultValue = "1") Integer quantity,
            RedirectAttributes redirectAttributes) {
        if (userDetails == null) {
            return "redirect:/login?redirect=/products/" + productId;
        }
        
        try {
            Long userId = userService.getUserByEmail(userDetails.getUsername()).getId();
            cartService.addToCart(userId, productId, quantity);
            redirectAttributes.addFlashAttribute("success", "Sản phẩm đã được thêm vào giỏ hàng");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/products/" + productId;
    }

    @PostMapping("/update")
    public String updateCartItem(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam Long cartItemId,
            @RequestParam Integer quantity,
            RedirectAttributes redirectAttributes) {
        if (userDetails == null) {
            return "redirect:/login?redirect=/cart";
        }
        
        try {
            Long userId = userService.getUserByEmail(userDetails.getUsername()).getId();
            cartService.updateCartItem(userId, cartItemId, quantity);
            redirectAttributes.addFlashAttribute("success", "Giỏ hàng đã được cập nhật");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/cart";
    }

    @PostMapping("/remove")
    public String removeFromCart(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam Long cartItemId,
            RedirectAttributes redirectAttributes) {
        if (userDetails == null) {
            return "redirect:/login?redirect=/cart";
        }
        
        try {
            Long userId = userService.getUserByEmail(userDetails.getUsername()).getId();
            cartService.removeFromCart(userId, cartItemId);
            redirectAttributes.addFlashAttribute("success", "Sản phẩm đã được xóa khỏi giỏ hàng");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/cart";
    }

    @PostMapping("/clear")
    public String clearCart(
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        if (userDetails == null) {
            return "redirect:/login?redirect=/cart";
        }
        
        try {
            Long userId = userService.getUserByEmail(userDetails.getUsername()).getId();
            cartService.clearCart(userId);
            redirectAttributes.addFlashAttribute("success", "Giỏ hàng đã được xóa");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/cart";
    }
} 