package com.ecommerce.E_commerce.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.search.engine.backend.types.ObjectStructure;
import org.hibernate.search.engine.backend.types.Sortable;
import org.hibernate.search.mapper.pojo.automaticindexing.ReindexOnUpdate;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "products", uniqueConstraints = {
        @UniqueConstraint(columnNames = "sku")
})
@SQLDelete(sql = "UPDATE products SET deleted_at = NOW(), is_active = false WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@NamedEntityGraph(
        name = "Product.summary",
        attributeNodes = {
                @NamedAttributeNode("category")
        }
)
@NamedEntityGraph(
        name = "Product.withDetails",
        attributeNodes = {
                @NamedAttributeNode("category"),
                @NamedAttributeNode(
                        value = "attributeValues",
                        subgraph = "graph.ProductAttributeValue.withAttribute"
                )
        },
        subgraphs = {
                @NamedSubgraph(
                        name = "graph.ProductAttributeValue.withAttribute",
                        attributeNodes = {
                                @NamedAttributeNode("attribute")
                        }
                )
        }
)
@Indexed
public class Product {
    @Id
    @ColumnDefault("nextval('products_id_seq'::regclass)")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    @GenericField(sortable = Sortable.YES)
    private Long id;

    @Size(max = 255)
    @NotNull
    @Column(name = "name", nullable = false)
    @FullTextField(analyzer = "standard")
    @KeywordField(name = "name_sort", sortable = Sortable.YES, normalizer = "lowercase")
    private String name;

    @NotNull
    @FullTextField(analyzer = "standard")
    @Column(name = "description", nullable = false, length = Integer.MAX_VALUE)
    private String description;

    @Size(max = 255)
    @Column(name = "short_description")
    private String shortDescription;

    @NotNull
    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    @GenericField(sortable = Sortable.YES)
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
    @GenericField(sortable = Sortable.YES)
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    @NotNull
    @IndexedEmbedded(includePaths = {"id"})
    @IndexingDependency(reindexOnUpdate = ReindexOnUpdate.SHALLOW)
    private Category category;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @IndexedEmbedded(structure = ObjectStructure.NESTED)
    private List<ProductAttributeValue> attributeValues = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductImage> images = new ArrayList<>();

    @NotNull
    @ColumnDefault("now()")
    @Column(name = "created_at", nullable = false)
    @GenericField(sortable = Sortable.YES)
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
    @GenericField
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