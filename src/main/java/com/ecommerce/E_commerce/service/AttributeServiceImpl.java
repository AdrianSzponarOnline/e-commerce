package com.ecommerce.E_commerce.service;

import com.ecommerce.E_commerce.dto.attribute.AttributeCreateDTO;
import com.ecommerce.E_commerce.dto.attribute.AttributeDTO;
import com.ecommerce.E_commerce.dto.attribute.AttributeUpdateDTO;
import com.ecommerce.E_commerce.exception.DuplicateResourceException;
import com.ecommerce.E_commerce.exception.ResourceNotFoundException;
import com.ecommerce.E_commerce.mapper.AttributeMapper;
import com.ecommerce.E_commerce.model.Attribute;
import com.ecommerce.E_commerce.model.CategoryAttributeType;
import com.ecommerce.E_commerce.repository.AttributeRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AttributeServiceImpl implements AttributeService {

    private static final Logger logger = LoggerFactory.getLogger(AttributeServiceImpl.class);
    private final AttributeRepository attributeRepository;
    private final AttributeMapper attributeMapper;

    @Autowired
    public AttributeServiceImpl(AttributeRepository attributeRepository, AttributeMapper attributeMapper) {
        this.attributeRepository = attributeRepository;
        this.attributeMapper = attributeMapper;
    }

    public Attribute findOrCreateAttribute(AttributeCreateDTO dto) {
        return attributeRepository.findByNameAndType(dto.name(), dto.type())
                .orElseGet(() -> {
                    Attribute newAttribute = attributeMapper.toEntity(dto);
                    return attributeRepository.save(newAttribute);
                });
    }

    @Override
    @CacheEvict(value = "attributes", allEntries = true)
    public AttributeDTO createAttribute(AttributeCreateDTO dto) {
        logger.info("Creating attribute: name={}, type={}", dto.name(), dto.type());
        validateUniqueness(dto.name(), dto.type());
        Attribute newAttribute = attributeMapper.toEntity(dto);
        Attribute savedAttribute = attributeRepository.save(newAttribute);
        logger.info("Attribute created successfully: id={}, name={}", savedAttribute.getId(), savedAttribute.getName());
        return attributeMapper.toDTO(savedAttribute);
    }


    @Override
    @CacheEvict(value = "attributes", allEntries = true)
    public AttributeDTO update(Long id, AttributeUpdateDTO dto) {
        Attribute existingAttribute = attributeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Attribute not found with id: " + id));

        if(dto.name() != null || dto.type() != null) {
            String newName = (dto.name() != null) ? dto.name() : existingAttribute.getName();
            CategoryAttributeType newType = (dto.type() != null) ? dto.type() : existingAttribute.getType();

            validateUniqueness(newName, newType, id);
        }
        attributeMapper.updateEntityFromDTO(dto, existingAttribute);
        Attribute updatedAttribute = attributeRepository.save(existingAttribute);
        return attributeMapper.toDTO(updatedAttribute);
    }
    @Override
    @Transactional
    @CacheEvict(value = "attributes", allEntries = true)
    public void deleteAttribute(Long id) {
        Attribute attributeToDelete = attributeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Attribute not found with id: " + id));
        attributeRepository.delete(attributeToDelete);
    }

    @Override
    @Transactional
    @CacheEvict(value = "attributes", allEntries = true)
    public void restoreAttribute(Long id) {
        Attribute attributeToRestore = attributeRepository.findDeletedById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Deleted attribute not found with id: " + id));

        attributeToRestore.setActive(true);
        attributeToRestore.setDeletedAt(null);

        attributeRepository.save(attributeToRestore);
    }

    @Override
    @Cacheable(value = "attributes", key = "'names'")
    public List<String> getAllAttributeNames() {
        return attributeRepository.findAllActiveAttributeNames();
    }

    @Override
    @Cacheable(value = "attributes", key = "#id")
    public AttributeDTO getAttributeById(Long id) {
        Attribute attribute = attributeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Attribute not found with id: " + id));
        return attributeMapper.toDTO(attribute);
    }

    @Override
    @Cacheable(value = "attributes",key = "'active_page_' + #pageable.pageNumber")
    public Page<AttributeDTO> getActiveAttributes(Pageable pageable) {
        Page<Attribute> attributePage = attributeRepository.findByIsActiveTrue(pageable);
        return attributePage.map(attributeMapper::toDTO);
    }

    @Override
    public Page<AttributeDTO> getInactiveAttributes(Pageable pageable) {
        Page<Attribute> attributePage = attributeRepository.findAllDeleted(pageable);
        return attributePage.map(attributeMapper::toDTO);
    }

    private void validateUniqueness(String name, CategoryAttributeType type) {
        attributeRepository.findByNameAndType(name, type)
                .ifPresent(attr -> {
                    throw new DuplicateResourceException("Attribute with name '" + name + "' and type '" + type + "' already exists.");
                });
    }

    private void validateUniqueness(String name, CategoryAttributeType type, Long currentId) {
        Optional<Attribute> conflictingAttribute = attributeRepository.findByNameAndType(name, type);

        conflictingAttribute.ifPresent(attr -> {
            if (!attr.getId().equals(currentId)) {
                throw new DuplicateResourceException("Attribute with name '" + name + "' and type '" + type + "' already exists.");
            }
        });
    }
}
