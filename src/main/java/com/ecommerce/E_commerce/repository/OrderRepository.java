package com.ecommerce.E_commerce.repository;

import com.ecommerce.E_commerce.model.Order;
import com.ecommerce.E_commerce.model.OrderStatus;
import org.aspectj.weaver.ast.Or;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    
    // Find by user
    Page<Order> findByUserId(Long userId, Pageable pageable);
    List<Order> findByUserId(Long userId);
    
    // Find by status
    Page<Order> findByStatus(OrderStatus status, Pageable pageable);
    Page<Order> findByUserIdAndStatus(Long userId, OrderStatus status, Pageable pageable);
    
    // Find by date range
    Page<Order> findByCreatedAtBetween(Instant startDate, Instant endDate, Pageable pageable);
    Page<Order> findByUserIdAndCreatedAtBetween(Long userId, Instant startDate, Instant endDate, Pageable pageable);
    
    // Find active orders
    Page<Order> findByIsActive(Boolean isActive, Pageable pageable);
    Page<Order> findByUserIdAndIsActive(Long userId, Boolean isActive, Pageable pageable);

    long countByUserId(Long userId);
    long countByStatus(OrderStatus status);
    long countByUserIdAndStatus(Long userId, OrderStatus status);
    
    // Advanced queries
    @Query("SELECT o FROM Order o WHERE " +
           "(:userId IS NULL OR o.user.id = :userId) AND " +
           "(:status IS NULL OR o.status = :status) AND " +
           "(:isActive IS NULL OR o.isActive = :isActive) AND " +
           "(:startDate IS NULL OR o.createdAt >= :startDate) AND " +
           "(:endDate IS NULL OR o.createdAt <= :endDate)")
    Page<Order> findByMultipleCriteria(
            @Param("userId") Long userId,
            @Param("status") OrderStatus status,
            @Param("isActive") Boolean isActive,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate,
            Pageable pageable
    );
}

