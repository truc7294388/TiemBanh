package com.bakeryshop.controller.admin;

import com.bakeryshop.admin.service.AdminOrderService;
import com.bakeryshop.admin.service.AdminProductService;
import com.bakeryshop.entity.Order;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Controller
@RequestMapping("/admin/reports")
@PreAuthorize("hasRole('ADMIN')")
public class AdminReportController {
    private final AdminOrderService adminOrderService;
    private final AdminProductService adminProductService;

    public AdminReportController(AdminOrderService adminOrderService,
                               AdminProductService adminProductService) {
        this.adminOrderService = adminOrderService;
        this.adminProductService = adminProductService;
    }

    @GetMapping("/sales")
    public String salesReport(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            Model model) {
        
        // Nếu không có ngày được chọn, mặc định lấy 30 ngày gần nhất
        if (startDate == null) {
            startDate = LocalDate.now().minusDays(29);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }

        // Chuyển đổi LocalDate thành LocalDateTime để bao gồm cả ngày kết thúc
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        // Thống kê tổng quan
        model.addAttribute("totalRevenue", adminOrderService.getTotalRevenue());
        model.addAttribute("totalOrders", adminOrderService.getTotalOrders());
        model.addAttribute("deliveredOrders", adminOrderService.getOrdersByStatus(Order.OrderStatus.DELIVERED));
        model.addAttribute("averageOrderValue", adminOrderService.getAverageOrderValue());

        // Thống kê theo trạng thái đơn hàng
        Map<String, Double> revenueByStatus = new LinkedHashMap<>();
        revenueByStatus.put("Đã giao", adminOrderService.getRevenueByStatus(Order.OrderStatus.DELIVERED));
        revenueByStatus.put("Đang giao", adminOrderService.getRevenueByStatus(Order.OrderStatus.SHIPPING));
        revenueByStatus.put("Chờ xử lý", adminOrderService.getRevenueByStatus(Order.OrderStatus.PENDING));
        revenueByStatus.put("Đã hủy", adminOrderService.getRevenueByStatus(Order.OrderStatus.CANCELLED));
        model.addAttribute("revenueByStatus", revenueByStatus);

        // Thống kê theo phương thức thanh toán
        Map<String, Double> revenueByPayment = new LinkedHashMap<>();
        revenueByPayment.put("Đã thanh toán", adminOrderService.getRevenueByPaymentStatus(Order.PaymentStatus.PAID));
        revenueByPayment.put("Chưa thanh toán", adminOrderService.getRevenueByPaymentStatus(Order.PaymentStatus.PENDING));
        model.addAttribute("revenueByPayment", revenueByPayment);

        // Thêm ngày bắt đầu và kết thúc vào model
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);

        return "admin/reports/sales";
    }

    @GetMapping("/inventory")
    public String inventoryReport(Model model) {
        model.addAttribute("totalProducts", adminProductService.getTotalProducts());
        model.addAttribute("activeProducts", adminProductService.getTotalActiveProducts());
        model.addAttribute("inactiveProducts", adminProductService.getTotalInactiveProducts());
        model.addAttribute("lowStockProducts", adminProductService.getLowStockProducts(10, null));
        return "admin/reports/inventory";
    }
} 