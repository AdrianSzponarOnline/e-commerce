package com.ecommerce.E_commerce.controller;

import com.ecommerce.E_commerce.dto.categoryattribute.CategoryAttributeCreateDTO;
import com.ecommerce.E_commerce.dto.categoryattribute.CategoryAttributeDTO;
import com.ecommerce.E_commerce.dto.categoryattribute.CategoryAttributeUpdateDTO;
import com.ecommerce.E_commerce.service.CategoryAttributeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories/{categoryId}/attributes")
public class CategoryAttributeController {
    private final CategoryAttributeService service;

    @Autowired
    public CategoryAttributeController(CategoryAttributeService service) {
        this.service = service;
    }

    @PostMapping
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<CategoryAttributeDTO> create(@PathVariable("categoryId") Long categoryId,
                                                       @Valid @RequestBody CategoryAttributeCreateDTO body) {
        CategoryAttributeCreateDTO dto = new CategoryAttributeCreateDTO(categoryId, body.name(), body.type(), body.isActive());
        CategoryAttributeDTO created = service.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<CategoryAttributeDTO> update(@PathVariable("categoryId") Long categoryId,
                                                       @PathVariable("id") Long id,
                                                       @Valid @RequestBody CategoryAttributeUpdateDTO body) {
        return ResponseEntity.ok(service.update(id, body));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryAttributeDTO> getById(@PathVariable("categoryId") Long categoryId,
                                                        @PathVariable("id") Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<CategoryAttributeDTO>> list(@PathVariable("categoryId") Long categoryId) {
        return ResponseEntity.ok(service.listByCategory(categoryId));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<Void> delete(@PathVariable("categoryId") Long categoryId,
                                       @PathVariable("id") Long id) {
        service.softDelete(id);
        return ResponseEntity.noContent().build();
    }
}


