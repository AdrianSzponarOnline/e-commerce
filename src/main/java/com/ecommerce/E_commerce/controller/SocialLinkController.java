package com.ecommerce.E_commerce.controller;

import com.ecommerce.E_commerce.dto.sociallink.SocialLinkCreateDTO;
import com.ecommerce.E_commerce.dto.sociallink.SocialLinkDTO;
import com.ecommerce.E_commerce.dto.sociallink.SocialLinkUpdateDTO;
import com.ecommerce.E_commerce.service.SocialLinkService;
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
@RequestMapping("/api/social-links")
@RequiredArgsConstructor
public class SocialLinkController {
    
    private static final Logger logger = LoggerFactory.getLogger(SocialLinkController.class);
    private final SocialLinkService socialLinkService;

    @PostMapping
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<SocialLinkDTO> create(@Valid @RequestBody SocialLinkCreateDTO dto) {
        logger.info("POST /api/social-links - Creating social link: platform={}", dto.platformName());
        SocialLinkDTO created = socialLinkService.create(dto);
        logger.info("POST /api/social-links - Social link created successfully: id={}", created.id());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<SocialLinkDTO> update(@PathVariable Long id, @Valid @RequestBody SocialLinkUpdateDTO dto) {
        logger.info("PUT /api/social-links/{} - Updating social link", id);
        SocialLinkDTO updated = socialLinkService.update(id, dto);
        logger.info("PUT /api/social-links/{} - Social link updated successfully", id);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        logger.info("DELETE /api/social-links/{} - Deleting social link", id);
        socialLinkService.delete(id);
        logger.info("DELETE /api/social-links/{} - Social link deleted successfully", id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<SocialLinkDTO> getById(@PathVariable Long id) {
        SocialLinkDTO socialLink = socialLinkService.getById(id);
        return ResponseEntity.ok(socialLink);
    }

    @GetMapping
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<List<SocialLinkDTO>> getAll() {
        List<SocialLinkDTO> socialLinks = socialLinkService.getAll();
        return ResponseEntity.ok(socialLinks);
    }

    @GetMapping("/active")
    public ResponseEntity<List<SocialLinkDTO>> getAllActive() {
        List<SocialLinkDTO> socialLinks = socialLinkService.getAllActive();
        return ResponseEntity.ok(socialLinks);
    }
}
