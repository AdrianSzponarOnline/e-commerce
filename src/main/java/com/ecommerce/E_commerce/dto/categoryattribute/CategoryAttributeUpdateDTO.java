package com.ecommerce.E_commerce.dto.categoryattribute;

import com.ecommerce.E_commerce.model.CategoryAttributeType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CategoryAttributeUpdateDTO(
        @NotNull
        @Size(max = 100)
        @Pattern(regexp = "^[^<>]*$", message = "must not contain HTML or script characters")
        String name,

        @NotNull
        CategoryAttributeType type,

        @NotNull
        Boolean isActive
) {
}


