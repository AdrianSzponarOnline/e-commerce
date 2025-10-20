package com.ecommerce.E_commerce.service;

import com.ecommerce.E_commerce.dto.categoryattribute.CategoryAttributeCreateDTO;
import com.ecommerce.E_commerce.dto.categoryattribute.CategoryAttributeDTO;
import com.ecommerce.E_commerce.dto.categoryattribute.CategoryAttributeUpdateDTO;

import java.util.List;

public interface CategoryAttributeService {
    CategoryAttributeDTO create(CategoryAttributeCreateDTO dto);
    CategoryAttributeDTO update(Long id, CategoryAttributeUpdateDTO dto);
    CategoryAttributeDTO getById(Long id);
    List<CategoryAttributeDTO> listByCategory(Long categoryId);
    void softDelete(Long id);
}


