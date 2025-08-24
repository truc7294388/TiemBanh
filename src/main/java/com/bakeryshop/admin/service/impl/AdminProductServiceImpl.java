package com.bakeryshop.admin.service.impl;

import com.bakeryshop.admin.service.AdminProductService;
import com.bakeryshop.dto.ProductDTO;
import com.bakeryshop.entity.Category;
import com.bakeryshop.entity.Product;
import com.bakeryshop.repository.CategoryRepository;
import com.bakeryshop.repository.ProductRepository;
import com.bakeryshop.service.FileStorageService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@Service
public class AdminProductServiceImpl implements AdminProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final FileStorageService fileStorageService;

    public AdminProductServiceImpl(ProductRepository productRepository,
                                 CategoryRepository categoryRepository,
                                 FileStorageService fileStorageService) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.fileStorageService = fileStorageService;
    }

    @Override
    @Transactional
    public ProductDTO createProduct(ProductDTO productDTO, MultipartFile image) {
        Product product = new Product();
        updateProductFromDTO(product, productDTO);
        
        if (image != null && !image.isEmpty()) {
            String imagePath = fileStorageService.storeFile(image);
            product.setImageUrl(imagePath);
        }
        
        return convertToDTO(productRepository.save(product));
    }

    @Override
    @Transactional
    public ProductDTO updateProduct(Long id, ProductDTO productDTO, MultipartFile image) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        updateProductFromDTO(product, productDTO);
        
        if (image != null && !image.isEmpty()) {
            String imagePath = fileStorageService.storeFile(image);
            product.setImageUrl(imagePath);
        }

        return convertToDTO(productRepository.save(product));
    }

    @Override
    @Transactional
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        
        // Delete the product image if it exists
        if (product.getImageUrl() != null) {
            fileStorageService.deleteFile(product.getImageUrl());
        }
        
        productRepository.delete(product);
    }

    @Override
    public ProductDTO getProductById(Long id) {
        return productRepository.findById(id)
                .map(this::convertToDTO)
                .orElseThrow(() -> new RuntimeException("Product not found"));
    }

    @Override
    public Page<ProductDTO> getAllProducts(String keyword, Pageable pageable) {
        if (keyword != null && !keyword.isEmpty()) {
            return productRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(keyword, keyword, pageable)
                    .map(this::convertToDTO);
        }
        return productRepository.findAll(pageable).map(this::convertToDTO);
    }

    @Override
    public long getTotalProducts() {
        return productRepository.count();
    }

    @Override
    public long getTotalActiveProducts() {
        return productRepository.countByActive(true);
    }

    @Override
    public long getTotalInactiveProducts() {
        return productRepository.countByActive(false);
    }

    @Override
    public Page<ProductDTO> getLowStockProducts(int threshold, Pageable pageable) {
        return productRepository.findByStockLessThan(threshold, pageable)
                .map(this::convertToDTO);
    }

    @Override
    @Transactional
    public void activateProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        product.setActive(true);
        productRepository.save(product);
    }

    @Override
    @Transactional
    public void deactivateProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        product.setActive(false);
        productRepository.save(product);
    }

    @Override
    @Transactional
    public void updateStock(Long id, int quantity) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        product.setStock(quantity);
        productRepository.save(product);
    }

    @Override
    @Transactional
    public void updateBestSeller(Long id, boolean isBestSeller) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        product.setBestSeller(isBestSeller);
        product.setUpdatedAt(LocalDateTime.now());
        productRepository.save(product);
    }

    @Override
    @Transactional
    public void updateNewArrival(Long id, boolean isNewArrival) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        product.setNewArrival(isNewArrival);
        product.setUpdatedAt(LocalDateTime.now());
        productRepository.save(product);
    }

    private void updateProductFromDTO(Product product, ProductDTO dto) {
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setStock(dto.getStock());
        
        // Find and set the category
        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));
        product.setCategory(category);
        
        product.setActive(dto.isActive());
        product.setBestSeller(dto.isBestSeller());
        product.setNewArrival(dto.isNewArrival());
        product.setUpdatedAt(LocalDateTime.now());
    }

    private ProductDTO convertToDTO(Product product) {
        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setStock(product.getStock());
        dto.setImageUrl(product.getImageUrl());
        
        // Set category information
        Category category = product.getCategory();
        if (category != null) {
            dto.setCategoryId(category.getId());
            dto.setCategoryName(category.getName());
        }
        
        dto.setActive(product.isActive());
        dto.setBestSeller(product.isBestSeller());
        dto.setNewArrival(product.isNewArrival());
        dto.setCreatedAt(product.getCreatedAt());
        dto.setUpdatedAt(product.getUpdatedAt());
        return dto;
    }
} 