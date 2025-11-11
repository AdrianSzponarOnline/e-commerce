package com.ecommerce.E_commerce.dto.payment;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record PaymentCreateDTO(
        @NotNull(message = "Order ID is required")
        Long orderId,
        
        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.0", inclusive = false, message = "Amount must be greater than 0")
        BigDecimal amount,
        
        @NotNull(message = "Payment method is required")
        String method,
        
        @Size(max = 255, message = "Transaction ID must not exceed 255 characters")
        String transactionId,
        
        @Size(max = 500, message = "Notes must not exceed 500 characters")
        String notes
) {
}

