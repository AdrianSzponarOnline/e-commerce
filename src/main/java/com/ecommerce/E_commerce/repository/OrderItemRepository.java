package com.ecommerce.E_commerce.repository;

import com.ecommerce.E_commerce.model.OrderItem;
import com.ecommerce.E_commerce.model.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    
    // Find by order
    List<OrderItem> findByOrderId(Long orderId);
    
    // Find by product
    List<OrderItem> findByProductId(Long productId);
    
    // Count queries
    long countByOrderId(Long orderId);
    long countByProductId(Long productId);
    
    // Advanced query - find items for multiple orders
    @Query("SELECT oi FROM OrderItem oi WHERE oi.order.id IN :orderIds")
    List<OrderItem> findByOrderIds(@Param("orderIds") List<Long> orderIds);
    
    // Statistics queries
    @Query("SELECT oi.product.id, oi.product.name, oi.product.sku, " +
           "SUM(oi.quantity) as totalQuantity, " +
           "SUM(oi.price * oi.quantity) as totalRevenue, " +
           "COUNT(DISTINCT oi.order.id) as orderCount " +
           "FROM OrderItem oi " +
           "WHERE oi.order.status IN :statuses " +
           "AND oi.order.createdAt >= :startDate " +
           "AND oi.order.createdAt <= :endDate " +
           "AND oi.isActive = true " +
           "GROUP BY oi.product.id, oi.product.name, oi.product.sku " +
           "ORDER BY totalQuantity DESC")
    List<Object[]> findTopProductsByQuantity(
            @Param("statuses") List<OrderStatus> statuses,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate
    );
    
    @Query("SELECT oi.product.id, oi.product.name, oi.product.sku, " +
           "SUM(oi.quantity) as totalQuantity, " +
           "SUM(oi.price * oi.quantity) as totalRevenue, " +
           "COUNT(DISTINCT oi.order.id) as orderCount " +
           "FROM OrderItem oi " +
           "WHERE oi.order.status IN :statuses " +
           "AND oi.order.createdAt >= :startDate " +
           "AND oi.order.createdAt <= :endDate " +
           "AND oi.isActive = true " +
           "GROUP BY oi.product.id, oi.product.name, oi.product.sku " +
           "ORDER BY totalRevenue DESC")
    List<Object[]> findTopProductsByRevenue(
            @Param("statuses") List<OrderStatus> statuses,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate
    );
    
    @Query("SELECT SUM(oi.quantity) " +
           "FROM OrderItem oi " +
           "WHERE oi.order.status IN :statuses " +
           "AND oi.order.createdAt >= :startDate " +
           "AND oi.order.createdAt <= :endDate " +
           "AND oi.isActive = true")
    Long countTotalProductsSold(
            @Param("statuses") List<OrderStatus> statuses,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate
    );
}

