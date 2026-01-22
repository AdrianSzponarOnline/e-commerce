package com.ecommerce.E_commerce.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "orders")
@SQLDelete(sql = "UPDATE orders SET deleted_at = NOW(), is_active = false WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class Order {
    private static final BigDecimal SHIPPING_COST = new BigDecimal("20.00");
    private static final BigDecimal FREE_SHIPPING_THRESHOLD = new BigDecimal("300.00");
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ColumnDefault("nextval('orders_id_seq'::regclass)")
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.RESTRICT)
    @JoinColumn(name = "user_id")
    private User user;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.RESTRICT)
    @JoinColumn(name = "address_id", nullable = false)
    private Address address;

    @NotNull
    @Enumerated(EnumType.STRING)
    @ColumnDefault("'NEW'")
    @Column(name = "status", nullable = false, length = 20)
    private OrderStatus status;

    @NotNull
    @ColumnDefault("0")
    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();
    
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Payment> payments = new ArrayList<>();

    @NotNull
    @ColumnDefault("now()")
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @NotNull
    @ColumnDefault("now()")
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @NotNull
    @ColumnDefault("true")
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    // Guest order contact information (nullable - only for guest orders)
    @Column(name = "guest_email", length = 255)
    private String guestEmail;

    @Column(name = "guest_first_name", length = 100)
    private String guestFirstName;

    @Column(name = "guest_last_name", length = 100)
    private String guestLastName;

    @Column(name = "guest_phone", length = 20)
    private String guestPhone;

    @PrePersist
    public void prePersist() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
        if (this.status == null) {
            this.status = OrderStatus.NEW;
        }
        if (this.totalAmount == null) {
            this.totalAmount = BigDecimal.ZERO;
        }
        if (this.isActive == null) {
            this.isActive = true;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
    }
    
    // Business logic methods (Domain-Driven Design)
    
    /**
     * Adds an item to the order and recalculates the total amount.
     * This method encapsulates the business logic of adding items to an order.
     * 
     * @param product The product to add
     * @param quantity The quantity of the product
     * @return The created OrderItem
     */
    public OrderItem addItem(Product product, Integer quantity) {
        if (product == null) {
            throw new IllegalArgumentException("Product cannot be null");
        }
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }
        
        OrderItem orderItem = new OrderItem();
        orderItem.setOrder(this);

        orderItem.setProduct(product);
        orderItem.setQuantity(quantity);
        orderItem.setPrice(product.getPrice());
        
        this.items.add(orderItem);
        recalculateTotalAmount();
        
        return orderItem;
    }
    
    /**
     * Recalculates the total amount based on all items in the order.
     * This method ensures the totalAmount is always in sync with the items.
     */
    public void recalculateTotalAmount() {
        BigDecimal itemsTotal = this.items.stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if(itemsTotal.compareTo(BigDecimal.ZERO) > 0 &&
           itemsTotal.compareTo(FREE_SHIPPING_THRESHOLD) <= 0) {
            this.totalAmount = itemsTotal.add(SHIPPING_COST);
        } else {
            this.totalAmount = itemsTotal;
        }
    }
    
    /**
     * Removes an item from the order and recalculates the total amount.
     * 
     * @param item The item to remove
     * @return true if the item was removed, false otherwise
     */
    public boolean removeItem(OrderItem item) {
        if (item == null) {
            return false;
        }
        boolean removed = this.items.remove(item);
        if (removed) {
            recalculateTotalAmount();
        }
        return removed;
    }
}