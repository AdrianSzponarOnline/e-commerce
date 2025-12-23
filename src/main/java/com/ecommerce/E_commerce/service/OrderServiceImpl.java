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
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@Transactional
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final ProductRepository productRepository;
    private final InventoryService inventoryService;
    private final OrderMapper orderMapper;
    private final OrderNotificationService orderNotificationService;


    @Override
    public OrderDTO create(Long userId, OrderCreateDTO dto) {
        logger.info("Creating order for userId={}, itemsCount={}", userId, dto.items() != null ? dto.items().size() : 0);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Address address = addressRepository.findById(dto.addressId())
                .orElseThrow(() -> new ResourceNotFoundException("Address not found with id: " + dto.addressId()));


        Order order = orderMapper.toOrder(dto);
        order.setUser(user);
        order.setAddress(address);
        order.setStatus(dto.status() != null ? OrderStatus.valueOf(dto.status().toUpperCase()) : OrderStatus.NEW);

        assert dto.items() != null;
        for (OrderItemCreateDTO itemDto : dto.items()) {
            Product product = productRepository.findById(itemDto.productId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + itemDto.productId()));

            if (Boolean.FALSE.equals(product.getIsActive())) {
                logger.warn("Attempted to add inactive product to order: productId={}, userId={}", itemDto.productId(), userId);
                throw new InvalidOperationException("Product with id " + itemDto.productId() + " is not active");
            }

            logger.debug("Reserving stock: productId={}, quantity={}", itemDto.productId(), itemDto.quantity());
            inventoryService.reserveStock(itemDto.productId(), itemDto.quantity());

            order.addItem(product, itemDto.quantity());
        }

        Order savedOrder = orderRepository.save(order);
        logger.info("Order created successfully: orderId={}, userId={}, total={}, status={}", savedOrder.getId(), userId, savedOrder.getTotalAmount(), savedOrder.getStatus());

        orderNotificationService.sendOrderConfirmation(order.getId());

        return orderMapper.toOrderDTO(savedOrder);
    }

    @Override
    public OrderDTO update(Long id, OrderUpdateDTO dto) {
        logger.info("Updating order: orderId={}, newStatus={}", id, dto.status());
        Order order = getOrderOrThrow(id);
        OrderStatus oldStatus = order.getStatus();

        if (dto.status() != null) {
            OrderStatus newStatus = OrderStatus.valueOf(dto.status().toUpperCase());
            order.setStatus(newStatus);

            if (oldStatus != newStatus) {
                logger.info("Order status changed: orderId={}, oldStatus={}, newStatus={}", id, oldStatus, newStatus);
                handleInventoryStatusChange(order, oldStatus, newStatus);
                handleNotificationTrigger(order, oldStatus, newStatus);
            }
        }

        if (dto.isActive() != null) {
            order.setIsActive(dto.isActive());
        }

        Order updatedOrder = orderRepository.save(order);
        logger.info("Order updated successfully: orderId={}, status={}", id, updatedOrder.getStatus());
        return orderMapper.toOrderDTO(updatedOrder);
    }

    @Override
    public OrderDTO cancelOrder(Long id) {
        logger.info("Cancelling order: orderId={}", id);
        Order order = getOrderOrThrow(id);

        if (order.getStatus() == OrderStatus.CANCELLED) {
            logger.warn("Attempted to cancel already cancelled order: orderId={}", id);
            throw new InvalidOperationException("Order is already cancelled");
        }

        OrderStatus oldStatus = order.getStatus();
        order.setStatus(OrderStatus.CANCELLED);

        handleInventoryStatusChange(order, oldStatus, OrderStatus.CANCELLED);

        Order cancelledOrder = orderRepository.save(order);
        logger.info("Order cancelled successfully: orderId={}", id);
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
    public Page<OrderDTO> findByStatus(OrderStatus status, Pageable pageable) {
        return orderRepository.findByStatus(status, pageable).map(orderMapper::toOrderDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderDTO> findAll(Pageable pageable) {
        return orderRepository.findAll(pageable).map(orderMapper::toOrderDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderDTO> findByUserIdAndStatus(Long userId, OrderStatus orderStatus, Pageable pageable) {
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
    public Page<OrderDTO> findByMultipleCriteria(Long userId,
                                                 OrderStatus status,
                                                 Boolean isActive,
                                                 Instant startDate,
                                                 Instant endDate,
                                                 Pageable pageable) {
        return orderRepository.findByMultipleCriteria(
                userId,
                status,
                isActive,
                startDate,
                endDate,
                pageable
        ).map(orderMapper::toOrderDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public long countByUserId(Long userId) {
        return orderRepository.countByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public long countByStatus(OrderStatus status) {
        return orderRepository.countByStatus(status);
    }

    @Override
    public long countByUserIdAndStatus(Long userId, OrderStatus status) {
        return orderRepository.countByUserIdAndStatus(userId, status);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isOrderOwner(Long orderId, String userEmail) {
        return orderRepository.findById(orderId)
                .map(order -> order.getUser().getEmail().equals(userEmail))
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
    }


    private void handleNotificationTrigger(Order order, OrderStatus oldStatus, OrderStatus newStatus) {
        if (newStatus == OrderStatus.CONFIRMED && oldStatus != OrderStatus.CONFIRMED) {
            orderNotificationService.sendOrderConfirmedToOwner(order.getId());
        }
        if (newStatus == OrderStatus.SHIPPED && oldStatus != OrderStatus.SHIPPED) {
            orderNotificationService.sendOrderShipped(order.getId());
        }
    }

}
