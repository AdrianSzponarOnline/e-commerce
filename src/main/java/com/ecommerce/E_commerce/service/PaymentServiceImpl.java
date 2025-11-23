package com.ecommerce.E_commerce.service;

import com.ecommerce.E_commerce.dto.payment.PaymentCreateDTO;
import com.ecommerce.E_commerce.dto.payment.PaymentDTO;
import com.ecommerce.E_commerce.dto.payment.PaymentUpdateDTO;
import com.ecommerce.E_commerce.exception.InvalidOperationException;
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
        Order order = orderRepository.findById(dto.orderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + dto.orderId()));
        
        checkPaymentPermission(order);
        
        if (order.getStatus() != OrderStatus.NEW && order.getStatus() != OrderStatus.CONFIRMED) {
            throw new InvalidOperationException("Payment can only be created for orders with status NEW or CONFIRMED");
        }
        
        if (dto.amount().compareTo(order.getTotalAmount()) != 0) {
            throw new InvalidOperationException(
                String.format("Payment amount %.2f does not match order total %.2f",
                    dto.amount(), order.getTotalAmount()));
        }
        
        Payment payment = paymentMapper.toPayment(dto);
        payment.setOrder(order);
        payment.setAmount(dto.amount());
        payment.setMethod(PaymentMethod.valueOf(dto.method().toUpperCase()));
        payment.setStatus(PaymentStatus.PENDING);
        payment.setTransactionId(dto.transactionId());
        payment.setNotes(dto.notes());
        
        Payment savedPayment = paymentRepository.save(payment);
        return paymentMapper.toPaymentDTO(savedPayment);
    }
    
    @Override
    public PaymentDTO update(Long id, PaymentUpdateDTO dto) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + id));
        
        checkPaymentPermission(payment.getOrder());
        
        PaymentStatus oldStatus = payment.getStatus();
        
        paymentMapper.updatePaymentFromDTO(dto, payment);
        
        if (dto.status() != null) {
            payment.setStatus(PaymentStatus.valueOf(dto.status().toUpperCase()));
        }
        
        if (dto.isActive() != null) {
            payment.setIsActive(dto.isActive());
        }
        
        PaymentStatus newStatus = payment.getStatus();
        
        if (oldStatus != newStatus) {
            handlePaymentStatusChange(payment, oldStatus, newStatus);
        }
        
        Payment updatedPayment = paymentRepository.save(payment);
        return paymentMapper.toPaymentDTO(updatedPayment);
    }
    
    private void handlePaymentStatusChange(Payment payment, PaymentStatus oldStatus, PaymentStatus newStatus) {
        Order order = payment.getOrder();
        
    
        if (order.getStatus() == OrderStatus.SHIPPED || order.getStatus() == OrderStatus.DELIVERED) {
            return;
        }
        
        if (newStatus == PaymentStatus.COMPLETED && oldStatus != PaymentStatus.COMPLETED) {
            if (order.getStatus() == OrderStatus.NEW) {
                order.setStatus(OrderStatus.CONFIRMED);
                orderRepository.save(order);
            }
        }
        else if ((newStatus == PaymentStatus.FAILED || newStatus == PaymentStatus.CANCELLED) && 
                 oldStatus == PaymentStatus.PENDING) {
        }
    }
    
    @Override
    public void delete(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + id));
        
        checkPaymentPermission(payment.getOrder());
        
        paymentRepository.delete(payment);
    }
    

    private void checkPaymentPermission(Order order) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new AccessDeniedException("User not authenticated");
        }
        
        boolean isOwner = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(auth -> auth.equals("ROLE_OWNER"));
        
        if (!isOwner) {
            String userEmail = authentication.getName();
            if (order.getUser() == null || !order.getUser().getEmail().equals(userEmail)) {
                throw new AccessDeniedException("You can only access payments for your own orders");
            }
        }
    }
    


    @Override
    @Transactional(readOnly = true)
    public PaymentDTO getById(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + id));
        
        checkPaymentPermission(payment.getOrder());
        
        return paymentMapper.toPaymentDTO(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentDTO> findByOrderId(Long orderId, Pageable pageable) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
        
        checkPaymentPermission(order);
        
        Page<Payment> payments = paymentRepository.findByOrderId(orderId, pageable);
        return payments.map(paymentMapper::toPaymentDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentDTO> findByStatus(String status, Pageable pageable) {
        PaymentStatus paymentStatus = status != null ? PaymentStatus.valueOf(status.toUpperCase()) : null;
        Page<Payment> payments = paymentRepository.findByStatus(paymentStatus, pageable);
        return payments.map(paymentMapper::toPaymentDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentDTO> findAll(Pageable pageable) {
        Page<Payment> payments = paymentRepository.findAll(pageable);
        return payments.map(paymentMapper::toPaymentDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentDTO> findByOrderIdAndStatus(Long orderId, String status, Pageable pageable) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
        
        checkPaymentPermission(order);
        
        PaymentStatus paymentStatus = status != null ? PaymentStatus.valueOf(status.toUpperCase()) : null;
        Page<Payment> payments = paymentRepository.findByOrderIdAndStatus(orderId, paymentStatus, pageable);
        return payments.map(paymentMapper::toPaymentDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentDTO> findByMultipleCriteria(Long orderId, String status, String method, Boolean isActive, Instant startDate, Instant endDate, Pageable pageable) {
        if (orderId != null) {
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
            checkPaymentPermission(order);
        }
        
        PaymentStatus paymentStatus = status != null ? PaymentStatus.valueOf(status.toUpperCase()) : null;
        PaymentMethod paymentMethod = method != null ? PaymentMethod.valueOf(method.toUpperCase()) : null;
        Page<Payment> payments = paymentRepository.findByMultipleCriteria(orderId, paymentStatus, paymentMethod, isActive, startDate, endDate, pageable);
        return payments.map(paymentMapper::toPaymentDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public long countByOrderId(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
        
        checkPaymentPermission(order);
        
        return paymentRepository.countByOrderId(orderId);
    }

    @Override
    @Transactional(readOnly = true)
    public long countByStatus(String status) {
        PaymentStatus paymentStatus = status != null ? PaymentStatus.valueOf(status.toUpperCase()) : null;
        return paymentRepository.countByStatus(paymentStatus);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isPaymentOwner(Long paymentId, String userEmail) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + paymentId));
        
        return payment.getOrder().getUser().getEmail().equals(userEmail);
    }
}

