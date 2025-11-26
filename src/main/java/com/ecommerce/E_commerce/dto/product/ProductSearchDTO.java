package com.ecommerce.E_commerce.dto.product;

import org.springframework.data.domain.Pageable;

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
        Boolean isActive,
        Map<String, String> attributes
) {
}
