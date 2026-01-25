package com.ecommerce.E_commerce.controller;

import com.ecommerce.E_commerce.dto.shopsetting.FooterDataDTO;
import com.ecommerce.E_commerce.dto.shopsetting.ShopSettingCreateDTO;
import com.ecommerce.E_commerce.dto.shopsetting.ShopSettingDTO;
import com.ecommerce.E_commerce.dto.shopsetting.ShopSettingUpdateDTO;
import com.ecommerce.E_commerce.service.ShopLogoService;
import com.ecommerce.E_commerce.service.ShopSettingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/settings")
@RequiredArgsConstructor
public class SettingsController {
    
    private static final Logger logger = LoggerFactory.getLogger(SettingsController.class);
    private final ShopSettingService shopSettingService;
    private final ShopLogoService shopLogoService;

    @PostMapping
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ShopSettingDTO> create(@Valid @RequestBody ShopSettingCreateDTO dto) {
        logger.info("POST /api/settings - Creating shop setting: key={}", dto.key());
        ShopSettingDTO created = shopSettingService.create(dto);
        logger.info("POST /api/settings - Shop setting created successfully: id={}, key={}", created.id(), created.key());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ShopSettingDTO> update(@PathVariable Long id, @Valid @RequestBody ShopSettingUpdateDTO dto) {
        logger.info("PUT /api/settings/{} - Updating shop setting", id);
        ShopSettingDTO updated = shopSettingService.update(id, dto);
        logger.info("PUT /api/settings/{} - Shop setting updated successfully", id);
        return ResponseEntity.ok(updated);
    }

    @PutMapping("/key/{key}")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ShopSettingDTO> updateByKey(@PathVariable String key, @Valid @RequestBody ShopSettingUpdateDTO dto) {
        logger.info("PUT /api/settings/key/{} - Updating shop setting by key", key);
        ShopSettingDTO updated = shopSettingService.updateByKey(key, dto);
        logger.info("PUT /api/settings/key/{} - Shop setting updated successfully", key);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        logger.info("DELETE /api/settings/{} - Deleting shop setting", id);
        shopSettingService.delete(id);
        logger.info("DELETE /api/settings/{} - Shop setting deleted successfully", id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ShopSettingDTO> getById(@PathVariable Long id) {
        ShopSettingDTO setting = shopSettingService.getById(id);
        return ResponseEntity.ok(setting);
    }

    @GetMapping("/key/{key}")
    public ResponseEntity<ShopSettingDTO> getByKey(@PathVariable String key) {
        ShopSettingDTO setting = shopSettingService.getByKey(key);
        return ResponseEntity.ok(setting);
    }

    @GetMapping
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<List<ShopSettingDTO>> getAll() {
        List<ShopSettingDTO> settings = shopSettingService.getAll();
        return ResponseEntity.ok(settings);
    }

    @GetMapping("/map")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<Map<String, String>> getAllAsMap() {
        Map<String, String> settings = shopSettingService.getAllAsMap();
        return ResponseEntity.ok(settings);
    }

    @GetMapping("/public/footer")
    public ResponseEntity<FooterDataDTO> getFooterData() {
        FooterDataDTO footerData = shopSettingService.getFooterData();
        return ResponseEntity.ok(footerData);
    }

    @PostMapping("/logo")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<Map<String, String>> uploadLogo(
            @RequestPart("file") MultipartFile file) {
        logger.info("POST /api/settings/logo - Uploading shop logo: fileName={}", file.getOriginalFilename());
        String logoUrl = shopLogoService.uploadLogo(file);
        logger.info("POST /api/settings/logo - Logo uploaded successfully: url={}", logoUrl);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("logoUrl", logoUrl));
    }

    @DeleteMapping("/logo")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<Void> deleteLogo() {
        logger.info("DELETE /api/settings/logo - Deleting shop logo");
        shopLogoService.deleteLogo();
        logger.info("DELETE /api/settings/logo - Logo deleted successfully");
        return ResponseEntity.noContent().build();
    }
}
