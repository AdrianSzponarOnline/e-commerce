package com.ecommerce.E_commerce.controller;

import com.ecommerce.E_commerce.dto.order.OrderCreateDTO;
import com.ecommerce.E_commerce.dto.order.OrderDTO;
import com.ecommerce.E_commerce.dto.order.OrderUpdateDTO;
import com.ecommerce.E_commerce.model.OrderStatus;
import com.ecommerce.E_commerce.model.User;
import com.ecommerce.E_commerce.service.OrderService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
public class OrderController {
    
    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);
    private final OrderService orderService;
    
    @Autowired
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }
    
    @PostMapping
    @PreAuthorize("hasRole('USER') or hasRole('OWNER')")
    public ResponseEntity<OrderDTO> createOrder(
            @Valid @RequestBody OrderCreateDTO dto,
            @AuthenticationPrincipal User user) {
        logger.info("POST /api/orders - Creating order for userId={}, itemsCount={}", user.getId(), dto.items() != null ? dto.items().size() : 0);
        OrderDTO order = orderService.create(user.getId(), dto);
        logger.info("POST /api/orders - Order created successfully: orderId={}, userId={}, total={}", order.id(), user.getId(), order.totalAmount());
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<OrderDTO> updateOrder(
            @PathVariable Long id,
            @Valid @RequestBody OrderUpdateDTO dto) {
        logger.info("PUT /api/orders/{} - Updating order, newStatus={}", id, dto.status());
        OrderDTO order = orderService.update(id, dto);
        logger.info("PUT /api/orders/{} - Order updated successfully, status={}", id, order.status());
        return ResponseEntity.ok(order);
    }
    
    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasRole('OWNER') or (hasRole('USER') and @orderServiceImpl.isOrderOwner(#id, authentication.name))")
    public ResponseEntity<OrderDTO> cancelOrder(@PathVariable Long id) {
        logger.info("PATCH /api/orders/{}/cancel - Cancelling order", id);
        OrderDTO order = orderService.cancelOrder(id);
        logger.info("PATCH /api/orders/{}/cancel - Order cancelled successfully", id);
        return ResponseEntity.ok(order);
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id) {
        logger.info("DELETE /api/orders/{} - Deleting order", id);
        orderService.delete(id);
        logger.info("DELETE /api/orders/{} - Order deleted successfully", id);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('OWNER') or (hasRole('USER') and @orderServiceImpl.isOrderOwner(#id, authentication.name))")
    public ResponseEntity<OrderDTO> getOrderById(@PathVariable Long id) {
        logger.debug("GET /api/orders/{} - Retrieving order", id);
        OrderDTO order = orderService.getById(id);
        logger.debug("GET /api/orders/{} - Order retrieved: status={}, total={}", id, order.status(), order.totalAmount());
        return ResponseEntity.ok(order);
    }
    
    @GetMapping
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<Page<OrderDTO>> getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<OrderDTO> orders = orderService.findAll(pageable);
        return ResponseEntity.ok(orders);
    }
    
    @GetMapping("/me")
    @PreAuthorize("hasRole('USER') or hasRole('OWNER')")
    public ResponseEntity<Page<OrderDTO>> getMyOrders(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<OrderDTO> orders = orderService.findByUserId(user.getId(), pageable);
        return ResponseEntity.ok(orders);
    }
    
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('OWNER') or (hasRole('USER') and #userId == authentication.principal.id)")
    public ResponseEntity<Page<OrderDTO>> getOrdersByUserId(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<OrderDTO> orders = orderService.findByUserId(userId, pageable);
        return ResponseEntity.ok(orders);
    }
    
    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<Page<OrderDTO>> getOrdersByStatus(
            @PathVariable OrderStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<OrderDTO> orders = orderService.findByStatus(status, pageable);
        return ResponseEntity.ok(orders);
    }
    
    @GetMapping("/user/{userId}/status/{status}")
    @PreAuthorize("hasRole('OWNER') or (hasRole('USER') and #userId == authentication.principal.id)")
    public ResponseEntity<Page<OrderDTO>> getOrdersByUserIdAndStatus(
            @PathVariable Long userId,
            @PathVariable OrderStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<OrderDTO> orders = orderService.findByUserIdAndStatus(userId, status, pageable);
        return ResponseEntity.ok(orders);
    }
    
    @GetMapping("/filter")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<Page<OrderDTO>> filterOrders(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<OrderDTO> orders = orderService.findByMultipleCriteria(userId, status, isActive, startDate, endDate, pageable);
        return ResponseEntity.ok(orders);
    }
    
    @GetMapping("/stats/count")
    @PreAuthorize("hasRole('OWNER') or (hasRole('USER') and #userId == authentication.principal.id)")
    public ResponseEntity<Long> getOrderCount(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) OrderStatus status) {
        long count;
        if (userId != null && status != null) {
            count = orderService.countByUserIdAndStatus(userId, status);
        } else if (userId != null) {
            count = orderService.countByUserId(userId);
        } else if (status != null) {
            count = orderService.countByStatus(status);
        } else {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(count);
    }
}

