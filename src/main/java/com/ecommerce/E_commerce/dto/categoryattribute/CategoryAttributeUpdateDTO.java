package com.ecommerce.E_commerce.dto.categoryattribute;

import jakarta.validation.constraints.NotNull;

public record CategoryAttributeUpdateDTO(
        Boolean isKeyAttribute,

        @NotNull
        Boolean isActive
) {
}


