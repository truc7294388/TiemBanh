package com.bakeryshop.service.impl;

import com.bakeryshop.dto.BlogDTO;
import com.bakeryshop.entity.Blog;
import com.bakeryshop.entity.User;
import com.bakeryshop.repository.BlogRepository;
import com.bakeryshop.repository.UserRepository;
import com.bakeryshop.service.BlogService;
import com.bakeryshop.service.FileStorageService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BlogServiceImpl implements BlogService {
    private final BlogRepository blogRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;

    public BlogServiceImpl(BlogRepository blogRepository,
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
        blog.setTitle(blogDTO.getTitle());
        blog.setContent(blogDTO.getContent());
        blog.setShortDescription(blogDTO.getShortDescription());
        blog.setPublished(blogDTO.isPublished());

        // Set author
        User author = userRepository.findById(blogDTO.getAuthorId())
                .orElseThrow(() -> new RuntimeException("Author not found"));
        blog.setAuthor(author);

        // Handle image upload
        if (image != null && !image.isEmpty()) {
            String imageUrl = fileStorageService.storeFile(image);
            blog.setImageUrl(imageUrl);
        }

        blog = blogRepository.save(blog);
        return convertToDTO(blog);
    }

    @Override
    @Transactional
    public BlogDTO updateBlog(Long id, BlogDTO blogDTO, MultipartFile image) {
        Blog blog = blogRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Blog not found"));

        blog.setTitle(blogDTO.getTitle());
        blog.setContent(blogDTO.getContent());
        blog.setShortDescription(blogDTO.getShortDescription());
        blog.setPublished(blogDTO.isPublished());

        // Handle image upload
        if (image != null && !image.isEmpty()) {
            // Delete old image if exists
            if (blog.getImageUrl() != null) {
                fileStorageService.deleteFile(blog.getImageUrl());
            }
            String imageUrl = fileStorageService.storeFile(image);
            blog.setImageUrl(imageUrl);
        }

        blog = blogRepository.save(blog);
        return convertToDTO(blog);
    }

    @Override
    @Transactional
    public void deleteBlog(Long id) {
        Blog blog = blogRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Blog not found"));

        // Delete image if exists
        if (blog.getImageUrl() != null) {
            fileStorageService.deleteFile(blog.getImageUrl());
        }

        blogRepository.delete(blog);
    }

    @Override
    @Transactional(readOnly = true)
    public BlogDTO getBlogById(Long id) {
        Blog blog = blogRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Blog not found"));
        
        if (!blog.isPublished()) {
            throw new RuntimeException("Blog is not published");
        }
        
        return convertToDTO(blog);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BlogDTO> getAllBlogs(String keyword, Pageable pageable) {
        if (keyword != null && !keyword.isEmpty()) {
            return blogRepository.findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase(keyword, keyword, pageable)
                    .map(this::convertToDTO);
        }
        return blogRepository.findAll(pageable)
                .map(this::convertToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BlogDTO> getPublishedBlogs(Pageable pageable) {
        return blogRepository.findByIsPublishedTrue(pageable)
                .map(this::convertToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BlogDTO> getBlogsByAuthor(Long authorId, Pageable pageable) {
        return blogRepository.findByAuthorId(authorId, pageable)
                .map(this::convertToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public long countPublishedBlogs() {
        return blogRepository.countByIsPublished(true);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BlogDTO> getLatestBlogs(int limit) {
        Page<Blog> latestBlogs = blogRepository.findByIsPublishedTrue(
            PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt"))
        );
        return latestBlogs.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
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

        if (blog.getAuthor() != null) {
            User author = blog.getAuthor();
            dto.setAuthorId(author.getId());
            dto.setAuthorName(author.getName());
            dto.setAuthorFullName(author.getName());
            dto.setAuthorEmail(author.getEmail());
        }
        
        return dto;
    }
} 