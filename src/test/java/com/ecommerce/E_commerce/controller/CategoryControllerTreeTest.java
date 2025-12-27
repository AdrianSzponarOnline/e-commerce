package com.ecommerce.E_commerce.controller;

import com.ecommerce.E_commerce.dto.category.CategoryDTO;
import com.ecommerce.E_commerce.service.CategoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(value = CategoryController.class,
        excludeAutoConfiguration = {SecurityAutoConfiguration.class})
@AutoConfigureMockMvc(addFilters = false)
public class CategoryControllerTreeTest extends BaseControllerTest{

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CategoryService categoryService;

    private CategoryDTO dto(Long id, String name, Long parentId) {
        return new CategoryDTO(id, name, null, name, true, Instant.now(), Instant.now(), parentId, new ArrayList<>());
    }

    @Test
    void getAll_returnsTreeJson() throws Exception {
        CategoryDTO root = dto(1L, "root", null);
        CategoryDTO child = dto(2L, "child", 1L);
        root.children().add(child);
        when(categoryService.listAll()).thenReturn(List.of(root));

        mockMvc.perform(get("/api/categories").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].children", hasSize(1)))
                .andExpect(jsonPath("$[0].children[0].id", is(2)));
    }

    @Test
    void getActive_returnsTreeJson() throws Exception {
        CategoryDTO root = dto(10L, "active-root", null);
        when(categoryService.listActive()).thenReturn(List.of(root));

        mockMvc.perform(get("/api/categories/active").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(10)));
    }

    @Test
    void removedPublicEndpoints_return400() throws Exception {
        mockMvc.perform(get("/api/categories/public")).andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/categories/public/active")).andExpect(status().isNotFound());
    }
}


