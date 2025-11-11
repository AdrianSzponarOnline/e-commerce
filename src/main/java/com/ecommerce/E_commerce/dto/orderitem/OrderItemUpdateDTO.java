package com.ecommerce.E_commerce.dto.orderitem;

import jakarta.validation.constraints.Min;

public record OrderItemUpdateDTO(
        @Min(value = 1, message = "Quantity must be at least 1")
        Integer quantity
) {
}

