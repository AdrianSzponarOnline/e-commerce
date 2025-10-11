package com.ecommerce.E_commerce.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "products")
public class Product {
    @Id
    @ColumnDefault("nextval('products_id_seq'::regclass)")
    @Column(name = "id", nullable = false)
    private Long id;

    @Size(max = 255)
    @NotNull
    @Column(name = "name", nullable = false)
    private String name;

    @NotNull
    @Column(name = "description", nullable = false, length = Integer.MAX_VALUE)
    private String description;

    @Size(max = 255)
    @Column(name = "short_description")
    private String shortDescription;

    @NotNull
    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Size(max = 64)
    @NotNull
    @Column(name = "sku", nullable = false, length = 64)
    private String sku;

    @NotNull
    @Column(name = "vat_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal vatRate;

    @NotNull
    @ColumnDefault("false")
    @Column(name = "is_featured", nullable = false)
    private Boolean isFeatured = false;

    @NotNull
    @ColumnDefault("0")
    @Column(name = "shipping_cost", nullable = false, precision = 10, scale = 2)
    private BigDecimal shippingCost;

    @Size(max = 100)
    @Column(name = "estimated_delivery_time", length = 100)
    private String estimatedDeliveryTime;

    @Column(name = "thumbnail_url", length = Integer.MAX_VALUE)
    private String thumbnailUrl;

    @Size(max = 255)
    @NotNull
    @Column(name = "seo_slug", nullable = false)
    private String seoSlug;

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
    private Boolean isActive = false;

}