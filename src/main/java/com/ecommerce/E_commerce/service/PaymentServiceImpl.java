package com.ecommerce.E_commerce.service;

import com.ecommerce.E_commerce.dto.payment.PaymentCreateDTO;
import com.ecommerce.E_commerce.dto.payment.PaymentDTO;
import com.ecommerce.E_commerce.dto.payment.PaymentUpdateDTO;
import com.ecommerce.E_commerce.exception.ResourceNotFoundException;
import com.ecommerce.E_commerce.mapper.PaymentMapper;
import com.ecommerce.E_commerce.model.Order;
import com.ecommerce.E_commerce.model.OrderStatus;
import com.ecommerce.E_commerce.model.Payment;
import com.ecommerce.E_commerce.model.PaymentMethod;
import com.ecommerce.E_commerce.model.PaymentStatus;
import com.ecommerce.E_commerce.repository.OrderRepository;
import com.ecommerce.E_commerce.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@Transactional
public class PaymentServiceImpl implements PaymentService {
    
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final PaymentMapper paymentMapper;
    
    @Autowired
    public PaymentServiceImpl(PaymentRepository paymentRepository,
                              OrderRepository orderRepository,
                              PaymentMapper paymentMapper) {
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
        this.paymentMapper = paymentMapper;
    }
    
    @Override
    public PaymentDTO create(PaymentCreateDTO dto) {
        // Validate order
        Order order = orderRepository.findById(dto.orderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + dto.orderId()));
        
        // Security check: User can only create payment for their own orders (unless OWNER)
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new AccessDeniedException("User not authenticated");
        }
        
        boolean isOwner = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(auth -> auth.equals("ROLE_OWNER"));
        
        if (!isOwner) {
            // Check if user is the owner of the order
            String userEmail = authentication.getName();
            if (order.getUser() == null || !order.getUser().getEmail().equals(userEmail)) {
                throw new AccessDeniedException("You can only create payments for your own orders");
            }
        }
        
        // Validate order status (can only pay for NEW or CONFIRMED orders)
        if (order.getStatus() != OrderStatus.NEW && order.getStatus() != OrderStatus.CONFIRMED) {
            throw new IllegalArgumentException("Payment can only be created for orders with status NEW or CONFIRMED");
        }
        
        // Validate amount matches order total
        if (dto.amount().compareTo(order.getTotalAmount()) != 0) {
            throw new IllegalArgumentException(
                String.format("Payment amount %.2f does not match order total %.2f",
                    dto.amount(), order.getTotalAmount()));
        }
        
        // Create payment
        Payment payment = paymentMapper.toPayment(dto);
        payment.setOrder(order);
        payment.setAmount(dto.amount());
        payment.setMethod(PaymentMethod.valueOf(dto.method().toUpperCase()));
        payment.setStatus(PaymentStatus.PENDING);
        payment.setTransactionId(dto.transactionId());
        payment.setNotes(dto.notes());
        
        Payment savedPayment = paymentRepository.save(payment);
        
        // Update order status to CONFIRMED if payment is completed
        // (This would typically be done by a payment gateway callback, but for now we'll handle it here)
        
        return paymentMapper.toPaymentDTO(savedPayment);
    }
    
    @Override
    public PaymentDTO update(Long id, PaymentUpdateDTO dto) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + id));
        
        PaymentStatus oldStatus = payment.getStatus();
        
        paymentMapper.updatePaymentFromDTO(dto, payment);
        
        // Update status if provided
        if (dto.status() != null) {
            payment.setStatus(PaymentStatus.valueOf(dto.status().toUpperCase()));
        }
        
        // Update isActive if provided
        if (dto.isActive() != null) {
            payment.setIsActive(dto.isActive());
        }
        
        PaymentStatus newStatus = payment.getStatus();
        
        // Handle order status update based on payment status
        if (oldStatus != newStatus) {
            handlePaymentStatusChange(payment, oldStatus, newStatus);
        }
        
        Payment updatedPayment = paymentRepository.save(payment);
        return paymentMapper.toPaymentDTO(updatedPayment);
    }
    
    private void handlePaymentStatusChange(Payment payment, PaymentStatus oldStatus, PaymentStatus newStatus) {
        Order order = payment.getOrder();
        
        // If payment is completed, update order status to CONFIRMED
        if (newStatus == PaymentStatus.COMPLETED && oldStatus != PaymentStatus.COMPLETED) {
            if (order.getStatus() == OrderStatus.NEW) {
                order.setStatus(OrderStatus.CONFIRMED);
                orderRepository.save(order);
            }
        }
        // If payment failed or cancelled, keep order as NEW (or handle accordingly)
        else if ((newStatus == PaymentStatus.FAILED || newStatus == PaymentStatus.CANCELLED) && 
                 oldStatus == PaymentStatus.PENDING) {
            // Order remains in NEW status, allowing retry
        }
    }
    
    @Override
    public void delete(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + id));
        
        // Soft delete
        payment.setDeletedAt(Instant.now());
        payment.setIsActive(false);
        paymentRepository.save(payment);
    }
    
    @Override
    public PaymentDTO getById(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + id));
        
        return paymentMapper.toPaymentDTO(payment);
    }
    
    @Override
    public Page<PaymentDTO> findByOrderId(Long orderId, Pageable pageable) {
        Page<Payment> payments = paymentRepository.findByOrderId(orderId, pageable);
        return payments.map(paymentMapper::toPaymentDTO);
    }
    
    @Override
    public Page<PaymentDTO> findByStatus(String status, Pageable pageable) {
        PaymentStatus paymentStatus = status != null ? PaymentStatus.valueOf(status.toUpperCase()) : null;
        Page<Payment> payments = paymentRepository.findByStatus(paymentStatus, pageable);
        return payments.map(paymentMapper::toPaymentDTO);
    }
    
    @Override
    public Page<PaymentDTO> findAll(Pageable pageable) {
        Page<Payment> payments = paymentRepository.findAll(pageable);
        return payments.map(paymentMapper::toPaymentDTO);
    }
    
    @Override
    public Page<PaymentDTO> findByOrderIdAndStatus(Long orderId, String status, Pageable pageable) {
        PaymentStatus paymentStatus = status != null ? PaymentStatus.valueOf(status.toUpperCase()) : null;
        Page<Payment> payments = paymentRepository.findByOrderIdAndStatus(orderId, paymentStatus, pageable);
        return payments.map(paymentMapper::toPaymentDTO);
    }
    
    @Override
    public Page<PaymentDTO> findByMultipleCriteria(Long orderId, String status, String method, Boolean isActive, Instant startDate, Instant endDate, Pageable pageable) {
        PaymentStatus paymentStatus = status != null ? PaymentStatus.valueOf(status.toUpperCase()) : null;
        PaymentMethod paymentMethod = method != null ? PaymentMethod.valueOf(method.toUpperCase()) : null;
        Page<Payment> payments = paymentRepository.findByMultipleCriteria(orderId, paymentStatus, paymentMethod, isActive, startDate, endDate, pageable);
        return payments.map(paymentMapper::toPaymentDTO);
    }
    
    @Override
    public long countByOrderId(Long orderId) {
        return paymentRepository.countByOrderId(orderId);
    }
    
    @Override
    public long countByStatus(String status) {
        PaymentStatus paymentStatus = status != null ? PaymentStatus.valueOf(status.toUpperCase()) : null;
        return paymentRepository.countByStatus(paymentStatus);
    }
    
    @Override
    public boolean isPaymentOwner(Long paymentId, String userEmail) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + paymentId));
        
        return payment.getOrder().getUser().getEmail().equals(userEmail);
    }
}

