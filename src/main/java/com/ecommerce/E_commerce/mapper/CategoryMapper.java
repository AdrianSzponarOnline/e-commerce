package com.ecommerce.E_commerce.mapper;

import com.ecommerce.E_commerce.dto.category.CategoryCreateDTO;
import com.ecommerce.E_commerce.dto.category.CategoryDTO;
import com.ecommerce.E_commerce.dto.category.CategoryUpdateDTO;
import com.ecommerce.E_commerce.dto.category.ChildCategoryDTO;
import com.ecommerce.E_commerce.model.Category;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    @Mapping(target = "parentId", source = "parent.id")
    CategoryDTO toCategoryDTO(Category category);

    @Mapping(target = "parent", ignore = true)
    Category toEntity(CategoryDTO categoryDTO);

    @Mapping(target = "parent", ignore = true) 
    Category fromCreateDTO(CategoryCreateDTO dto);

    @Mapping(target = "parent", ignore = true)
    void updateFromDTO(CategoryUpdateDTO dto, @MappingTarget Category category);

    default Set<ChildCategoryDTO> mapChildren(Set<Category> children) {
        if (children == null) {
            return null;
        }
        return children.stream()
                .map(category -> new ChildCategoryDTO(category.getId(), category.getName(), category.getSeoSlug()))
                .collect(Collectors.toSet());
    }
}
