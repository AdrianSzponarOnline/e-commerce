package com.ecommerce.E_commerce.service;

import com.ecommerce.E_commerce.dto.order.OrderCreateDTO;
import com.ecommerce.E_commerce.dto.order.OrderDTO;
import com.ecommerce.E_commerce.dto.order.OrderUpdateDTO;
import com.ecommerce.E_commerce.dto.orderitem.OrderItemCreateDTO;
import com.ecommerce.E_commerce.exception.ResourceNotFoundException;
import com.ecommerce.E_commerce.mapper.OrderMapper;
import com.ecommerce.E_commerce.model.Address;
import com.ecommerce.E_commerce.model.Order;
import com.ecommerce.E_commerce.model.OrderItem;
import com.ecommerce.E_commerce.model.OrderStatus;
import com.ecommerce.E_commerce.model.Product;
import com.ecommerce.E_commerce.model.User;
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
            throw new IllegalArgumentException("Address does not belong to user");
        }
        
        Order order = orderMapper.toOrder(dto);
        order.setUser(user);
        order.setAddress(address);
        order.setStatus(dto.status() != null ? OrderStatus.valueOf(dto.status().toUpperCase()) : OrderStatus.NEW);
        
        for (OrderItemCreateDTO itemDto : dto.items()) {
        
            Product product = productRepository.findById(itemDto.productId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + itemDto.productId()));
            
         
            if (!product.getIsActive()) {
                throw new IllegalArgumentException("Product with id " + itemDto.productId() + " is not active");
            }
            
         
            inventoryService.reserveStock(itemDto.productId(), itemDto.quantity());
            order.addItem(product, itemDto.quantity());
        }
        
   
        Order savedOrder = orderRepository.save(order);
        
        return orderMapper.toOrderDTO(savedOrder);
    }
    
    @Override
    public OrderDTO update(Long id, OrderUpdateDTO dto) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));
        
        OrderStatus oldStatus = order.getStatus();
        
   
        if (dto.status() != null) {
            order.setStatus(OrderStatus.valueOf(dto.status().toUpperCase()));
        }
 
        if (dto.isActive() != null) {
            order.setIsActive(dto.isActive());
        }
        
        OrderStatus newStatus = order.getStatus();
        
        if (oldStatus != null && newStatus != null && oldStatus != newStatus) {
            handleInventoryStatusChange(order, oldStatus, newStatus);
        }
        
        Order updatedOrder = orderRepository.save(order);
        return orderMapper.toOrderDTO(updatedOrder);
    }
    
    private void handleInventoryStatusChange(Order order, OrderStatus oldStatus, OrderStatus newStatus) {
        if (newStatus == OrderStatus.CANCELLED && oldStatus != OrderStatus.CANCELLED) {
            for (OrderItem item : order.getItems()) {
                inventoryService.releaseStock(item.getProduct().getId(), item.getQuantity());
            }
        }
      
        else if ((newStatus == OrderStatus.CONFIRMED || 
                  newStatus == OrderStatus.SHIPPED || 
                  newStatus == OrderStatus.DELIVERED) && 
                 oldStatus != OrderStatus.CONFIRMED && 
                 oldStatus != OrderStatus.SHIPPED && 
                 oldStatus != OrderStatus.DELIVERED) {
            for (OrderItem item : order.getItems()) {
                inventoryService.finalizeReservation(item.getProduct().getId(), item.getQuantity());
            }
        }
    }
    
    @Override
    public OrderDTO cancelOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));
        
        // Check if already cancelled
        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new IllegalArgumentException("Order is already cancelled");
        }
        
        // Check if user is OWNER
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new AccessDeniedException("User not authenticated");
        }
        
        boolean isOwner = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(auth -> auth.equals("ROLE_OWNER"));
        
        // If USER (not OWNER), validate that order can be cancelled
        if (!isOwner) {
            if (order.getStatus() != OrderStatus.NEW && order.getStatus() != OrderStatus.CONFIRMED) {
                throw new AccessDeniedException("You can only cancel orders with status NEW or CONFIRMED. Current status: " + order.getStatus());
            }
        }
        
        // Cancel the order
        OrderStatus oldStatus = order.getStatus();
        order.setStatus(OrderStatus.CANCELLED);
        
        // Handle inventory release (automatic via handleInventoryStatusChange)
        handleInventoryStatusChange(order, oldStatus, OrderStatus.CANCELLED);
        
        Order cancelledOrder = orderRepository.save(order);
        return orderMapper.toOrderDTO(cancelledOrder);
    }
    
    @Override
    public void delete(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));
        
      
        if (order.getStatus() != OrderStatus.CANCELLED) {
            for (OrderItem item : order.getItems()) {
                inventoryService.releaseStock(item.getProduct().getId(), item.getQuantity());
            }
        }
        
        order.setDeletedAt(Instant.now());
        order.setIsActive(false);
        orderRepository.save(order);
    }
    
    @Override
    public OrderDTO getById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));
        
        return orderMapper.toOrderDTO(order);
    }
    
    @Override
    public Page<OrderDTO> findByUserId(Long userId, Pageable pageable) {
        Page<Order> orders = orderRepository.findByUserId(userId, pageable);
        return orders.map(orderMapper::toOrderDTO);
    }
    
    @Override
    public Page<OrderDTO> findByStatus(String status, Pageable pageable) {
        OrderStatus orderStatus = status != null ? OrderStatus.valueOf(status.toUpperCase()) : null;
        Page<Order> orders = orderRepository.findByStatus(orderStatus, pageable);
        return orders.map(orderMapper::toOrderDTO);
    }
    
    @Override
    public Page<OrderDTO> findAll(Pageable pageable) {
        Page<Order> orders = orderRepository.findAll(pageable);
        return orders.map(orderMapper::toOrderDTO);
    }
    
    @Override
    public Page<OrderDTO> findByUserIdAndStatus(Long userId, String status, Pageable pageable) {
        OrderStatus orderStatus = status != null ? OrderStatus.valueOf(status.toUpperCase()) : null;
        Page<Order> orders = orderRepository.findByUserIdAndStatus(userId, orderStatus, pageable);
        return orders.map(orderMapper::toOrderDTO);
    }
    
    @Override
    public Page<OrderDTO> findByCreatedAtBetween(Instant startDate, Instant endDate, Pageable pageable) {
        Page<Order> orders = orderRepository.findByCreatedAtBetween(startDate, endDate, pageable);
        return orders.map(orderMapper::toOrderDTO);
    }
    
    @Override
    public Page<OrderDTO> findByUserIdAndCreatedAtBetween(Long userId, Instant startDate, Instant endDate, Pageable pageable) {
        Page<Order> orders = orderRepository.findByUserIdAndCreatedAtBetween(userId, startDate, endDate, pageable);
        return orders.map(orderMapper::toOrderDTO);
    }
    
    @Override
    public Page<OrderDTO> findByMultipleCriteria(Long userId, String status, Boolean isActive, Instant startDate, Instant endDate, Pageable pageable) {
        OrderStatus orderStatus = status != null ? OrderStatus.valueOf(status.toUpperCase()) : null;
        Page<Order> orders = orderRepository.findByMultipleCriteria(userId, orderStatus, isActive, startDate, endDate, pageable);
        return orders.map(orderMapper::toOrderDTO);
    }
    
    @Override
    public long countByUserId(Long userId) {
        return orderRepository.countByUserId(userId);
    }
    
    @Override
    public long countByStatus(String status) {
        OrderStatus orderStatus = status != null ? OrderStatus.valueOf(status.toUpperCase()) : null;
        return orderRepository.countByStatus(orderStatus);
    }
    
    @Override
    public boolean isOrderOwner(Long orderId, String userEmail) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
        
        return order.getUser().getEmail().equals(userEmail);
    }
}

