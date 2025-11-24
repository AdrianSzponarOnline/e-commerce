package com.ecommerce.E_commerce.service;

import com.ecommerce.E_commerce.dto.payment.PaymentCreateDTO;
import com.ecommerce.E_commerce.dto.payment.PaymentDTO;
import com.ecommerce.E_commerce.dto.payment.PaymentUpdateDTO;
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
    
    @PreAuthorize("hasRole('OWNER') or (hasRole('USER') and @paymentService.isPaymentOwner(#id, authentication.name))")
    PaymentDTO getById(Long id);
    
    @PreAuthorize("hasRole('OWNER') or (hasRole('USER') and #orderId != null and @orderService.isOrderOwner(#orderId, authentication.name))")
    Page<PaymentDTO> findByOrderId(Long orderId, Pageable pageable);
    
    @PreAuthorize("hasRole('OWNER') or (hasRole('USER') and #userId == authentication.principal.id)")
    Page<PaymentDTO> findByUserId(Long userId, Pageable pageable);
    
    @PreAuthorize("hasRole('OWNER')")
    Page<PaymentDTO> findByStatus(String status, Pageable pageable);
    
    @PreAuthorize("hasRole('OWNER')")
    Page<PaymentDTO> findAll(Pageable pageable);
    
    @PreAuthorize("hasRole('OWNER') or (hasRole('USER') and #orderId != null and @orderService.isOrderOwner(#orderId, authentication.name))")
    Page<PaymentDTO> findByOrderIdAndStatus(Long orderId, String status, Pageable pageable);
    
    @PreAuthorize("hasRole('OWNER')")
    Page<PaymentDTO> findByMultipleCriteria(Long orderId, String status, String method, Boolean isActive, Instant startDate, Instant endDate, Pageable pageable);
    
    @PreAuthorize("hasRole('OWNER') or (hasRole('USER') and #orderId != null and @orderService.isOrderOwner(#orderId, authentication.name))")
    long countByOrderId(Long orderId);
    
    @PreAuthorize("hasRole('OWNER')")
    long countByStatus(String status);

    PaymentDTO simulatePayment(Long paymentId, String scenario);
    
    boolean isPaymentOwner(Long paymentId, String userEmail);
}

