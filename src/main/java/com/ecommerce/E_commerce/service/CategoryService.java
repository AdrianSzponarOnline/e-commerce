package com.ecommerce.E_commerce.service;

import com.ecommerce.E_commerce.dto.category.CategoryCreateDTO;
import com.ecommerce.E_commerce.dto.category.CategoryDTO;
import com.ecommerce.E_commerce.dto.category.CategoryUpdateDTO;

import java.util.List;

public interface CategoryService {
    CategoryDTO create(CategoryCreateDTO dto);

    CategoryDTO update(Long id, CategoryUpdateDTO dto);

    CategoryDTO getById(Long id);

    CategoryDTO getBySeoSlug(String seoSlug);

    List<CategoryDTO> listAll();

    List<CategoryDTO> listActive();

    List<CategoryDTO> listByParent(Long parentId);

    void delete(Long id);
    String getCategoryTreeStructure();
}


