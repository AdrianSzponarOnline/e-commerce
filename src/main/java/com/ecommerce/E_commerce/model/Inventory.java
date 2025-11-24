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

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "inventory", uniqueConstraints = {
        @UniqueConstraint(columnNames = "product_id")
})
@SQLDelete(sql = "UPDATE inventory SET deleted_at = NOW(), is_active = false WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class Inventory {
    @Id
    @ColumnDefault("nextval('inventory_id_seq'::regclass)")
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.RESTRICT)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @NotNull
    @ColumnDefault("0")
    @Column(name = "available_quantity", nullable = false)
    private Integer availableQuantity = 0;

    @NotNull
    @ColumnDefault("0")
    @Column(name = "reserved_quantity", nullable = false)
    private Integer reservedQuantity = 0;

    @NotNull
    @ColumnDefault("0")
    @Column(name = "minimum_stock_level", nullable = false)
    private Integer minimumStockLevel = 0;

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

    @PrePersist
    public void prePersist() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
    }
}

