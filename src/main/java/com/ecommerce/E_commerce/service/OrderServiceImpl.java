package com.ecommerce.E_commerce.service;

import com.ecommerce.E_commerce.dto.order.GuestOrderCreateDTO;
import com.ecommerce.E_commerce.dto.order.OrderCreateDTO;
import com.ecommerce.E_commerce.dto.order.OrderDTO;
import com.ecommerce.E_commerce.dto.order.OrderUpdateDTO;
import com.ecommerce.E_commerce.dto.orderitem.OrderItemCreateDTO;
import com.ecommerce.E_commerce.event.OrderConfirmedEvent;
import com.ecommerce.E_commerce.event.OrderCreatedEvent;
import com.ecommerce.E_commerce.event.OrderShippedEvent;
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
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);
    private final ApplicationEventPublisher eventPublisher;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final ProductRepository productRepository;
    private final InventoryService inventoryService;
    private final OrderMapper orderMapper;

    @Override
    public OrderDTO create(Long userId, OrderCreateDTO dto) {
        if (dto.items() == null || dto.items().isEmpty()) {
            logger.error("Attempted to create order with no items for userId={}", userId);
            throw new InvalidOperationException("Order must contain at least one item");
        }

        logger.info("Creating order for userId={}, itemsCount={}", userId, dto.items().size());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Address address = addressRepository.findById(dto.addressId())
                .orElseThrow(() -> new ResourceNotFoundException("Address not found with id: " + dto.addressId()));

        Map<Long, Integer> quantitiesMap = dto.items().stream()
                .collect(Collectors.toMap(
                        OrderItemCreateDTO::productId,
                        OrderItemCreateDTO::quantity,
                        Integer::sum
                ));

        inventoryService.reserveStockBatch(quantitiesMap);

        List<Product> products = productRepository.findAllById(quantitiesMap.keySet());

        if (products.size() != quantitiesMap.size()) {
            throw new ResourceNotFoundException("Some products specified in the order were not found");
        }

        Map<Long, Product> productMap = products.stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));

        Order order = orderMapper.toOrder(dto);
        order.setUser(user);
        order.setAddress(address);

        order.setStatus(OrderStatus.NEW);

        for (OrderItemCreateDTO itemDto : dto.items()) {
            Product product = productMap.get(itemDto.productId());

            if (Boolean.FALSE.equals(product.getIsActive())) {
                throw new InvalidOperationException("Product " + itemDto.productId() + " is inactive");
            }
            order.addItem(product, itemDto.quantity());
        }

        Order savedOrder = orderRepository.save(order);

        logger.info("Order created successfully: orderId={}, userId={}, total={}",
                savedOrder.getId(), userId, savedOrder.getTotalAmount());

        eventPublisher.publishEvent(new OrderCreatedEvent(savedOrder.getId()));
        return orderMapper.toOrderDTO(savedOrder);
    }

    @Override
    public OrderDTO createGuestOrder(GuestOrderCreateDTO dto) {
        if (dto.items() == null || dto.items().isEmpty()) {
            logger.error("Attempted to create guest order with no items");
            throw new InvalidOperationException("Order must contain at least one item");
        }

        logger.info("Creating guest order for email={}, itemsCount={}", dto.email(), dto.items().size());

        // Create address for guest order (without user)
        Address address = new Address();
        address.setUser(null); // Guest order - no user
        address.setLine1(dto.addressLine1());
        address.setLine2(dto.addressLine2());
        address.setCity(dto.city());
        address.setRegion(dto.region());
        address.setPostalCode(dto.postalCode());
        address.setCountry(dto.country());
        address.setIsActive(true);
        Address savedAddress = addressRepository.save(address);

        Map<Long, Integer> quantitiesMap = dto.items().stream()
                .collect(Collectors.toMap(
                        OrderItemCreateDTO::productId,
                        OrderItemCreateDTO::quantity,
                        Integer::sum
                ));

        inventoryService.reserveStockBatch(quantitiesMap);

        List<Product> products = productRepository.findAllById(quantitiesMap.keySet());

        if (products.size() != quantitiesMap.size()) {
            throw new ResourceNotFoundException("Some products specified in the order were not found");
        }

        Map<Long, Product> productMap = products.stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));

        // Create order without user
        Order order = new Order();
        order.setUser(null); // Guest order - no user
        order.setAddress(savedAddress);
        order.setStatus(OrderStatus.NEW);
        // Store guest contact information
        order.setGuestEmail(dto.email());
        order.setGuestFirstName(dto.firstName());
        order.setGuestLastName(dto.lastName());
        order.setGuestPhone(dto.phone());

        for (OrderItemCreateDTO itemDto : dto.items()) {
            Product product = productMap.get(itemDto.productId());

            if (Boolean.FALSE.equals(product.getIsActive())) {
                throw new InvalidOperationException("Product " + itemDto.productId() + " is inactive");
            }
            order.addItem(product, itemDto.quantity());
        }

        Order savedOrder = orderRepository.save(order);

        logger.info("Guest order created successfully: orderId={}, email={}, total={}",
                savedOrder.getId(), dto.email(), savedOrder.getTotalAmount());

      
        eventPublisher.publishEvent(new OrderCreatedEvent(savedOrder.getId()));
        
        
        return orderMapper.toOrderDTO(savedOrder);
    }

    @Override
    @Transactional
    public OrderDTO update(Long id, OrderUpdateDTO dto) {
        logger.info("Updating order: orderId={}, dto={}", id, dto);
        Order order = getOrderOrThrow(id);

        if (dto.status() != null) {
            updateOrderStatusInternal(order, dto.status());
        }

        if (dto.isActive() != null) {
            order.setIsActive(dto.isActive());
        }

        Order savedOrder = orderRepository.save(order);
        logger.info("Order updated successfully: orderId={}", id);
        return orderMapper.toOrderDTO(savedOrder);
    }

    @Override
    public OrderDTO cancelOrder(Long id) {
        logger.info("Cancelling order: orderId={}", id);
        Order order = getOrderOrThrow(id);

        if (order.getStatus() == OrderStatus.CANCELLED) {
            logger.warn("Attempted to cancel already cancelled order: orderId={}", id);
            throw new InvalidOperationException("Order is already cancelled");
        }

        if (isFinalizingStatus(order.getStatus())) {
            throw new InvalidOperationException("Cannot cancel order that has already been shipped or delivered");
        }

        updateOrderStatusInternal(order, OrderStatus.CANCELLED);

        logger.info("Order cancelled successfully: orderId={}", id);
        return orderMapper.toOrderDTO(order);
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
        logger.debug("Handling inventory status change for order: orderId={}, oldStatus={}, newStatus={}",
                order.getId(), oldStatus, newStatus);

        if (newStatus == OrderStatus.CANCELLED) {
            if (isReservationActive(oldStatus)) {
                logger.info("Releasing stock for cancelled order: orderId={}, itemsCount={}",
                        order.getId(), order.getItems().size());
                for (OrderItem item : order.getItems()) {
                    inventoryService.releaseStock(item.getProduct().getId(), item.getQuantity());
                }
            }
        } else if (isFinalizingStatus(newStatus) && !isFinalizingStatus(oldStatus)) {
            logger.info("Finalizing stock reservations for order: orderId={}, newStatus={}, itemsCount={}",
                    order.getId(), newStatus, order.getItems().size());
            for (OrderItem item : order.getItems()) {
                inventoryService.finalizeReservation(item.getProduct().getId(), item.getQuantity());
            }
        }
    }


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handlePaymentFailure(Long orderId, String failureReason) {
        Order order = getOrderOrThrow(orderId);

        if (order.getStatus() == OrderStatus.NEW) {
            logger.warn("Cancelling order due to payment failure: orderId={}, reason={}", orderId, failureReason);

            updateOrderStatusInternal(order, OrderStatus.CANCELLED);
        } else {
            logger.info("Skipping cancellation for payment failure - order not in NEW status: orderId={}, status={}",
                    orderId, order.getStatus());
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void confirmOrderPayment(Long orderId) {
        Order order = getOrderOrThrow(orderId);

        if (order.getStatus() == OrderStatus.NEW) {
            logger.info("Confirming order after successful payment: orderId={}", orderId);
            updateOrderStatusInternal(order, OrderStatus.CONFIRMED);
        }
    }

    private void updateOrderStatusInternal(Order order, OrderStatus newStatus) {
        OrderStatus oldStatus = order.getStatus();

        if (oldStatus != newStatus) {
            order.setStatus(newStatus);
            Order savedOrder = orderRepository.saveAndFlush(order);

            handleInventoryStatusChange(savedOrder, oldStatus, newStatus);
            handleNotificationTrigger(savedOrder, oldStatus, newStatus);

            logger.info("Order status updated internally: orderId={}, oldStatus={}, newStatus={}",
                    order.getId(), oldStatus, newStatus);
        }
    }


    private boolean isReservationActive(OrderStatus status) {
        return status == OrderStatus.NEW || status == OrderStatus.CONFIRMED;
    }

    private boolean isFinalizingStatus(OrderStatus status) {
        return status == OrderStatus.SHIPPED || status == OrderStatus.DELIVERED;
    }


    private Order getOrderOrThrow(Long id) {
        return orderRepository.findByIdWithUser(id)
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
                .map(order -> {
                    // For registered user orders
                    if (order.getUser() != null) {
                        return order.getUser().getEmail().equals(userEmail);
                    }
                    // For guest orders - check if email matches
                    if (order.getGuestEmail() != null) {
                        return order.getGuestEmail().equalsIgnoreCase(userEmail);
                    }
                    return false;
                })
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
    }


    private void handleNotificationTrigger(Order order, OrderStatus oldStatus, OrderStatus newStatus) {
        logger.debug("Handling notification trigger: orderId={}, oldStatus={}, newStatus={}",
                order.getId(), oldStatus, newStatus);

        if (newStatus == OrderStatus.CONFIRMED && oldStatus != OrderStatus.CONFIRMED) {
            logger.info("Sending order confirmation notification: orderId={}", order.getId());
            String email;
            String name;
            if (order.getUser() != null) {
                email = order.getUser().getEmail();
                name = order.getUser().getFirstName();
            } else {
                email = order.getGuestEmail();
                name = order.getGuestFirstName();
            }
            if (email != null) {
                eventPublisher.publishEvent(new OrderConfirmedEvent(order.getId(), email, name));
            }
        }
        if (newStatus == OrderStatus.SHIPPED && oldStatus != OrderStatus.SHIPPED) {
            logger.info("Sending order shipped notification: orderId={}", order.getId());
            eventPublisher.publishEvent(new OrderShippedEvent(order.getId()));
        }
    }

}
