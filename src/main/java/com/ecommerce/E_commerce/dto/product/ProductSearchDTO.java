package com.ecommerce.E_commerce.dto.product;

import java.math.BigDecimal;
import java.util.Map;

public record ProductSearchDTO(
        Long id,
        String name,
        BigDecimal price,
        String shortDescription,
        String thumbnailUrl,
        String seoSlug,
        String categoryName,
        Map<String, String> attributes
) {
}
