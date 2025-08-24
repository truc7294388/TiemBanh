package com.bakeryshop.controller;

import com.bakeryshop.dto.OrderDTO;
import com.bakeryshop.service.OrderService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/order")
public class OrderResultController {
    private final OrderService orderService;

    public OrderResultController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/success/{orderId}")
    public String orderSuccess(@PathVariable Long orderId, Model model) {
        try {
            OrderDTO order = orderService.getOrderDTOById(orderId);
            model.addAttribute("order", order);
            return "order/success";
        } catch (Exception e) {
            return "redirect:/order/error";
        }
    }

    @GetMapping("/error")
    public String orderError() {
        return "order/error";
    }
} 