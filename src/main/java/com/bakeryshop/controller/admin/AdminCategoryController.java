package com.bakeryshop.controller.admin;

import com.bakeryshop.dto.CategoryDTO;
import com.bakeryshop.service.CategoryService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;

@Controller
@RequestMapping("/admin/categories")
@PreAuthorize("hasRole('ADMIN')")
public class AdminCategoryController {
    private final CategoryService categoryService;

    public AdminCategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public String listCategories(Model model) {
        model.addAttribute("categories", categoryService.getAllCategories());
        return "admin/categories";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("category", new CategoryDTO());
        return "admin/category-add";
    }

    @PostMapping("/create")
    public String createCategory(@Valid @ModelAttribute("category") CategoryDTO categoryDTO,
                               @RequestParam(value = "image", required = false) MultipartFile image,
                               BindingResult result,
                               RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "admin/category-add";
        }

        try {
            categoryService.createCategory(categoryDTO, image);
            redirectAttributes.addFlashAttribute("success", "Danh mục đã được tạo thành công");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
            return "admin/category-add";
        }

        return "redirect:/admin/categories";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        try {
            CategoryDTO category = categoryService.getCategoryById(id);
            model.addAttribute("category", category);
            return "admin/category-edit";
        } catch (Exception e) {
            return "redirect:/admin/categories";
        }
    }

    @PostMapping("/edit/{id}")
    public String updateCategory(@PathVariable Long id,
                               @Valid @ModelAttribute("category") CategoryDTO categoryDTO,
                               @RequestParam(value = "image", required = false) MultipartFile image,
                               BindingResult result,
                               RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "admin/category-edit";
        }

        try {
            categoryService.updateCategory(id, categoryDTO, image);
            redirectAttributes.addFlashAttribute("success", "Danh mục đã được cập nhật thành công");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
            return "admin/category-edit";
        }

        return "redirect:/admin/categories";
    }

    @PostMapping("/delete/{id}")
    public String deleteCategory(@PathVariable Long id,
                               RedirectAttributes redirectAttributes) {
        try {
            categoryService.deleteCategory(id);
            redirectAttributes.addFlashAttribute("success", "Danh mục đã được xóa thành công");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }

        return "redirect:/admin/categories";
    }

    @PostMapping("/{id}/featured")
    public String toggleFeatured(@PathVariable Long id,
                               @RequestParam boolean featured,
                               RedirectAttributes redirectAttributes) {
        try {
            categoryService.setFeatured(id, featured);
            String message = featured ? "Danh mục đã được đặt làm nổi bật" : "Danh mục đã bỏ nổi bật";
            redirectAttributes.addFlashAttribute("success", message);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }

        return "redirect:/admin/categories";
    }
} 