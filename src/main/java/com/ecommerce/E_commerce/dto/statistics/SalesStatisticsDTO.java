package com.ecommerce.E_commerce.dto.statistics;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.Instant;

public record SalesStatisticsDTO(
    @NotNull(message = "Total revenue is required")
    @PositiveOrZero(message = "Total revenue must be non-negative")
    BigDecimal totalRevenue,
    
    @NotNull(message = "Total orders is required")
    @PositiveOrZero(message = "Total orders must be non-negative")
    Long totalOrders,
    
    @NotNull(message = "Total products sold is required")
    @PositiveOrZero(message = "Total products sold must be non-negative")
    Long totalProductsSold,
    
    @NotNull(message = "Average order value is required")
    @PositiveOrZero(message = "Average order value must be non-negative")
    BigDecimal averageOrderValue,
    
    @NotNull(message = "Period start is required")
    Instant periodStart,
    
    @NotNull(message = "Period end is required")
    Instant periodEnd
) {}
