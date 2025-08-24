package com.bakeryshop.controller;

import com.bakeryshop.dto.OrderDTO;
import com.bakeryshop.entity.Order;
import com.bakeryshop.entity.User;
import com.bakeryshop.service.OrderService;
import com.bakeryshop.service.PaymentService;
import com.bakeryshop.service.UserService;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/orders")
public class WebOrderController {
    private final OrderService orderService;
    private final UserService userService;
    private final PaymentService paymentService;

    public WebOrderController(OrderService orderService, UserService userService, PaymentService paymentService) {
        this.orderService = orderService;
        this.userService = userService;
        this.paymentService = paymentService;
    }

    @GetMapping
    public String viewOrders(@AuthenticationPrincipal UserDetails userDetails,
                           @RequestParam(required = false) String keyword,
                           @RequestParam(required = false) Order.OrderStatus status,
                           @RequestParam(required = false) Order.PaymentStatus paymentStatus,
                           @RequestParam(required = false) Order.PaymentMethod paymentMethod,
                           @RequestParam(defaultValue = "0") int page,
                           @RequestParam(defaultValue = "10") int size,
                           Model model) {
        String email = userDetails.getUsername(); // email is stored as username in UserDetails
        User user = userService.getUserByEmail(email);
        
        // Add search parameters to model for form
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("selectedPaymentStatus", paymentStatus);
        model.addAttribute("selectedPaymentMethod", paymentMethod);
        
        // Add enum values for dropdowns
        model.addAttribute("orderStatuses", Order.OrderStatus.values());
        model.addAttribute("paymentStatuses", Order.PaymentStatus.values());
        model.addAttribute("paymentMethods", Order.PaymentMethod.values());
        
        // Get filtered orders
        model.addAttribute("orders", 
            orderService.searchOrders(user.getId(), keyword, status, paymentStatus, paymentMethod,
                                    PageRequest.of(page, size)));
        
        return "order/orders";
    }

    @GetMapping("/{id}")
    public String viewOrderDetail(@AuthenticationPrincipal UserDetails userDetails,
                                @PathVariable Long id,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        try {
            String email = userDetails.getUsername();
            User user = userService.getUserByEmail(email);
            
            // Get order
            OrderDTO order = orderService.getOrderDTOById(id);
            
            // Check if user has access to this order
            if (!user.getRole().equals("ADMIN") && !order.getUserId().equals(user.getId())) {
                redirectAttributes.addFlashAttribute("error", "Bạn không có quyền xem đơn hàng này");
                return "redirect:/orders";
            }
            
            model.addAttribute("order", order);
            return "order/order-detail";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy đơn hàng");
            return "redirect:/orders";
        }
    }

    @PostMapping("/{id}/cancel")
    public String cancelOrder(@AuthenticationPrincipal UserDetails userDetails,
                            @PathVariable Long id,
                            @RequestParam(required = false) String reason,
                            RedirectAttributes redirectAttributes) {
        try {
            String email = userDetails.getUsername();
            User user = userService.getUserByEmail(email);
            
            // Get order
            OrderDTO order = orderService.getOrderDTOById(id);
            
            // Check if user has access to this order
            if (!user.getRole().equals("ADMIN") && !order.getUserId().equals(user.getId())) {
                redirectAttributes.addFlashAttribute("error", "Bạn không có quyền hủy đơn hàng này");
                return "redirect:/orders";
            }
            
            orderService.cancelOrder(id, reason);
            redirectAttributes.addFlashAttribute("success", "Đã hủy đơn hàng thành công");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/orders";
    }

    @PostMapping("/create")
    public String createOrder(@AuthenticationPrincipal UserDetails userDetails,
                            @ModelAttribute OrderDTO orderDTO,
                            RedirectAttributes redirectAttributes) {
        try {
            String email = userDetails.getUsername();
            User user = userService.getUserByEmail(email);
            
            // Create order
            OrderDTO createdOrder = orderService.createOrder(user.getId(), orderDTO);
            
            // If payment method is VNPAY, redirect to payment page
            if (Order.PaymentMethod.VNPAY.equals(createdOrder.getPaymentMethod())) {
                String paymentUrl = paymentService.createPaymentUrl(
                    orderService.findById(createdOrder.getId())
                );
                return "redirect:" + paymentUrl;
            }
            
            // For other payment methods, redirect to order detail
            redirectAttributes.addFlashAttribute("success", "Đặt hàng thành công");
            return "redirect:/orders/" + createdOrder.getId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi đặt hàng: " + e.getMessage());
            return "redirect:/cart";
        }
    }
} 