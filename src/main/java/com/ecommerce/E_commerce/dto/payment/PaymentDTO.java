package com.ecommerce.E_commerce.dto.payment;

import java.math.BigDecimal;
import java.time.Instant;

public record PaymentDTO(
        Long id,
        Long orderId,
        BigDecimal amount,
        String method,
        String status,
        Instant transactionDate,
        String transactionId,
        String notes,
        Instant createdAt,
        Instant updatedAt,
        Boolean isActive
) {
}

