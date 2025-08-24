package com.bakeryshop.controller;

import com.bakeryshop.entity.Category;
import com.bakeryshop.service.CategoryService;
import com.bakeryshop.service.ProductService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/products")
public class WebProductController {
    private final ProductService productService;
    private final CategoryService categoryService;

    public WebProductController(ProductService productService, CategoryService categoryService) {
        this.productService = productService;
        this.categoryService = categoryService;
    }

    @GetMapping
    public String listProducts(
            @RequestParam(required = false) Long category,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            Model model) {

        // Load categories for sidebar
        model.addAttribute("categories", categoryService.getAllCategories());

        // Load selected category if any
        if (category != null) {
            model.addAttribute("category", categoryService.getCategoryById(category));
        }

        // Get products with filters
        var productsPage = productService.getProducts(category, keyword, minPrice, maxPrice, sort,
                PageRequest.of(page, size));

        // Add products to model
        model.addAttribute("products", productsPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productsPage.getTotalPages());

        // Add filter parameters to model for form values
        model.addAttribute("selectedCategory", category);
        model.addAttribute("keyword", keyword);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        model.addAttribute("selectedSort", sort);

        return "product/products";
    }

    @GetMapping("/{id}")
    public String viewProduct(@PathVariable Long id, Model model) {
        model.addAttribute("product", productService.getProductById(id));
        return "product/product-detail";
    }

    @GetMapping("/search")
    public String searchProducts(@RequestParam String keyword, Model model) {
        return "redirect:/products?keyword=" + keyword;
    }
} 