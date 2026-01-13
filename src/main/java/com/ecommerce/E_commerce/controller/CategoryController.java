package com.ecommerce.E_commerce.controller;

import com.ecommerce.E_commerce.dto.category.CategoryCreateDTO;
import com.ecommerce.E_commerce.dto.category.CategoryDTO;
import com.ecommerce.E_commerce.dto.category.CategoryUpdateDTO;
import com.ecommerce.E_commerce.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {
    
    private static final Logger logger = LoggerFactory.getLogger(CategoryController.class);
    private final CategoryService categoryService;

    @PostMapping
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<CategoryDTO> create(@Valid @RequestBody CategoryCreateDTO body) {
        logger.info("POST /api/categories - Creating category: name={}, parentId={}", body.name(), body.parentId());
        CategoryDTO created = categoryService.create(body);
        logger.info("POST /api/categories - Category created successfully: categoryId={}, name={}", created.id(), created.name());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<CategoryDTO> update(@PathVariable("id") Long id, @Valid @RequestBody CategoryUpdateDTO body) {
        logger.info("PUT /api/categories/{} - Updating category", id);
        CategoryDTO updated = categoryService.update(id, body);
        logger.info("PUT /api/categories/{} - Category updated successfully: name={}", id, updated.name());
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryDTO> getById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(categoryService.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<CategoryDTO>> listAll() {
        return ResponseEntity.ok(categoryService.listAll());
    }

    @GetMapping("/active")
    public ResponseEntity<List<CategoryDTO>> listActive() {
        return ResponseEntity.ok(categoryService.listActive());
    }



    @GetMapping("/parent/{parentId}")
    public ResponseEntity<List<CategoryDTO>> listByParent(@PathVariable("parentId") Long parentId) {
        return ResponseEntity.ok(categoryService.listByParent(parentId));
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<CategoryDTO> getBySlug(@PathVariable("slug") String slug) {
        return ResponseEntity.ok(categoryService.getBySeoSlug(slug));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        logger.info("DELETE /api/categories/{} - Deleting category", id);
        categoryService.delete(id);
        logger.info("DELETE /api/categories/{} - Category deleted successfully", id);
        return ResponseEntity.noContent().build();
    }
}


