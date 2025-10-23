package com.ecommerce.E_commerce.dto.productattributevalue;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ProductAttributeValueCreateDTO(
        @NotNull(message = "Product ID is required")
        Long productId,
        
        @NotNull(message = "Category attribute ID is required")
        Long categoryAttributeId,
        
        @Size(max = 1000, message = "Value must not exceed 1000 characters")
        String value
) {
}
