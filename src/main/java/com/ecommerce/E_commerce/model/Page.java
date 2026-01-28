package com.ecommerce.E_commerce.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "pages")
@SQLDelete(sql = "UPDATE pages SET deleted_at = NOW(), is_active = false WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class Page {
    @Id
    @ColumnDefault("nextval('pages_id_seq'::regclass)")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Size(max = 100)
    @NotNull
    @Column(name = "slug", nullable = false, length = 100, unique = true)
    private String slug;

    @Size(max = 100)
    @NotNull
    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Column(name = "content", length = Integer.MAX_VALUE)
    private String content;

    @NotNull
    @ColumnDefault("false")
    @Column(name = "is_system", nullable = false)
    private Boolean isSystem = false;

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