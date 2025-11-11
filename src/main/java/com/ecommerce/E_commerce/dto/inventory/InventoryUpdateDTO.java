package com.ecommerce.E_commerce.dto.inventory;

import jakarta.validation.constraints.Min;

public record InventoryUpdateDTO(
        @Min(value = 0, message = "Available quantity must be non-negative")
        Integer availableQuantity,

        @Min(value = 0, message = "Reserved quantity must be non-negative")
        Integer reservedQuantity,

        @Min(value = 0, message = "Minimum stock level must be non-negative")
        Integer minimumStockLevel,

        Boolean isActive
) {
}

