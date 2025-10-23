package com.ecommerce.E_commerce.controller;

import com.ecommerce.E_commerce.dto.category.CategoryCreateDTO;
import com.ecommerce.E_commerce.dto.category.CategoryDTO;
import com.ecommerce.E_commerce.dto.category.CategoryUpdateDTO;
import com.ecommerce.E_commerce.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {
    private final CategoryService categoryService;

    @Autowired
    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<CategoryDTO> create(@Valid @RequestBody CategoryCreateDTO body) {
        CategoryDTO created = categoryService.create(body);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<CategoryDTO> update(@PathVariable("id") Long id, @Valid @RequestBody CategoryUpdateDTO body) {
        return ResponseEntity.ok(categoryService.update(id, body));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryDTO> getById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(categoryService.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<CategoryDTO>> listAll() {
        return ResponseEntity.ok(categoryService.listAll());
    }

    @GetMapping("/public")
    public ResponseEntity<List<CategoryDTO>> listAllPublic() {
        return ResponseEntity.ok(categoryService.listAll());
    }

    @GetMapping("/active")
    public ResponseEntity<List<CategoryDTO>> listActive() {
        return ResponseEntity.ok(categoryService.listActive());
    }

    @GetMapping("/public/active")
    public ResponseEntity<List<CategoryDTO>> listActivePublic() {
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
    public ResponseEntity<Void> softDelete(@PathVariable("id") Long id) {
        categoryService.softDelete(id);
        return ResponseEntity.noContent().build();
    }
}


