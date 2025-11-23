package com.ecommerce.E_commerce.controller;

import com.ecommerce.E_commerce.dto.product.ProductCreateDTO;
import com.ecommerce.E_commerce.dto.product.ProductDTO;
import com.ecommerce.E_commerce.dto.product.ProductSummaryDTO;
import com.ecommerce.E_commerce.dto.product.ProductUpdateDTO;
import com.ecommerce.E_commerce.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ProductController.class,
        excludeAutoConfiguration = {SecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class})
@ActiveProfiles("test")
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createProduct_ShouldReturnCreatedProduct() throws Exception {
        ProductCreateDTO createDTO = new ProductCreateDTO(
                "Test Product",
                "Description",
                "Short desc",
                new BigDecimal("99.99"),
                new BigDecimal("23.00"),
                new BigDecimal("10.00"),
                "3-5 days",
                "https://example.com/image.jpg",
                "test-product",
                1L,
                false,
                null
        );

        ProductDTO productDTO = new ProductDTO(
                1L,
                "Test Product",
                "Description",
                "Short desc",
                new BigDecimal("99.99"),
                "TEST-001",
                new BigDecimal("23.00"),
                false,
                new BigDecimal("10.00"),
                "3-5 days",
                "https://example.com/image.jpg",
                "test-product",
                null,
                new ArrayList<>(),
                Instant.now(),
                Instant.now()
        );

        Mockito.when(productService.create(any(ProductCreateDTO.class))).thenReturn(productDTO);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Test Product"));
    }

    @Test
    void getProductById_ShouldReturnProduct() throws Exception {
        ProductDTO productDTO = new ProductDTO(
                1L,
                "Test Product",
                "Description",
                "Short desc",
                new BigDecimal("99.99"),
                "TEST-001",
                new BigDecimal("23.00"),
                false,
                new BigDecimal("10.00"),
                "3-5 days",
                "https://example.com/image.jpg",
                "test-product",
                null,
                new ArrayList<>(),
                Instant.now(),
                Instant.now()
        );

        Mockito.when(productService.getById(1L)).thenReturn(productDTO);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Test Product"));
    }

    @Test
    void getAllProducts_ShouldReturnPage() throws Exception {
        ProductSummaryDTO summary = new ProductSummaryDTO(
                1L,
                "Test Product",
                new BigDecimal("99.99"),
                "Short desc",
                "https://example.com/image.jpg",
                "test-product",
                "Electronics"
        );

        Page<ProductSummaryDTO> page = new PageImpl<>(List.of(summary));
        Mockito.when(productService.findAll(anyInt(), anyInt(), anyString(), anyString())).thenReturn(page);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/products")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].name").value("Test Product"));
    }

    @Test
    void getProductsByCategory_ShouldReturnPage() throws Exception {
        ProductSummaryDTO summary = new ProductSummaryDTO(
                1L,
                "Test Product",
                new BigDecimal("99.99"),
                "Short desc",
                "https://example.com/image.jpg",
                "test-product",
                "Electronics"
        );

        Page<ProductSummaryDTO> page = new PageImpl<>(List.of(summary));
        Mockito.when(productService.findByCategory(anyLong(), anyInt(), anyInt(), anyString(), anyString()))
                .thenReturn(page);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/products/category/1")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void searchProductsByName_ShouldReturnPage() throws Exception {
        ProductSummaryDTO summary = new ProductSummaryDTO(
                1L,
                "Test Product",
                new BigDecimal("99.99"),
                "Short desc",
                "https://example.com/image.jpg",
                "test-product",
                "Electronics"
        );

        Page<ProductSummaryDTO> page = new PageImpl<>(List.of(summary));
        Mockito.when(productService.searchByName(anyString(), anyInt(), anyInt(), anyString(), anyString()))
                .thenReturn(page);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/products/search/name")
                        .param("name", "test")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void updateProduct_ShouldReturnUpdatedProduct() throws Exception {
        ProductUpdateDTO updateDTO = new ProductUpdateDTO(
                "Updated Product",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );

        ProductDTO productDTO = new ProductDTO(
                1L,
                "Updated Product",
                "Description",
                "Short desc",
                new BigDecimal("99.99"),
                "TEST-001",
                new BigDecimal("23.00"),
                false,
                new BigDecimal("10.00"),
                "3-5 days",
                "https://example.com/image.jpg",
                "test-product",
                null,
                new ArrayList<>(),
                Instant.now(),
                Instant.now()
        );

        Mockito.when(productService.update(eq(1L), any(ProductUpdateDTO.class))).thenReturn(productDTO);

        mockMvc.perform(MockMvcRequestBuilders.put("/api/products/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Product"));
    }

    @Test
    void deleteProduct_ShouldReturnNoContent() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/products/1"))
                .andExpect(status().isNoContent());

        Mockito.verify(productService).delete(1L);
    }

    @Test
    void getProductCountByCategory_ShouldReturnCount() throws Exception {
        Mockito.when(productService.countByCategory(1L)).thenReturn(5L);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/products/stats/category/1/count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(5));
    }
}

