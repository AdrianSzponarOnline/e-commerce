package com.ecommerce.E_commerce.service;

import com.ecommerce.E_commerce.dto.payment.PaymentCreateDTO;
import com.ecommerce.E_commerce.dto.payment.PaymentDTO;
import com.ecommerce.E_commerce.dto.payment.PaymentUpdateDTO;
import com.ecommerce.E_commerce.model.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;

import java.time.Instant;

public interface PaymentService {
    
    @PreAuthorize("hasRole('USER') or hasRole('OWNER')")
    PaymentDTO create(PaymentCreateDTO dto);
    
    @PreAuthorize("hasRole('OWNER')")
    PaymentDTO update(Long id, PaymentUpdateDTO dto);
    
    @PreAuthorize("hasRole('OWNER')")
    void delete(Long id);
    
    PaymentDTO getById(Long id);
    
    Page<PaymentDTO> findByOrderId(Long orderId, Pageable pageable);
    
    Page<PaymentDTO> findByUserId(Long userId, Pageable pageable);
    
    Page<PaymentDTO> findByStatus(PaymentStatus status, Pageable pageable);
    
    Page<PaymentDTO> findAll(Pageable pageable);
    
    Page<PaymentDTO> findByOrderIdAndStatus(Long orderId, String status, Pageable pageable);
    
    Page<PaymentDTO> findByMultipleCriteria(Long orderId, String status, String method, Boolean isActive, Instant startDate, Instant endDate, Pageable pageable);
    
    long countByOrderId(Long orderId);
    
    long countByStatus(String status);

    PaymentDTO simulatePayment(Long paymentId, String scenario);
    
    boolean isPaymentOwner(Long paymentId, String userEmail);
}

