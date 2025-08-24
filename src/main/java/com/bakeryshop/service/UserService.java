package com.bakeryshop.service;

import com.bakeryshop.dto.SignUpRequest;
import com.bakeryshop.dto.UserDTO;
import com.bakeryshop.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {
    // User CRUD operations
    UserDTO createUser(UserDTO userDTO);
    User createUser(SignUpRequest signUpRequest);
    UserDTO getUserDTOById(Long id);
    User getUserById(Long id);
    User getUserByEmail(String email);
    Page<UserDTO> getAllUsers(Pageable pageable);
    Page<UserDTO> searchUsers(String keyword, Pageable pageable);
    UserDTO updateUser(Long id, UserDTO userDTO);
    void deleteUser(Long id);
    
    // Profile management
    void updateProfile(Long userId, String name, String phone, String address);
    void changePassword(Long userId, String oldPassword, String newPassword);
    
    // Email & verification
    boolean existsByEmail(String email);
    void verifyEmail(String token);
    void verifyUser(String token);
    void sendVerificationEmail(User user);
    
    // Password reset
    void forgotPassword(String email);
    void sendPasswordResetEmail(String email);
    void createPasswordResetTokenForUser(String email);
    void resetPassword(String token, String newPassword);
    void confirmResetPassword(String token, String newPassword);
    
    // Statistics & Dashboard
    long countTotalUsers();
} 