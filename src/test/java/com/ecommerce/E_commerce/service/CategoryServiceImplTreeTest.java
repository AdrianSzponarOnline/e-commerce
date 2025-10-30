package com.ecommerce.E_commerce.service;

import com.ecommerce.E_commerce.dto.category.CategoryDTO;
import com.ecommerce.E_commerce.mapper.CategoryMapper;
import com.ecommerce.E_commerce.model.Category;
import com.ecommerce.E_commerce.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.Mockito;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

public class CategoryServiceImplTreeTest {

    private CategoryRepository repository;
    private CategoryMapper mapper;
    private CategoryServiceImpl service;

    @BeforeEach
    void setup() {
        repository = Mockito.mock(CategoryRepository.class);
        mapper = Mappers.getMapper(CategoryMapper.class);
        service = new CategoryServiceImpl(repository, mapper);
    }

    private Category createCategory(Long id, String name, String slug, Category parent, boolean active) {
        Category c = new Category();
        c.setId(id);
        c.setName(name);
        c.setSeoSlug(slug);
        c.setIsActive(active);
        c.setCreatedAt(Instant.now());
        c.setUpdatedAt(Instant.now());
        c.setParent(parent);
        if (parent != null) {
            parent.getChildren().add(c);
        }
        return c;
    }

    @Test
    void listAll_returnsTreeWithChildren() {
        Category root = createCategory(1L, "root", "root", null, true);
        Category child1 = createCategory(2L, "child1", "child1", root, true);
        Category child2 = createCategory(3L, "child2", "child2", root, true);
        when(repository.findAll()).thenReturn(List.of(root, child1, child2));

        List<CategoryDTO> tree = service.listAll();
        assertEquals(1, tree.size());
        CategoryDTO rootDto = tree.get(0);
        assertEquals(1L, rootDto.id());
        assertNotNull(rootDto.children());
        assertEquals(2, rootDto.children().size());
    }

    @Test
    void listActive_filtersAndReturnsTree() {
        Category root = createCategory(1L, "root", "root", null, true);
        Category childActive = createCategory(2L, "childA", "child-a", root, true);
        createCategory(3L, "childI", "child-i", root, false);
        when(repository.findAllByIsActiveTrue()).thenReturn(List.of(root, childActive));

        List<CategoryDTO> tree = service.listActive();
        assertEquals(1, tree.size());
        assertEquals(1L, tree.get(0).id());
        assertEquals(1, tree.get(0).children().size());
        assertEquals(2L, tree.get(0).children().get(0).id());
    }

    @Test
    void listByParent_returnsSubtree() {
        Category parent = createCategory(10L, "parent", "parent", null, true);
        Category child1 = createCategory(11L, "child1", "child1", parent, true);
        Category child2 = createCategory(12L, "child2", "child2", parent, true);
        when(repository.findAllByParent_Id(10L)).thenReturn(List.of(child1, child2));

        List<CategoryDTO> tree = service.listByParent(10L);
        // Because listByParent builds a tree from children-only input, results are children as roots
        assertEquals(2, tree.size());
        assertTrue(tree.stream().anyMatch(c -> c.id().equals(11L)));
        assertTrue(tree.stream().anyMatch(c -> c.id().equals(12L)));
    }
}


