package com.bakeryshop.repository;

import com.bakeryshop.entity.Blog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface BlogRepository extends JpaRepository<Blog, Long>, JpaSpecificationExecutor<Blog> {
    Page<Blog> findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase(String title, String content, Pageable pageable);
    
    Page<Blog> findByIsPublishedTrue(Pageable pageable);
    
    Page<Blog> findByAuthorId(Long authorId, Pageable pageable);
    
    long countByIsPublished(boolean isPublished);
} 