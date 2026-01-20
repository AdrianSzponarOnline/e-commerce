package com.ecommerce.E_commerce.dto.statistics;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record MonthlySalesDTO(
    @NotNull(message = "Year is required")
    @Min(value = 2000, message = "Year must be at least 2000")
    @Max(value = 2100, message = "Year cannot exceed 2100")
    Integer year,
    
    @NotNull(message = "Month is required")
    @Min(value = 1, message = "Month must be between 1 and 12")
    @Max(value = 12, message = "Month must be between 1 and 12")
    Integer month,
    
    @NotBlank(message = "Month name is required")
    String monthName,
    
    @NotNull(message = "Total revenue is required")
    @PositiveOrZero(message = "Total revenue must be non-negative")
    BigDecimal totalRevenue,
    
    @NotNull(message = "Total orders is required")
    @PositiveOrZero(message = "Total orders must be non-negative")
    Long totalOrders,
    
    @NotNull(message = "Total products sold is required")
    @PositiveOrZero(message = "Total products sold must be non-negative")
    Long totalProductsSold
) {}
