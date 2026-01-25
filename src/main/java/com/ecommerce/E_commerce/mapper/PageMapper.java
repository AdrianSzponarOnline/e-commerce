package com.ecommerce.E_commerce.mapper;

import com.ecommerce.E_commerce.dto.page.PageCreateDTO;
import com.ecommerce.E_commerce.dto.page.PageDTO;
import com.ecommerce.E_commerce.dto.page.PageUpdateDTO;
import com.ecommerce.E_commerce.model.WebsitePage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface PageMapper {
    PageDTO toPageDTO(WebsitePage websitePage);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "isActive", constant = "true")
    WebsitePage fromCreateDTO(PageCreateDTO dto);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "slug", ignore = true)
    @Mapping(target = "isSystem", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    void updateFromDTO(PageUpdateDTO dto, @MappingTarget WebsitePage websitePage);
}
