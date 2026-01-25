package com.ecommerce.E_commerce.dto.sociallink;

import java.time.Instant;

public record SocialLinkDTO(
        Long id,
        String platformName,
        String url,
        String iconCode,
        Integer sortOrder,
        Boolean isActive,
        Instant createdAt,
        Instant updatedAt
) {
}
