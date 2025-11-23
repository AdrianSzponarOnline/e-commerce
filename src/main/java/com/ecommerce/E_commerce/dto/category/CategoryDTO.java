package com.ecommerce.E_commerce.dto.category;

import java.time.Instant;
import java.util.List;

public record CategoryDTO(
        Long id,
        String name,
        String description,
        String seoSlug,
        Boolean isActive,
        Instant createdAt,
        Instant updatedAt,
        Long parentId,
        List<CategoryDTO> children
) {
}
