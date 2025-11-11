package com.ecommerce.E_commerce.service;

import com.ecommerce.E_commerce.dto.order.OrderCreateDTO;
import com.ecommerce.E_commerce.dto.order.OrderDTO;
import com.ecommerce.E_commerce.dto.order.OrderUpdateDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;

import java.time.Instant;

public interface OrderService {
    
    // CRUD Operations
    @PreAuthorize("hasRole('OWNER') or (hasRole('USER') and #userId == authentication.principal.id)")
    OrderDTO create(Long userId, OrderCreateDTO dto);
    
    @PreAuthorize("hasRole('OWNER')")
    OrderDTO update(Long id, OrderUpdateDTO dto);
    
    @PreAuthorize("hasRole('OWNER')")
    void delete(Long id);
    
    // Cancel order
    @PreAuthorize("hasRole('OWNER') or (hasRole('USER') and @orderService.isOrderOwner(#id, authentication.name))")
    OrderDTO cancelOrder(Long id);
    
    // Single Order Retrieval
    @PreAuthorize("hasRole('OWNER') or (hasRole('USER') and @orderService.isOrderOwner(#id, authentication.name))")
    OrderDTO getById(Long id);
    
    // Paginated Order Lists
    @PreAuthorize("hasRole('OWNER') or (hasRole('USER') and #userId == authentication.principal.id)")
    Page<OrderDTO> findByUserId(Long userId, Pageable pageable);
    
    @PreAuthorize("hasRole('OWNER')")
    Page<OrderDTO> findByStatus(String status, Pageable pageable);
    
    @PreAuthorize("hasRole('OWNER')")
    Page<OrderDTO> findAll(Pageable pageable);
    
    @PreAuthorize("hasRole('OWNER') or (hasRole('USER') and #userId == authentication.principal.id)")
    Page<OrderDTO> findByUserIdAndStatus(Long userId, String status, Pageable pageable);
    
    // Date range queries
    @PreAuthorize("hasRole('OWNER')")
    Page<OrderDTO> findByCreatedAtBetween(Instant startDate, Instant endDate, Pageable pageable);
    
    @PreAuthorize("hasRole('OWNER') or (hasRole('USER') and #userId == authentication.principal.id)")
    Page<OrderDTO> findByUserIdAndCreatedAtBetween(Long userId, Instant startDate, Instant endDate, Pageable pageable);
    
    // Advanced filtering
    @PreAuthorize("hasRole('OWNER')")
    Page<OrderDTO> findByMultipleCriteria(Long userId, String status, Boolean isActive, Instant startDate, Instant endDate, Pageable pageable);
    
    // Statistics
    @PreAuthorize("hasRole('OWNER') or (hasRole('USER') and #userId == authentication.principal.id)")
    long countByUserId(Long userId);
    
    @PreAuthorize("hasRole('OWNER')")
    long countByStatus(String status);
    
    // Helper method for security
    boolean isOrderOwner(Long orderId, String userEmail);
}

