package com.ecommerce.E_commerce.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;

@Data
@Entity
@Table(name = "category_attributes", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"category_id", "name"})
})
public class CategoryAttribute {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Size(max = 100)
    @NotNull
    @Pattern(regexp = "^[^<>]*$", message = "must not contain HTML or script characters")
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 50)
    private CategoryAttributeType type;

    @NotNull
    @Column(name = "keyAttribute")
    boolean isKeyAttribute = false;

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

    public @NotNull Boolean getActive() {
        return isActive;
    }

    public void setActive(@NotNull Boolean active) {
        isActive = active;
    }

    public Instant getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(Instant deletedAt) {
        this.deletedAt = deletedAt;
    }

    public @NotNull Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(@NotNull Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public @NotNull Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(@NotNull Instant createdAt) {
        this.createdAt = createdAt;
    }

    public @NotNull CategoryAttributeType getType() {
        return type;
    }

    public void setType(@NotNull CategoryAttributeType type) {
        this.type = type;
    }

    public @Size(max = 100) @NotNull @Pattern(regexp = "^[^<>]*$", message = "must not contain HTML or script characters") String getName() {
        return name;
    }

    public void setName(@Size(max = 100) @NotNull @Pattern(regexp = "^[^<>]*$", message = "must not contain HTML or script characters") String name) {
        this.name = name;
    }

    public @NotNull Category getCategory() {
        return category;
    }

    public void setCategory(@NotNull Category category) {
        this.category = category;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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