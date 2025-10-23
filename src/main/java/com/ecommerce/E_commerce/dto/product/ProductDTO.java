package com.ecommerce.E_commerce.dto.product;

import com.ecommerce.E_commerce.model.Category;
import com.ecommerce.E_commerce.model.ProductAttributeValue;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record ProductDTO(
        Long id,
        String name,
        String description,
        String shortDescription,
        BigDecimal price,
        String sku,
        BigDecimal vatRate,
        Boolean isFeatured,
        BigDecimal shippingCost,
        String estimatedDeliveryTime,
        String thumbnailUrl,
        String seoSlug,
        Category category,
        List<ProductAttributeValue> attributeValues,
        Instant createdAt,
        Instant updatedAt,
        Instant deletedAt,
        Boolean isActive
) {
}
