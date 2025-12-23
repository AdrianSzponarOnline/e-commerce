package com.ecommerce.E_commerce.dto.productattributevalue;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ProductAttributeValueCreateDTO(
        @NotNull(message = "Product ID is required")
        Long productId,
        
        @NotNull(message = "Attribute ID is required")
        Long attributeId,
        
        @Size(max = 1000, message = "Value must not exceed 1000 characters")
        @Pattern(regexp = "^[^<>]*$", message = "Value must not contain HTML or script characters")
        String attibuteValue
) {
}
