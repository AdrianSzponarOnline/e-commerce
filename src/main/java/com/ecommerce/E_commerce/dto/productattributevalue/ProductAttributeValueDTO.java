package com.ecommerce.E_commerce.dto.productattributevalue;

public record ProductAttributeValueDTO(
       Long id,
       String attributeName,
       String attributeType,
       Boolean isKeyAttribute,
       String attributeValue
) {
}
