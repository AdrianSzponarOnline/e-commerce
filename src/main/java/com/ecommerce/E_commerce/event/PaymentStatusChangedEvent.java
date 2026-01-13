package com.ecommerce.E_commerce.event;

import com.ecommerce.E_commerce.model.PaymentStatus;

public record PaymentStatusChangedEvent(
        Long paymentId,
        Long orderId,
        PaymentStatus oldStatus,
        PaymentStatus newStatus
) {
}
