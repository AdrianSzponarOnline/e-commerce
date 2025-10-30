package com.ecommerce.E_commerce.dto.productimage;

import java.time.Instant;

public record ProductImageDTO(
        Long id,
        Long productId,
        String url,
        String altText,
        Boolean isThumbnail,
        Instant createdAt,
        Instant updatedAt
) {}


