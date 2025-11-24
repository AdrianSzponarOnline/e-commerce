package com.ecommerce.E_commerce.repository;

import com.ecommerce.E_commerce.model.Payment;
import com.ecommerce.E_commerce.model.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    
    // Find by order
    List<Payment> findByOrderId(Long orderId);
    Page<Payment> findByOrderId(Long orderId, Pageable pageable);
    
    // Find by user (through order)
    @Query("SELECT p FROM Payment p WHERE p.order.user.id = :userId")
    Page<Payment> findByUserId(@Param("userId") Long userId, Pageable pageable);
    
    // Find by status
    Page<Payment> findByStatus(PaymentStatus status, Pageable pageable);
    Page<Payment> findByOrderIdAndStatus(Long orderId, PaymentStatus status, Pageable pageable);
    
    // Find by payment method
    Page<Payment> findByMethod(com.ecommerce.E_commerce.model.PaymentMethod method, Pageable pageable);
    
    // Find by date range
    Page<Payment> findByTransactionDateBetween(Instant startDate, Instant endDate, Pageable pageable);
    Page<Payment> findByOrderIdAndTransactionDateBetween(Long orderId, Instant startDate, Instant endDate, Pageable pageable);
    
    // Find active payments
    Page<Payment> findByIsActive(Boolean isActive, Pageable pageable);
    Page<Payment> findByOrderIdAndIsActive(Long orderId, Boolean isActive, Pageable pageable);
    
    // Find by transaction ID
    Optional<Payment> findByTransactionId(String transactionId);
    
    // Count queries
    long countByOrderId(Long orderId);
    long countByStatus(PaymentStatus status);
    long countByOrderIdAndStatus(Long orderId, PaymentStatus status);
    
    // Advanced queries
    @Query("SELECT p FROM Payment p WHERE " +
           "(:orderId IS NULL OR p.order.id = :orderId) AND " +
           "(:status IS NULL OR p.status = :status) AND " +
           "(:method IS NULL OR p.method = :method) AND " +
           "(:isActive IS NULL OR p.isActive = :isActive) AND " +
           "(:startDate IS NULL OR p.transactionDate >= :startDate) AND " +
           "(:endDate IS NULL OR p.transactionDate <= :endDate)")
    Page<Payment> findByMultipleCriteria(
            @Param("orderId") Long orderId,
            @Param("status") PaymentStatus status,
            @Param("method") com.ecommerce.E_commerce.model.PaymentMethod method,
            @Param("isActive") Boolean isActive,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate,
            Pageable pageable
    );
}

