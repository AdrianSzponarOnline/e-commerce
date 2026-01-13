package com.ecommerce.E_commerce.dto.payment;

import com.ecommerce.E_commerce.model.PaymentStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record PaymentUpdateDTO(
        @NotNull
        PaymentStatus status,
        
        @Size(max = 255, message = "Transaction ID must not exceed 255 characters")
        @Pattern(regexp = "^[\\p{L}\\p{N} \\-_]*$", message = "Transaction ID contains invalid characters")
        String transactionId,
        
        @Size(max = 500, message = "Notes must not exceed 500 characters")
        @Pattern(regexp = "^[^<>]*$", message = "Notes must not contain HTML or script characters")
        String notes
) {
}

