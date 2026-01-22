package com.ecommerce.E_commerce.dto.inventory;

import java.time.Instant;

public record InventoryDTO(
        Long id,
        Long productId,
        String productName,
        String thumbnailUrl,
        Integer availableQuantity,
        Integer reservedQuantity,
        Integer minimumStockLevel,
        Instant createdAt,
        Instant updatedAt,
        Instant deletedAt,
        Boolean isActive
) {
}

