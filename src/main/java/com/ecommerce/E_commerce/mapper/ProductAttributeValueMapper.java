package com.ecommerce.E_commerce.mapper;

import com.ecommerce.E_commerce.dto.productattributevalue.ProductAttributeValueCreateDTO;
import com.ecommerce.E_commerce.dto.productattributevalue.ProductAttributeValueDTO;
import com.ecommerce.E_commerce.dto.productattributevalue.ProductAttributeValueUpdateDTO;
import com.ecommerce.E_commerce.model.ProductAttributeValue;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ProductAttributeValueMapper {

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "categoryAttributeId", source = "categoryAttribute.id")
    @Mapping(target = "categoryAttributeName", source = "categoryAttribute.name")
    @Mapping(target = "categoryAttributeType", source = "categoryAttribute.type")
    @Mapping(target = "isKeyAttribute", source = "categoryAttribute.keyAttribute")
    ProductAttributeValueDTO toProductAttributeValueDTO(ProductAttributeValue productAttributeValue);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "product", ignore = true) // Will be set by service
    @Mapping(target = "categoryAttribute", ignore = true) // Will be set by service
    @Mapping(target = "createdAt", ignore = true) // Will be set by entity
    @Mapping(target = "updatedAt", ignore = true) // Will be set by entity
    @Mapping(target = "deletedAt", ignore = true) // Will be set by service
    @Mapping(target = "isActive", constant = "true") // Default to active
    ProductAttributeValue toProductAttributeValue(ProductAttributeValueCreateDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "product", ignore = true) // Never update
    @Mapping(target = "categoryAttribute", ignore = true) // Never update
    @Mapping(target = "createdAt", ignore = true) // Never update
    @Mapping(target = "updatedAt", ignore = true) // Will be set by entity
    @Mapping(target = "deletedAt", ignore = true) // Will be handled by service
    void updateProductAttributeValueFromDTO(ProductAttributeValueUpdateDTO dto, @MappingTarget ProductAttributeValue productAttributeValue);
}
