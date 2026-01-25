package com.ecommerce.E_commerce.controller;

import com.ecommerce.E_commerce.dto.faq.FaqItemCreateDTO;
import com.ecommerce.E_commerce.dto.faq.FaqItemDTO;
import com.ecommerce.E_commerce.dto.faq.FaqItemUpdateDTO;
import com.ecommerce.E_commerce.service.FaqService;
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
@RequestMapping("/api/faq")
@RequiredArgsConstructor
public class FaqController {
    
    private static final Logger logger = LoggerFactory.getLogger(FaqController.class);
    private final FaqService faqService;

    @PostMapping
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<FaqItemDTO> create(@Valid @RequestBody FaqItemCreateDTO dto) {
        logger.info("POST /api/faq - Creating FAQ item: question={}", dto.question());
        FaqItemDTO created = faqService.create(dto);
        logger.info("POST /api/faq - FAQ item created successfully: id={}", created.id());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<FaqItemDTO> update(@PathVariable Long id, @Valid @RequestBody FaqItemUpdateDTO dto) {
        logger.info("PUT /api/faq/{} - Updating FAQ item", id);
        FaqItemDTO updated = faqService.update(id, dto);
        logger.info("PUT /api/faq/{} - FAQ item updated successfully", id);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        logger.info("DELETE /api/faq/{} - Deleting FAQ item", id);
        faqService.delete(id);
        logger.info("DELETE /api/faq/{} - FAQ item deleted successfully", id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<FaqItemDTO> getById(@PathVariable Long id) {
        FaqItemDTO faqItem = faqService.getById(id);
        return ResponseEntity.ok(faqItem);
    }

    @GetMapping
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<List<FaqItemDTO>> getAll() {
        List<FaqItemDTO> faqItems = faqService.getAll();
        return ResponseEntity.ok(faqItems);
    }

    @GetMapping("/active")
    public ResponseEntity<List<FaqItemDTO>> getAllActive() {
        List<FaqItemDTO> faqItems = faqService.getAllActive();
        return ResponseEntity.ok(faqItems);
    }
}
