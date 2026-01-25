package com.ecommerce.E_commerce.mapper;

import com.ecommerce.E_commerce.dto.faq.FaqItemCreateDTO;
import com.ecommerce.E_commerce.dto.faq.FaqItemDTO;
import com.ecommerce.E_commerce.dto.faq.FaqItemUpdateDTO;
import com.ecommerce.E_commerce.model.FaqItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface FaqItemMapper {
    FaqItemDTO toFaqItemDTO(FaqItem faqItem);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "isActive", constant = "true")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "sortOrder", expression = "java(dto.sortOrder() != null ? dto.sortOrder() : 0)")
    FaqItem fromCreateDTO(FaqItemCreateDTO dto);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    void updateFromDTO(FaqItemUpdateDTO dto, @MappingTarget FaqItem faqItem);
}
