package com.ecommerce.E_commerce.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.FullTextField;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.IndexedEmbedded;

import java.time.Instant;
import java.util.Objects;


@Entity
@Table(name = "product_attribute_values",
        uniqueConstraints = @UniqueConstraint(columnNames = {"product_id", "attribute_id"}))
@SQLDelete(sql = "UPDATE product_attribute_values SET deleted_at = NOW(), is_active = false WHERE id = ?")
@SQLRestriction("deleted_at IS NULL AND is_active = true")
@Getter
@Setter
@NoArgsConstructor
public class ProductAttributeValue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(name = "value", columnDefinition = "TEXT")
    @FullTextField(name = "value", analyzer = "standard")
    private String attributeValue;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;

    private Instant deletedAt;

    @Column(nullable = false)
    private boolean isActive = true;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attribute_id", nullable = false)
    @IndexedEmbedded(includePaths = {"name"})
    private Attribute attribute;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProductAttributeValue that = (ProductAttributeValue) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}