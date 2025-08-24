package com.bakeryshop.controller.admin;

import com.bakeryshop.service.OrderService;
import com.bakeryshop.service.ProductService;
import com.bakeryshop.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminDashboardController {

    private final OrderService orderService;
    private final ProductService productService;
    private final UserService userService;

    @Autowired
    public AdminDashboardController(OrderService orderService,
                                  ProductService productService,
                                  UserService userService) {
        this.orderService = orderService;
        this.productService = productService;
        this.userService = userService;
    }

    @GetMapping({"", "/"})
    public String dashboard(Model model) {
        // Thống kê tổng quan
        model.addAttribute("totalOrders", orderService.countTotalOrders());
        model.addAttribute("totalProducts", productService.countTotalProducts());
        model.addAttribute("totalUsers", userService.countTotalUsers());
        model.addAttribute("totalRevenue", orderService.calculateTotalRevenue());

        // Đơn hàng gần đây
        model.addAttribute("recentOrders", orderService.getRecentOrders());

        // Sản phẩm sắp hết hàng
        model.addAttribute("lowStockProducts", productService.getLowStockProducts());

        return "admin/dashboard";
    }
} 