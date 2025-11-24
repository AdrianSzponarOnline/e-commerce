package com.ecommerce.E_commerce.dto.order;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record OrderUpdateDTO(
        @Size(max = 20, message = "Status must not exceed 20 characters")
        @Pattern(regexp = "^(NEW|CONFIRMED|PROCESSING|SHIPPED|DELIVERED|CANCELLED|REFUNDED)?$", 
                message = "Status must be one of the valid order statuses")
        String status,
        
        Boolean isActive
) {
}

