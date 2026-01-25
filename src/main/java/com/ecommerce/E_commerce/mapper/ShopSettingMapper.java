package com.ecommerce.E_commerce.mapper;

import com.ecommerce.E_commerce.dto.shopsetting.ShopSettingCreateDTO;
import com.ecommerce.E_commerce.dto.shopsetting.ShopSettingDTO;
import com.ecommerce.E_commerce.dto.shopsetting.ShopSettingUpdateDTO;
import com.ecommerce.E_commerce.model.ShopSetting;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ShopSettingMapper {
    ShopSettingDTO toShopSettingDTO(ShopSetting shopSetting);
    
    @Mapping(target = "id", ignore = true)
    ShopSetting fromCreateDTO(ShopSettingCreateDTO dto);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "key", ignore = true)
    void updateFromDTO(ShopSettingUpdateDTO dto, @MappingTarget ShopSetting shopSetting);
}
