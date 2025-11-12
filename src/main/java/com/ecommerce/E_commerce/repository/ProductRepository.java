package com.ecommerce.E_commerce.repository;

import com.ecommerce.E_commerce.model.Product;
import jakarta.persistence.Entity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    
    // Single Product Queries
    @Override
    @EntityGraph(value = "Product.withDetails")
    Page<Product> findAll(Pageable pageable);

    @Override
    @EntityGraph(value = "Product.withDetails")
    Optional<Product> findById(Long id);

    @EntityGraph(value = "Product.withDetails")
    Optional<Product> findBySeoSlug(String seoSlug);
    @EntityGraph(value = "Product.withDetails")
    Optional<Product> findBySku(String sku);
    
    // Category-based Queries
    @EntityGraph(value = "Product.withDetails")
    Page<Product> findByCategoryId(Long categoryId, Pageable pageable);
    @EntityGraph(value = "Product.withDetails")
    Page<Product> findByCategorySeoSlug(String categorySlug, Pageable pageable);
    
    // Price-based Queries
    @EntityGraph(value = "Product.withDetails")
    Page<Product> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);
    @EntityGraph(value = "Product.withDetails")
    Page<Product> findByCategoryIdAndPriceBetween(Long categoryId, BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);
    
    // Boolean Field Queries
    @EntityGraph(value = "Product.withDetails")
    Page<Product> findByIsFeatured(Boolean isFeatured, Pageable pageable);
    @EntityGraph(value = "Product.withDetails")
    Page<Product> findByIsActive(Boolean isActive, Pageable pageable);
    @EntityGraph(value = "Product.withDetails")
    Page<Product> findByCategoryIdAndIsFeatured(Long categoryId, Boolean isFeatured, Pageable pageable);
    @EntityGraph(value = "Product.withDetails")
    Page<Product> findByPriceBetweenAndIsFeatured(BigDecimal minPrice, BigDecimal maxPrice, Boolean isFeatured, Pageable pageable);
    
    // Search Queries
    @EntityGraph(value = "Product.withDetails")
    Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable);
    @EntityGraph(value = "Product.withDetails")
    Page<Product> findByDescriptionContainingIgnoreCase(String description, Pageable pageable);
    
    // Count Queries
    long countByCategoryId(Long categoryId);
    long countByIsFeatured(Boolean isFeatured);
    long countByIsActive(Boolean isActive);
    
    // Advanced Search with Multiple Criteria
    @Query("SELECT p FROM Product p WHERE " +
           "(:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
           "(:categoryId IS NULL OR p.category.id = :categoryId) AND " +
           "(:minPrice IS NULL OR p.price >= :minPrice) AND " +
           "(:maxPrice IS NULL OR p.price <= :maxPrice) AND " +
           "(:isFeatured IS NULL OR p.isFeatured = :isFeatured) AND " +
           "(:isActive IS NULL OR p.isActive = :isActive)")
    @EntityGraph(value = "Product.withDetails")
    Page<Product> findByMultipleCriteria(
            @Param("name") String name,
            @Param("categoryId") Long categoryId,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("isFeatured") Boolean isFeatured,
            @Param("isActive") Boolean isActive,
            Pageable pageable
    );
    
    // Featured Products in Category
    @Query("SELECT p FROM Product p WHERE p.category.id = :categoryId AND p.isFeatured = true AND p.isActive = true")
    @EntityGraph(value = "Product.withDetails")
    Page<Product> findFeaturedByCategory(@Param("categoryId") Long categoryId, Pageable pageable);
    
    // Products with Price Range and Category
    @Query("SELECT p FROM Product p WHERE p.category.id = :categoryId AND p.price BETWEEN :minPrice AND :maxPrice AND p.isActive = true")
    @EntityGraph(value = "Product.withDetails")
    Page<Product> findByCategoryAndPriceRangeActive(
            @Param("categoryId") Long categoryId,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            Pageable pageable
    );
}
