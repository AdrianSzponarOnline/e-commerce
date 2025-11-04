package com.ecommerce.E_commerce.dto.productattributevalue;

import java.time.Instant;

public record ProductAttributeValueDTO(
       Long id,
       String categoryAttributeName,
       String categoryAttributeType,
       Boolean isKeyAttribute,
       String value
) {
}
