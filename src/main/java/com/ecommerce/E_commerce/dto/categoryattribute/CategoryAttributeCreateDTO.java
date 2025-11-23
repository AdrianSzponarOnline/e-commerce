package com.ecommerce.E_commerce.dto.categoryattribute;

import jakarta.validation.constraints.NotNull;

public record CategoryAttributeCreateDTO(
        @NotNull
        Long categoryId,

        @NotNull
        Long attributeId,

        @NotNull
        Boolean isKeyAttribute
) {
}


