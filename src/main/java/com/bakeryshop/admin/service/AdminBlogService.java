package com.bakeryshop.admin.service;

import com.bakeryshop.dto.BlogDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface AdminBlogService {
    // Blog CRUD operations
    BlogDTO createBlog(BlogDTO blogDTO, MultipartFile image);
    BlogDTO updateBlog(Long id, BlogDTO blogDTO, MultipartFile image);
    void deleteBlog(Long id);
    BlogDTO getBlogById(Long id);
    Page<BlogDTO> getAllBlogs(String keyword, Pageable pageable);
    
    // Blog Statistics
    long getTotalBlogs();
    long getTotalPublishedBlogs();
    long getTotalDraftBlogs();
    
    // Blog Status Management
    void publishBlog(Long id);
    void unpublishBlog(Long id);
} 