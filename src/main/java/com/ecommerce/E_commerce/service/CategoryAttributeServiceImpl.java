package com.ecommerce.E_commerce.service;

import com.ecommerce.E_commerce.dto.categoryattribute.CategoryAttributeCreateDTO;
import com.ecommerce.E_commerce.dto.categoryattribute.CategoryAttributeDTO;
import com.ecommerce.E_commerce.dto.categoryattribute.CategoryAttributeUpdateDTO;
import com.ecommerce.E_commerce.exception.DuplicateResourceException;
import com.ecommerce.E_commerce.exception.ResourceNotFoundException;
import com.ecommerce.E_commerce.mapper.CategoryAttributeMapper;
import com.ecommerce.E_commerce.model.Attribute;
import com.ecommerce.E_commerce.model.Category;
import com.ecommerce.E_commerce.model.CategoryAttribute;
import com.ecommerce.E_commerce.repository.AttributeRepository;
import com.ecommerce.E_commerce.repository.CategoryAttributeRepository;
import com.ecommerce.E_commerce.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoryAttributeServiceImpl implements CategoryAttributeService {
    private final CategoryAttributeRepository categoryAttributeRepository;
    private final CategoryRepository categoryRepository;
    private final AttributeRepository attributeRepository;
    private final CategoryAttributeMapper mapper;

    @Autowired
    public CategoryAttributeServiceImpl(CategoryAttributeRepository categoryAttributeRepository,
                                        CategoryRepository categoryRepository,
                                        AttributeRepository attributeRepository,
                                        CategoryAttributeMapper mapper) {
        this.categoryAttributeRepository = categoryAttributeRepository;
        this.categoryRepository = categoryRepository;
        this.attributeRepository = attributeRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public CategoryAttributeDTO create(CategoryAttributeCreateDTO dto) {
        Category category = categoryRepository.findById(dto.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + dto.categoryId()));
        
        Attribute attribute = attributeRepository.findById(dto.attributeId())
                .orElseThrow(() -> new ResourceNotFoundException("Attribute not found: " + dto.attributeId()));
        if (categoryAttributeRepository.findByCategoryIdAndAttributeId(dto.categoryId(), dto.attributeId()).isPresent()) {
            throw new DuplicateResourceException("Category attribute already exists for this category and attribute");
        }
        
        CategoryAttribute entity = new CategoryAttribute();
        entity.setCategory(category);
        entity.setAttribute(attribute);
        entity.setKeyAttribute(dto.isKeyAttribute());
        
        CategoryAttribute saved = categoryAttributeRepository.save(entity);
        return mapper.toDTO(saved);
    }

    @Override
    @Transactional
    public CategoryAttributeDTO update(Long id, CategoryAttributeUpdateDTO dto) {
        CategoryAttribute entity = categoryAttributeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category attribute not found: " + id));
        
        if (dto.isKeyAttribute() != null) {
            entity.setKeyAttribute(dto.isKeyAttribute());
        }
        if (dto.isActive() != null) {
            entity.setActive(dto.isActive());
        }
        
        CategoryAttribute saved = categoryAttributeRepository.save(entity);
        return mapper.toDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryAttributeDTO getById(Long id) {
        CategoryAttribute entity = categoryAttributeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category attribute not found: " + id));
        return mapper.toDTO(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryAttributeDTO> listByCategory(Long categoryId) {
        return categoryAttributeRepository.findAllByCategory_Id(categoryId)
                .stream().map(mapper::toDTO).toList();
    }

    @Override
    @Transactional
    public void softDelete(Long id) {
        CategoryAttribute entity = categoryAttributeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category attribute not found: " + id));
        
        categoryAttributeRepository.delete(entity);
    }
}


