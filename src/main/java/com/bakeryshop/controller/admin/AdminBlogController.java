package com.bakeryshop.controller.admin;

import com.bakeryshop.admin.service.AdminBlogService;
import com.bakeryshop.dto.BlogDTO;
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
@RequestMapping("/admin/blogs")
@PreAuthorize("hasRole('ADMIN')")
public class AdminBlogController {
    private final AdminBlogService adminBlogService;

    public AdminBlogController(AdminBlogService adminBlogService) {
        this.adminBlogService = adminBlogService;
    }

    @GetMapping
    public String listBlogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            Model model) {
        
        // Get blogs with filters
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<BlogDTO> blogsPage = adminBlogService.getAllBlogs(keyword, pageRequest);
        
        // Add blogs to model
        model.addAttribute("blogs", blogsPage.getContent());
        model.addAttribute("currentPage", blogsPage.getNumber());
        model.addAttribute("totalPages", blogsPage.getTotalPages());
        model.addAttribute("size", size);
        model.addAttribute("keyword", keyword);

        // Add statistics
        model.addAttribute("totalBlogs", adminBlogService.getTotalBlogs());
        model.addAttribute("totalPublished", adminBlogService.getTotalPublishedBlogs());
        model.addAttribute("totalDrafts", adminBlogService.getTotalDraftBlogs());

        return "admin/blog/blogs";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("blog", new BlogDTO());
        return "admin/blog/blog-add";
    }

    @PostMapping("/add")
    public String addBlog(
            @Valid @ModelAttribute("blog") BlogDTO blogDTO,
            BindingResult result,
            @RequestParam(value = "image", required = false) MultipartFile image,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            return "admin/blog/blog-add";
        }

        try {
            adminBlogService.createBlog(blogDTO, image);
            redirectAttributes.addFlashAttribute("success", "Bài viết đã được tạo thành công");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
            return "admin/blog/blog-add";
        }

        return "redirect:/admin/blogs";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        try {
            model.addAttribute("blog", adminBlogService.getBlogById(id));
            return "admin/blog/blog-edit";
        } catch (Exception e) {
            return "redirect:/admin/blogs";
        }
    }

    @PostMapping("/edit/{id}")
    public String updateBlog(
            @PathVariable Long id,
            @Valid @ModelAttribute("blog") BlogDTO blogDTO,
            BindingResult result,
            @RequestParam(value = "image", required = false) MultipartFile image,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            return "admin/blog/blog-edit";
        }

        try {
            adminBlogService.updateBlog(id, blogDTO, image);
            redirectAttributes.addFlashAttribute("success", "Bài viết đã được cập nhật thành công");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
            return "admin/blog/blog-edit";
        }

        return "redirect:/admin/blogs";
    }

    @PostMapping("/delete/{id}")
    public String deleteBlog(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {
        try {
            adminBlogService.deleteBlog(id);
            redirectAttributes.addFlashAttribute("success", "Bài viết đã được xóa thành công");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/blogs";
    }

    @PostMapping("/{id}/publish")
    public String publishBlog(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            adminBlogService.publishBlog(id);
            redirectAttributes.addFlashAttribute("success", "Bài viết đã được xuất bản thành công");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/blogs";
    }

    @PostMapping("/{id}/unpublish")
    public String unpublishBlog(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            adminBlogService.unpublishBlog(id);
            redirectAttributes.addFlashAttribute("success", "Bài viết đã được lưu nháp thành công");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/blogs";
    }
} 