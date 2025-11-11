package com.ecommerce.E_commerce.dto.payment;

import jakarta.validation.constraints.Size;

public record PaymentUpdateDTO(
        String status,
        
        @Size(max = 255, message = "Transaction ID must not exceed 255 characters")
        String transactionId,
        
        @Size(max = 500, message = "Notes must not exceed 500 characters")
        String notes,
        
        Boolean isActive
) {
}

