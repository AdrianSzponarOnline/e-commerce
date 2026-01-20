package com.ecommerce.E_commerce.dto.statistics;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record TopProductDTO(
    @NotNull(message = "Product ID is required")
    @Positive(message = "Product ID must be positive")
    Long productId,
    
    @NotBlank(message = "Product name is required")
    String productName,
    
    @NotBlank(message = "Product SKU is required")
    String productSku,
    
    @NotNull(message = "Total quantity sold is required")
    @PositiveOrZero(message = "Total quantity sold must be non-negative")
    Long totalQuantitySold,
    
    @NotNull(message = "Total revenue is required")
    @PositiveOrZero(message = "Total revenue must be non-negative")
    BigDecimal totalRevenue,
    
    @NotNull(message = "Order count is required")
    @PositiveOrZero(message = "Order count must be non-negative")
    Long orderCount
) {}
