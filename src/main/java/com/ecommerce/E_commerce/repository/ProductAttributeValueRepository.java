package com.ecommerce.E_commerce.repository;

import com.ecommerce.E_commerce.model.ProductAttributeValue;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductAttributeValueRepository extends JpaRepository<ProductAttributeValue, Long> {
    
    // Find by product
    List<ProductAttributeValue> findByProductId(Long productId);
    Page<ProductAttributeValue> findByProductId(Long productId, Pageable pageable);
    List<ProductAttributeValue> findByProductIdAndIsActive(Long productId, Boolean isActive);
    
    // Find by category attribute
    List<ProductAttributeValue> findByCategoryAttributeId(Long categoryAttributeId);
    Page<ProductAttributeValue> findByCategoryAttributeId(Long categoryAttributeId, Pageable pageable);
    List<ProductAttributeValue> findByCategoryAttributeIdAndIsActive(Long categoryAttributeId, Boolean isActive);
    
    // Find by value
    List<ProductAttributeValue> findByValueContainingIgnoreCase(String value);
    Page<ProductAttributeValue> findByValueContainingIgnoreCase(String value, Pageable pageable);
    
    // Find by product and category attribute
    Optional<ProductAttributeValue> findByProductIdAndCategoryAttributeId(Long productId, Long categoryAttributeId);
    List<ProductAttributeValue> findByProductIdAndCategoryAttributeIdAndIsActive(Long productId, Long categoryAttributeId, Boolean isActive);
    
    // Find by key attributes
    List<ProductAttributeValue> findByProductIdAndCategoryAttributeIsKeyAttribute(Long productId, Boolean isKeyAttribute);
    List<ProductAttributeValue> findByProductIdAndCategoryAttributeIsKeyAttributeAndIsActive(Long productId, Boolean isKeyAttribute, Boolean isActive);
    
    // Find by category
    List<ProductAttributeValue> findByProductCategoryId(Long categoryId);
    Page<ProductAttributeValue> findByProductCategoryId(Long categoryId, Pageable pageable);
    List<ProductAttributeValue> findByProductCategoryIdAndIsActive(Long categoryId, Boolean isActive);
    
    // Find by category attribute type
    List<ProductAttributeValue> findByCategoryAttributeType(String type);
    Page<ProductAttributeValue> findByCategoryAttributeType(String type, Pageable pageable);
    List<ProductAttributeValue> findByCategoryAttributeTypeAndIsActive(String type, Boolean isActive);
    
    // Count methods
    long countByProductId(Long productId);
    long countByProductIdAndIsActive(Long productId, Boolean isActive);
    long countByCategoryAttributeId(Long categoryAttributeId);
    long countByCategoryAttributeIdAndIsActive(Long categoryAttributeId, Boolean isActive);
    long countByProductCategoryId(Long categoryId);
    long countByProductCategoryIdAndIsActive(Long categoryId, Boolean isActive);
    
    // Advanced queries
    @Query("SELECT pav FROM ProductAttributeValue pav WHERE " +
           "(:productId IS NULL OR pav.product.id = :productId) AND " +
           "(:categoryAttributeId IS NULL OR pav.categoryAttribute.id = :categoryAttributeId) AND " +
           "(:value IS NULL OR LOWER(pav.value) LIKE LOWER(CONCAT('%', :value, '%'))) AND " +
           "(:isActive IS NULL OR pav.isActive = :isActive)")
    Page<ProductAttributeValue> findByMultipleCriteria(
            @Param("productId") Long productId,
            @Param("categoryAttributeId") Long categoryAttributeId,
            @Param("value") String value,
            @Param("isActive") Boolean isActive,
            Pageable pageable
    );
    
    @Query("SELECT pav FROM ProductAttributeValue pav WHERE " +
           "pav.product.id = :productId AND " +
           "pav.categoryAttribute.isKeyAttribute = true AND " +
           "pav.isActive = true")
    List<ProductAttributeValue> findKeyAttributesByProduct(@Param("productId") Long productId);
    
    @Query("SELECT pav FROM ProductAttributeValue pav WHERE " +
           "pav.product.category.id = :categoryId AND " +
           "pav.categoryAttribute.name = :attributeName AND " +
           "pav.isActive = true")
    List<ProductAttributeValue> findByCategoryAndAttributeName(
            @Param("categoryId") Long categoryId,
            @Param("attributeName") String attributeName
    );
    
    @Query("SELECT DISTINCT pav.value FROM ProductAttributeValue pav WHERE " +
           "pav.categoryAttribute.id = :categoryAttributeId AND " +
           "pav.isActive = true")
    List<String> findDistinctValuesByCategoryAttribute(@Param("categoryAttributeId") Long categoryAttributeId);
    
    @Query("SELECT pav FROM ProductAttributeValue pav WHERE " +
           "pav.product.id = :productId AND " +
           "pav.categoryAttribute.type = :attributeType AND " +
           "pav.isActive = true")
    List<ProductAttributeValue> findByProductAndAttributeType(
            @Param("productId") Long productId,
            @Param("attributeType") String attributeType
    );
}
