package com.ecommerce.E_commerce.service;

import com.ecommerce.E_commerce.dto.page.PageCreateDTO;
import com.ecommerce.E_commerce.dto.page.PageDTO;
import com.ecommerce.E_commerce.dto.page.PageUpdateDTO;
import com.ecommerce.E_commerce.exception.InvalidOperationException;
import com.ecommerce.E_commerce.exception.ResourceNotFoundException;
import com.ecommerce.E_commerce.mapper.PageMapper;
import com.ecommerce.E_commerce.model.WebsitePage;
import com.ecommerce.E_commerce.repository.PageRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PageServiceImpl implements PageService {
    
    private static final Logger logger = LoggerFactory.getLogger(PageServiceImpl.class);
    private final PageRepository pageRepository;
    private final PageMapper pageMapper;

    @Override
    @Transactional
    @CacheEvict(value = "pages", allEntries = true)
    public PageDTO create(PageCreateDTO dto) {
        logger.info("Creating page: slug={}, title={}", dto.slug(), dto.title());
        if (pageRepository.existsBySlug(dto.slug())) {
            throw new InvalidOperationException("Page with slug already exists: " + dto.slug());
        }
        WebsitePage websitePage = pageMapper.fromCreateDTO(dto);
        if (dto.isSystem() == null) {
            websitePage.setIsSystem(false);
        }
        WebsitePage saved = pageRepository.save(websitePage);
        logger.info("Page created successfully: id={}, slug={}", saved.getId(), saved.getSlug());
        return pageMapper.toPageDTO(saved);
    }

    @Override
    @Transactional
    @CacheEvict(value = "pages", allEntries = true)
    public PageDTO update(Long id, PageUpdateDTO dto) {
        logger.info("Updating page: id={}", id);
        WebsitePage websitePage = pageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Page not found: " + id));
        
        if (dto.slug() != null && !websitePage.getSlug().equals(dto.slug())) {
            if (pageRepository.existsBySlug(dto.slug())) {
                throw new InvalidOperationException("Page with slug already exists: " + dto.slug());
            }
        }
        
        pageMapper.updateFromDTO(dto, websitePage);
        WebsitePage saved = pageRepository.save(websitePage);
        logger.info("Page updated successfully: id={}", saved.getId());
        return pageMapper.toPageDTO(saved);
    }

    @Override
    @Transactional
    @CacheEvict(value = "pages", allEntries = true)
    public void delete(Long id) {
        logger.info("Deleting page: id={}", id);
        WebsitePage websitePage = pageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Page not found: " + id));
        
        if (Boolean.TRUE.equals(websitePage.getIsSystem())) {
            throw new InvalidOperationException("Cannot delete system page: " + id);
        }
        
        pageRepository.delete(websitePage);
        logger.info("Page deleted successfully: id={}", id);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "pages", key = "#id")
    public PageDTO getById(Long id) {
        WebsitePage websitePage = pageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Page not found: " + id));
        return pageMapper.toPageDTO(websitePage);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "pages", key = "'slug_' + #slug")
    public PageDTO getBySlug(String slug) {
        WebsitePage websitePage = pageRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Page not found by slug: " + slug));
        return pageMapper.toPageDTO(websitePage);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "pages", key = "'all'")
    public List<PageDTO> getAll() {
        return pageRepository.findAll().stream()
                .map(pageMapper::toPageDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "pages", key = "'active'")
    public List<PageDTO> getAllActive() {
        return pageRepository.findAllByIsActiveTrueOrderByTitleAsc().stream()
                .map(pageMapper::toPageDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "pages", key = "'search_slug_' + #slug + '_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public Page<PageDTO> searchBySlug(String slug, Pageable pageable) {
        logger.info("Searching pages by slug: slug={}, page={}, size={}", slug, pageable.getPageNumber(), pageable.getPageSize());
        return pageRepository.findBySlugContainingIgnoreCase(slug, pageable)
                .map(pageMapper::toPageDTO);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "pages", key = "'search_title_' + #title + '_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public Page<PageDTO> searchByTitle(String title, Pageable pageable) {
        logger.info("Searching pages by title: title={}, page={}, size={}", title, pageable.getPageNumber(), pageable.getPageSize());
        return pageRepository.findByTitleContainingIgnoreCase(title, pageable)
                .map(pageMapper::toPageDTO);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "pages", key = "'search_' + #slug + '_' + #title + '_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public Page<PageDTO> search(String slug, String title, Pageable pageable) {
        logger.info("Searching pages: slug={}, title={}, page={}, size={}", slug, title, pageable.getPageNumber(), pageable.getPageSize());
        return pageRepository.findBySlugContainingIgnoreCaseOrTitleContainingIgnoreCase(
                slug != null ? slug : "", 
                title != null ? title : "", 
                pageable)
                .map(pageMapper::toPageDTO);
    }
}
