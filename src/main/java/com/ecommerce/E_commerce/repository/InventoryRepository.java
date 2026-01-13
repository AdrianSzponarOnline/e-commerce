package com.ecommerce.E_commerce.repository;

import com.ecommerce.E_commerce.model.Inventory;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<Inventory> findByProductIdIn(Collection<Long> productIds);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM Inventory i WHERE i.product.id = :productId")
    Optional<Inventory> findByProductIdWithLock(@Param("productId") Long productId);



    Optional<Inventory> findByProductId(Long productId);
    Optional<Inventory> findByProductIdAndIsActive(Long productId, Boolean isActive);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM Inventory i WHERE i.product.id = :productId AND i.isActive = :isActive")
    Optional<Inventory> findByProductIdAndIsActiveWithLock(@Param("productId") Long productId, @Param("isActive") Boolean isActive);
    
    // Find by quantity ranges
    Page<Inventory> findByAvailableQuantityBetween(Integer minAvailable, Integer maxAvailable, Pageable pageable);
    Page<Inventory> findByReservedQuantityBetween(Integer minReserved, Integer maxReserved, Pageable pageable);
    
    // Find by stock level
    Page<Inventory> findByAvailableQuantityLessThan(Integer quantity, Pageable pageable);
    Page<Inventory> findByAvailableQuantityGreaterThan(Integer quantity, Pageable pageable);
    
    // Find by active status
    Page<Inventory> findByIsActive(Boolean isActive, Pageable pageable);
    
    // Find below minimum stock level
    @Query("SELECT i FROM Inventory i WHERE i.availableQuantity < i.minimumStockLevel AND (:isActive IS NULL OR i.isActive = :isActive)")
    Page<Inventory> findBelowMinimumStockLevel(@Param("isActive") Boolean isActive, Pageable pageable);
    
    // Find by product name
    @Query("SELECT i FROM Inventory i WHERE LOWER(i.product.name) LIKE LOWER(CONCAT('%', :productName, '%')) AND (:isActive IS NULL OR i.isActive = :isActive)")
    Page<Inventory> findByProductNameContaining(@Param("productName") String productName, @Param("isActive") Boolean isActive, Pageable pageable);
    
    // Count methods
    long countByIsActive(Boolean isActive);
    long countByAvailableQuantityLessThan(Integer quantity);
    
    // Advanced search with multiple criteria
    @Query("SELECT i FROM Inventory i WHERE " +
           "(:productId IS NULL OR i.product.id = :productId) AND " +
           "(:productName IS NULL OR LOWER(i.product.name) LIKE LOWER(CONCAT('%', :productName, '%'))) AND " +
           "(:minAvailableQuantity IS NULL OR i.availableQuantity >= :minAvailableQuantity) AND " +
           "(:maxAvailableQuantity IS NULL OR i.availableQuantity <= :maxAvailableQuantity) AND " +
           "(:minReservedQuantity IS NULL OR i.reservedQuantity >= :minReservedQuantity) AND " +
           "(:maxReservedQuantity IS NULL OR i.reservedQuantity <= :maxReservedQuantity) AND " +
           "(:belowMinimum IS NULL OR " +
           "  (:belowMinimum = true AND i.availableQuantity < i.minimumStockLevel) OR " +
           "  (:belowMinimum = false AND i.availableQuantity >= i.minimumStockLevel)) AND " +
           "(:isActive IS NULL OR i.isActive = :isActive)")
    Page<Inventory> findByMultipleCriteria(
            @Param("productId") Long productId,
            @Param("productName") String productName,
            @Param("minAvailableQuantity") Integer minAvailableQuantity,
            @Param("maxAvailableQuantity") Integer maxAvailableQuantity,
            @Param("minReservedQuantity") Integer minReservedQuantity,
            @Param("maxReservedQuantity") Integer maxReservedQuantity,
            @Param("belowMinimum") Boolean belowMinimum,
            @Param("isActive") Boolean isActive,
            Pageable pageable
    );
    
    // Find by product SKU
    @Query("SELECT i FROM Inventory i WHERE i.product.sku = :sku AND (:isActive IS NULL OR i.isActive = :isActive)")
    Optional<Inventory> findByProductSku(@Param("sku") String sku, @Param("isActive") Boolean isActive);
}

