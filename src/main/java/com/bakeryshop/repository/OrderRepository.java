package com.bakeryshop.repository;

import com.bakeryshop.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {
    // Paginated queries
    Page<Order> findByUserId(Long userId, Pageable pageable);
    Page<Order> findByStatus(Order.OrderStatus status, Pageable pageable);
    Page<Order> findByUserEmailContainingOrUserPhoneContaining(String email, String phone, Pageable pageable);
    Page<Order> findAllByOrderByCreatedAtDesc(Pageable pageable);
    
    // List queries
    List<Order> findByStatus(Order.OrderStatus status);
    List<Order> findByStatusAndPaymentStatus(Order.OrderStatus status,Order.PaymentStatus PaymentStatus);
    List<Order> findByPaymentStatus(Order.PaymentStatus status);
    List<Order> findTop10ByOrderByCreatedAtDesc();

    // Count queries
    long countByStatus(Order.OrderStatus status);
    long countByPaymentStatus(Order.PaymentStatus status);
} 