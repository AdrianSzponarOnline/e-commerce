package com.ecommerce.E_commerce.mapper;

import com.ecommerce.E_commerce.dto.category.CategoryDTO;
import com.ecommerce.E_commerce.dto.category.ChildCategoryDTO;
import com.ecommerce.E_commerce.model.Category;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.Instant;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class CategoryMapperTest {

    private final CategoryMapper mapper = Mappers.getMapper(CategoryMapper.class);

    @Test
    void toCategoryDTO_mapsParentAndChildren() {
        Category parent = new Category();
        parent.setId(1L);

        Category child1 = new Category();
        child1.setId(3L);
        child1.setName("child-one");
        child1.setSeoSlug("child-one");

        Category child2 = new Category();
        child2.setId(4L);
        child2.setName("child-two");
        child2.setSeoSlug("child-two");

        Category category = new Category();
        category.setId(2L);
        category.setName("electronics");
        category.setSeoSlug("electronics");
        category.setDescription("desc");
        category.setIsActive(true);
        category.setCreatedAt(Instant.now());
        category.setUpdatedAt(Instant.now());
        category.setParent(parent);
        category.setChildren(Set.of(child1, child2));

        CategoryDTO dto = mapper.toCategoryDTO(category);

        assertNotNull(dto);
        assertEquals(2L, dto.id());
        assertEquals("electronics", dto.name());
        assertEquals(1L, dto.parentId());
        assertNotNull(dto.children());
        assertEquals(2, dto.children().size());
        assertTrue(dto.children().stream().map(ChildCategoryDTO::name).anyMatch(n -> n.equals("child-one")));
        assertTrue(dto.children().stream().map(ChildCategoryDTO::seoSlug).anyMatch(s -> s.equals("child-two")));
    }
}


