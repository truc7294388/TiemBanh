package com.bakeryshop.controller;

import com.bakeryshop.entity.Order;
import com.bakeryshop.service.OrderService;
import com.bakeryshop.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

@Controller
@RequestMapping("/vnpay")
public class PaymentController {
    private final PaymentService paymentService;
    private final OrderService orderService;

    public PaymentController(PaymentService paymentService, OrderService orderService) {
        this.paymentService = paymentService;
        this.orderService = orderService;
    }

    @GetMapping("/create-payment/{orderId}")
    public String createPaymentGet(@PathVariable Long orderId, RedirectAttributes redirectAttributes) {
        try {
            Order order = orderService.findById(orderId);
            if (order == null) {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy đơn hàng");
                return "redirect:/orders";
            }
            
            // Validate order amount
            if (order.getTotalAmount() == null || order.getTotalAmount().longValue() <= 0) {
                redirectAttributes.addFlashAttribute("error", "Số tiền đơn hàng không hợp lệ");
                return "redirect:/orders/" + orderId;
            }

            // Check if order is already paid
            if (Order.PaymentStatus.PAID.equals(order.getPaymentStatus())) {
                redirectAttributes.addFlashAttribute("error", "Đơn hàng đã được thanh toán");
                return "redirect:/orders/" + orderId;
            }

            String paymentUrl = paymentService.createPaymentUrl(order);

            return "redirect:" + paymentUrl;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi tạo thanh toán: " + e.getMessage());
            return "redirect:/orders/" + orderId;
        }
    }

    @PostMapping("/create-payment/{orderId}")
    public ResponseEntity<?> createPaymentPost(@PathVariable Long orderId) {
        try {
            Order order = orderService.findById(orderId);
            if (order == null) {
                return ResponseEntity.notFound().build();
            }
            
            // Validate order amount
            if (order.getTotalAmount() == null || order.getTotalAmount().longValue() <= 0) {
                return ResponseEntity.badRequest().body("Invalid order amount");
            }

            // Check if order is already paid
            if (Order.PaymentStatus.PAID.equals(order.getPaymentStatus())) {
                return ResponseEntity.badRequest().body("Order is already paid");
            }

            String paymentUrl = paymentService.createPaymentUrl(order);
            return ResponseEntity.ok(paymentUrl);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error creating payment: " + e.getMessage());
        }
    }

    @GetMapping("/return")
    public String paymentCallback(@RequestParam Map<String, String> queryParams,
                                RedirectAttributes redirectAttributes) {
        try {
            if (paymentService.validatePaymentResponse(queryParams)) {
                String orderId = queryParams.get("vnp_TxnRef");
                String vnpResponseCode = queryParams.get("vnp_ResponseCode");
                String transactionNo = queryParams.get("vnp_TransactionNo");

                if ("00".equals(vnpResponseCode)) {
                    // Update order payment status and transaction number
                    orderService.updatePaymentStatus(Long.parseLong(orderId), Order.PaymentStatus.PAID, "Payment completed via VNPay - Transaction: " + transactionNo);
//                    orderService.updateTransactionNo(Long.parseLong(orderId), transactionNo);
                    
                    redirectAttributes.addFlashAttribute("paymentSuccess", true);
                    return "redirect:/order/success/" + orderId;
                }
            }else{
                String orderId = queryParams.get("vnp_TxnRef");
                String vnpResponseCode = queryParams.get("vnp_ResponseCode");
                String transactionNo = queryParams.get("vnp_TransactionNo");
                orderService.updatePaymentStatus(Long.parseLong(orderId), Order.PaymentStatus.FAILED, "Payment completed via VNPay - Transaction: " + transactionNo);
//                orderService.updateTransactionNo(Long.parseLong(orderId), transactionNo);
                redirectAttributes.addFlashAttribute("paymentSuccess", true);
                return "redirect:/order/success/" + orderId;
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Payment processing error: " + e.getMessage());
        }
        
        redirectAttributes.addFlashAttribute("paymentError", true);
        return "redirect:/order/error";
    }
} 