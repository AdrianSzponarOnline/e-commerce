package com.ecommerce.E_commerce.dto.newsletter;

import java.time.Instant;

public record NewsletterSubscriptionDTO(
        Long id,
        String email,
        Instant subscribedAt,
        Long userId
) {
}
