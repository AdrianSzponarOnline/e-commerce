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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class AttributeServiceImpl implements AttributeService {

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
    public AttributeDTO createAttribute(AttributeCreateDTO dto) {
        validateUniqueness(dto.name(), dto.type());
        Attribute newAttribute = attributeMapper.toEntity(dto);
        Attribute savedAttribute = attributeRepository.save(newAttribute);
        return attributeMapper.toDTO(savedAttribute);
    }


    @Override
    public AttributeDTO update(Long id, AttributeUpdateDTO dto) {
        Attribute existingAttribute = findActiveAttributeByIdInternal(id);

        if(dto.name() != null || dto.type() != null) {
            String newName = (dto.name() != null) ? dto.name() : existingAttribute.getName();
            CategoryAttributeType newType = (dto.type() != null) ? dto.type() : existingAttribute.getType();

            validateUniqueness(newName, newType);
        }
        attributeMapper.updateEntityFromDTO(dto, existingAttribute);
        Attribute updatedAttribute = attributeRepository.save(existingAttribute);
        return attributeMapper.toDTO(updatedAttribute);
    }
    @Override
    @Transactional
    public void deleteAttribute(Long id) {
        Attribute attributeToDelete = findActiveAttributeByIdInternal(id);
        attributeToDelete.setActive(false);
        attributeToDelete.setDeletedAt(Instant.now());
        attributeRepository.save(attributeToDelete);
    }

    @Override
    @Transactional
    public void restoreAttribute(Long id) {
        Attribute attributeToRestore = attributeRepository.findByIdAndIsActiveFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inactive attribute not found with id: " + id));

        attributeToRestore.setActive(true);
        attributeToRestore.setDeletedAt(null);

        attributeRepository.save(attributeToRestore);
    }

    @Override
    public AttributeDTO getAttributeById(Long id) {
        Attribute attribute = findActiveAttributeByIdInternal(id);
        return attributeMapper.toDTO(attribute);
    }

    @Override
    public Page<AttributeDTO> getActiveAttributes(Pageable pageable) {
        Page<Attribute> attributePage = attributeRepository.findByIsActiveTrue(pageable);
        return attributePage.map(attributeMapper::toDTO);
    }

    @Override
    public Page<AttributeDTO> getInactiveAttributes(Pageable pageable) {
        Page<Attribute> attributePage = attributeRepository.findByIsActiveFalse(pageable);
        return attributePage.map(attributeMapper::toDTO);
    }

    private void validateUniqueness(String name, CategoryAttributeType type) {
        attributeRepository.findByNameAndType(name, type)
                .ifPresent(attr -> {
                    throw new DuplicateResourceException("Attribute with name '" + name + "' and type '" + type + "' already exists.");
                });
    }
    private Attribute findActiveAttributeByIdInternal(Long id) {
        return attributeRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Attribute not found with id: " + id));
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
