package com.bakeryshop.controller;

import com.bakeryshop.service.BlogService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/blogs")
public class BlogController {
    private final BlogService blogService;

    public BlogController(BlogService blogService) {
        this.blogService = blogService;
    }

    @GetMapping
    public String listBlogs(Model model, 
                          @RequestParam(defaultValue = "0") int page,
                          @RequestParam(defaultValue = "9") int size) {
        var blogPage = blogService.getPublishedBlogs(
            PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))
        );
        model.addAttribute("blogs", blogPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", blogPage.getTotalPages());
        return "blog/blogs";
    }

    @GetMapping("/{id}")
    public String viewBlog(@PathVariable Long id, Model model) {
        try {
            model.addAttribute("blog", blogService.getBlogById(id));
            model.addAttribute("relatedBlogs", blogService.getLatestBlogs(3));
            return "blog/blog-detail";
        } catch (RuntimeException e) {
            model.addAttribute("status", 404);
            model.addAttribute("error", "Không tìm thấy bài viết");
            model.addAttribute("message", e.getMessage());
            return "error";
        }
    }
}