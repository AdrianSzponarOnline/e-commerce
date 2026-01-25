package com.ecommerce.E_commerce.controller;

import com.ecommerce.E_commerce.dto.page.PageCreateDTO;
import com.ecommerce.E_commerce.dto.page.PageDTO;
import com.ecommerce.E_commerce.dto.page.PageUpdateDTO;
import com.ecommerce.E_commerce.service.PageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pages")
@RequiredArgsConstructor
public class PageController {
    
    private static final Logger logger = LoggerFactory.getLogger(PageController.class);
    private final PageService pageService;

    @PostMapping
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<PageDTO> create(@Valid @RequestBody PageCreateDTO dto) {
        logger.info("POST /api/pages - Creating page: slug={}, title={}", dto.slug(), dto.title());
        PageDTO created = pageService.create(dto);
        logger.info("POST /api/pages - Page created successfully: id={}, slug={}", created.id(), created.slug());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<PageDTO> update(@PathVariable Long id, @Valid @RequestBody PageUpdateDTO dto) {
        logger.info("PUT /api/pages/{} - Updating page", id);
        PageDTO updated = pageService.update(id, dto);
        logger.info("PUT /api/pages/{} - Page updated successfully", id);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        logger.info("DELETE /api/pages/{} - Deleting page", id);
        pageService.delete(id);
        logger.info("DELETE /api/pages/{} - Page deleted successfully", id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<PageDTO> getById(@PathVariable Long id) {
        PageDTO page = pageService.getById(id);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<PageDTO> getBySlug(@PathVariable String slug) {
        PageDTO page = pageService.getBySlug(slug);
        return ResponseEntity.ok(page);
    }

    @GetMapping
    public ResponseEntity<List<PageDTO>> getAll() {
        List<PageDTO> pages = pageService.getAll();
        return ResponseEntity.ok(pages);
    }

    @GetMapping("/active")
    public ResponseEntity<List<PageDTO>> getAllActive() {
        List<PageDTO> pages = pageService.getAllActive();
        return ResponseEntity.ok(pages);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<PageDTO>> searchPages(
            @RequestParam(required = false) String slug,
            @RequestParam(required = false) String title,
            @PageableDefault(size = 10, sort = "title") Pageable pageable) {
        logger.info("GET /api/pages/search - Searching pages: slug={}, title={}", slug, title);
        Page<PageDTO> pages = pageService.search(slug, title, pageable);
        return ResponseEntity.ok(pages);
    }

    @GetMapping("/search/slug")
    public ResponseEntity<Page<PageDTO>> searchBySlug(
            @RequestParam String slug,
            @PageableDefault(size = 10, sort = "slug") Pageable pageable) {
        logger.info("GET /api/pages/search/slug - Searching pages by slug: slug={}", slug);
        Page<PageDTO> pages = pageService.searchBySlug(slug, pageable);
        return ResponseEntity.ok(pages);
    }

    @GetMapping("/search/title")
    public ResponseEntity<Page<PageDTO>> searchByTitle(
            @RequestParam String title,
            @PageableDefault(size = 10, sort = "title") Pageable pageable) {
        logger.info("GET /api/pages/search/title - Searching pages by title: title={}", title);
        Page<PageDTO> pages = pageService.searchByTitle(title, pageable);
        return ResponseEntity.ok(pages);
    }
}
