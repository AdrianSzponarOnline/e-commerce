package com.ecommerce.E_commerce.controller;

import com.ecommerce.E_commerce.dto.productattributevalue.ProductAttributeValueCreateDTO;
import com.ecommerce.E_commerce.dto.productattributevalue.ProductAttributeValueDTO;
import com.ecommerce.E_commerce.dto.productattributevalue.ProductAttributeValueUpdateDTO;
import com.ecommerce.E_commerce.service.ProductAttributeValueService;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ProductAttributeValueController.class,
        excludeAutoConfiguration = {SecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class})
@ActiveProfiles("test")
class ProductAttributeValueControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductAttributeValueService productAttributeValueService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createProductAttributeValue_ShouldReturnCreated() throws Exception {
        ProductAttributeValueCreateDTO createDTO = new ProductAttributeValueCreateDTO(
                1L,
                1L,
                "Red"
        );

        ProductAttributeValueDTO dto = new ProductAttributeValueDTO(
                1L,
                "Color",
                "TEXT",
                true,
                "Red"
        );

        Mockito.when(productAttributeValueService.create(any(ProductAttributeValueCreateDTO.class))).thenReturn(dto);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/product-attribute-values")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.attributeValue").value("Red"));
    }

    @Test
    void getProductAttributeValueById_ShouldReturnValue() throws Exception {
        ProductAttributeValueDTO dto = new ProductAttributeValueDTO(
                1L,
                "Color",
                "TEXT",
                true,
                "Red"
        );

        Mockito.when(productAttributeValueService.getById(1L)).thenReturn(dto);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/product-attribute-values/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.attributeValue").value("Red"));
    }

    @Test
    void getProductAttributeValuesByProduct_ShouldReturnList() throws Exception {
        ProductAttributeValueDTO dto = new ProductAttributeValueDTO(
                1L,
                "Color",
                "TEXT",
                true,
                "Red"
        );

        Mockito.when(productAttributeValueService.getByProductId(1L)).thenReturn(List.of(dto));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/product-attribute-values/product/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void getAllProductAttributeValues_ShouldReturnPage() throws Exception {
        ProductAttributeValueDTO dto = new ProductAttributeValueDTO(
                1L,
                "Color",
                "TEXT",
                true,
                "Red"
        );

        Page<ProductAttributeValueDTO> page = new PageImpl<>(List.of(dto));
        Mockito.when(productAttributeValueService.findAll(any())).thenReturn(page);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/product-attribute-values")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(1));
    }

    @Test
    void updateProductAttributeValue_ShouldReturnUpdated() throws Exception {
        ProductAttributeValueUpdateDTO updateDTO = new ProductAttributeValueUpdateDTO(null, null, "Blue", null);

        ProductAttributeValueDTO dto = new ProductAttributeValueDTO(
                1L,
                "Color",
                "TEXT",
                true,
                "Blue"
        );

        Mockito.when(productAttributeValueService.update(eq(1L), any(ProductAttributeValueUpdateDTO.class)))
                .thenReturn(dto);

        mockMvc.perform(MockMvcRequestBuilders.put("/api/product-attribute-values/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.attributeValue").value("Blue"));
    }

    @Test
    void deleteProductAttributeValue_ShouldReturnNoContent() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/product-attribute-values/1"))
                .andExpect(status().isNoContent());

        Mockito.verify(productAttributeValueService).delete(1L);
    }

    @Test
    void getProductAttributeValueCountByProduct_ShouldReturnCount() throws Exception {
        Mockito.when(productAttributeValueService.countByProductId(1L)).thenReturn(5L);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/product-attribute-values/stats/product/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(5));
    }
}

