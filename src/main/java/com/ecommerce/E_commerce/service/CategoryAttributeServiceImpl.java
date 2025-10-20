package com.ecommerce.E_commerce.service;

import com.ecommerce.E_commerce.dto.categoryattribute.CategoryAttributeCreateDTO;
import com.ecommerce.E_commerce.dto.categoryattribute.CategoryAttributeDTO;
import com.ecommerce.E_commerce.dto.categoryattribute.CategoryAttributeUpdateDTO;
import com.ecommerce.E_commerce.exception.ResourceNotFoundException;
import com.ecommerce.E_commerce.mapper.CategoryAttributeMapper;
import com.ecommerce.E_commerce.model.Category;
import com.ecommerce.E_commerce.model.CategoryAttribute;
import com.ecommerce.E_commerce.repository.CategoryAttributeRepository;
import com.ecommerce.E_commerce.repository.CategoryRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class CategoryAttributeServiceImpl implements CategoryAttributeService {
    private final CategoryAttributeRepository attributeRepository;
    private final CategoryRepository categoryRepository;
    private final CategoryAttributeMapper mapper;

    @Autowired
    public CategoryAttributeServiceImpl(CategoryAttributeRepository attributeRepository,
                                        CategoryRepository categoryRepository,
                                        CategoryAttributeMapper mapper) {
        this.attributeRepository = attributeRepository;
        this.categoryRepository = categoryRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public CategoryAttributeDTO create(CategoryAttributeCreateDTO dto) {
        CategoryAttribute entity = mapper.fromCreateDTO(dto);
        Category category = categoryRepository.findById(dto.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + dto.categoryId()));
        entity.setCategory(category);
        CategoryAttribute saved = attributeRepository.save(entity);
        return mapper.toDTO(saved);
    }

    @Override
    @Transactional
    public CategoryAttributeDTO update(Long id, CategoryAttributeUpdateDTO dto) {
        CategoryAttribute entity = attributeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category attribute not found: " + id));
        mapper.updateFromDTO(dto, entity);
        CategoryAttribute saved = attributeRepository.save(entity);
        return mapper.toDTO(saved);
    }

    @Override
    public CategoryAttributeDTO getById(Long id) {
        CategoryAttribute entity = attributeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category attribute not found: " + id));
        return mapper.toDTO(entity);
    }

    @Override
    public List<CategoryAttributeDTO> listByCategory(Long categoryId) {
        return attributeRepository.findAllByCategory_Id(categoryId)
                .stream().map(mapper::toDTO).toList();
    }

    @Override
    @Transactional
    public void softDelete(Long id) {
        CategoryAttribute entity = attributeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category attribute not found: " + id));
        entity.setIsActive(false);
        entity.setDeletedAt(Instant.now());
        attributeRepository.save(entity);
    }
}


