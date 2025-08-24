package com.bakeryshop.controller;

import com.bakeryshop.service.ProductService;
import com.bakeryshop.service.BlogService;
import com.bakeryshop.service.CategoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/")
public class WebController {
    private static final Logger logger = LoggerFactory.getLogger(WebController.class);
    
    private final ProductService productService;
    private final BlogService blogService;
    private final CategoryService categoryService;

    @Autowired
    public WebController(ProductService productService, 
                        BlogService blogService,
                        CategoryService categoryService) {
        this.productService = productService;
        this.blogService = blogService;
        this.categoryService = categoryService;
    }

    @GetMapping
    public String home(Model model) {
        try {
            model.addAttribute("bestSellers", productService.getBestSellers());
            model.addAttribute("newArrivals", productService.getNewArrivals());
            model.addAttribute("featuredCategories", categoryService.getFeaturedCategories());
            model.addAttribute("latestBlogs", blogService.getLatestBlogs(3));
            return "index";
        } catch (Exception e) {
            logger.error("Error loading home page", e);
            model.addAttribute("error", "Có lỗi xảy ra khi tải trang chủ");
            model.addAttribute("message", "Vui lòng thử lại sau");
            return "error";
        }
    }

    @GetMapping("/contact")
    public String contact() {
        return "contact";
    }
} 