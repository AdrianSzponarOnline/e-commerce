package com.ecommerce.E_commerce.dto.attribute;

import com.ecommerce.E_commerce.model.CategoryAttributeType;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AttributeUpdateDTO(
        @Size(max = 100, message = "Attribute name cannot exceed 100 characters")
        @Pattern(
                regexp = "^[\\p{L}\\p{N} \\-_]+$",
                message = "Attribute name can only contain letters, numbers, spaces, hyphens, and underscores"
        )
        String name,
        CategoryAttributeType type
) {
}
