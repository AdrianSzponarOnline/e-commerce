package com.ecommerce.E_commerce.dto.attribute;

import com.ecommerce.E_commerce.model.CategoryAttributeType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AttributeCreateDTO(
        @NotBlank(message = "Attribute name cannot be null")
        @Size(max = 100, message = "Attribute name cannot be longer than 100 characters")
        @Pattern(
                regexp = "^[\\p{L}\\p{N} \\-_]+$",
                message = "Attribute name can only contain letters, numbers, spaces, hyphens, and underscores")
        String name,
        @NotBlank(message = "Attribute typo cannot be null")
        @Size(max = 50, message = "Attribute type cannot be longer than 50 characters")
        CategoryAttributeType type
) {
}
