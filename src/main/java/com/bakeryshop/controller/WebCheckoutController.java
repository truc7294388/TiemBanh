package com.bakeryshop.controller;

import com.bakeryshop.dto.CartItemDTO;
import com.bakeryshop.dto.OrderDTO;
import com.bakeryshop.dto.OrderItemDTO;
import com.bakeryshop.entity.Order;
import com.bakeryshop.entity.User;
import com.bakeryshop.service.CartService;
import com.bakeryshop.service.OrderService;
import com.bakeryshop.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/checkout")
public class WebCheckoutController {
    private final CartService cartService;
    private final UserService userService;
    private final OrderService orderService;

    public WebCheckoutController(CartService cartService, UserService userService, OrderService orderService) {
        this.cartService = cartService;
        this.userService = userService;
        this.orderService = orderService;
    }

    @GetMapping
    public String showCheckoutPage(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails == null) {
            return "redirect:/login?redirect=/checkout";
        }

        try {
            User user = userService.getUserByEmail(userDetails.getUsername());
            Long userId = user.getId();

            // Check if cart is empty
            if (cartService.getCartItems(userId).isEmpty()) {
                return "redirect:/cart";
            }

            // Add cart items and totals to model
            model.addAttribute("cartItems", cartService.getCartItems(userId));
            model.addAttribute("totalAmount", cartService.calculateTotal(userId));
            model.addAttribute("shippingFee", 0); // Free shipping
            model.addAttribute("user", user);

            return "cart/checkout";
        } catch (Exception e) {
            model.addAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
            return "redirect:/cart";
        }
    }

    @PostMapping
    public String processCheckout(@AuthenticationPrincipal UserDetails userDetails,
                                @RequestParam String name,
                                @RequestParam String email,
                                @RequestParam String phone,
                                @RequestParam String address,
                                @RequestParam(required = false) String note,
                                @RequestParam Order.PaymentMethod paymentMethod,
                                RedirectAttributes redirectAttributes) {
        if (userDetails == null) {
            return "redirect:/login?redirect=/checkout";
        }

        try {
            User user = userService.getUserByEmail(userDetails.getUsername());
            Long userId = user.getId();

            // Get cart items and convert to OrderItemDTO
            List<CartItemDTO> cartItems = cartService.getCartItems(userId);
            List<OrderItemDTO> orderItems = cartItems.stream()
                .map(cartItem -> {
                    OrderItemDTO orderItem = new OrderItemDTO();
                    orderItem.setProductId(cartItem.getProductId());
                    orderItem.setQuantity(cartItem.getQuantity());
                    orderItem.setPrice(cartItem.getPrice());
                    return orderItem;
                })
                .collect(Collectors.toList());

            // Create OrderDTO
            OrderDTO orderDTO = new OrderDTO();
            orderDTO.setShippingName(name);
            orderDTO.setShippingPhone(phone);
            orderDTO.setShippingAddress(address);
            orderDTO.setNote(note);
            orderDTO.setPaymentMethod(paymentMethod);
            orderDTO.setOrderItems(orderItems);

            // Create order
            OrderDTO createdOrder = orderService.createOrder(userId, orderDTO);

            // Handle different payment methods
            if (paymentMethod == Order.PaymentMethod.VNPAY) {
                return "redirect:/vnpay/create-payment/" + createdOrder.getId();
            } else {
                redirectAttributes.addFlashAttribute("success", "Đặt hàng thành công!");
                return "redirect:/orders/" + createdOrder.getId();
            }

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra khi đặt hàng: " + e.getMessage());
            return "redirect:/checkout";
        }
    }
} 