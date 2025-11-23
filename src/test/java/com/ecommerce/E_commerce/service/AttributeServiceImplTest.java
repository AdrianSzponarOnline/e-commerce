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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AttributeServiceImplTest {

    @Mock
    private AttributeRepository attributeRepository;

    @Mock
    private AttributeMapper attributeMapper;

    private AttributeServiceImpl attributeService;

    private Attribute testAttribute;

    @BeforeEach
    void setUp() {
        attributeService = new AttributeServiceImpl(attributeRepository, attributeMapper);

        testAttribute = new Attribute();
        testAttribute.setId(1L);
        testAttribute.setName("Color");
        testAttribute.setType(CategoryAttributeType.TEXT);
        testAttribute.setActive(true);
    }

    @Test
    void createAttribute_ShouldCreateAttributeSuccessfully() {
        // Given
        AttributeCreateDTO createDTO = new AttributeCreateDTO("Color", CategoryAttributeType.TEXT);
        AttributeDTO attributeDTO = new AttributeDTO(1L, "Color", CategoryAttributeType.TEXT, Instant.now(), Instant.now());

        when(attributeRepository.findByNameAndType("Color", CategoryAttributeType.TEXT)).thenReturn(Optional.empty());
        when(attributeMapper.toEntity(createDTO)).thenReturn(testAttribute);
        when(attributeRepository.save(any(Attribute.class))).thenReturn(testAttribute);
        when(attributeMapper.toDTO(testAttribute)).thenReturn(attributeDTO);

        // When
        AttributeDTO result = attributeService.createAttribute(createDTO);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.id());
        verify(attributeRepository).findByNameAndType("Color", CategoryAttributeType.TEXT);
        verify(attributeRepository).save(any(Attribute.class));
    }

    @Test
    void createAttribute_ShouldThrowException_WhenAttributeAlreadyExists() {
        // Given
        AttributeCreateDTO createDTO = new AttributeCreateDTO("Color", CategoryAttributeType.TEXT);
        when(attributeRepository.findByNameAndType("Color", CategoryAttributeType.TEXT))
                .thenReturn(Optional.of(testAttribute));

        // When & Then
        DuplicateResourceException exception = assertThrows(DuplicateResourceException.class,
                () -> attributeService.createAttribute(createDTO));
        assertTrue(exception.getMessage().contains("already exists"));
        verify(attributeRepository, never()).save(any());
    }

    @Test
    void update_ShouldUpdateAttributeSuccessfully() {
        // Given
        AttributeUpdateDTO updateDTO = new AttributeUpdateDTO("Size", null);
        AttributeDTO attributeDTO = new AttributeDTO(1L, "Size", CategoryAttributeType.TEXT, Instant.now(), Instant.now());

        when(attributeRepository.findById(1L)).thenReturn(Optional.of(testAttribute));
        when(attributeRepository.findByNameAndType("Size", CategoryAttributeType.TEXT)).thenReturn(Optional.empty());
        when(attributeRepository.save(any(Attribute.class))).thenReturn(testAttribute);
        when(attributeMapper.toDTO(testAttribute)).thenReturn(attributeDTO);

        // When
        AttributeDTO result = attributeService.update(1L, updateDTO);

        // Then
        assertNotNull(result);
        verify(attributeRepository).findById(1L);
        verify(attributeMapper).updateEntityFromDTO(updateDTO, testAttribute);
        verify(attributeRepository).save(testAttribute);
    }

    @Test
    void update_ShouldThrowException_WhenAttributeNotFound() {
        // Given
        AttributeUpdateDTO updateDTO = new AttributeUpdateDTO("Size", null);
        when(attributeRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> attributeService.update(1L, updateDTO));
        assertEquals("Attribute not found with id: 1", exception.getMessage());
    }

    @Test
    void getAttributeById_ShouldReturnAttribute_WhenAttributeExists() {
        // Given
        AttributeDTO attributeDTO = new AttributeDTO(1L, "Color", CategoryAttributeType.TEXT, Instant.now(), Instant.now());
        when(attributeRepository.findById(1L)).thenReturn(Optional.of(testAttribute));
        when(attributeMapper.toDTO(testAttribute)).thenReturn(attributeDTO);

        // When
        AttributeDTO result = attributeService.getAttributeById(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.id());
        verify(attributeRepository).findById(1L);
    }

    @Test
    void getAttributeById_ShouldThrowException_WhenAttributeNotFound() {
        // Given
        when(attributeRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> attributeService.getAttributeById(1L));
        assertEquals("Attribute not found with id: 1", exception.getMessage());
    }

    @Test
    void getActiveAttributes_ShouldReturnPageOfAttributes() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Attribute> attributePage = new PageImpl<>(List.of(testAttribute));
        AttributeDTO attributeDTO = new AttributeDTO(1L, "Color", CategoryAttributeType.TEXT, Instant.now(), Instant.now());

        when(attributeRepository.findByIsActiveTrue(pageable)).thenReturn(attributePage);
        when(attributeMapper.toDTO(testAttribute)).thenReturn(attributeDTO);

        // When
        Page<AttributeDTO> result = attributeService.getActiveAttributes(pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(attributeRepository).findByIsActiveTrue(pageable);
    }

    @Test
    void deleteAttribute_ShouldDeleteAttribute() {
        // Given
        when(attributeRepository.findById(1L)).thenReturn(Optional.of(testAttribute));

        // When
        attributeService.deleteAttribute(1L);

        // Then
        verify(attributeRepository).findById(1L);
        verify(attributeRepository).delete(testAttribute);
    }

    @Test
    void findOrCreateAttribute_ShouldReturnExistingAttribute() {
        // Given
        AttributeCreateDTO createDTO = new AttributeCreateDTO("Color", CategoryAttributeType.TEXT);
        when(attributeRepository.findByNameAndType("Color", CategoryAttributeType.TEXT))
                .thenReturn(Optional.of(testAttribute));

        // When
        Attribute result = attributeService.findOrCreateAttribute(createDTO);

        // Then
        assertNotNull(result);
        assertEquals(testAttribute, result);
        verify(attributeRepository, never()).save(any());
    }

    @Test
    void findOrCreateAttribute_ShouldCreateNewAttribute_WhenNotFound() {
        // Given
        AttributeCreateDTO createDTO = new AttributeCreateDTO("Color", CategoryAttributeType.TEXT);
        when(attributeRepository.findByNameAndType("Color", CategoryAttributeType.TEXT))
                .thenReturn(Optional.empty());
        when(attributeMapper.toEntity(createDTO)).thenReturn(testAttribute);
        when(attributeRepository.save(any(Attribute.class))).thenReturn(testAttribute);

        // When
        Attribute result = attributeService.findOrCreateAttribute(createDTO);

        // Then
        assertNotNull(result);
        verify(attributeRepository).save(any(Attribute.class));
    }
}

