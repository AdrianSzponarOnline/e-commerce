package com.ecommerce.E_commerce.controller;

import com.ecommerce.E_commerce.dto.product.ProductSearchDTO;
import com.ecommerce.E_commerce.service.SearchService;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = SearchController.class,
        excludeAutoConfiguration = {SecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class})
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class SearchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SearchService searchService;

    @Test
    void search_ShouldReturnPageOfProducts() throws Exception {
        ProductSearchDTO productDTO = new ProductSearchDTO(
                1L,
                "Test Product",
                new BigDecimal("99.99"),
                "Short description",
                "https://example.com/image.jpg",
                "test-product",
                "Electronics",
                true,
                Map.of("Color", "Red")
        );

        Page<ProductSearchDTO> page = new PageImpl<>(List.of(productDTO), PageRequest.of(0, 20), 1);

        Mockito.when(searchService.search(
                anyString(),
                any(Long.class),
                any(BigDecimal.class),
                any(BigDecimal.class),
                any(Boolean.class),
                any(Map.class),
                any(Pageable.class)
        )).thenReturn(page);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/search")
                        .param("query", "test")
                        .param("minPrice", "10")
                        .param("maxPrice", "100")
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].name").value("Test Product"));
    }

    @Test
    void search_WithAttributes_ShouldReturnFilteredResults() throws Exception {
        ProductSearchDTO productDTO = new ProductSearchDTO(
                1L,
                "Test Product",
                new BigDecimal("99.99"),
                "Short description",
                "https://example.com/image.jpg",
                "test-product",
                "Electronics",
                true,
                Map.of("Color", "Red")
        );

        Page<ProductSearchDTO> page = new PageImpl<>(List.of(productDTO));

        Mockito.when(searchService.search(
                anyString(),
                any(Long.class),
                any(BigDecimal.class),
                any(BigDecimal.class),
                any(Boolean.class),
                any(Map.class),
                any(Pageable.class)
        )).thenReturn(page);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/search")
                        .param("query", "test")
                        .contentType("application/json")
                        .content("{\"Color\":\"Red\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }
}

