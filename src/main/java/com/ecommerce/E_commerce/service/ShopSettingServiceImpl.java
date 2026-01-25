package com.ecommerce.E_commerce.service;

import com.ecommerce.E_commerce.dto.shopsetting.FooterDataDTO;
import com.ecommerce.E_commerce.dto.shopsetting.ShopSettingCreateDTO;
import com.ecommerce.E_commerce.dto.shopsetting.ShopSettingDTO;
import com.ecommerce.E_commerce.dto.shopsetting.ShopSettingUpdateDTO;
import com.ecommerce.E_commerce.dto.sociallink.SocialLinkDTO;
import com.ecommerce.E_commerce.exception.InvalidOperationException;
import com.ecommerce.E_commerce.exception.ResourceNotFoundException;
import com.ecommerce.E_commerce.mapper.ShopSettingMapper;
import com.ecommerce.E_commerce.mapper.SocialLinkMapper;
import com.ecommerce.E_commerce.model.ShopSetting;
import com.ecommerce.E_commerce.repository.ShopSettingRepository;
import com.ecommerce.E_commerce.repository.SocialLinkRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShopSettingServiceImpl implements ShopSettingService {
    
    private static final Logger logger = LoggerFactory.getLogger(ShopSettingServiceImpl.class);
    private final ShopSettingRepository shopSettingRepository;
    private final SocialLinkRepository socialLinkRepository;
    private final ShopSettingMapper shopSettingMapper;
    private final SocialLinkMapper socialLinkMapper;

    @Override
    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "shop_settings", allEntries = true),
        @CacheEvict(value = "footer_data", allEntries = true)
    })
    public ShopSettingDTO create(ShopSettingCreateDTO dto) {
        logger.info("Creating shop setting: key={}", dto.key());
        if (shopSettingRepository.existsByKey(dto.key())) {
            throw new InvalidOperationException("Shop setting with key already exists: " + dto.key());
        }
        ShopSetting setting = shopSettingMapper.fromCreateDTO(dto);
        ShopSetting saved = shopSettingRepository.save(setting);
        logger.info("Shop setting created successfully: id={}, key={}", saved.getId(), saved.getKey());
        return shopSettingMapper.toShopSettingDTO(saved);
    }

    @Override
    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "shop_settings", allEntries = true),
        @CacheEvict(value = "footer_data", allEntries = true)
    })
    public ShopSettingDTO update(Long id, ShopSettingUpdateDTO dto) {
        logger.info("Updating shop setting: id={}", id);
        ShopSetting setting = shopSettingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shop setting not found: " + id));
        shopSettingMapper.updateFromDTO(dto, setting);
        ShopSetting saved = shopSettingRepository.save(setting);
        logger.info("Shop setting updated successfully: id={}", saved.getId());
        return shopSettingMapper.toShopSettingDTO(saved);
    }

    @Override
    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "shop_settings", allEntries = true),
        @CacheEvict(value = "footer_data", allEntries = true)
    })
    public ShopSettingDTO updateByKey(String key, ShopSettingUpdateDTO dto) {
        logger.info("Updating shop setting by key: key={}", key);
        ShopSetting setting = shopSettingRepository.findByKey(key)
                .orElseThrow(() -> new ResourceNotFoundException("Shop setting not found with key: " + key));
        shopSettingMapper.updateFromDTO(dto, setting);
        ShopSetting saved = shopSettingRepository.save(setting);
        logger.info("Shop setting updated successfully: key={}", key);
        return shopSettingMapper.toShopSettingDTO(saved);
    }

    @Override
    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "shop_settings", allEntries = true),
        @CacheEvict(value = "footer_data", allEntries = true)
    })
    public void delete(Long id) {
        logger.info("Deleting shop setting: id={}", id);
        if (!shopSettingRepository.existsById(id)) {
            throw new ResourceNotFoundException("Shop setting not found: " + id);
        }
        shopSettingRepository.deleteById(id);
        logger.info("Shop setting deleted successfully: id={}", id);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "shop_settings", key = "#id")
    public ShopSettingDTO getById(Long id) {
        ShopSetting setting = shopSettingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shop setting not found: " + id));
        return shopSettingMapper.toShopSettingDTO(setting);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "shop_settings", key = "'key_' + #key")
    public ShopSettingDTO getByKey(String key) {
        ShopSetting setting = shopSettingRepository.findByKey(key)
                .orElseThrow(() -> new ResourceNotFoundException("Shop setting not found with key: " + key));
        return shopSettingMapper.toShopSettingDTO(setting);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "shop_settings", key = "'all'")
    public List<ShopSettingDTO> getAll() {
        return shopSettingRepository.findAll().stream()
                .map(shopSettingMapper::toShopSettingDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "shop_settings", key = "'map'")
    public Map<String, String> getAllAsMap() {
        return shopSettingRepository.findAll().stream()
                .collect(Collectors.toMap(
                        ShopSetting::getKey,
                        s -> s.getValue() != null ? s.getValue() : ""
                ));
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "footer_data", key = "'footer'")
    public FooterDataDTO getFooterData() {
        Map<String, String> settings = getAllAsMap();
        List<SocialLinkDTO> socialLinks = socialLinkRepository.findAllByIsActiveTrueOrderBySortOrderAsc().stream()
                .map(socialLinkMapper::toSocialLinkDTO)
                .toList();
        
        return new FooterDataDTO(
                settings.getOrDefault("shop_name", "E-Shop"),
                settings.getOrDefault("logo_url", ""),
                settings.getOrDefault("footer_copyright", ""),
                new FooterDataDTO.ContactInfoDTO(
                        settings.getOrDefault("contact_phone", ""),
                        settings.getOrDefault("contact_email", ""),
                        settings.getOrDefault("contact_address", ""),
                        settings.getOrDefault("opening_hours", "")
                ),
                socialLinks
        );
    }
}
