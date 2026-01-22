package com.ecommerce.E_commerce.service;

import com.ecommerce.E_commerce.dto.payment.GuestPaymentCreateDTO;
import com.ecommerce.E_commerce.dto.payment.PaymentCreateDTO;
import com.ecommerce.E_commerce.dto.payment.PaymentDTO;
import com.ecommerce.E_commerce.dto.payment.PaymentUpdateDTO;
import com.ecommerce.E_commerce.event.PaymentStatusChangedEvent;
import com.ecommerce.E_commerce.exception.InvalidOperationException;
import com.ecommerce.E_commerce.exception.ResourceNotFoundException;
import com.ecommerce.E_commerce.mapper.PaymentMapper;
import com.ecommerce.E_commerce.model.*;
import com.ecommerce.E_commerce.repository.OrderRepository;
import com.ecommerce.E_commerce.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentServiceImpl.class);
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final PaymentMapper paymentMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public PaymentDTO create(PaymentCreateDTO dto) {
        logger.info("Creating payment: orderId={}, amount={}, method={}", dto.orderId(), dto.amount(), dto.method());
        Order order = orderRepository.findById(dto.orderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + dto.orderId()));

        if (order.getStatus() != OrderStatus.NEW && order.getStatus() != OrderStatus.CONFIRMED) {
            logger.warn("Attempted to create payment for order with invalid status: orderId={}, status={}",
                    dto.orderId(), order.getStatus());
            throw new InvalidOperationException("Payment can only be created for orders with status NEW or CONFIRMED");
        }

        if (dto.amount().compareTo(order.getTotalAmount()) != 0) {
            logger.warn("Payment amount mismatch: orderId={}, paymentAmount={}, orderTotal={}",
                    dto.orderId(), dto.amount(), order.getTotalAmount());
            throw new InvalidOperationException(
                    String.format("Payment amount %.2f does not match order total %.2f",
                            dto.amount(), order.getTotalAmount()));
        }

        Payment payment = paymentMapper.toPayment(dto);
        payment.setOrder(order);
        payment.setAmount(dto.amount());

        payment.setMethod(dto.method());

        payment.setStatus(PaymentStatus.PENDING);
        payment.setTransactionId(dto.transactionId());
        payment.setNotes(dto.notes());

        Payment savedPayment = paymentRepository.save(payment);
        logger.info("Payment created successfully: paymentId={}, orderId={}, status={}",
                savedPayment.getId(), dto.orderId(), savedPayment.getStatus());
        return paymentMapper.toPaymentDTO(savedPayment);
    }

    @Override
    public PaymentDTO createGuestPayment(GuestPaymentCreateDTO dto) {
        logger.info("Creating guest payment: orderId={}, email={}, amount={}, method={}", 
                dto.orderId(), dto.email(), dto.amount(), dto.method());
        
        Order order = orderRepository.findById(dto.orderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + dto.orderId()));

        // Verify that this is a guest order
        if (order.getUser() != null) {
            logger.warn("Attempted to create guest payment for registered user order: orderId={}", dto.orderId());
            throw new InvalidOperationException("This endpoint is only for guest orders. Use /api/payments for registered users.");
        }

        // Verify email matches order guest email
        if (order.getGuestEmail() == null || !order.getGuestEmail().equalsIgnoreCase(dto.email())) {
            logger.warn("Email mismatch for guest payment: orderId={}, providedEmail={}, orderEmail={}", 
                    dto.orderId(), dto.email(), order.getGuestEmail());
            throw new InvalidOperationException("Email does not match the order email");
        }

        if (order.getStatus() != OrderStatus.NEW && order.getStatus() != OrderStatus.CONFIRMED) {
            logger.warn("Attempted to create payment for order with invalid status: orderId={}, status={}",
                    dto.orderId(), order.getStatus());
            throw new InvalidOperationException("Payment can only be created for orders with status NEW or CONFIRMED");
        }

        if (dto.amount().compareTo(order.getTotalAmount()) != 0) {
            logger.warn("Payment amount mismatch: orderId={}, paymentAmount={}, orderTotal={}",
                    dto.orderId(), dto.amount(), order.getTotalAmount());
            throw new InvalidOperationException(
                    String.format("Payment amount %.2f does not match order total %.2f",
                            dto.amount(), order.getTotalAmount()));
        }

        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setAmount(dto.amount());
        payment.setMethod(dto.method());
        payment.setStatus(PaymentStatus.PENDING);
        payment.setTransactionId(dto.transactionId());
        payment.setNotes(dto.notes());

        Payment savedPayment = paymentRepository.save(payment);
        logger.info("Guest payment created successfully: paymentId={}, orderId={}, status={}",
                savedPayment.getId(), dto.orderId(), savedPayment.getStatus());
        return paymentMapper.toPaymentDTO(savedPayment);
    }

    @Override
    public PaymentDTO update(Long id, PaymentUpdateDTO dto) {
        logger.info("Updating payment: paymentId={}, newStatus={}", id, dto.status());
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + id));

        PaymentStatus oldStatus = payment.getStatus();

        paymentMapper.updatePaymentFromDTO(dto, payment);

        if (dto.status() != null) {
            payment.setStatus(dto.status());
        }

        PaymentStatus newStatus = payment.getStatus();

        if (oldStatus != newStatus) {
            logger.info("Payment status changed: paymentId={}, oldStatus={}, newStatus={}", id, oldStatus, newStatus);
            handlePaymentStatusChange(payment, oldStatus, newStatus);
        }

        Payment updatedPayment = paymentRepository.save(payment);
        logger.info("Payment updated successfully: paymentId={}, status={}", id, updatedPayment.getStatus());
        return paymentMapper.toPaymentDTO(updatedPayment);
    }


    private void handlePaymentStatusChange(Payment payment, PaymentStatus oldStatus, PaymentStatus newStatus) {
        logger.info("Payment status transition: paymentId={} [{} -> {}]",
                payment.getId(), oldStatus, newStatus);

        logger.debug("Publishing PaymentStatusChangedEvent for orderId={}", payment.getOrder().getId());

        eventPublisher.publishEvent(new PaymentStatusChangedEvent(
                payment.getId(),
                payment.getOrder().getId(),
                oldStatus,
                newStatus
        ));
    }


    @Override
    @Transactional
    public void delete(Long id) {
        logger.info("Deleting payment: paymentId={}", id);
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + id));

        paymentRepository.delete(payment);
        logger.info("Payment deleted successfully: paymentId={}", id);
    }


    @Override
    @Transactional(readOnly = true)
    public PaymentDTO getById(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + id));

        return paymentMapper.toPaymentDTO(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentDTO> findByOrderId(Long orderId, Pageable pageable) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        Page<Payment> payments = paymentRepository.findByOrderId(orderId, pageable);
        return payments.map(paymentMapper::toPaymentDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentDTO> findByUserId(Long userId, Pageable pageable) {
        Page<Payment> payments = paymentRepository.findByUserId(userId, pageable);
        return payments.map(paymentMapper::toPaymentDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentDTO> findByStatus(PaymentStatus status, Pageable pageable) {
        Page<Payment> payments = paymentRepository.findByStatus(status, pageable);
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

        return paymentRepository.countByOrderId(orderId);
    }

    @Override
    @Transactional(readOnly = true)
    public long countByStatus(String status) {
        PaymentStatus paymentStatus = status != null ? PaymentStatus.valueOf(status.toUpperCase()) : null;
        return paymentRepository.countByStatus(paymentStatus);
    }
    
    @Override
    @Transactional
    public PaymentDTO simulatePayment(Long paymentId, String scenario) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));


        if (payment.getStatus() == PaymentStatus.COMPLETED) {
            throw new InvalidOperationException("Payment is already completed");
        }

        PaymentStatus newStatus;
        String transactionId = "MOCK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        String notes = switch (scenario.toUpperCase()) {
            case "SUCCESS" -> {
                newStatus = PaymentStatus.COMPLETED;
                yield "Symulacja: Płatność udana (Bank OK)";
            }
            case "FAIL" -> {
                newStatus = PaymentStatus.FAILED;
                yield "Symulacja: Odmowa banku (Brak środków)";
            }
            case "ERROR" -> {
                newStatus = PaymentStatus.FAILED;
                yield "Symulacja: Błąd połączenia z bankiem";
            }
            default -> throw new InvalidOperationException("Unknown scenario: " + scenario);
        };

        PaymentStatus oldStatus = payment.getStatus();

        payment.setStatus(newStatus);
        payment.setTransactionId(transactionId);
        payment.setNotes(notes);
        if (payment.getMethod() == null) payment.setMethod(PaymentMethod.CREDIT_CARD);

        if (oldStatus != newStatus) {
            handlePaymentStatusChange(payment, oldStatus, newStatus);
        }

        return paymentMapper.toPaymentDTO(paymentRepository.save(payment));
    }

    @Override
    @Transactional
    public PaymentDTO simulateGuestPayment(Long paymentId, String email, String scenario) {
        logger.info("Simulating guest payment: paymentId={}, email={}, scenario={}", paymentId, email, scenario);
        
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));

        Order order = payment.getOrder();
        
        if (order.getUser() != null) {
            logger.warn("Attempted to simulate guest payment for registered user order: paymentId={}", paymentId);
            throw new InvalidOperationException("This endpoint is only for guest payments. Use /api/payments/{paymentId}/simulate for registered users.");
        }

        if (order.getGuestEmail() == null || !order.getGuestEmail().equalsIgnoreCase(email)) {
            logger.warn("Email mismatch for guest payment simulation: paymentId={}, providedEmail={}, orderEmail={}", 
                    paymentId, email, order.getGuestEmail());
            throw new InvalidOperationException("Email does not match the order email");
        }

        if (payment.getStatus() == PaymentStatus.COMPLETED) {
            throw new InvalidOperationException("Payment is already completed");
        }

        PaymentStatus newStatus;
        String transactionId = "MOCK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        String notes = switch (scenario.toUpperCase()) {
            case "SUCCESS" -> {
                newStatus = PaymentStatus.COMPLETED;
                yield "Symulacja: Płatność udana (Bank OK)";
            }
            case "FAIL" -> {
                newStatus = PaymentStatus.FAILED;
                yield "Symulacja: Odmowa banku (Brak środków)";
            }
            case "ERROR" -> {
                newStatus = PaymentStatus.FAILED;
                yield "Symulacja: Błąd połączenia z bankiem";
            }
            default -> throw new InvalidOperationException("Unknown scenario: " + scenario);
        };

        PaymentStatus oldStatus = payment.getStatus();

        payment.setStatus(newStatus);
        payment.setTransactionId(transactionId);
        payment.setNotes(notes);
        if (payment.getMethod() == null) payment.setMethod(PaymentMethod.CREDIT_CARD);

        if (oldStatus != newStatus) {
            handlePaymentStatusChange(payment, oldStatus, newStatus);
        }

        logger.info("Guest payment simulation completed: paymentId={}, status={}", paymentId, newStatus);
        return paymentMapper.toPaymentDTO(paymentRepository.save(payment));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isPaymentOwner(Long paymentId, String userEmail) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + paymentId));

        Order order = payment.getOrder();
        
        // For registered user orders
        if (order.getUser() != null) {
            return order.getUser().getEmail().equals(userEmail);
        }
        
        // For guest orders - check if email matches
        if (order.getGuestEmail() != null) {
            return order.getGuestEmail().equalsIgnoreCase(userEmail);
        }
        
        return false;
    }
}