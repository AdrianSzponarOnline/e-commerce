package com.ecommerce.E_commerce.dto.faq;

import java.time.Instant;

public record FaqItemDTO(
        Long id,
        String question,
        String answer,
        Integer sortOrder,
        Boolean isActive,
        Instant createdAt,
        Instant updatedAt
) {
}
