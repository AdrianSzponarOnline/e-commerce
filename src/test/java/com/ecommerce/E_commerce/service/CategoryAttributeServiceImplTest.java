package com.ecommerce.E_commerce.service;

import com.ecommerce.E_commerce.dto.categoryattribute.CategoryAttributeCreateDTO;
import com.ecommerce.E_commerce.dto.categoryattribute.CategoryAttributeDTO;
import com.ecommerce.E_commerce.dto.categoryattribute.CategoryAttributeUpdateDTO;
import com.ecommerce.E_commerce.exception.ResourceNotFoundException;
import com.ecommerce.E_commerce.mapper.CategoryAttributeMapper;
import com.ecommerce.E_commerce.model.Category;
import com.ecommerce.E_commerce.model.CategoryAttribute;
import com.ecommerce.E_commerce.model.CategoryAttributeType;
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
    private CategoryAttributeMapper mapper;
    private CategoryAttributeServiceImpl service;

    @BeforeEach
    void setup() {
        attrRepo = mock(CategoryAttributeRepository.class);
        categoryRepo = mock(CategoryRepository.class);
        mapper = Mappers.getMapper(CategoryAttributeMapper.class);
        service = new CategoryAttributeServiceImpl(attrRepo, categoryRepo, mapper);
    }

    @Test
    void create_assignsCategory() {
        Category category = new Category();
        category.setId(3L);
        when(categoryRepo.findById(3L)).thenReturn(Optional.of(category));

        CategoryAttribute saved = new CategoryAttribute();
        saved.setId(10L);
        saved.setName("color");
        saved.setType(CategoryAttributeType.TEXT);
        saved.setIsActive(true);
        saved.setCategory(category);
        when(attrRepo.save(any())).thenReturn(saved);

        CategoryAttributeDTO dto = service.create(new CategoryAttributeCreateDTO(3L, "color", CategoryAttributeType.TEXT, true));
        assertEquals(10L, dto.id());
        assertEquals(3L, dto.categoryId());
    }

    @Test
    void create_throwsWhenCategoryMissing() {
        when(categoryRepo.findById(5L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () ->
                service.create(new CategoryAttributeCreateDTO(5L, "size", CategoryAttributeType.SELECT, true)));
    }

    @Test
    void update_updatesFields() {
        CategoryAttribute entity = new CategoryAttribute();
        entity.setId(8L);
        entity.setName("old");
        entity.setType(CategoryAttributeType.NUMBER);
        entity.setIsActive(true);
        when(attrRepo.findById(8L)).thenReturn(Optional.of(entity));
        when(attrRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CategoryAttributeDTO dto = service.update(8L, new CategoryAttributeUpdateDTO("new", CategoryAttributeType.BOOLEAN, false));
        assertEquals("new", dto.name());
        assertEquals(CategoryAttributeType.BOOLEAN, dto.type());
        assertFalse(dto.isActive());
    }

    @Test
    void listByCategory_mapsToDTOs() {
        Category category = new Category();
        category.setId(2L);
        CategoryAttribute a = new CategoryAttribute(); a.setId(1L); a.setCategory(category); a.setName("n1"); a.setType(CategoryAttributeType.TEXT); a.setIsActive(true);
        CategoryAttribute b = new CategoryAttribute(); b.setId(2L); b.setCategory(category); b.setName("n2"); b.setType(CategoryAttributeType.NUMBER); b.setIsActive(true);
        when(attrRepo.findAllByCategory_Id(2L)).thenReturn(List.of(a,b));

        var list = service.listByCategory(2L);
        assertEquals(2, list.size());
        assertEquals(1L, list.get(0).id());
        assertEquals(2L, list.get(1).id());
    }
}


