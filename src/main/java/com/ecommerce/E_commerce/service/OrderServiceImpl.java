package com.ecommerce.E_commerce.service;

import com.ecommerce.E_commerce.dto.order.OrderCreateDTO;
import com.ecommerce.E_commerce.dto.order.OrderDTO;
import com.ecommerce.E_commerce.dto.order.OrderUpdateDTO;
import com.ecommerce.E_commerce.dto.orderitem.OrderItemCreateDTO;
import com.ecommerce.E_commerce.exception.InvalidOperationException;
import com.ecommerce.E_commerce.exception.ResourceNotFoundException;
import com.ecommerce.E_commerce.mapper.OrderMapper;
import com.ecommerce.E_commerce.model.*;
import com.ecommerce.E_commerce.repository.AddressRepository;
import com.ecommerce.E_commerce.repository.OrderRepository;
import com.ecommerce.E_commerce.repository.ProductRepository;
import com.ecommerce.E_commerce.repository.UserRepository;
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
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final ProductRepository productRepository;
    private final InventoryService inventoryService;
    private final OrderMapper orderMapper;

    @Autowired
    public OrderServiceImpl(OrderRepository orderRepository,
                            UserRepository userRepository,
                            AddressRepository addressRepository,
                            ProductRepository productRepository,
                            InventoryService inventoryService,
                            OrderMapper orderMapper) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.addressRepository = addressRepository;
        this.productRepository = productRepository;
        this.inventoryService = inventoryService;
        this.orderMapper = orderMapper;
    }

    @Override
    public OrderDTO create(Long userId, OrderCreateDTO dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Address address = addressRepository.findById(dto.addressId())
                .orElseThrow(() -> new ResourceNotFoundException("Address not found with id: " + dto.addressId()));

        if (!address.getUser().getId().equals(userId)) {
            throw new InvalidOperationException("Address does not belong to user");
        }

        Order order = orderMapper.toOrder(dto);
        order.setUser(user);
        order.setAddress(address);
        order.setStatus(dto.status() != null ? OrderStatus.valueOf(dto.status().toUpperCase()) : OrderStatus.NEW);

        for (OrderItemCreateDTO itemDto : dto.items()) {
            Product product = productRepository.findById(itemDto.productId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + itemDto.productId()));

            if (Boolean.FALSE.equals(product.getIsActive())) {
                throw new InvalidOperationException("Product with id " + itemDto.productId() + " is not active");
            }

            inventoryService.reserveStock(itemDto.productId(), itemDto.quantity());

            order.addItem(product, itemDto.quantity());
        }

        Order savedOrder = orderRepository.save(order);
        return orderMapper.toOrderDTO(savedOrder);
    }

    @Override
    public OrderDTO update(Long id, OrderUpdateDTO dto) {
        Order order = getOrderOrThrow(id);
        OrderStatus oldStatus = order.getStatus();

        if (dto.status() != null) {
            OrderStatus newStatus = OrderStatus.valueOf(dto.status().toUpperCase());
            order.setStatus(newStatus);

            if (oldStatus != newStatus) {
                handleInventoryStatusChange(order, oldStatus, newStatus);
            }
        }

        if (dto.isActive() != null) {
            order.setIsActive(dto.isActive());
        }

        Order updatedOrder = orderRepository.save(order);
        return orderMapper.toOrderDTO(updatedOrder);
    }

    @Override
    public OrderDTO cancelOrder(Long id) {
        Order order = getOrderOrThrow(id);

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new InvalidOperationException("Order is already cancelled");
        }

        checkCancelPermission(order);

        OrderStatus oldStatus = order.getStatus();
        order.setStatus(OrderStatus.CANCELLED);

        handleInventoryStatusChange(order, oldStatus, OrderStatus.CANCELLED);

        Order cancelledOrder = orderRepository.save(order);
        return orderMapper.toOrderDTO(cancelledOrder);
    }

    @Override
    public void delete(Long id) {
        Order order = getOrderOrThrow(id);

        if (isReservationActive(order.getStatus())) {
            for (OrderItem item : order.getItems()) {
                inventoryService.releaseStock(item.getProduct().getId(), item.getQuantity());
            }
        }

        orderRepository.delete(order);
    }


    private void handleInventoryStatusChange(Order order, OrderStatus oldStatus, OrderStatus newStatus) {

        if (newStatus == OrderStatus.CANCELLED) {
            if (isReservationActive(oldStatus)) {
                for (OrderItem item : order.getItems()) {
                    inventoryService.releaseStock(item.getProduct().getId(), item.getQuantity());
                }
            }
        } else if (isFinalizingStatus(newStatus) && !isFinalizingStatus(oldStatus)) {
            for (OrderItem item : order.getItems()) {
                inventoryService.finalizeReservation(item.getProduct().getId(), item.getQuantity());
            }
        }
    }

    private boolean isReservationActive(OrderStatus status) {
        return status == OrderStatus.NEW || status == OrderStatus.CONFIRMED;
    }

    private boolean isFinalizingStatus(OrderStatus status) {
        return status == OrderStatus.SHIPPED || status == OrderStatus.DELIVERED;
    }

    private void checkCancelPermission(Order order) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new AccessDeniedException("User not authenticated");
        }

        boolean isOwnerRole = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(auth -> auth.equals("ROLE_OWNER"));

        if (!isOwnerRole) {
            if (order.getStatus() != OrderStatus.NEW && order.getStatus() != OrderStatus.CONFIRMED) {
                throw new AccessDeniedException("You can only cancel orders with status NEW or CONFIRMED.");
            }

            String currentUserEmail = authentication.getName();
            if (!order.getUser().getEmail().equals(currentUserEmail)) {
                throw new AccessDeniedException("You can only cancel your own orders");
            }
        }
    }

    private Order getOrderOrThrow(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public OrderDTO getById(Long id) {
        return orderMapper.toOrderDTO(getOrderOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderDTO> findByUserId(Long userId, Pageable pageable) {
        return orderRepository.findByUserId(userId, pageable).map(orderMapper::toOrderDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderDTO> findByStatus(String status, Pageable pageable) {
        OrderStatus orderStatus = status != null ? OrderStatus.valueOf(status.toUpperCase()) : null;
        return orderRepository.findByStatus(orderStatus, pageable).map(orderMapper::toOrderDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderDTO> findAll(Pageable pageable) {
        return orderRepository.findAll(pageable).map(orderMapper::toOrderDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderDTO> findByUserIdAndStatus(Long userId, String status, Pageable pageable) {
        OrderStatus orderStatus = status != null ? OrderStatus.valueOf(status.toUpperCase()) : null;
        return orderRepository.findByUserIdAndStatus(userId, orderStatus, pageable).map(orderMapper::toOrderDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderDTO> findByCreatedAtBetween(Instant startDate, Instant endDate, Pageable pageable) {
        return orderRepository.findByCreatedAtBetween(startDate, endDate, pageable).map(orderMapper::toOrderDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderDTO> findByUserIdAndCreatedAtBetween(Long userId, Instant startDate, Instant endDate, Pageable pageable) {
        return orderRepository.findByUserIdAndCreatedAtBetween(userId, startDate, endDate, pageable).map(orderMapper::toOrderDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderDTO> findByMultipleCriteria(Long userId, String status, Boolean isActive, Instant startDate, Instant endDate, Pageable pageable) {
        OrderStatus orderStatus = status != null ? OrderStatus.valueOf(status.toUpperCase()) : null;
        return orderRepository.findByMultipleCriteria(userId, orderStatus, isActive, startDate, endDate, pageable).map(orderMapper::toOrderDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public long countByUserId(Long userId) {
        return orderRepository.countByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public long countByStatus(String status) {
        OrderStatus orderStatus = status != null ? OrderStatus.valueOf(status.toUpperCase()) : null;
        return orderRepository.countByStatus(orderStatus);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isOrderOwner(Long orderId, String userEmail) {
        return orderRepository.findById(orderId)
                .map(order -> order.getUser().getEmail().equals(userEmail))
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
    }
}
