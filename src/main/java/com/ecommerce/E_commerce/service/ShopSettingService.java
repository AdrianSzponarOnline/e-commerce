package com.ecommerce.E_commerce.service;

import com.ecommerce.E_commerce.dto.shopsetting.FooterDataDTO;
import com.ecommerce.E_commerce.dto.shopsetting.ShopSettingCreateDTO;
import com.ecommerce.E_commerce.dto.shopsetting.ShopSettingDTO;
import com.ecommerce.E_commerce.dto.shopsetting.ShopSettingUpdateDTO;

import java.util.List;
import java.util.Map;

public interface ShopSettingService {
    ShopSettingDTO create(ShopSettingCreateDTO dto);
    
    ShopSettingDTO update(Long id, ShopSettingUpdateDTO dto);
    
    ShopSettingDTO updateByKey(String key, ShopSettingUpdateDTO dto);
    
    void delete(Long id);
    
    ShopSettingDTO getById(Long id);
    
    ShopSettingDTO getByKey(String key);
    
    List<ShopSettingDTO> getAll();
    
    Map<String, String> getAllAsMap();
    
    FooterDataDTO getFooterData();
}
