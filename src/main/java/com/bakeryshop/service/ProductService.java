package com.bakeryshop.service;

import com.bakeryshop.dto.ProductDTO;
import com.bakeryshop.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ProductService {
    ProductDTO createProduct(ProductDTO productDTO, MultipartFile image);
    
    ProductDTO updateProduct(Long id, ProductDTO productDTO, MultipartFile image);
    
    void deleteProduct(Long id);
    
    ProductDTO getProductById(Long id);
    
    Page<ProductDTO> getAllProducts(Pageable pageable);
    
    Page<ProductDTO> getProductsByCategory(Long categoryId, Pageable pageable);
    
    Page<ProductDTO> searchProducts(String keyword, Pageable pageable);
    
    List<ProductDTO> getBestSellers();
    
    List<ProductDTO> getNewArrivals();
    
    List<ProductDTO> getLowStockProducts(int minStock);
    
    void updateStock(Long productId, int quantity);

    Page<ProductDTO> getProducts(Long categoryId, String keyword, Double minPrice, Double maxPrice, String sort, Pageable pageable);

    // New methods for admin
    long getTotalProducts();

    // Dashboard methods
    long countTotalProducts();
    List<Product> getLowStockProducts();
} 