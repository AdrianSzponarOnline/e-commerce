package com.ecommerce.E_commerce.dto.order;

import jakarta.validation.constraints.Size;

public record OrderUpdateDTO(
        @Size(max = 20, message = "Status must not exceed 20 characters")
        String status,
        
        Boolean isActive
) {
}

