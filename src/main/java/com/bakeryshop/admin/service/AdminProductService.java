package com.bakeryshop.admin.service;

import com.bakeryshop.dto.ProductDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface AdminProductService {
    // Product CRUD operations
    ProductDTO createProduct(ProductDTO productDTO, MultipartFile image);
    ProductDTO updateProduct(Long id, ProductDTO productDTO, MultipartFile image);
    void deleteProduct(Long id);
    ProductDTO getProductById(Long id);
    Page<ProductDTO> getAllProducts(String keyword, Pageable pageable);
    
    // Product Statistics
    long getTotalProducts();
    long getTotalActiveProducts();
    long getTotalInactiveProducts();
    Page<ProductDTO> getLowStockProducts(int threshold, Pageable pageable);
    
    // Product Status Management
    void activateProduct(Long id);
    void deactivateProduct(Long id);
    void updateStock(Long id, int quantity);
    
    // Featured Status Management
    void updateBestSeller(Long id, boolean isBestSeller);
    void updateNewArrival(Long id, boolean isNewArrival);
} 