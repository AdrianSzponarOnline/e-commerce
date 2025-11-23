package com.ecommerce.E_commerce.mapper;

import com.ecommerce.E_commerce.dto.categoryattribute.CategoryAttributeCreateDTO;
import com.ecommerce.E_commerce.dto.categoryattribute.CategoryAttributeDTO;
import com.ecommerce.E_commerce.dto.categoryattribute.CategoryAttributeUpdateDTO;
import com.ecommerce.E_commerce.model.Attribute;
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

        Attribute attribute = new Attribute();
        attribute.setId(2L);
        attribute.setName("color");
        attribute.setType(CategoryAttributeType.TEXT);

        CategoryAttribute attr = new CategoryAttribute();
        attr.setId(1L);
        attr.setCategory(category);
        attr.setAttribute(attribute);
        attr.setKeyAttribute(false);
        attr.setActive(true);

        CategoryAttributeDTO dto = mapper.toDTO(attr);
        assertEquals(1L, dto.id());
        assertEquals(7L, dto.categoryId());
        assertEquals(2L, dto.attributeId());
        assertEquals("color", dto.attributeName());
        assertEquals(CategoryAttributeType.TEXT, dto.attributeType());
        assertFalse(dto.isKeyAttribute());
        assertTrue(dto.isActive());
    }

    @Test
    void fromCreateDTO_ignoresCategory() {
        CategoryAttributeCreateDTO dto = new CategoryAttributeCreateDTO(9L, 3L, true, true);
        CategoryAttribute entity = mapper.fromCreateDTO(dto);
        assertNull(entity.getCategory());
        assertNull(entity.getAttribute());
    }

    @Test
    void updateFromDTO_updatesFields() {
        CategoryAttributeUpdateDTO dto = new CategoryAttributeUpdateDTO(true, false);
        CategoryAttribute entity = new CategoryAttribute();
        entity.setKeyAttribute(false);
        entity.setActive(true);
        mapper.updateFromDTO(dto, entity);
        assertTrue(entity.isKeyAttribute());
        assertFalse(entity.isActive());
    }
}


