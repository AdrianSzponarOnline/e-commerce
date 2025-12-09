package com.ecommerce.E_commerce.service;

import com.ecommerce.E_commerce.dto.order.OrderCreateDTO;
import com.ecommerce.E_commerce.dto.order.OrderDTO;
import com.ecommerce.E_commerce.dto.order.OrderUpdateDTO;
import com.ecommerce.E_commerce.dto.orderitem.OrderItemCreateDTO;
import com.ecommerce.E_commerce.dto.payment.PaymentDTO;
import com.ecommerce.E_commerce.exception.InvalidOperationException;
import com.ecommerce.E_commerce.exception.ResourceNotFoundException;
import com.ecommerce.E_commerce.mapper.OrderMapper;
import com.ecommerce.E_commerce.model.*;
import com.ecommerce.E_commerce.repository.AddressRepository;
import com.ecommerce.E_commerce.repository.OrderRepository;
import com.ecommerce.E_commerce.repository.ProductRepository;
import com.ecommerce.E_commerce.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private InventoryService inventoryService;

    @Mock
    private OrderNotificationService orderNotificationService;

    @Mock
    private OrderMapper orderMapper;

    private OrderServiceImpl orderService;

    private User testUser;
    private Address testAddress;
    private Product testProduct;
    private Order testOrder;

    @BeforeEach
    void setUp() {
        orderService = new OrderServiceImpl(
                orderRepository,
                userRepository,
                addressRepository,
                productRepository,
                inventoryService,
                orderMapper,
                orderNotificationService
        );

        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");

        testAddress = new Address();
        testAddress.setId(1L);
        testAddress.setUser(testUser);

        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setName("Test Product");
        testProduct.setPrice(new BigDecimal("99.99"));
        testProduct.setIsActive(true);

        testOrder = new Order();
        testOrder.setId(1L);
        testOrder.setUser(testUser);
        testOrder.setAddress(testAddress);
        testOrder.setStatus(OrderStatus.NEW);
        testOrder.setIsActive(true);
    }

    @Test
    void create_ShouldCreateOrderSuccessfully() {
        // Given
        OrderCreateDTO createDTO = new OrderCreateDTO(
                1L,
                "NEW",
                List.of(new OrderItemCreateDTO(1L, 2))
        );

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(addressRepository.findById(1L)).thenReturn(Optional.of(testAddress));
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(orderMapper.toOrder(createDTO)).thenReturn(testOrder);
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);
        when(orderMapper.toOrderDTO(testOrder)).thenReturn(new OrderDTO(
                1L, 1L, null, "NEW", new BigDecimal("199.98"), new ArrayList<>(), new ArrayList<PaymentDTO>(), Instant.now(), Instant.now(), true
        ));

        // When
        OrderDTO result = orderService.create(1L, createDTO);

        // Then
        assertNotNull(result);
        verify(userRepository).findById(1L);
        verify(addressRepository).findById(1L);
        verify(productRepository).findById(1L);
        verify(inventoryService).reserveStock(1L, 2);
        verify(orderRepository).save(any(Order.class));
        verify(orderNotificationService).sendOrderConfirmation(testOrder);
    }

    @Test
    void create_ShouldThrowException_WhenUserNotFound() {
        // Given
        OrderCreateDTO createDTO = new OrderCreateDTO(1L, "NEW", List.of(new OrderItemCreateDTO(1L, 2)));
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> orderService.create(1L, createDTO));
        assertEquals("User not found with id: 1", exception.getMessage());
        verify(userRepository).findById(1L);
        verify(orderRepository, never()).save(any());
    }


    @Test
    void update_ShouldUpdateOrderSuccessfully() {
        // Given
        OrderUpdateDTO updateDTO = new OrderUpdateDTO("CONFIRMED", null);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);
        when(orderMapper.toOrderDTO(testOrder)).thenReturn(new OrderDTO(
                1L, 1L, null, "CONFIRMED", new BigDecimal("199.98"), new ArrayList<>(), new ArrayList<PaymentDTO>(), Instant.now(), Instant.now(), true
        ));

        // When
        OrderDTO result = orderService.update(1L, updateDTO);

        // Then
        assertNotNull(result);
        verify(orderRepository).findById(1L);
        verify(orderRepository).save(testOrder);
        verify(orderNotificationService).sendOrderConfirmedToOwner(testOrder);
    }

    @Test
    void cancelOrder_ShouldCancelOrderSuccessfully() {
        // Given
        // Mock SecurityContext
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        lenient().when(authentication.getName()).thenReturn("test@example.com");
        lenient().doReturn(java.util.Set.of(new SimpleGrantedAuthority("ROLE_OWNER"))).when(authentication).getAuthorities();
        SecurityContextHolder.setContext(securityContext);
        
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);
        when(orderMapper.toOrderDTO(testOrder)).thenReturn(new OrderDTO(
                1L, 1L, null, "CANCELLED", new BigDecimal("199.98"), new ArrayList<>(), new ArrayList<PaymentDTO>(), Instant.now(), Instant.now(), true
        ));

        // When
        OrderDTO result = orderService.cancelOrder(1L);

        // Then
        assertNotNull(result);
        assertEquals(OrderStatus.CANCELLED, testOrder.getStatus());
        verify(orderRepository).save(testOrder);
    }

    @Test
    void cancelOrder_ShouldThrowException_WhenOrderAlreadyCancelled() {
        // Given
        testOrder.setStatus(OrderStatus.CANCELLED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        // When & Then
        InvalidOperationException exception = assertThrows(InvalidOperationException.class,
                () -> orderService.cancelOrder(1L));
        assertEquals("Order is already cancelled", exception.getMessage());
    }

    @Test
    void getById_ShouldReturnOrder_WhenOrderExists() {
        // Given
        OrderDTO orderDTO = new OrderDTO(
                1L, 1L, null, "NEW", new BigDecimal("199.98"), new ArrayList<>(), new ArrayList<PaymentDTO>(), Instant.now(), Instant.now(), true
        );
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(orderMapper.toOrderDTO(testOrder)).thenReturn(orderDTO);

        // When
        OrderDTO result = orderService.getById(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.id());
        verify(orderRepository).findById(1L);
    }

    @Test
    void getById_ShouldThrowException_WhenOrderNotFound() {
        // Given
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> orderService.getById(1L));
        assertEquals("Order not found with id: 1", exception.getMessage());
    }

    @Test
    void findByUserId_ShouldReturnPageOfOrders() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Order> orderPage = new PageImpl<>(List.of(testOrder));
        when(orderRepository.findByUserId(1L, pageable)).thenReturn(orderPage);
        when(orderMapper.toOrderDTO(testOrder)).thenReturn(new OrderDTO(
                1L, 1L, null, "NEW", new BigDecimal("199.98"), new ArrayList<>(), new ArrayList<PaymentDTO>(), Instant.now(), Instant.now(), true
        ));

        // When
        Page<OrderDTO> result = orderService.findByUserId(1L, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(orderRepository).findByUserId(1L, pageable);
    }

    @Test
    void delete_ShouldDeleteOrder() {
        // Given
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        // When
        orderService.delete(1L);

        // Then
        verify(orderRepository).findById(1L);
        verify(orderRepository).delete(testOrder);
    }
}

