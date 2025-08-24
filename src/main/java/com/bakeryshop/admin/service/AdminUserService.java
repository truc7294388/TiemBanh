package com.bakeryshop.admin.service;

import com.bakeryshop.dto.UserDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminUserService {
    // User CRUD operations
    UserDTO createUser(UserDTO userDTO);
    UserDTO updateUser(Long id, UserDTO userDTO);
    void deleteUser(Long id);
    UserDTO getUserById(Long id);
    Page<UserDTO> getAllUsers(String keyword, Pageable pageable);
    
    // User Statistics
    long getTotalUsers();
    long getTotalActiveUsers();
    long getTotalBlockedUsers();
    
    // User Status Management
    void blockUser(Long id);
    void unblockUser(Long id);
    void setUserRole(Long id, String role);
} 