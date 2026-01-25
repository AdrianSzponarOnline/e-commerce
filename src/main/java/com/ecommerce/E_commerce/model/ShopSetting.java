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
@Table(name = "shop_settings", uniqueConstraints = {
        @UniqueConstraint(columnNames = "setting_key")
})
public class ShopSetting {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Size(max = 100)
    @NotNull
    @Column(name = "setting_key", nullable = false, unique = true, length = 100)
    private String key;

    @Column(name = "setting_value", length = 1000)
    private String value;

    @Column(name = "description", length = 500)
    private String description;
}
