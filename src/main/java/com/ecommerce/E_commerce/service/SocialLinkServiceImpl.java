package com.ecommerce.E_commerce.service;

import com.ecommerce.E_commerce.dto.sociallink.SocialLinkCreateDTO;
import com.ecommerce.E_commerce.dto.sociallink.SocialLinkDTO;
import com.ecommerce.E_commerce.dto.sociallink.SocialLinkUpdateDTO;
import com.ecommerce.E_commerce.exception.InvalidOperationException;
import com.ecommerce.E_commerce.exception.ResourceNotFoundException;
import com.ecommerce.E_commerce.mapper.SocialLinkMapper;
import com.ecommerce.E_commerce.model.SocialLink;
import com.ecommerce.E_commerce.repository.SocialLinkRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SocialLinkServiceImpl implements SocialLinkService {
    
    private static final Logger logger = LoggerFactory.getLogger(SocialLinkServiceImpl.class);
    private final SocialLinkRepository socialLinkRepository;
    private final SocialLinkMapper socialLinkMapper;

    @Override
    @Transactional
    @CacheEvict(value = {"social_links", "footer_data"}, allEntries = true)
    public SocialLinkDTO create(SocialLinkCreateDTO dto) {
        logger.info("Creating social link: platform={}, url={}", dto.platformName(), dto.url());
        if (socialLinkRepository.existsByPlatformName(dto.platformName())) {
            throw new InvalidOperationException("Social link with platform name already exists: " + dto.platformName());
        }
        SocialLink socialLink = socialLinkMapper.fromCreateDTO(dto);
        SocialLink saved = socialLinkRepository.save(socialLink);
        logger.info("Social link created successfully: id={}, platform={}", saved.getId(), saved.getPlatformName());
        return socialLinkMapper.toSocialLinkDTO(saved);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"social_links", "footer_data"}, allEntries = true)
    public SocialLinkDTO update(Long id, SocialLinkUpdateDTO dto) {
        logger.info("Updating social link: id={}", id);
        SocialLink socialLink = socialLinkRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Social link not found: " + id));
        
        if (dto.platformName() != null && !socialLink.getPlatformName().equals(dto.platformName())) {
            if (socialLinkRepository.existsByPlatformName(dto.platformName())) {
                throw new InvalidOperationException("Social link with platform name already exists: " + dto.platformName());
            }
        }
        
        socialLinkMapper.updateFromDTO(dto, socialLink);
        SocialLink saved = socialLinkRepository.save(socialLink);
        logger.info("Social link updated successfully: id={}", saved.getId());
        return socialLinkMapper.toSocialLinkDTO(saved);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"social_links", "footer_data"}, allEntries = true)
    public void delete(Long id) {
        logger.info("Deleting social link: id={}", id);
        SocialLink socialLink = socialLinkRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Social link not found: " + id));
        socialLinkRepository.delete(socialLink);
        logger.info("Social link deleted successfully: id={}", id);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "social_links", key = "#id")
    public SocialLinkDTO getById(Long id) {
        SocialLink socialLink = socialLinkRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Social link not found: " + id));
        return socialLinkMapper.toSocialLinkDTO(socialLink);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "social_links", key = "'all'")
    public List<SocialLinkDTO> getAll() {
        return socialLinkRepository.findAll().stream()
                .map(socialLinkMapper::toSocialLinkDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "social_links", key = "'active'")
    public List<SocialLinkDTO> getAllActive() {
        return socialLinkRepository.findAllByIsActiveTrueOrderBySortOrderAsc().stream()
                .map(socialLinkMapper::toSocialLinkDTO)
                .toList();
    }
}
