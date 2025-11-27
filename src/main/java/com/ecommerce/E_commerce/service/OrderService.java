package com.ecommerce.E_commerce.service;

import com.ecommerce.E_commerce.dto.order.OrderCreateDTO;
import com.ecommerce.E_commerce.dto.order.OrderDTO;
import com.ecommerce.E_commerce.dto.order.OrderUpdateDTO;
import com.ecommerce.E_commerce.model.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;

import java.time.Instant;

public interface OrderService {
    
    OrderDTO create(Long userId, OrderCreateDTO dto);
    
    OrderDTO update(Long id, OrderUpdateDTO dto);
    
    void delete(Long id);
    
    OrderDTO cancelOrder(Long id);
    
    OrderDTO getById(Long id);
    
    Page<OrderDTO> findByUserId(Long userId, Pageable pageable);
    
    Page<OrderDTO> findByStatus(OrderStatus status, Pageable pageable);
    
    Page<OrderDTO> findAll(Pageable pageable);
    
    Page<OrderDTO> findByUserIdAndStatus(Long userId, OrderStatus status, Pageable pageable);
    
    Page<OrderDTO> findByCreatedAtBetween(Instant startDate, Instant endDate, Pageable pageable);
    
    Page<OrderDTO> findByUserIdAndCreatedAtBetween(Long userId, Instant startDate, Instant endDate, Pageable pageable);
    
    Page<OrderDTO> findByMultipleCriteria(Long userId, OrderStatus status, Boolean isActive, Instant startDate, Instant endDate, Pageable pageable);
    
    long countByUserId(Long userId);
    
    long countByStatus(OrderStatus status);

    long countByUserIdAndStatus(Long userId, OrderStatus status);

    boolean isOrderOwner(Long orderId, String userEmail);
}

