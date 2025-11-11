package com.ecommerce.E_commerce.dto.inventory;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record InventoryCreateDTO(
        @NotNull(message = "Product ID is required")
        Long productId,

        @NotNull(message = "Available quantity is required")
        @Min(value = 0, message = "Available quantity must be non-negative")
        Integer availableQuantity,

        @Min(value = 0, message = "Reserved quantity must be non-negative")
        Integer reservedQuantity,

        @Min(value = 0, message = "Minimum stock level must be non-negative")
        Integer minimumStockLevel
) {
}

