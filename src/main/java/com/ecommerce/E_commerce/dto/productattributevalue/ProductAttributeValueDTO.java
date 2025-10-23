package com.ecommerce.E_commerce.dto.productattributevalue;

import java.time.Instant;

public record ProductAttributeValueDTO(
        Long id,
        Long productId,
        String productName,
        Long categoryAttributeId,
        String categoryAttributeName,
        String categoryAttributeType,
        Boolean isKeyAttribute,
        String value,
        Instant createdAt,
        Instant updatedAt,
        Instant deletedAt,
        Boolean isActive
) {
}
