package com.ecommerce.E_commerce.mapper;

import com.ecommerce.E_commerce.dto.productattributevalue.ProductAttributeValueCreateDTO;
import com.ecommerce.E_commerce.dto.productattributevalue.ProductAttributeValueDTO;
import com.ecommerce.E_commerce.dto.productattributevalue.ProductAttributeValueUpdateDTO;
import com.ecommerce.E_commerce.model.CategoryAttribute;
import com.ecommerce.E_commerce.model.ProductAttributeValue;
import com.ecommerce.E_commerce.repository.CategoryAttributeRepository;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

@Mapper(componentModel = "spring")
public abstract class ProductAttributeValueMapper {

    @Autowired
    protected CategoryAttributeRepository categoryAttributeRepository;

    @Mapping(target = "attributeName", source = "attribute.name")
    @Mapping(target = "attributeType", source = "attribute.type")
    @Mapping(target = "isKeyAttribute", expression = "java(getIsKeyAttribute(productAttributeValue))")
    public abstract ProductAttributeValueDTO toProductAttributeValueDTO(ProductAttributeValue productAttributeValue);

    protected Boolean getIsKeyAttribute(ProductAttributeValue productAttributeValue) {
        if (productAttributeValue == null || productAttributeValue.getProduct() == null || 
            productAttributeValue.getAttribute() == null) {
            return false;
        }
        
        Long categoryId = productAttributeValue.getProduct().getCategory().getId();
        Long attributeId = productAttributeValue.getAttribute().getId();
        
        Optional<CategoryAttribute> categoryAttribute = categoryAttributeRepository
                .findByCategoryIdAndAttributeId(categoryId, attributeId);
        
        return categoryAttribute.map(CategoryAttribute::isKeyAttribute).orElse(false);
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "product", ignore = true) // Will be set by service
    @Mapping(target = "attributeValue", ignore = true) // Will be set by service
    @Mapping(target = "createdAt", ignore = true) // Will be set by entity
    @Mapping(target = "updatedAt", ignore = true) // Will be set by entity
    @Mapping(target = "deletedAt", ignore = true) // Will be set by service
    @Mapping(target = "active", constant = "true") // Default to active
    public abstract ProductAttributeValue toProductAttributeValue(ProductAttributeValueCreateDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "product", ignore = true) // Never update
    @Mapping(target = "attributeValue", ignore = true) // Never update
    @Mapping(target = "createdAt", ignore = true) // Never update
    @Mapping(target = "updatedAt", ignore = true) // Will be set by entity
    @Mapping(target = "deletedAt", ignore = true) // Will be handled by service
    public abstract void updateProductAttributeValueFromDTO(ProductAttributeValueUpdateDTO dto, @MappingTarget ProductAttributeValue productAttributeValue);
}
