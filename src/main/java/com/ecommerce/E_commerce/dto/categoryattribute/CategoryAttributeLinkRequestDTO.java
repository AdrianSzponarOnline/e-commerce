package com.ecommerce.E_commerce.dto.categoryattribute;

import jakarta.validation.constraints.NotNull;

public record CategoryAttributeLinkRequestDTO(
        @NotNull Long attributeId,
        @NotNull Boolean isKeyAttribute
) {
}
