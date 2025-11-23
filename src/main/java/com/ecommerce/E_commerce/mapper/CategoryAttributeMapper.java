package com.ecommerce.E_commerce.mapper;

import com.ecommerce.E_commerce.dto.categoryattribute.CategoryAttributeCreateDTO;
import com.ecommerce.E_commerce.dto.categoryattribute.CategoryAttributeDTO;
import com.ecommerce.E_commerce.dto.categoryattribute.CategoryAttributeUpdateDTO;
import com.ecommerce.E_commerce.model.CategoryAttribute;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CategoryAttributeMapper {


    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "attributeId", source = "attribute.id")
    @Mapping(target = "attributeName", source = "attribute.name")
    @Mapping(target = "attributeType", source = "attribute.type")
    @Mapping(target = "isKeyAttribute", source = "keyAttribute")
    @Mapping(target = "isActive", source = "active")
    CategoryAttributeDTO toDTO(CategoryAttribute entity);



    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "attribute", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "active", constant = "true")
    @Mapping(target = "keyAttribute", source = "isKeyAttribute")
    CategoryAttribute fromCreateDTO(CategoryAttributeCreateDTO dto);



    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "attribute", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "keyAttribute", source = "isKeyAttribute")
    @Mapping(target = "active", source = "isActive")
    void updateFromDTO(CategoryAttributeUpdateDTO dto, @MappingTarget CategoryAttribute entity);
}