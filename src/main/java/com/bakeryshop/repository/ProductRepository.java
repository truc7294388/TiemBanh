package com.bakeryshop.repository;

import com.bakeryshop.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
    Page<Product> findByCategoryId(Long categoryId, Pageable pageable);
    
    Page<Product> findByNameContainingIgnoreCase(String keyword, Pageable pageable);
    
    Page<Product> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String name, String description, Pageable pageable);
    
    Page<Product> findByPriceBetween(Double minPrice, Double maxPrice, Pageable pageable);
    
    Page<Product> findAllByOrderByCreatedAtDesc(Pageable pageable);
    
    Page<Product> findAllByOrderByPriceAsc(Pageable pageable);
    
    Page<Product> findAllByOrderByPriceDesc(Pageable pageable);
    
    Page<Product> findByBestSellerTrueOrderByCreatedAtDesc(Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.bestSeller = true ORDER BY p.createdAt DESC")
    List<Product> findBestSellers();

    @Query("SELECT p FROM Product p WHERE p.newArrival = true ORDER BY p.createdAt DESC")
    List<Product> findNewArrivals();

    @Query("SELECT p FROM Product p WHERE p.stock <= :minStock")
    List<Product> findLowStockProducts(int minStock);

    List<Product> findByCategoryId(Long categoryId);
    List<Product> findByStockLessThanEqual(int threshold);
    List<Product> findTop8ByBestSellerTrueOrderByCreatedAtDesc();
    List<Product> findTop8ByOrderByCreatedAtDesc();
    
    long countByActive(boolean active);
    
    Page<Product> findByActive(boolean active, Pageable pageable);
    
    Page<Product> findByStockLessThan(int threshold, Pageable pageable);
} 