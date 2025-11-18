package com.ecommerce.E_commerce.service;

import com.ecommerce.E_commerce.dto.categoryattribute.CategoryAttributeCreateDTO;
import com.ecommerce.E_commerce.dto.categoryattribute.CategoryAttributeDTO;
import com.ecommerce.E_commerce.dto.categoryattribute.CategoryAttributeUpdateDTO;
import com.ecommerce.E_commerce.exception.ResourceNotFoundException;
import com.ecommerce.E_commerce.mapper.CategoryAttributeMapper;
import com.ecommerce.E_commerce.model.Attribute;
import com.ecommerce.E_commerce.model.Category;
import com.ecommerce.E_commerce.model.CategoryAttribute;
import com.ecommerce.E_commerce.model.CategoryAttributeType;
import com.ecommerce.E_commerce.repository.AttributeRepository;
import com.ecommerce.E_commerce.repository.CategoryAttributeRepository;
import com.ecommerce.E_commerce.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class CategoryAttributeServiceImplTest {

    private CategoryAttributeRepository attrRepo;
    private CategoryRepository categoryRepo;
    private AttributeRepository attributeRepo;
    private CategoryAttributeMapper mapper;
    private CategoryAttributeServiceImpl service;

    @BeforeEach
    void setup() {
        attrRepo = mock(CategoryAttributeRepository.class);
        categoryRepo = mock(CategoryRepository.class);
        attributeRepo = mock(AttributeRepository.class);
        mapper = Mappers.getMapper(CategoryAttributeMapper.class);
        service = new CategoryAttributeServiceImpl(attrRepo, categoryRepo, attributeRepo, mapper);
    }

    @Test
    void create_assignsCategory() {
        Category category = new Category();
        category.setId(3L);
        when(categoryRepo.findById(3L)).thenReturn(Optional.of(category));

        Attribute attribute = new Attribute();
        attribute.setId(1L);
        attribute.setName("color");
        attribute.setType(CategoryAttributeType.TEXT);
        when(attributeRepo.findById(1L)).thenReturn(Optional.of(attribute));
        when(attrRepo.findByCategoryIdAndAttributeId(3L, 1L)).thenReturn(Optional.empty());

        CategoryAttribute saved = new CategoryAttribute();
        saved.setId(10L);
        saved.setCategory(category);
        saved.setAttribute(attribute);
        saved.setKeyAttribute(true);
        saved.setActive(true);
        when(attrRepo.save(any())).thenReturn(saved);

        CategoryAttributeDTO dto = service.create(new CategoryAttributeCreateDTO(3L, 1L, true, true));
        assertEquals(10L, dto.id());
        assertEquals(3L, dto.categoryId());
    }

    @Test
    void create_throwsWhenCategoryMissing() {
        when(categoryRepo.findById(5L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () ->
                service.create(new CategoryAttributeCreateDTO(5L, 1L, true, true)));
    }

    @Test
    void update_updatesFields() {
        Attribute attribute = new Attribute();
        attribute.setId(1L);
        attribute.setName("old");
        attribute.setType(CategoryAttributeType.NUMBER);
        
        CategoryAttribute entity = new CategoryAttribute();
        entity.setId(8L);
        entity.setAttribute(attribute);
        entity.setKeyAttribute(false);
        entity.setActive(true);
        when(attrRepo.findById(8L)).thenReturn(Optional.of(entity));
        when(attrRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CategoryAttributeDTO dto = service.update(8L, new CategoryAttributeUpdateDTO(true, false));
        assertTrue(dto.isKeyAttribute());
        assertFalse(dto.isActive());
    }

    @Test
    void listByCategory_mapsToDTOs() {
        Category category = new Category();
        category.setId(2L);
        
        Attribute attr1 = new Attribute();
        attr1.setId(1L);
        attr1.setName("n1");
        attr1.setType(CategoryAttributeType.TEXT);
        
        Attribute attr2 = new Attribute();
        attr2.setId(2L);
        attr2.setName("n2");
        attr2.setType(CategoryAttributeType.NUMBER);
        
        CategoryAttribute a = new CategoryAttribute();
        a.setId(1L);
        a.setCategory(category);
        a.setAttribute(attr1);
        a.setKeyAttribute(false);
        a.setActive(true);
        
        CategoryAttribute b = new CategoryAttribute();
        b.setId(2L);
        b.setCategory(category);
        b.setAttribute(attr2);
        b.setKeyAttribute(false);
        b.setActive(true);
        
        when(attrRepo.findAllByCategory_Id(2L)).thenReturn(List.of(a, b));

        var list = service.listByCategory(2L);
        assertEquals(2, list.size());
        assertEquals(1L, list.get(0).id());
        assertEquals(2L, list.get(1).id());
    }
}


