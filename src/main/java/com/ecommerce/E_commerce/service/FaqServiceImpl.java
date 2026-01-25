package com.ecommerce.E_commerce.service;

import com.ecommerce.E_commerce.dto.faq.FaqItemCreateDTO;
import com.ecommerce.E_commerce.dto.faq.FaqItemDTO;
import com.ecommerce.E_commerce.dto.faq.FaqItemUpdateDTO;
import com.ecommerce.E_commerce.exception.InvalidOperationException;
import com.ecommerce.E_commerce.exception.ResourceNotFoundException;
import com.ecommerce.E_commerce.mapper.FaqItemMapper;
import com.ecommerce.E_commerce.model.FaqItem;
import com.ecommerce.E_commerce.repository.FaqItemRepository;
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
public class FaqServiceImpl implements FaqService {
    
    private static final Logger logger = LoggerFactory.getLogger(FaqServiceImpl.class);
    private final FaqItemRepository faqItemRepository;
    private final FaqItemMapper faqItemMapper;

    @Override
    @Transactional
    @CacheEvict(value = "faqItems", allEntries = true)
    public FaqItemDTO create(FaqItemCreateDTO dto) {
        logger.info("Creating FAQ item: question={}", dto.question());
        if (faqItemRepository.existsByQuestion(dto.question())) {
            throw new InvalidOperationException("FAQ item with question already exists: " + dto.question());
        }
        FaqItem faqItem = faqItemMapper.fromCreateDTO(dto);
        FaqItem saved = faqItemRepository.save(faqItem);
        logger.info("FAQ item created successfully: id={}", saved.getId());
        return faqItemMapper.toFaqItemDTO(saved);
    }

    @Override
    @Transactional
    @CacheEvict(value = "faqItems", allEntries = true)
    public FaqItemDTO update(Long id, FaqItemUpdateDTO dto) {
        logger.info("Updating FAQ item: id={}", id);
        FaqItem faqItem = faqItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("FAQ item not found: " + id));
        
        // Check if question is being changed and if it conflicts with existing record
        if (dto.question() != null && !faqItem.getQuestion().equals(dto.question())) {
            if (faqItemRepository.existsByQuestion(dto.question())) {
                throw new InvalidOperationException("FAQ item with question already exists: " + dto.question());
            }
        }
        
        faqItemMapper.updateFromDTO(dto, faqItem);
        FaqItem saved = faqItemRepository.save(faqItem);
        logger.info("FAQ item updated successfully: id={}", saved.getId());
        return faqItemMapper.toFaqItemDTO(saved);
    }

    @Override
    @Transactional
    @CacheEvict(value = "faq_items", allEntries = true)
    public void delete(Long id) {
        logger.info("Deleting FAQ item: id={}", id);
        FaqItem faqItem = faqItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("FAQ item not found: " + id));
        faqItemRepository.delete(faqItem);
        logger.info("FAQ item deleted successfully: id={}", id);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "faq_items", key = "#id")
    public FaqItemDTO getById(Long id) {
        FaqItem faqItem = faqItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("FAQ item not found: " + id));
        return faqItemMapper.toFaqItemDTO(faqItem);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "faq_items", key = "'all'")
    public List<FaqItemDTO> getAll() {
        return faqItemRepository.findAll().stream()
                .map(faqItemMapper::toFaqItemDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "faq_items", key = "'active'")
    public List<FaqItemDTO> getAllActive() {
        return faqItemRepository.findAllByIsActiveTrueOrderBySortOrderAsc().stream()
                .map(faqItemMapper::toFaqItemDTO)
                .toList();
    }
}
