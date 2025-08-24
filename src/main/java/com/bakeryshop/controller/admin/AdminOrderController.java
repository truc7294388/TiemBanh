package com.bakeryshop.controller.admin;

import com.bakeryshop.admin.service.AdminOrderService;
import com.bakeryshop.dto.OrderDTO;
import com.bakeryshop.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/orders")
@PreAuthorize("hasRole('ADMIN')")
public class AdminOrderController {
    private final AdminOrderService adminOrderService;

    public AdminOrderController(AdminOrderService adminOrderService) {
        this.adminOrderService = adminOrderService;
    }

    @GetMapping
    public String listOrders(@RequestParam(defaultValue = "0") int page,
                           @RequestParam(defaultValue = "10") int size,
                           @RequestParam(required = false) String status,
                           @RequestParam(required = false) String keyword,
                           Model model) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<OrderDTO> ordersPage = adminOrderService.getAllOrders(keyword, pageRequest);

        model.addAttribute("orders", ordersPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", ordersPage.getTotalPages());
        model.addAttribute("selectedStatus", status);
        model.addAttribute("keyword", keyword);

        // Add order statistics
        model.addAttribute("totalOrders", adminOrderService.getTotalOrders());
        model.addAttribute("pendingOrders", adminOrderService.getOrdersByStatus(Order.OrderStatus.PENDING));
        model.addAttribute("shippingOrders", adminOrderService.getOrdersByStatus(Order.OrderStatus.SHIPPING));
        model.addAttribute("deliveredOrders", adminOrderService.getOrdersByStatus(Order.OrderStatus.DELIVERED));
        model.addAttribute("cancelledOrders", adminOrderService.getOrdersByStatus(Order.OrderStatus.CANCELLED));

        // Add payment statistics
        model.addAttribute("paidOrders", adminOrderService.getOrdersByPaymentStatus(Order.PaymentStatus.PAID));

        // Add revenue statistics
        model.addAttribute("totalRevenue", adminOrderService.getTotalRevenue());
        model.addAttribute("deliveredRevenue", adminOrderService.getRevenueByStatus(Order.OrderStatus.DELIVERED));
        model.addAttribute("paidRevenue", adminOrderService.getRevenueByPaymentStatus(Order.PaymentStatus.PAID));

        return "admin/orders";
    }

    @GetMapping("/{id}")
    public String orderDetail(@PathVariable Long id, Model model) {
        model.addAttribute("order", adminOrderService.getOrderById(id));
        return "admin/order-detail";
    }

    @PostMapping("/{id}/status")
    public String updateOrderStatus(@PathVariable Long id,
                                  @RequestParam Order.OrderStatus status,
                                  @RequestParam(required = false) String note,
                                  RedirectAttributes redirectAttributes) {
        try {
            adminOrderService.updateOrderStatus(id, status, note);
            redirectAttributes.addFlashAttribute("success", "Trạng thái đơn hàng đã được cập nhật thành công");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }

        return "redirect:/admin/orders/" + id;
    }

    @PostMapping("/{id}/payment-status")
    public String updatePaymentStatus(@PathVariable Long id,
                                    @RequestParam Order.PaymentStatus status,
                                    @RequestParam(required = false) String note,
                                    RedirectAttributes redirectAttributes) {
        try {
            adminOrderService.updatePaymentStatus(id, status, note);
            redirectAttributes.addFlashAttribute("success", "Trạng thái thanh toán đã được cập nhật thành công");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }

        return "redirect:/admin/orders/" + id;
    }

    @PostMapping("/process")
    public String processOrder(@RequestParam Long orderId,
                             RedirectAttributes redirectAttributes) {
        try {
            adminOrderService.updateOrderStatus(orderId, Order.OrderStatus.CONFIRMED, "Đơn hàng đã được xác nhận");
            redirectAttributes.addFlashAttribute("success", "Đơn hàng đã được xác nhận thành công");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/orders";
    }

    @PostMapping("/cancel")
    public String cancelOrder(@RequestParam Long orderId,
                            @RequestParam String reason,
                            RedirectAttributes redirectAttributes) {
        try {
            adminOrderService.updateOrderStatus(orderId, Order.OrderStatus.CANCELLED, reason);
            redirectAttributes.addFlashAttribute("success", "Đơn hàng đã được hủy thành công");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/orders";
    }

    @PostMapping("/{id}/delete")
    public String deleteOrder(@PathVariable Long id,
                            RedirectAttributes redirectAttributes) {
        try {
            adminOrderService.deleteOrder(id);
            redirectAttributes.addFlashAttribute("success", "Đơn hàng đã được xóa thành công");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }

        return "redirect:/admin/orders";
    }

    @GetMapping("/export")
    public String exportOrders(@RequestParam(required = false) String status,
                             @RequestParam(required = false) String startDate,
                             @RequestParam(required = false) String endDate) {
        // TODO: Implement export orders to Excel/CSV
        return "redirect:/admin/orders";
    }
} 