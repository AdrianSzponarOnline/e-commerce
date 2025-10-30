package com.ecommerce.E_commerce.service;

import com.ecommerce.E_commerce.dto.product.ProductCreateDTO;
import com.ecommerce.E_commerce.dto.product.ProductDTO;
import com.ecommerce.E_commerce.dto.product.ProductUpdateDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;

import java.math.BigDecimal;

public interface ProductService {
    
    // CRUD Operations - Owner only
    @PreAuthorize("hasRole('OWNER')")
    ProductDTO create(ProductCreateDTO dto);
    
    @PreAuthorize("hasRole('OWNER')")
    ProductDTO update(Long id, ProductUpdateDTO dto);
    
    @PreAuthorize("hasRole('OWNER')")
    void delete(Long id);
    
    // Single Product Retrieval
    ProductDTO getById(Long id);
    ProductDTO getBySeoSlug(String seoSlug);
    ProductDTO getBySku(String sku);
    
    // Paginated Product Lists
    Page<ProductDTO> findAll(Pageable pageable);
    Page<ProductDTO> findByCategory(Long categoryId, Pageable pageable);
    Page<ProductDTO> findByCategorySlug(String categorySlug, Pageable pageable);
    Page<ProductDTO> findByPriceRange(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);
    Page<ProductDTO> findByFeatured(Boolean isFeatured, Pageable pageable);
    // Only owner can access inactive products
    @PreAuthorize("hasRole('OWNER') or #isActive == true")
    Page<ProductDTO> findByActive(Boolean isActive, Pageable pageable);
    
    // Search and Filter
    Page<ProductDTO> searchByName(String name, Pageable pageable);
    Page<ProductDTO> searchByDescription(String description, Pageable pageable);
    Page<ProductDTO> findByCategoryAndPriceRange(Long categoryId, BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);
    
    // Advanced Filtering
    Page<ProductDTO> findByCategoryAndFeatured(Long categoryId, Boolean isFeatured, Pageable pageable);
    Page<ProductDTO> findByPriceRangeAndFeatured(BigDecimal minPrice, BigDecimal maxPrice, Boolean isFeatured, Pageable pageable);
    
    // Statistics
    long countByCategory(Long categoryId);
    long countByFeatured(Boolean isFeatured);
    long countByActive(Boolean isActive);
    
    // Overloads that encapsulate Sort/Pageable creation
    Page<ProductDTO> findAll(int page, int size, String sortBy, String sortDir);
    Page<ProductDTO> findByCategory(Long categoryId, int page, int size, String sortBy, String sortDir);
    Page<ProductDTO> findByCategorySlug(String categorySlug, int page, int size, String sortBy, String sortDir);
    Page<ProductDTO> findByPriceRange(BigDecimal minPrice, BigDecimal maxPrice, int page, int size, String sortBy, String sortDir);
    Page<ProductDTO> findByFeatured(Boolean isFeatured, int page, int size, String sortBy, String sortDir);
    @PreAuthorize("hasRole('OWNER') or #isActive == true")
    Page<ProductDTO> findByActive(Boolean isActive, int page, int size, String sortBy, String sortDir);
    Page<ProductDTO> searchByName(String name, int page, int size, String sortBy, String sortDir);
    Page<ProductDTO> searchByDescription(String description, int page, int size, String sortBy, String sortDir);
    Page<ProductDTO> findByCategoryAndPriceRange(Long categoryId, BigDecimal minPrice, BigDecimal maxPrice, int page, int size, String sortBy, String sortDir);
    Page<ProductDTO> findByCategoryAndFeatured(Long categoryId, Boolean isFeatured, int page, int size, String sortBy, String sortDir);
    Page<ProductDTO> findByPriceRangeAndFeatured(BigDecimal minPrice, BigDecimal maxPrice, Boolean isFeatured, int page, int size, String sortBy, String sortDir);
    
}
