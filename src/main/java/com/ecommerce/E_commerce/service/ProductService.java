package com.ecommerce.E_commerce.service;

import com.ecommerce.E_commerce.dto.product.ProductCreateDTO;
import com.ecommerce.E_commerce.dto.product.ProductDTO;
import com.ecommerce.E_commerce.dto.product.ProductSummaryDTO;
import com.ecommerce.E_commerce.dto.product.ProductUpdateDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;

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
    Page<ProductSummaryDTO> findAll(Pageable pageable);
    Page<ProductSummaryDTO> findByCategory(Long categoryId, Pageable pageable);
    Page<ProductSummaryDTO> findByCategorySlug(String categorySlug, Pageable pageable);
    Page<ProductSummaryDTO> findByFeatured(Boolean isFeatured, Pageable pageable);
    // Only owner can access inactive products
    @PreAuthorize("hasRole('OWNER') or #isActive == true")
    Page<ProductSummaryDTO> findByActive(Boolean isActive, Pageable pageable);
    
    // Statistics
    long countByCategory(Long categoryId);
    long countByFeatured(Boolean isFeatured);
    long countByActive(Boolean isActive);
}
