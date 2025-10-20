package com.ecommerce.E_commerce.service;

import com.ecommerce.E_commerce.dto.category.CategoryCreateDTO;
import com.ecommerce.E_commerce.dto.category.CategoryDTO;
import com.ecommerce.E_commerce.dto.category.CategoryUpdateDTO;
import com.ecommerce.E_commerce.exception.ResourceNotFoundException;
import com.ecommerce.E_commerce.exception.SeoSlugAlreadyExistsException;
import com.ecommerce.E_commerce.mapper.CategoryMapper;
import com.ecommerce.E_commerce.model.Category;
import com.ecommerce.E_commerce.repository.CategoryRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Autowired
    public CategoryServiceImpl(CategoryRepository categoryRepository, CategoryMapper categoryMapper) {
        this.categoryRepository = categoryRepository;
        this.categoryMapper = categoryMapper;
    }

    @Override
    @Transactional
    public CategoryDTO create(CategoryCreateDTO dto) {
        if (categoryRepository.existsBySeoSlug(dto.seoSlug())) {
            throw new SeoSlugAlreadyExistsException(dto.seoSlug());
        }
        Category category = categoryMapper.fromCreateDTO(dto);
        if (dto.parentId() != null) {
            Category parent = categoryRepository.findById(dto.parentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent category not found: " + dto.parentId()));
            category.setParent(parent);
        }
        Category saved = categoryRepository.save(category);
        return categoryMapper.toCategoryDTO(saved);
    }

    @Override
    @Transactional
    public CategoryDTO update(Long id, CategoryUpdateDTO dto) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + id));
        if (!category.getSeoSlug().equals(dto.seoSlug()) && categoryRepository.existsBySeoSlug(dto.seoSlug())) {
            throw new SeoSlugAlreadyExistsException(dto.seoSlug());
        }
        categoryMapper.updateFromDTO(dto, category);
        if (dto.parentId() != null) {
            if (id.equals(dto.parentId())) {
                throw new com.ecommerce.E_commerce.exception.InvalidOperationException("Category cannot be its own parent");
            }
            Category parent = categoryRepository.findById(dto.parentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent category not found: " + dto.parentId()));
            ensureNoCycle(category, parent);
            category.setParent(parent);
        } else {
            category.setParent(null);
        }
        Category saved = categoryRepository.save(category);
        return categoryMapper.toCategoryDTO(saved);
    }

    @Override
    public CategoryDTO getById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + id));
        return categoryMapper.toCategoryDTO(category);
    }

    @Override
    public CategoryDTO getBySeoSlug(String seoSlug) {
        Category category = categoryRepository.findBySeoSlug(seoSlug)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found by seoSlug: " + seoSlug));
        return categoryMapper.toCategoryDTO(category);
    }

    @Override
    public List<CategoryDTO> listAll() {
        return categoryRepository.findAll().stream()
                .map(categoryMapper::toCategoryDTO)
                .toList();
    }

    @Override
    public List<CategoryDTO> listActive() {
        return categoryRepository.findAllByIsActiveTrue().stream()
                .map(categoryMapper::toCategoryDTO)
                .toList();
    }

    @Override
    public List<CategoryDTO> listByParent(Long parentId) {
        return categoryRepository.findAllByParent_Id(parentId).stream()
                .map(categoryMapper::toCategoryDTO)
                .toList();
    }

    @Override
    @Transactional
    public void softDelete(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + id));
        category.setIsActive(false);
        category.setDeletedAt(Instant.now());
        categoryRepository.save(category);
    }

    private void ensureNoCycle(Category category, Category potentialParent) {
        Category cursor = potentialParent;
        while (cursor != null) {
            if (cursor.getId() != null && cursor.getId().equals(category.getId())) {
                throw new com.ecommerce.E_commerce.exception.InvalidOperationException("Setting this parent would create a circular dependency");
            }
            cursor = cursor.getParent();
        }
    }
}


