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
    
    // Find by attribute
    List<ProductAttributeValue> findByAttributeId(Long attributeId);
    Page<ProductAttributeValue> findByAttributeId(Long attributeId, Pageable pageable);
    List<ProductAttributeValue> findByAttributeIdAndIsActive(Long attributeId, Boolean isActive);
    
    // Find by value
    List<ProductAttributeValue> findByAttributeValueContainingIgnoreCase(String value);
    Page<ProductAttributeValue> findByAttributeValueContainingIgnoreCase(String value, Pageable pageable);
    
    // Find by product and attribute
    Optional<ProductAttributeValue> findByProductIdAndAttributeId(Long productId, Long attributeId);
    List<ProductAttributeValue> findByProductIdAndAttributeIdAndIsActive(Long productId, Long attributeId, Boolean isActive);
    
    // Find by key attributes (through CategoryAttribute)
    @Query("SELECT pav FROM ProductAttributeValue pav JOIN CategoryAttribute ca ON ca.attribute.id = pav.attribute.id WHERE pav.product.id = :productId AND ca.isKeyAttribute = :isKeyAttribute")
    List<ProductAttributeValue> findByProductIdAndKeyAttribute(Long productId, Boolean isKeyAttribute);
    @Query("SELECT pav FROM ProductAttributeValue pav JOIN CategoryAttribute ca ON ca.attribute.id = pav.attribute.id WHERE pav.product.id = :productId AND ca.isKeyAttribute = :isKeyAttribute AND pav.isActive = :isActive")
    List<ProductAttributeValue> findByProductIdAndKeyAttributeAndIsActive(Long productId, Boolean isKeyAttribute, Boolean isActive);
    
    // Find by category
    List<ProductAttributeValue> findByProductCategoryId(Long categoryId);
    Page<ProductAttributeValue> findByProductCategoryId(Long categoryId, Pageable pageable);
    List<ProductAttributeValue> findByProductCategoryIdAndIsActive(Long categoryId, Boolean isActive);
    
    // Find by attribute type
    @Query("SELECT pav FROM ProductAttributeValue pav WHERE pav.attribute.type = :type")
    List<ProductAttributeValue> findByAttributeType(@Param("type") String type);
    @Query("SELECT pav FROM ProductAttributeValue pav WHERE pav.attribute.type = :type")
    Page<ProductAttributeValue> findByAttributeType(@Param("type") String type, Pageable pageable);
    @Query("SELECT pav FROM ProductAttributeValue pav WHERE pav.attribute.type = :type AND pav.isActive = :isActive")
    List<ProductAttributeValue> findByAttributeTypeAndIsActive(@Param("type") String type, @Param("isActive") Boolean isActive);
    
    // Count methods
    long countByProductId(Long productId);
    long countByProductIdAndIsActive(Long productId, Boolean isActive);
    long countByAttributeId(Long attributeId);
    long countByAttributeIdAndIsActive(Long attributeId, Boolean isActive);
    long countByProductCategoryId(Long categoryId);
    long countByProductCategoryIdAndIsActive(Long categoryId, Boolean isActive);
    
    // Advanced queries
    @Query("SELECT pav FROM ProductAttributeValue pav WHERE " +
           "(:productId IS NULL OR pav.product.id = :productId) AND " +
           "(:attributeId IS NULL OR pav.attribute.id = :attributeId) AND " +
           "(:value IS NULL OR LOWER(pav.attributeValue) LIKE LOWER(CONCAT('%', :value, '%'))) AND " +
           "(:isActive IS NULL OR pav.isActive = :isActive)")
    Page<ProductAttributeValue> findByMultipleCriteria(
            @Param("productId") Long productId,
            @Param("attributeId") Long attributeId,
            @Param("value") String value,
            @Param("isActive") Boolean isActive,
            Pageable pageable
    );
    
    @Query("SELECT pav FROM ProductAttributeValue pav JOIN CategoryAttribute ca ON ca.attribute.id = pav.attribute.id WHERE " +
           "pav.product.id = :productId AND " +
           "ca.isKeyAttribute = true AND " +
           "pav.isActive = true")
    List<ProductAttributeValue> findKeyAttributesByProduct(@Param("productId") Long productId);
    
    @Query("SELECT pav FROM ProductAttributeValue pav WHERE " +
           "pav.product.category.id = :categoryId AND " +
           "pav.attribute.name = :attributeName AND " +
           "pav.isActive = true")
    List<ProductAttributeValue> findByCategoryAndAttributeName(
            @Param("categoryId") Long categoryId,
            @Param("attributeName") String attributeName
    );
    
    @Query("SELECT DISTINCT pav.attributeValue FROM ProductAttributeValue pav WHERE " +
           "pav.attribute.id = :attributeId AND " +
           "pav.isActive = true")
    List<String> findDistinctValuesByAttribute(@Param("attributeId") Long attributeId);
    
    @Query("SELECT pav FROM ProductAttributeValue pav WHERE " +
           "pav.product.id = :productId AND " +
           "pav.attribute.type = :attributeType AND " +
           "pav.isActive = true")
    List<ProductAttributeValue> findByProductAndAttributeType(
            @Param("productId") Long productId,
            @Param("attributeType") String attributeType
    );
}
