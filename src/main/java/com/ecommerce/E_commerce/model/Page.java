package com.ecommerce.E_commerce.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

@Getter
@Setter
@Entity
@Table(name = "pages")
public class Page {
    @Id
    @ColumnDefault("nextval('pages_id_seq'::regclass)")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Size(max = 100)
    @NotNull
    @Column(name = "slug", nullable = false, length = 100)
    private String slug;

    @Size(max = 100)
    @NotNull
    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Column(name = "content", length = Integer.MAX_VALUE)
    private String content;
}