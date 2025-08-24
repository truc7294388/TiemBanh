package com.bakeryshop.repository;

import com.bakeryshop.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    
    boolean existsByEmail(String email);
    
    Optional<User> findByVerificationToken(String token);
    
    Optional<User> findByResetPasswordToken(String token);
    
    Page<User> findByEmailContainingOrPhoneContaining(String email, String phone, Pageable pageable);

    Page<User> findByNameContainingOrEmailContainingOrPhoneContaining(
            String name, String email, String phone, Pageable pageable);
    
    long countByBlockedFalse();
    
    long countByBlockedTrue();
} 