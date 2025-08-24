package com.bakeryshop.service;

import com.bakeryshop.dto.CategoryDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface CategoryService {
    CategoryDTO createCategory(CategoryDTO categoryDTO, MultipartFile image);
    
    CategoryDTO updateCategory(Long id, CategoryDTO categoryDTO, MultipartFile image);
    
    void deleteCategory(Long id);
    
    CategoryDTO getCategoryById(Long id);
    
    List<CategoryDTO> getAllCategories();
    
    Page<CategoryDTO> getAllCategoriesPaged(Pageable pageable);
    
    boolean existsByName(String name);

    List<CategoryDTO> getFeaturedCategories();
    
    void setFeatured(Long id, boolean featured);
} 