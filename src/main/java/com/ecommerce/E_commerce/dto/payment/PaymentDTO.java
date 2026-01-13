package com.ecommerce.E_commerce.dto.payment;

import com.ecommerce.E_commerce.model.PaymentMethod;
import com.ecommerce.E_commerce.model.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record PaymentDTO(
        Long id,
        Long orderId,
        BigDecimal amount,
        PaymentMethod method,
        PaymentStatus status,
        Instant transactionDate,
        String transactionId,
        String notes,
        Instant createdAt,
        Instant updatedAt,
        Boolean isActive
) {
}

