package com.bakeryshop.controller;

import com.bakeryshop.dto.OrderDTO;
import com.bakeryshop.entity.Order;
import com.bakeryshop.service.OrderService;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<OrderDTO> createOrder(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody OrderDTO orderDTO) {
        return ResponseEntity.ok(orderService.createOrder(
                Long.parseLong(userDetails.getUsername()),
                orderDTO));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderDTO> getOrder(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long orderId) {
        return ResponseEntity.ok(orderService.getOrderDTOById(orderId));
    }

    @GetMapping("/my-orders")
    public ResponseEntity<Page<OrderDTO>> getUserOrders(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Order.OrderStatus status,
            @RequestParam(required = false) Order.PaymentStatus paymentStatus,
            @RequestParam(required = false) Order.PaymentMethod paymentMethod,
            Pageable pageable) {
        return ResponseEntity.ok(orderService.searchOrders(
                Long.parseLong(userDetails.getUsername()),
                keyword, status, paymentStatus, paymentMethod,
                pageable));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<OrderDTO>> getAllOrders(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Order.OrderStatus status,
            @RequestParam(required = false) Order.PaymentStatus paymentStatus,
            @RequestParam(required = false) Order.PaymentMethod paymentMethod,
            Pageable pageable) {
        return ResponseEntity.ok(orderService.searchOrders(
                null, keyword, status, paymentStatus, paymentMethod,
                pageable));
    }

    @PutMapping("/{orderId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestParam Order.OrderStatus status,
            @RequestParam(required = false) String note) {
        orderService.updateOrderStatus(orderId, status, note);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{orderId}/payment-status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> updatePaymentStatus(
            @PathVariable Long orderId,
            @RequestParam Order.PaymentStatus status,
            @RequestParam(required = false) String note) {
        orderService.updatePaymentStatus(orderId, status, note);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<Void> cancelOrder(
            @PathVariable Long orderId,
            @RequestParam(required = false) String reason) {
        orderService.cancelOrder(orderId, reason);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{orderId}/invoice")
    public ResponseEntity<Resource> downloadInvoice(@PathVariable Long orderId) {
        byte[] invoice = orderService.generateOrderInvoice(orderId);
        ByteArrayResource resource = new ByteArrayResource(invoice);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=invoice-" + orderId + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(invoice.length)
                .body(resource);
    }
} 