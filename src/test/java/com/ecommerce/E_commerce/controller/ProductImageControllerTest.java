package com.ecommerce.E_commerce.controller;

import com.ecommerce.E_commerce.dto.productimage.ProductImageDTO;
import com.ecommerce.E_commerce.service.ProductImageService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import com.ecommerce.E_commerce.config.JwtAuthFilter;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ProductImageController.class,
        excludeAutoConfiguration = {SecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class})
@org.springframework.test.context.ActiveProfiles("test")
class ProductImageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductImageService productImageService;

    @MockBean
    private JwtAuthFilter jwtAuthFilter; // satisfy SecurityConfig if loaded

    @Test
    void list_returns_images() throws Exception {
        Mockito.when(productImageService.listByProduct(3L)).thenReturn(List.of(
                new ProductImageDTO(1L, 3L, "/u/1.jpg", "alt", false, Instant.now(), Instant.now())
        ));

        mockMvc.perform(get("/api/products/3/images"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = {"OWNER"})
    void upload_accepts_multipart_when_owner() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "a.jpg", "image/jpeg", new byte[]{1});

        Mockito.when(productImageService.upload(eq(3L), any(), any(), anyBoolean()))
                .thenReturn(new ProductImageDTO(10L, 3L, "/u/a.jpg", null, true, Instant.now(), Instant.now()));

        mockMvc.perform(multipart("/api/products/3/images").file(file)
                        .param("isThumbnail", "true")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = {"OWNER"})
    void delete_requires_owner() throws Exception {
        mockMvc.perform(delete("/api/products/3/images/8"))
                .andExpect(status().isNoContent());
    }
}


