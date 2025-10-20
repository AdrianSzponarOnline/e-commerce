package com.ecommerce.E_commerce.service;

import com.ecommerce.E_commerce.dto.category.CategoryCreateDTO;
import com.ecommerce.E_commerce.dto.category.CategoryDTO;
import com.ecommerce.E_commerce.dto.category.CategoryUpdateDTO;
import com.ecommerce.E_commerce.exception.InvalidOperationException;
import com.ecommerce.E_commerce.exception.ResourceNotFoundException;
import com.ecommerce.E_commerce.exception.SeoSlugAlreadyExistsException;
import com.ecommerce.E_commerce.mapper.CategoryMapper;
import com.ecommerce.E_commerce.model.Category;
import com.ecommerce.E_commerce.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class CategoryServiceImplTest {

    private CategoryRepository repository;
    private CategoryMapper mapper;
    private CategoryServiceImpl service;

    @BeforeEach
    void setup() {
        repository = Mockito.mock(CategoryRepository.class);
        mapper = Mappers.getMapper(CategoryMapper.class);
        service = new CategoryServiceImpl(repository, mapper);
    }

    @Test
    void create_rejectsDuplicateSlug() {
        when(repository.existsBySeoSlug("phones")).thenReturn(true);
        CategoryCreateDTO dto = new CategoryCreateDTO("phones", "desc", "phones", null, true);
        assertThrows(SeoSlugAlreadyExistsException.class, () -> service.create(dto));
    }

    @Test
    void getBySeoSlug_returnsDTO() {
        Category c = new Category();
        c.setId(1L);
        c.setName("phones");
        c.setSeoSlug("phones");
        when(repository.findBySeoSlug("phones")).thenReturn(Optional.of(c));
        CategoryDTO dto = service.getBySeoSlug("phones");
        assertEquals(1L, dto.id());
        assertEquals("phones", dto.seoSlug());
    }

    @Test
    void update_throwsWhenNotFound() {
        when(repository.findById(100L)).thenReturn(Optional.empty());
        CategoryUpdateDTO dto = new CategoryUpdateDTO("n", "d", "slug", null, true);
        assertThrows(ResourceNotFoundException.class, () -> service.update(100L, dto));
    }

    @Test
    void softDelete_setsFlags() {
        Category c = new Category();
        c.setId(5L);
        c.setSeoSlug("x");
        when(repository.findById(5L)).thenReturn(Optional.of(c));
        service.softDelete(5L);
        ArgumentCaptor<Category> captor = ArgumentCaptor.forClass(Category.class);
        verify(repository).save(captor.capture());
        assertEquals(Boolean.FALSE, captor.getValue().getIsActive());
        assertNotNull(captor.getValue().getDeletedAt());
    }

    @Test
    void update_preventsSelfParent() {
        Category c = new Category();
        c.setId(10L);
        c.setSeoSlug("phones");
        when(repository.findById(10L)).thenReturn(Optional.of(c));
        CategoryUpdateDTO dto = new CategoryUpdateDTO("n","d","phones",10L,true);
        assertThrows(InvalidOperationException.class, () -> service.update(10L, dto));
    }

    @Test
    void update_preventsCycleViaAncestor() {
        Category child = new Category();
        child.setId(20L);
        child.setSeoSlug("child");

        Category parent = new Category();
        parent.setId(21L);
        parent.setSeoSlug("parent");
        parent.setParent(child); // cycle if child -> parent

        when(repository.findById(20L)).thenReturn(Optional.of(child));
        when(repository.findById(21L)).thenReturn(Optional.of(parent));

        CategoryUpdateDTO dto = new CategoryUpdateDTO("n","d","child",21L,true);
        assertThrows(InvalidOperationException.class, () -> service.update(20L, dto));
    }
}


