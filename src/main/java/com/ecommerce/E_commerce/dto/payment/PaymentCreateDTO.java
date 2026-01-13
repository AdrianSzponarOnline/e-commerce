package com.ecommerce.E_commerce.dto.payment;

import com.ecommerce.E_commerce.model.PaymentMethod;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record PaymentCreateDTO(
        @NotNull(message = "Order ID is required")
        Long orderId,
        
        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.0", inclusive = false, message = "Amount must be greater than 0")
        BigDecimal amount,

        @NotNull
        PaymentMethod method,
        
        @Size(max = 255, message = "Transaction ID must not exceed 255 characters")
        @Pattern(regexp = "^[\\p{L}\\p{N} \\-_]+$", message = "Transaction ID contains invalid characters")
        String transactionId,
        
        @Size(max = 500, message = "Notes must not exceed 500 characters")
        @Pattern(regexp = "^[^<>]*$", message = "Notes must not contain HTML or script characters")
        String notes
) {
}

