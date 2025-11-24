package com.ecommerce.E_commerce.service;

import com.ecommerce.E_commerce.dto.category.CategoryCreateDTO;
import com.ecommerce.E_commerce.dto.category.CategoryDTO;
import com.ecommerce.E_commerce.dto.category.CategoryUpdateDTO;
import com.ecommerce.E_commerce.exception.InvalidOperationException;
import com.ecommerce.E_commerce.exception.ResourceNotFoundException;
import com.ecommerce.E_commerce.exception.SeoSlugAlreadyExistsException;
import com.ecommerce.E_commerce.mapper.CategoryMapper;
import com.ecommerce.E_commerce.model.Category;
import com.ecommerce.E_commerce.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        return categoryMapper.toCategoryDTOFlat(saved);
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
                throw new InvalidOperationException("Category cannot be its own parent");
            }
            Category parent = categoryRepository.findById(dto.parentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent category not found: " + dto.parentId()));
            ensureNoCycle(category, parent);
            category.setParent(parent);
        } else {
            category.setParent(null);
        }
        Category saved = categoryRepository.save(category);
        return categoryMapper.toCategoryDTOFlat(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryDTO getById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + id));
        return categoryMapper.toCategoryDTOFlat(category);
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryDTO getBySeoSlug(String seoSlug) {
        Category category = categoryRepository.findBySeoSlug(seoSlug)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found by seoSlug: " + seoSlug));
        return categoryMapper.toCategoryDTOFlat(category);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDTO> listAll() {
        List<Category> allCategories = categoryRepository.findAll();

        List<CategoryDTO> flat = allCategories.stream()
                .map(categoryMapper::toCategoryDTOFlat)
                .toList();

        return buildTree(flat);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDTO> listActive() {
        List<Category> activeCategories = categoryRepository.findAllByIsActiveTrue();
        List<CategoryDTO> flat = activeCategories.stream()
                .map(categoryMapper::toCategoryDTOFlat)
                .toList();
        return buildTree(flat);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDTO> listByParent(Long parentId) {
        List<CategoryDTO> flat = categoryRepository.findAllByParent_Id(parentId).stream()
                .map(categoryMapper::toCategoryDTOFlat)
                .toList();
        return buildTree(flat);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + id));
        
        categoryRepository.delete(category);
    }

    private void ensureNoCycle(Category category, Category potentialParent) {
        Category cursor = potentialParent;
        while (cursor != null) {
            if (cursor.getId() != null && cursor.getId().equals(category.getId())) {
                throw new InvalidOperationException("Setting this parent would create a circular dependency");
            }
            cursor = cursor.getParent();
        }
    }
    public static List<CategoryDTO> buildTree(List<CategoryDTO> allCategories) {
        if (allCategories == null || allCategories.isEmpty()) {
            return List.of();
        }

        Map<Long, CategoryDTO> copyById = new HashMap<>();
        for (CategoryDTO c : allCategories) {
            copyById.put(c.id(), new CategoryDTO(
                    c.id(), c.name(), c.description(), c.seoSlug(), c.isActive(),
                    c.createdAt(), c.updatedAt(), c.parentId(), new ArrayList<>()
            ));
        }

        List<CategoryDTO> roots = new ArrayList<>();
        for (CategoryDTO c : allCategories) {
            CategoryDTO current = copyById.get(c.id());
            if (c.parentId() == null) {
                roots.add(current);
            } else {
                CategoryDTO parent = copyById.get(c.parentId());
                if (parent != null) {
                    parent.children().add(current);
                } else {
                    roots.add(current);
                }
            }
        }
        return roots;
    }
}


