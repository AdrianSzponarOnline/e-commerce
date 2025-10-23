package com.ecommerce.E_commerce.dto.productattributevalue;

import jakarta.validation.constraints.Size;

public record ProductAttributeValueUpdateDTO(
        @Size(max = 1000, message = "Value must not exceed 1000 characters")
        String value,
        
        Boolean isActive
) {
}
