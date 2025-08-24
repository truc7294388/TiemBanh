package com.bakeryshop.controller.admin;

import com.bakeryshop.admin.service.AdminUserService;
import com.bakeryshop.dto.UserDTO;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;

@Controller
@RequestMapping("/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {
    private final AdminUserService adminUserService;

    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    @GetMapping
    public String listUsers(@RequestParam(required = false) String keyword,
                          @RequestParam(defaultValue = "0") int page,
                          @RequestParam(defaultValue = "10") int size,
                          Model model) {
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<UserDTO> usersPage = adminUserService.getAllUsers(keyword, pageRequest);
        
        model.addAttribute("users", usersPage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalUsers", adminUserService.getTotalUsers());
        model.addAttribute("activeUsers", adminUserService.getTotalActiveUsers());
        model.addAttribute("blockedUsers", adminUserService.getTotalBlockedUsers());
        return "admin/user/users";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("user", new UserDTO());
        return "admin/user/user-add";
    }

    @PostMapping("/add")
    public String addUser(@Valid @ModelAttribute("user") UserDTO userDTO,
                           BindingResult result,
                           RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "admin/user/user-add";
        }

        try {
            adminUserService.createUser(userDTO);
            redirectAttributes.addFlashAttribute("success", "Người dùng đã được tạo thành công");
            return "redirect:/admin/users";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
            return "admin/user/user-add";
        }
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        try {
            UserDTO user = adminUserService.getUserById(id);
            model.addAttribute("user", user);
            return "admin/user/user-edit";
        } catch (Exception e) {
            return "redirect:/admin/users";
        }
    }

    @PostMapping("/edit/{id}")
    public String editUser(@PathVariable Long id,
                           @Valid @ModelAttribute("user") UserDTO userDTO,
                           BindingResult result,
                           RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "admin/user/user-edit";
        }

        try {
            adminUserService.updateUser(id, userDTO);
            redirectAttributes.addFlashAttribute("success", "Người dùng đã được cập nhật thành công");
            return "redirect:/admin/users";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
            return "admin/user/user-edit";
        }
    }

    @PostMapping("/delete/{id}")
    public String deleteUser(@PathVariable Long id,
                           RedirectAttributes redirectAttributes) {
        try {
            adminUserService.deleteUser(id);
            redirectAttributes.addFlashAttribute("success", "Người dùng đã được xóa thành công");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/{id}/block")
    public String blockUser(@PathVariable Long id,
                          RedirectAttributes redirectAttributes) {
        try {
            adminUserService.blockUser(id);
            redirectAttributes.addFlashAttribute("success", "Người dùng đã bị khóa");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/{id}/unblock")
    public String unblockUser(@PathVariable Long id,
                            RedirectAttributes redirectAttributes) {
        try {
            adminUserService.unblockUser(id);
            redirectAttributes.addFlashAttribute("success", "Người dùng đã được mở khóa");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/{id}/role")
    public String setUserRole(@PathVariable Long id,
                            @RequestParam String role,
                            RedirectAttributes redirectAttributes) {
        try {
            adminUserService.setUserRole(id, role);
            redirectAttributes.addFlashAttribute("success", "Vai trò người dùng đã được cập nhật");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }
} 