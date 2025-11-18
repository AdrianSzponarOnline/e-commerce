package com.ecommerce.E_commerce.dto.attribute;

import com.ecommerce.E_commerce.model.CategoryAttributeType;

import java.time.Instant;

public record AttributeDTO(
        Long id,
        String name,
        CategoryAttributeType type,
        Instant createdAt,
        Instant updatedAt
) {
}
