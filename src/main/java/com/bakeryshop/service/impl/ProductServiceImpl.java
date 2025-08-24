package com.bakeryshop.service.impl;

import com.bakeryshop.dto.ProductDTO;
import com.bakeryshop.entity.Category;
import com.bakeryshop.entity.Product;
import com.bakeryshop.repository.CategoryRepository;
import com.bakeryshop.repository.ProductRepository;
import com.bakeryshop.service.FileStorageService;
import com.bakeryshop.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final FileStorageService fileStorageService;
    private static final int LOW_STOCK_THRESHOLD = 10;

    public ProductServiceImpl(ProductRepository productRepository,
                            CategoryRepository categoryRepository,
                            FileStorageService fileStorageService) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.fileStorageService = fileStorageService;
    }

    @Override
    @Transactional
    public ProductDTO createProduct(ProductDTO productDTO, MultipartFile image) {
        Category category = categoryRepository.findById(productDTO.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));

        Product product = new Product();
        updateProductFromDTO(product, productDTO, category);

        if (image != null && !image.isEmpty()) {
            String imagePath = fileStorageService.storeFile(image);
            product.setImageUrl(imagePath);
        }

        Product savedProduct = productRepository.save(product);
        return convertToDTO(savedProduct);
    }

    @Override
    @Transactional
    public ProductDTO updateProduct(Long id, ProductDTO productDTO, MultipartFile image) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        Category category = categoryRepository.findById(productDTO.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));

        updateProductFromDTO(product, productDTO, category);

        if (image != null && !image.isEmpty()) {
            if (product.getImageUrl() != null) {
                fileStorageService.deleteFile(product.getImageUrl());
            }
            String imagePath = fileStorageService.storeFile(image);
            product.setImageUrl(imagePath);
        }

        Product updatedProduct = productRepository.save(product);
        return convertToDTO(updatedProduct);
    }

    @Override
    @Transactional
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (product.getImageUrl() != null) {
            fileStorageService.deleteFile(product.getImageUrl());
        }

        productRepository.delete(product);
    }

    @Override
    public ProductDTO getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        return convertToDTO(product);
    }

    @Override
    public Page<ProductDTO> getAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable).map(this::convertToDTO);
    }

    @Override
    public Page<ProductDTO> getProductsByCategory(Long categoryId, Pageable pageable) {
        return productRepository.findByCategoryId(categoryId, pageable).map(this::convertToDTO);
    }

    @Override
    public Page<ProductDTO> searchProducts(String keyword, Pageable pageable) {
        return productRepository.findByNameContainingIgnoreCase(keyword, pageable)
                .map(this::convertToDTO);
    }

    @Override
    public List<ProductDTO> getBestSellers() {
        return productRepository.findBestSellers().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductDTO> getNewArrivals() {
        return productRepository.findNewArrivals().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductDTO> getLowStockProducts(int minStock) {
        return productRepository.findLowStockProducts(minStock).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void updateStock(Long productId, int quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        int newStock = product.getStock() + quantity;
        if (newStock < 0) {
            throw new RuntimeException("Insufficient stock");
        }

        product.setStock(newStock);
        productRepository.save(product);
    }

    @Override
    public Page<ProductDTO> getProducts(Long categoryId, String keyword, Double minPrice, Double maxPrice, String sort, Pageable pageable) {
        // Create base specification
        Specification<Product> spec = Specification.where(null);

        // Add category filter
        if (categoryId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("category").get("id"), categoryId));
        }

        // Add keyword search
        if (keyword != null && !keyword.trim().isEmpty()) {
            String likeKeyword = "%" + keyword.trim().toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> 
                cb.or(
                    cb.like(cb.lower(root.get("name")), likeKeyword),
                    cb.like(cb.lower(root.get("description")), likeKeyword)
                )
            );
        }

        // Add price range filter
        if (minPrice != null && minPrice > 0) {
            spec = spec.and((root, query, cb) -> 
                cb.greaterThanOrEqualTo(root.get("price"), BigDecimal.valueOf(minPrice)));
        }
        if (maxPrice != null && maxPrice > 0) {
            spec = spec.and((root, query, cb) -> 
                cb.lessThanOrEqualTo(root.get("price"), BigDecimal.valueOf(maxPrice)));
        }

        // Apply sorting
        if (sort != null) {
            switch (sort) {
                case "newest":
                    pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
                        Sort.by(Sort.Direction.DESC, "createdAt"));
                    break;
                case "price_asc":
                    pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), 
                        Sort.by(Sort.Direction.ASC, "price"));
                    break;
                case "price_desc":
                    pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), 
                        Sort.by(Sort.Direction.DESC, "price"));
                    break;
                case "bestseller":
                    spec = spec.and((root, query, cb) -> cb.isTrue(root.get("isBestSeller")));
                    pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), 
                        Sort.by(Sort.Direction.DESC, "createdAt"));
                    break;
            }
        } else {
            // Default sorting by newest
            pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), 
                Sort.by(Sort.Direction.DESC, "createdAt"));
        }

        // Execute query with all filters
        return productRepository.findAll(spec, pageable).map(this::convertToDTO);
    }

    @Override
    public long getTotalProducts() {
        return productRepository.count();
    }

    @Override
    public long countTotalProducts() {
        return productRepository.count();
    }

    @Override
    public List<Product> getLowStockProducts() {
        return productRepository.findByStockLessThanEqual(LOW_STOCK_THRESHOLD);
    }

    private void updateProductFromDTO(Product product, ProductDTO dto, Category category) {
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setBestSeller(dto.isBestSeller());
        product.setNewArrival(dto.isNewArrival());
        product.setStock(dto.getStock());
        product.setCategory(category);
    }

    private ProductDTO convertToDTO(Product product) {
        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setImageUrl(product.getImageUrl());
        dto.setBestSeller(product.isBestSeller());
        dto.setNewArrival(product.isNewArrival());
        dto.setStock(product.getStock());
        dto.setCategoryId(product.getCategory().getId());
        dto.setCategoryName(product.getCategory().getName());
        return dto;
    }
} 