package com.ecommerce.E_commerce.mapper;

import com.ecommerce.E_commerce.dto.category.CategoryCreateDTO;
import com.ecommerce.E_commerce.dto.category.CategoryDTO;
import com.ecommerce.E_commerce.dto.category.CategoryUpdateDTO;
import com.ecommerce.E_commerce.dto.category.ChildCategoryDTO;
import com.ecommerce.E_commerce.model.Category;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

import java.util.ArrayList; // Upewnij się, że ten import jest
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    @Named("categoryFlat")
    @Mapping(target = "parentId", source = "parent.id")
    @Mapping(target = "children", expression = "java(new java.util.ArrayList<CategoryDTO>())")
    CategoryDTO toCategoryDTOFlat(Category category);

    @Named("categoryWithTree")
    @Mapping(target = "parentId", source = "parent.id")
    @Mapping(target = "children", source = "children", qualifiedByName = "categoryWithTree")
    CategoryDTO toCategoryDTOWithTree(Category category);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "parent", ignore = true)
    @Mapping(target = "children", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    Category toEntity(CategoryDTO categoryDTO);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "parent", ignore = true)
    @Mapping(target = "children", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    Category fromCreateDTO(CategoryCreateDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "parent", ignore = true)
    @Mapping(target = "children", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    void updateFromDTO(CategoryUpdateDTO dto, @MappingTarget Category category);
}