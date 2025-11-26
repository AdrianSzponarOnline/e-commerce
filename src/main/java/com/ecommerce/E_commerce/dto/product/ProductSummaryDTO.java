package com.ecommerce.E_commerce.dto.product;

import java.math.BigDecimal;

public record ProductSummaryDTO(
        Long id,
        String name,
        BigDecimal price,
        String shortDescription,
        String thumbnailUrl,
        String seoSlug,
        String categoryName,
        Boolean isActive
) {
}
