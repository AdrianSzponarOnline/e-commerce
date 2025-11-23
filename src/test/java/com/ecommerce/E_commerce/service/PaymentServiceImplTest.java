package com.ecommerce.E_commerce.service;

import com.ecommerce.E_commerce.dto.payment.PaymentCreateDTO;
import com.ecommerce.E_commerce.dto.payment.PaymentDTO;
import com.ecommerce.E_commerce.dto.payment.PaymentUpdateDTO;
import com.ecommerce.E_commerce.exception.InvalidOperationException;
import com.ecommerce.E_commerce.exception.ResourceNotFoundException;
import com.ecommerce.E_commerce.mapper.PaymentMapper;
import com.ecommerce.E_commerce.model.*;
import com.ecommerce.E_commerce.repository.OrderRepository;
import com.ecommerce.E_commerce.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private PaymentMapper paymentMapper;

    private PaymentServiceImpl paymentService;

    private Order testOrder;
    private Payment testPayment;
    private User testUser;

    @BeforeEach
    void setUp() {
        paymentService = new PaymentServiceImpl(paymentRepository, orderRepository, paymentMapper);

        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");

        testOrder = new Order();
        testOrder.setId(1L);
        testOrder.setUser(testUser);
        testOrder.setStatus(OrderStatus.NEW);
        testOrder.setTotalAmount(new BigDecimal("199.98"));

        testPayment = new Payment();
        testPayment.setId(1L);
        testPayment.setOrder(testOrder);
        testPayment.setAmount(new BigDecimal("199.98"));
        testPayment.setMethod(PaymentMethod.CREDIT_CARD);
        testPayment.setStatus(PaymentStatus.PENDING);
        testPayment.setIsActive(true);
    }

    @Test
    void create_ShouldCreatePaymentSuccessfully() {
        // Given
        PaymentCreateDTO createDTO = new PaymentCreateDTO(
                1L,
                new BigDecimal("199.98"),
                "CREDIT_CARD",
                "TXN-123",
                "Payment notes"
        );

        PaymentDTO paymentDTO = new PaymentDTO(
                1L, 1L, new BigDecimal("199.98"), "CREDIT_CARD", "PENDING",
                Instant.now(), "TXN-123", "Payment notes", Instant.now(), Instant.now(), true
        );

        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(paymentMapper.toPayment(createDTO)).thenReturn(testPayment);
        when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);
        when(paymentMapper.toPaymentDTO(testPayment)).thenReturn(paymentDTO);

        // When
        PaymentDTO result = paymentService.create(createDTO);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.id());
        verify(orderRepository).findById(1L);
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    void create_ShouldThrowException_WhenOrderNotFound() {
        // Given
        PaymentCreateDTO createDTO = new PaymentCreateDTO(
                1L, new BigDecimal("199.98"), "CREDIT_CARD", "TXN-123", null
        );
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> paymentService.create(createDTO));
        assertEquals("Order not found with id: 1", exception.getMessage());
        verify(paymentRepository, never()).save(any());
    }

    @Test
    void create_ShouldThrowException_WhenOrderStatusIsInvalid() {
        // Given
        testOrder.setStatus(OrderStatus.SHIPPED);
        PaymentCreateDTO createDTO = new PaymentCreateDTO(
                1L, new BigDecimal("199.98"), "CREDIT_CARD", "TXN-123", null
        );
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        // When & Then
        InvalidOperationException exception = assertThrows(InvalidOperationException.class,
                () -> paymentService.create(createDTO));
        assertTrue(exception.getMessage().contains("Payment can only be created for orders with status NEW or CONFIRMED"));
    }

    @Test
    void create_ShouldThrowException_WhenAmountDoesNotMatch() {
        // Given
        PaymentCreateDTO createDTO = new PaymentCreateDTO(
                1L, new BigDecimal("100.00"), "CREDIT_CARD", "TXN-123", null
        );
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        // When & Then
        InvalidOperationException exception = assertThrows(InvalidOperationException.class,
                () -> paymentService.create(createDTO));
        assertTrue(exception.getMessage().contains("Payment amount"));
    }

    @Test
    void update_ShouldUpdatePaymentSuccessfully() {
        // Given
        PaymentUpdateDTO updateDTO = new PaymentUpdateDTO("COMPLETED", null, null, null);
        PaymentDTO paymentDTO = new PaymentDTO(
                1L, 1L, new BigDecimal("199.98"), "CREDIT_CARD", "COMPLETED",
                Instant.now(), "TXN-123", null, Instant.now(), Instant.now(), true
        );

        when(paymentRepository.findById(1L)).thenReturn(Optional.of(testPayment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);
        when(paymentMapper.toPaymentDTO(testPayment)).thenReturn(paymentDTO);

        // When
        PaymentDTO result = paymentService.update(1L, updateDTO);

        // Then
        assertNotNull(result);
        verify(paymentRepository).findById(1L);
        verify(paymentMapper).updatePaymentFromDTO(updateDTO, testPayment);
        verify(paymentRepository).save(testPayment);
    }

    @Test
    void update_ShouldThrowException_WhenPaymentNotFound() {
        // Given
        PaymentUpdateDTO updateDTO = new PaymentUpdateDTO("COMPLETED", null, null, null);
        when(paymentRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> paymentService.update(1L, updateDTO));
        assertEquals("Payment not found with id: 1", exception.getMessage());
    }

    @Test
    void getById_ShouldReturnPayment_WhenPaymentExists() {
        // Given
        PaymentDTO paymentDTO = new PaymentDTO(
                1L, 1L, new BigDecimal("199.98"), "CREDIT_CARD", "PENDING",
                Instant.now(), "TXN-123", null, Instant.now(), Instant.now(), true
        );
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(testPayment));
        when(paymentMapper.toPaymentDTO(testPayment)).thenReturn(paymentDTO);

        // When
        PaymentDTO result = paymentService.getById(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.id());
        verify(paymentRepository).findById(1L);
    }

    @Test
    void delete_ShouldDeletePayment() {
        // Given
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(testPayment));

        // When
        paymentService.delete(1L);

        // Then
        verify(paymentRepository).findById(1L);
        verify(paymentRepository).delete(testPayment);
    }

    @Test
    void findByOrderId_ShouldReturnPageOfPayments() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Payment> paymentPage = new PageImpl<>(List.of(testPayment));
        PaymentDTO paymentDTO = new PaymentDTO(
                1L, 1L, new BigDecimal("199.98"), "CREDIT_CARD", "PENDING",
                Instant.now(), "TXN-123", null, Instant.now(), Instant.now(), true
        );

        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(paymentRepository.findByOrderId(1L, pageable)).thenReturn(paymentPage);
        when(paymentMapper.toPaymentDTO(testPayment)).thenReturn(paymentDTO);

        // When
        Page<PaymentDTO> result = paymentService.findByOrderId(1L, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(paymentRepository).findByOrderId(1L, pageable);
    }
}

