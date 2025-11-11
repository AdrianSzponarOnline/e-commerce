package com.ecommerce.E_commerce.dto.orderitem;

import com.ecommerce.E_commerce.dto.product.ProductDTO;

import java.math.BigDecimal;
import java.time.Instant;

public record OrderItemDTO(
        Long id,
        Long orderId,
        ProductDTO product,
        Integer quantity,
        BigDecimal price,
        Instant createdAt,
        Instant updatedAt,
        Boolean isActive
) {
}

