package com.ecommerce.E_commerce.dto.order;

import com.ecommerce.E_commerce.model.OrderStatus;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record OrderUpdateDTO(
        OrderStatus status,
        
        Boolean isActive
) {
}

