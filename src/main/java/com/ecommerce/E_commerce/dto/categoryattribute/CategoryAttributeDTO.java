package com.ecommerce.E_commerce.dto.categoryattribute;

import com.ecommerce.E_commerce.model.CategoryAttributeType;

import java.time.Instant;

public record CategoryAttributeDTO(
        Long id,
        Long categoryId,
        String name,
        CategoryAttributeType type,
        Boolean isActive,
        Instant createdAt,
        Instant updatedAt
) {
}


