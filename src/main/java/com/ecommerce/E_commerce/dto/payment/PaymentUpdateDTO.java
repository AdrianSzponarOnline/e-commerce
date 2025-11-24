package com.ecommerce.E_commerce.dto.payment;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record PaymentUpdateDTO(
        @Size(max = 20, message = "Status must not exceed 20 characters")
        @Pattern(regexp = "^(PENDING|PROCESSING|COMPLETED|FAILED|CANCELLED|REFUNDED)?$", 
                message = "Status must be one of the valid payment statuses")
        String status,
        
        @Size(max = 255, message = "Transaction ID must not exceed 255 characters")
        @Pattern(regexp = "^[\\p{L}\\p{N} \\-_]*$", message = "Transaction ID contains invalid characters")
        String transactionId,
        
        @Size(max = 500, message = "Notes must not exceed 500 characters")
        @Pattern(regexp = "^[^<>]*$", message = "Notes must not contain HTML or script characters")
        String notes
) {
}

