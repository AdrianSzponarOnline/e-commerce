package com.ecommerce.E_commerce.repository;

import com.ecommerce.E_commerce.model.Order;
import com.ecommerce.E_commerce.model.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

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
    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.user WHERE o.id = :id")
    Optional<Order> findByIdWithUser(@Param("id") Long id);
    
    @Query("SELECT DISTINCT o FROM Order o " +
           "LEFT JOIN FETCH o.user " +
           "LEFT JOIN FETCH o.address " +
           "LEFT JOIN FETCH o.items i " +
           "LEFT JOIN FETCH i.product " +
           "WHERE o.id = :id")
    Optional<Order> findByIdWithDetails(@Param("id") Long id);
    Page<Order> findByIsActive(Boolean isActive, Pageable pageable);
    Page<Order> findByUserIdAndIsActive(Long userId, Boolean isActive, Pageable pageable);

    long countByUserId(Long userId);
    long countByStatus(OrderStatus status);
    long countByUserIdAndStatus(Long userId, OrderStatus status);
    

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
    

    @Query("SELECT SUM(o.totalAmount) " +
           "FROM Order o " +
           "WHERE o.status IN :statuses " +
           "AND o.createdAt >= :startDate " +
           "AND o.createdAt <= :endDate " +
           "AND o.isActive = true")
    BigDecimal calculateTotalRevenue(
            @Param("statuses") List<OrderStatus> statuses,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate
    );
    
    @Query("SELECT COUNT(o) " +
           "FROM Order o " +
           "WHERE o.status IN :statuses " +
           "AND o.createdAt >= :startDate " +
           "AND o.createdAt <= :endDate " +
           "AND o.isActive = true")
    Long countOrders(
            @Param("statuses") List<OrderStatus> statuses,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate
    );
    
    @Query("SELECT AVG(o.totalAmount) " +
           "FROM Order o " +
           "WHERE o.status IN :statuses " +
           "AND o.createdAt >= :startDate " +
           "AND o.createdAt <= :endDate " +
           "AND o.isActive = true")
    BigDecimal calculateAverageOrderValue(
            @Param("statuses") List<OrderStatus> statuses,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate
    );
    
    @Query("SELECT EXTRACT(YEAR FROM o.createdAt) as year, " +
           "EXTRACT(MONTH FROM o.createdAt) as month, " +
           "SUM(o.totalAmount) as totalRevenue, " +
           "COUNT(o) as totalOrders " +
           "FROM Order o " +
           "WHERE o.status IN :statuses " +
           "AND o.createdAt >= :startDate " +
           "AND o.createdAt <= :endDate " +
           "AND o.isActive = true " +
           "GROUP BY EXTRACT(YEAR FROM o.createdAt), EXTRACT(MONTH FROM o.createdAt) " +
           "ORDER BY year DESC, month DESC")
    List<Object[]> findMonthlySales(
            @Param("statuses") List<OrderStatus> statuses,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate
    );
}

