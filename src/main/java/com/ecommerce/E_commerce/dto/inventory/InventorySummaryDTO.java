package com.ecommerce.E_commerce.dto.inventory;

public record InventorySummaryDTO(
        Long productId,
        String productName,
        String sku,
        Integer availableQuantity,
        Integer reservedQuantity,
        Boolean belowMinimum
) {
}

