package com.ecommerce.E_commerce.repository;

import com.ecommerce.E_commerce.model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
}

