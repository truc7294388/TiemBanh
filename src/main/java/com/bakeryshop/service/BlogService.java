package com.bakeryshop.service;

import com.bakeryshop.dto.BlogDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface BlogService {
    BlogDTO createBlog(BlogDTO blogDTO, MultipartFile image);
    BlogDTO updateBlog(Long id, BlogDTO blogDTO, MultipartFile image);
    void deleteBlog(Long id);
    BlogDTO getBlogById(Long id);
    Page<BlogDTO> getAllBlogs(String keyword, Pageable pageable);
    Page<BlogDTO> getPublishedBlogs(Pageable pageable);
    Page<BlogDTO> getBlogsByAuthor(Long authorId, Pageable pageable);
    long countPublishedBlogs();
    List<BlogDTO> getLatestBlogs(int limit);
} 