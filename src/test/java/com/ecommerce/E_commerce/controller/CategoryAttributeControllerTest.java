package com.ecommerce.E_commerce.controller;

import com.ecommerce.E_commerce.dto.categoryattribute.CategoryAttributeCreateDTO;
import com.ecommerce.E_commerce.dto.categoryattribute.CategoryAttributeDTO;
import com.ecommerce.E_commerce.dto.categoryattribute.CategoryAttributeUpdateDTO;
import com.ecommerce.E_commerce.model.CategoryAttributeType;
import com.ecommerce.E_commerce.service.CategoryAttributeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = CategoryAttributeController.class)
@ImportAutoConfiguration(exclude = {DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class})
@AutoConfigureMockMvc(addFilters = false)
public class CategoryAttributeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CategoryAttributeService service;

    @MockitoBean
    private com.ecommerce.E_commerce.config.JwtAuthFilter jwtAuthFilter;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser
    void list_returnsAttributes() throws Exception {
        var dto1 = new CategoryAttributeDTO(1L, 10L, 100L, "color", CategoryAttributeType.TEXT, false, true, Instant.now(), Instant.now());
        var dto2 = new CategoryAttributeDTO(2L, 10L, 101L, "size", CategoryAttributeType.SELECT, true, true, Instant.now(), Instant.now());
        when(service.listByCategory(10L)).thenReturn(List.of(dto1, dto2));
        mockMvc.perform(get("/api/categories/10/attributes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[1].attributeName").value("size"));
    }

    @Test
    @WithMockUser(roles = {"OWNER"})
    void create_requiresOwnerAndReturnsCreated() throws Exception {
        var body = new CategoryAttributeCreateDTO(10L, 100L, false, true);
        var response = new CategoryAttributeDTO(5L, 10L, 100L, "color", CategoryAttributeType.TEXT, false, true, Instant.now(), Instant.now());
        when(service.create(any())).thenReturn(response);
        mockMvc.perform(post("/api/categories/10/attributes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(5L))
                .andExpect(jsonPath("$.categoryId").value(10L));
    }

    @Test
    @WithMockUser(roles = {"OWNER"})
    void update_requiresOwner() throws Exception {
        var body = new CategoryAttributeUpdateDTO(true, false);
        var response = new CategoryAttributeDTO(6L, 10L, 101L, "size", CategoryAttributeType.SELECT, true, false, Instant.now(), Instant.now());
        when(service.update(eq(6L), any())).thenReturn(response);
        mockMvc.perform(put("/api/categories/10/attributes/6")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.attributeName").value("size"))
                .andExpect(jsonPath("$.isActive").value(false));
    }
}


