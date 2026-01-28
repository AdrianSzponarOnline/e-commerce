package com.ecommerce.E_commerce.repository;

import com.ecommerce.E_commerce.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    
    
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
    
    @EntityGraph(value = "Product.withDetails")
    Page<Product> findByCategoryId(Long categoryId, Pageable pageable);
    @EntityGraph(value = "Product.withDetails")
    Page<Product> findByCategorySeoSlug(String categorySlug, Pageable pageable);
    

  
    @EntityGraph(value = "Product.summary")
    Page<Product> findByIsFeatured(Boolean isFeatured, Pageable pageable);
    @EntityGraph(value = "Product.summary")
    Page<Product> findByIsActive(Boolean isActive, Pageable pageable);
    Page<Product> findByCategoryIdIn(List<Long> categoryIds, Pageable pageable);

    long countByCategoryId(Long categoryId);
    long countByIsFeatured(Boolean isFeatured);
    long countByIsActive(Boolean isActive);
}
