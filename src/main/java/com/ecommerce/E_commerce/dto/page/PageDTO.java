package com.ecommerce.E_commerce.dto.page;

import java.time.Instant;

public record PageDTO(
        Long id,
        String slug,
        String title,
        String content,
        Boolean isSystem,
        Instant createdAt,
        Instant updatedAt,
        Boolean isActive
) {
}
