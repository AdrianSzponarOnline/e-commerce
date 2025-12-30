package com.ecommerce.E_commerce.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.security.core.GrantedAuthority;

import java.time.Instant;

@Entity
@Table(name = "roles")
public class Role implements GrantedAuthority {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long roleID;

    @Column(name = "name")
    @Enumerated(EnumType.STRING)
    private ERole role;

    @Setter
    @Column(name = "description", length = Integer.MAX_VALUE)
    private String description;

    @Setter
    @NotNull
    @ColumnDefault("now()")
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Setter
    @NotNull
    @ColumnDefault("now()")
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Setter
    @Column(name = "deleted_at")
    private Instant deletedAt;

    public Instant getDeletedAt() {
        return deletedAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public String getDescription() {
        return description;
    }

    public Role() {
    }

    @Override
    public String getAuthority() {
        return role.name();
    }

    public Role(ERole role) {
        this.role = role;
    }

    public Role(Long roleID, ERole role, String description, Instant createdAt, Instant updatedAt, Instant deletedAt) {
        this.roleID = roleID;
        this.role = role;
        this.description = description;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deletedAt = deletedAt;
    }

    public ERole getRole() {
        return role;
    }

    public Long getRoleID() {
        return roleID;
    }
}