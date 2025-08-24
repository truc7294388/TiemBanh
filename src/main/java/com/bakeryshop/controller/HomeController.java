package com.bakeryshop.controller;

import com.bakeryshop.service.BlogService;
import com.bakeryshop.service.ProductService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/")
public class HomeController {
    private final ProductService productService;
    private final BlogService blogService;

    public HomeController(ProductService productService, BlogService blogService) {
        this.productService = productService;
        this.blogService = blogService;
    }

//    @GetMapping
//    public String home(Model model) {
//        // Add latest products
////        model.addAttribute("latestProducts", productService.getLatestProducts(6));
//
//        // Add latest blogs
//        model.addAttribute("latestBlogs", blogService.getLatestBlogs(3));
//
//        return "index";
//    }
} 