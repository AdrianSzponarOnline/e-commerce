package com.ecommerce.E_commerce.dto.search;

import java.math.BigDecimal;

public record ProductDetailsResponse(
        String name,
        String description,
        BigDecimal price,
        String category,
        String technicalSpecs,
        BigDecimal shippingCost,
        String deliveryTime
) {
}
