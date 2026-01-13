package com.ecommerce.E_commerce.event;

public record OrderConfirmedEvent(
        Long orderId,
        String userEmail,
        String customerName
) {
}
