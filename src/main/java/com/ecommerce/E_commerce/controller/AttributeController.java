package com.ecommerce.E_commerce.controller;

import com.ecommerce.E_commerce.dto.attribute.AttributeCreateDTO;
import com.ecommerce.E_commerce.dto.attribute.AttributeDTO;
import com.ecommerce.E_commerce.dto.attribute.AttributeUpdateDTO;
import com.ecommerce.E_commerce.service.AttributeService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/attributes")
public class AttributeController {

    private static final Logger logger = LoggerFactory.getLogger(AttributeController.class);
    private final AttributeService attributeService;

    @Autowired
    public AttributeController(AttributeService attributeService) {
        this.attributeService = attributeService;
    }

    @GetMapping
    public ResponseEntity<Page<AttributeDTO>> getAllActive(
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        return ResponseEntity.ok(attributeService.getActiveAttributes(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AttributeDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(attributeService.getAttributeById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<AttributeDTO> create(@Valid @RequestBody AttributeCreateDTO dto) {
        logger.info("POST /api/attributes - Creating attribute: name={}", dto.name());
        AttributeDTO created = attributeService.createAttribute(dto);
        logger.info("POST /api/attributes - Attribute created successfully: attributeId={}, name={}", created.id(), created.name());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<AttributeDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody AttributeUpdateDTO dto) {
        logger.info("PUT /api/attributes/{} - Updating attribute", id);
        AttributeDTO updated = attributeService.update(id, dto);
        logger.info("PUT /api/attributes/{} - Attribute updated successfully: name={}", id, updated.name());
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        logger.info("DELETE /api/attributes/{} - Deleting attribute", id);
        attributeService.deleteAttribute(id);
        logger.info("DELETE /api/attributes/{} - Attribute deleted successfully", id);
        return ResponseEntity.noContent().build();
    }


    @GetMapping("/inactive")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<Page<AttributeDTO>> getInactive(
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        return ResponseEntity.ok(attributeService.getInactiveAttributes(pageable));
    }

    @PatchMapping("/{id}/restore")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<Void> restore(@PathVariable Long id) {
        logger.info("PATCH /api/attributes/{}/restore - Restoring attribute", id);
        attributeService.restoreAttribute(id);
        logger.info("PATCH /api/attributes/{}/restore - Attribute restored successfully", id);
        return ResponseEntity.ok().build();
    }
}