package com.bakeryshop.admin.service.impl;

import com.bakeryshop.admin.service.AdminBlogService;
import com.bakeryshop.dto.BlogDTO;
import com.bakeryshop.entity.Blog;
import com.bakeryshop.entity.User;
import com.bakeryshop.repository.BlogRepository;
import com.bakeryshop.repository.UserRepository;
import com.bakeryshop.service.FileStorageService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@Service
public class AdminBlogServiceImpl implements AdminBlogService {
    private final BlogRepository blogRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;

    public AdminBlogServiceImpl(BlogRepository blogRepository,
                              UserRepository userRepository,
                              FileStorageService fileStorageService) {
        this.blogRepository = blogRepository;
        this.userRepository = userRepository;
        this.fileStorageService = fileStorageService;
    }

    @Override
    @Transactional
    public BlogDTO createBlog(BlogDTO blogDTO, MultipartFile image) {
        Blog blog = new Blog();
        updateBlogFromDTO(blog, blogDTO);
        
        if (image != null && !image.isEmpty()) {
            String imagePath = fileStorageService.storeFile(image);
            blog.setImageUrl(imagePath);
        }
        
        // Set current user as author
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        blog.setAuthor(currentUser);
        
        return convertToDTO(blogRepository.save(blog));
    }

    @Override
    @Transactional
    public BlogDTO updateBlog(Long id, BlogDTO blogDTO, MultipartFile image) {
        Blog blog = blogRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Blog not found"));

        updateBlogFromDTO(blog, blogDTO);
        
        if (image != null && !image.isEmpty()) {
            if (blog.getImageUrl() != null) {
                fileStorageService.deleteFile(blog.getImageUrl());
            }
            String imagePath = fileStorageService.storeFile(image);
            blog.setImageUrl(imagePath);
        }

        return convertToDTO(blogRepository.save(blog));
    }

    @Override
    @Transactional
    public void deleteBlog(Long id) {
        Blog blog = blogRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Blog not found"));
        
        if (blog.getImageUrl() != null) {
            fileStorageService.deleteFile(blog.getImageUrl());
        }
        
        blogRepository.delete(blog);
    }

    @Override
    public BlogDTO getBlogById(Long id) {
        return blogRepository.findById(id)
                .map(this::convertToDTO)
                .orElseThrow(() -> new RuntimeException("Blog not found"));
    }

    @Override
    public Page<BlogDTO> getAllBlogs(String keyword, Pageable pageable) {
        if (keyword != null && !keyword.isEmpty()) {
            return blogRepository.findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase(keyword, keyword, pageable)
                    .map(this::convertToDTO);
        }
        return blogRepository.findAll(pageable).map(this::convertToDTO);
    }

    @Override
    public long getTotalBlogs() {
        return blogRepository.count();
    }

    @Override
    public long getTotalPublishedBlogs() {
        return blogRepository.countByIsPublished(true);
    }

    @Override
    public long getTotalDraftBlogs() {
        return blogRepository.countByIsPublished(false);
    }

    @Override
    @Transactional
    public void publishBlog(Long id) {
        Blog blog = blogRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Blog not found"));
        blog.setPublished(true);
        blogRepository.save(blog);
    }

    @Override
    @Transactional
    public void unpublishBlog(Long id) {
        Blog blog = blogRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Blog not found"));
        blog.setPublished(false);
        blogRepository.save(blog);
    }

    private void updateBlogFromDTO(Blog blog, BlogDTO dto) {
        blog.setTitle(dto.getTitle());
        blog.setContent(dto.getContent());
        dto.setShortDescription(blog.getShortDescription());
        blog.setPublished(dto.isPublished());
        blog.setUpdatedAt(LocalDateTime.now());
    }

    private BlogDTO convertToDTO(Blog blog) {
        BlogDTO dto = new BlogDTO();
        dto.setId(blog.getId());
        dto.setTitle(blog.getTitle());
        dto.setContent(blog.getContent());
        dto.setShortDescription(blog.getShortDescription());
        dto.setImageUrl(blog.getImageUrl());
        dto.setPublished(blog.isPublished());
        dto.setCreatedAt(blog.getCreatedAt());
        dto.setUpdatedAt(blog.getUpdatedAt());

        // Set author information
        if (blog.getAuthor() != null) {
            dto.setAuthorId(blog.getAuthor().getId());
            dto.setAuthorName(blog.getAuthor().getName());
        }
        
        return dto;
    }
} 