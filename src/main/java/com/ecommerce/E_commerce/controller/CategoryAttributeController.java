package com.ecommerce.E_commerce.controller;

import com.ecommerce.E_commerce.dto.categoryattribute.CategoryAttributeCreateDTO;
import com.ecommerce.E_commerce.dto.categoryattribute.CategoryAttributeDTO;
import com.ecommerce.E_commerce.dto.categoryattribute.CategoryAttributeLinkRequestDTO;
import com.ecommerce.E_commerce.dto.categoryattribute.CategoryAttributeUpdateDTO;
import com.ecommerce.E_commerce.service.CategoryAttributeService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories/{categoryId}/attributes")
public class CategoryAttributeController {
    
    private static final Logger logger = LoggerFactory.getLogger(CategoryAttributeController.class);
    private final CategoryAttributeService service;

    @Autowired
    public CategoryAttributeController(CategoryAttributeService service) {
        this.service = service;
    }

    @PostMapping
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<CategoryAttributeDTO> create(@PathVariable("categoryId") Long categoryId,
                                                       @Valid @RequestBody CategoryAttributeLinkRequestDTO requestDTO) {
        logger.info("POST /api/categories/{}/attributes - Creating category attribute: attributeId={}", categoryId, requestDTO.attributeId());
        CategoryAttributeCreateDTO dto = new CategoryAttributeCreateDTO(
                categoryId,
                requestDTO.attributeId(),
                requestDTO.isKeyAttribute()
        );
        CategoryAttributeDTO created = service.create(dto);
        logger.info("POST /api/categories/{}/attributes - Category attribute created successfully: id={}", categoryId, created.id());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<CategoryAttributeDTO> update(@PathVariable("categoryId") Long categoryId,
                                                       @PathVariable("id") Long id,
                                                       @Valid @RequestBody CategoryAttributeUpdateDTO body) {
        logger.info("PUT /api/categories/{}/attributes/{} - Updating category attribute", categoryId, id);
        CategoryAttributeDTO updated = service.update(id, body);
        logger.info("PUT /api/categories/{}/attributes/{} - Category attribute updated successfully", categoryId, id);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryAttributeDTO> getById(@PathVariable("id") Long id) {
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
        logger.info("DELETE /api/categories/{}/attributes/{} - Deleting category attribute", categoryId, id);
        service.softDelete(id);
        logger.info("DELETE /api/categories/{}/attributes/{} - Category attribute deleted successfully", categoryId, id);
        return ResponseEntity.noContent().build();
    }
}