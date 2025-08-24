package com.bakeryshop.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class CategoryDTO {
    private Long id;

    @NotBlank(message = "Category name is required")
    private String name;

    private String description;

    private String imageUrl;

    private int productCount;
    
    private boolean featured;
} 