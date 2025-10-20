package com.ecommerce.E_commerce.mapper;

import com.ecommerce.E_commerce.dto.categoryattribute.CategoryAttributeCreateDTO;
import com.ecommerce.E_commerce.dto.categoryattribute.CategoryAttributeDTO;
import com.ecommerce.E_commerce.dto.categoryattribute.CategoryAttributeUpdateDTO;
import com.ecommerce.E_commerce.model.Category;
import com.ecommerce.E_commerce.model.CategoryAttribute;
import com.ecommerce.E_commerce.model.CategoryAttributeType;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.junit.jupiter.api.Assertions.*;

public class CategoryAttributeMapperTest {

    private final CategoryAttributeMapper mapper = Mappers.getMapper(CategoryAttributeMapper.class);

    @Test
    void toDTO_mapsCategoryId() {
        Category category = new Category();
        category.setId(7L);

        CategoryAttribute attr = new CategoryAttribute();
        attr.setId(1L);
        attr.setCategory(category);
        attr.setName("color");
        attr.setType(CategoryAttributeType.TEXT);
        attr.setIsActive(true);

        CategoryAttributeDTO dto = mapper.toDTO(attr);
        assertEquals(1L, dto.id());
        assertEquals(7L, dto.categoryId());
        assertEquals("color", dto.name());
        assertEquals(CategoryAttributeType.TEXT, dto.type());
        assertTrue(dto.isActive());
    }

    @Test
    void fromCreateDTO_ignoresCategory() {
        CategoryAttributeCreateDTO dto = new CategoryAttributeCreateDTO(9L, "size", CategoryAttributeType.SELECT, true);
        CategoryAttribute entity = mapper.fromCreateDTO(dto);
        assertNull(entity.getCategory());
        assertEquals("size", entity.getName());
        assertEquals(CategoryAttributeType.SELECT, entity.getType());
        assertTrue(entity.getIsActive());
    }

    @Test
    void updateFromDTO_updatesFields() {
        CategoryAttributeUpdateDTO dto = new CategoryAttributeUpdateDTO("material", CategoryAttributeType.TEXT, false);
        CategoryAttribute entity = new CategoryAttribute();
        entity.setName("old");
        entity.setType(CategoryAttributeType.NUMBER);
        entity.setIsActive(true);
        mapper.updateFromDTO(dto, entity);
        assertEquals("material", entity.getName());
        assertEquals(CategoryAttributeType.TEXT, entity.getType());
        assertFalse(entity.getIsActive());
    }
}


