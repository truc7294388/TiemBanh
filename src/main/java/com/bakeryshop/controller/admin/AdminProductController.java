package com.bakeryshop.controller.admin;

import com.bakeryshop.admin.service.AdminProductService;
import com.bakeryshop.dto.ProductDTO;
import com.bakeryshop.service.CategoryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;

@Controller
@RequestMapping("/admin/products")
@PreAuthorize("hasRole('ADMIN')")
public class AdminProductController {
    private final AdminProductService adminProductService;
    private final CategoryService categoryService;

    public AdminProductController(AdminProductService adminProductService, CategoryService categoryService) {
        this.adminProductService = adminProductService;
        this.categoryService = categoryService;
    }

    @GetMapping
    public String listProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {

        // Add categories for filter
        model.addAttribute("categories", categoryService.getAllCategories());

        // Get products with filters
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<ProductDTO> productsPage = adminProductService.getAllProducts(keyword, pageRequest);

        // Add products to model
        model.addAttribute("products", productsPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productsPage.getTotalPages());
        model.addAttribute("size", size);

        return "admin/product/products";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("categories", categoryService.getAllCategories());
        return "admin/product/product-add";
    }

    @PostMapping("/create")
    public String createProduct(
            @Valid @ModelAttribute("product") ProductDTO productDTO,
            BindingResult result,
            @RequestParam(value = "image", required = false) MultipartFile image,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            model.addAttribute("categories", categoryService.getAllCategories());
            return "admin/product/product-add";
        }

        try {
            adminProductService.createProduct(productDTO, image);
            redirectAttributes.addFlashAttribute("success", "Sản phẩm đã được tạo thành công");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
            return "admin/product/product-add";
        }

        return "redirect:/admin/products";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        try {
        model.addAttribute("product", adminProductService.getProductById(id));
        model.addAttribute("categories", categoryService.getAllCategories());
            return "admin/product/product-edit";
        } catch (Exception e) {
            return "redirect:/admin/products";
        }
    }

    @PostMapping("/edit/{id}")
    public String updateProduct(
            @PathVariable Long id,
            @Valid @ModelAttribute("product") ProductDTO productDTO,
            BindingResult result,
            @RequestParam(value = "image", required = false) MultipartFile image,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            model.addAttribute("categories", categoryService.getAllCategories());
            return "admin/product/product-edit";
        }

        try {
            adminProductService.updateProduct(id, productDTO, image);
            redirectAttributes.addFlashAttribute("success", "Sản phẩm đã được cập nhật thành công");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
            return "admin/product/product-edit";
        }

        return "redirect:/admin/products";
    }

    @PostMapping("/delete/{id}")
    public String deleteProduct(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {
        try {
            adminProductService.deleteProduct(id);
            redirectAttributes.addFlashAttribute("success", "Sản phẩm đã được xóa thành công");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/products";
    }

    @PostMapping("/{id}/toggle-status")
    public String toggleStatus(
            @PathVariable Long id,
            @RequestParam boolean active,
            RedirectAttributes redirectAttributes) {
        try {
            if (active) {
            adminProductService.activateProduct(id);
            redirectAttributes.addFlashAttribute("success", "Sản phẩm đã được kích hoạt");
            } else {
            adminProductService.deactivateProduct(id);
            redirectAttributes.addFlashAttribute("success", "Sản phẩm đã bị vô hiệu hóa");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/products";
    }

    @PostMapping("/{id}/bestseller")
    public String toggleBestSeller(
            @PathVariable Long id,
            @RequestParam boolean bestSeller,
            RedirectAttributes redirectAttributes) {
        try {
            adminProductService.updateBestSeller(id, bestSeller);
            String message = bestSeller ? "Sản phẩm đã được đánh dấu là bán chạy" : "Sản phẩm đã bỏ đánh dấu bán chạy";
            redirectAttributes.addFlashAttribute("success", message);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/products";
    }

    @PostMapping("/{id}/newarrival")
    public String toggleNewArrival(
            @PathVariable Long id,
            @RequestParam boolean newArrival,
            RedirectAttributes redirectAttributes) {
        try {
            adminProductService.updateNewArrival(id, newArrival);
            String message = newArrival ? "Sản phẩm đã được đánh dấu là mới về" : "Sản phẩm đã bỏ đánh dấu mới về";
            redirectAttributes.addFlashAttribute("success", message);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/products";
    }
} 