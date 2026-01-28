package com.ecommerce.E_commerce.service;

import com.ecommerce.E_commerce.dto.shopsetting.ShopSettingCreateDTO;
import com.ecommerce.E_commerce.dto.shopsetting.ShopSettingUpdateDTO;
import com.ecommerce.E_commerce.exception.InvalidOperationException;
import com.ecommerce.E_commerce.repository.ShopSettingRepository;
import lombok.RequiredArgsConstructor;
import org.apache.tika.Tika;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@RequiredArgsConstructor
public class ShopLogoServiceImpl implements ShopLogoService {
    
    private static final Logger logger = LoggerFactory.getLogger(ShopLogoServiceImpl.class);
    private static final String LOGO_SETTING_KEY = "logo_url";
    
    private final ShopSettingRepository shopSettingRepository;
    private final ShopSettingService shopSettingService;
    private final Tika tika = new Tika();
    
    @Value("${app.upload-dir:uploads}")
    private String uploadDir;
    
    @Value("${app.upload-max-bytes:5242880}")
    private long maxUploadBytes;
    
    @Value("${app.upload-allowed-types:image/jpeg,image/png,image/webp,image/svg+xml}")
    private String allowedTypesCsv;
    
    private Path uploadRoot;
    
    @javax.annotation.PostConstruct
    public void init() throws IOException {
        this.uploadRoot = Paths.get(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(this.uploadRoot.resolve("shop"));
    }
    
    @Override
    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "shop_settings", allEntries = true),
        @CacheEvict(value = "footer_data", allEntries = true)
    })
    public String uploadLogo(MultipartFile file) {
        logger.info("Uploading shop logo: fileName={}, size={}", file.getOriginalFilename(), file.getSize());
        
        String extension = validateAndAnalyzeFile(file);
        
        
        deleteOldLogoFile();
        
     
        String filename = "logo." + extension;
        Path shopDir = uploadRoot.resolve("shop");
        Path target = shopDir.resolve(filename);
        
        try {
            Files.createDirectories(shopDir);
            file.transferTo(target);
            logger.info("Logo file stored successfully: {}", target);
        } catch (IOException e) {
            logger.error("Failed to store logo file", e);
            throw new RuntimeException("Failed to store logo file on disk", e);
        }
        
    
        String logoUrl = "/uploads/shop/" + filename;
        updateLogoUrlSetting(logoUrl);
        
        logger.info("Shop logo uploaded successfully: url={}", logoUrl);
        return logoUrl;
    }
    
    @Override
    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "shop_settings", allEntries = true),
        @CacheEvict(value = "footer_data", allEntries = true)
    })
    public void deleteLogo() {
        logger.info("Deleting shop logo");
        
        String currentLogoUrl = shopSettingRepository.findByKey(LOGO_SETTING_KEY)
                .map(setting -> setting.getValue())
                .orElse(null);
        
        if (currentLogoUrl != null && !currentLogoUrl.isEmpty()) {
            try {
                String filename = currentLogoUrl.substring(currentLogoUrl.lastIndexOf('/') + 1);
                Path logoFile = uploadRoot.resolve("shop").resolve(filename);
                
                if (Files.exists(logoFile)) {
                    Files.delete(logoFile);
                    logger.info("Logo file deleted from disk: {}", logoFile);
                }
            } catch (IOException e) {
                logger.warn("Failed to delete logo file from disk", e);
            }
        }
        
        shopSettingRepository.findByKey(LOGO_SETTING_KEY)
                .ifPresent(setting -> {
                    ShopSettingUpdateDTO updateDTO = new ShopSettingUpdateDTO("", null);
                    shopSettingService.updateByKey(LOGO_SETTING_KEY, updateDTO);
                });
        
        logger.info("Shop logo deleted successfully");
    }
    
    private String validateAndAnalyzeFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidOperationException("File is required");
        }
        
        if (file.getSize() > maxUploadBytes) {
            throw new InvalidOperationException("File too big (limit " + maxUploadBytes + " bytes)");
        }
        
        String detectedType;
        try {
            detectedType = tika.detect(file.getInputStream());
        } catch (IOException e) {
            throw new InvalidOperationException("Failed to analyze file content");
        }
        
        if (!isAllowedContentType(detectedType)) {
            throw new InvalidOperationException("Invalid file content type. Detected: " + detectedType + 
                    ". Allowed types: " + allowedTypesCsv);
        }
        
        String extension = getExtensionFromMimeType(detectedType);
        if (extension == null) {
            throw new InvalidOperationException("Could not determine extension for MIME type: " + detectedType);
        }
        
        return extension;
    }
    
    private boolean isAllowedContentType(String mimeType) {
        if (mimeType == null) {
            return false;
        }
        String[] allowed = allowedTypesCsv.split(",");
        for (String allowedType : allowed) {
            if (mimeType.trim().equalsIgnoreCase(allowedType.trim())) {
                return true;
            }
        }
        return false;
    }
    
    private String getExtensionFromMimeType(String mimeType) {
        if (mimeType == null) {
            return null;
        }
        return switch (mimeType.toLowerCase()) {
            case "image/jpeg" -> "jpg";
            case "image/png" -> "png";
            case "image/webp" -> "webp";
            case "image/svg+xml" -> "svg";
            default -> null;
        };
    }
    
    private void deleteOldLogoFile() {
        shopSettingRepository.findByKey(LOGO_SETTING_KEY)
                .map(setting -> setting.getValue())
                .filter(url -> url != null && !url.isEmpty())
                .ifPresent(oldLogoUrl -> {
                    try {
                        String filename = oldLogoUrl.substring(oldLogoUrl.lastIndexOf('/') + 1);
                        Path oldLogoFile = uploadRoot.resolve("shop").resolve(filename);
                        if (Files.exists(oldLogoFile)) {
                            Files.delete(oldLogoFile);
                            logger.info("Old logo file deleted: {}", oldLogoFile);
                        }
                    } catch (IOException e) {
                        logger.warn("Failed to delete old logo file", e);
                        // Continue with upload even if old file deletion fails
                    }
                });
    }
    
    private void updateLogoUrlSetting(String logoUrl) {
        if (shopSettingRepository.existsByKey(LOGO_SETTING_KEY)) {
            // Update existing setting
            ShopSettingUpdateDTO updateDTO = new ShopSettingUpdateDTO(logoUrl, null);
            shopSettingService.updateByKey(LOGO_SETTING_KEY, updateDTO);
        } else {
            // Create new setting (should not happen if migration ran, but handle gracefully)
            logger.warn("logo_url setting not found, creating it");
            ShopSettingCreateDTO createDTO = new ShopSettingCreateDTO(
                    LOGO_SETTING_KEY, 
                    logoUrl, 
                    "Shop logo URL");
            shopSettingService.create(createDTO);
        }
    }
}
