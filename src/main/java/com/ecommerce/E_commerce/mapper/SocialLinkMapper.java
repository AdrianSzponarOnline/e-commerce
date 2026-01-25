package com.ecommerce.E_commerce.mapper;

import com.ecommerce.E_commerce.dto.sociallink.SocialLinkCreateDTO;
import com.ecommerce.E_commerce.dto.sociallink.SocialLinkDTO;
import com.ecommerce.E_commerce.dto.sociallink.SocialLinkUpdateDTO;
import com.ecommerce.E_commerce.model.SocialLink;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface SocialLinkMapper {
    SocialLinkDTO toSocialLinkDTO(SocialLink socialLink);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "isActive", constant = "true")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "sortOrder", expression = "java(dto.sortOrder() != null ? dto.sortOrder() : 0)")
    SocialLink fromCreateDTO(SocialLinkCreateDTO dto);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    void updateFromDTO(SocialLinkUpdateDTO dto, @MappingTarget SocialLink socialLink);
}
