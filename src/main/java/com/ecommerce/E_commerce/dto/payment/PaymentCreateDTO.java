package com.ecommerce.E_commerce.dto.payment;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
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
        
        @NotBlank(message = "Payment method is required")
        @Size(max = 50, message = "Payment method must not exceed 50 characters")
        @Pattern(regexp = "^(CREDIT_CARD|DEBIT_CARD|PAYPAL|BANK_TRANSFER|CASH_ON_DELIVERY|BLIK|APPLE_PAY|GOOGLE_PAY)$", 
                message = "Payment method must be one of the valid payment methods")
        String method,
        
        @Size(max = 255, message = "Transaction ID must not exceed 255 characters")
        @Pattern(regexp = "^[\\p{L}\\p{N} \\-_]+$", message = "Transaction ID contains invalid characters")
        String transactionId,
        
        @Size(max = 500, message = "Notes must not exceed 500 characters")
        @Pattern(regexp = "^[^<>]*$", message = "Notes must not contain HTML or script characters")
        String notes
) {
}

