package com.ecommerce.E_commerce.controller;

import com.ecommerce.E_commerce.config.JwtAuthFilter;
import com.ecommerce.E_commerce.dto.productimage.ProductImageDTO;
import com.ecommerce.E_commerce.service.ProductImageService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf; // WAŻNE
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ProductImageController.class)
@org.springframework.test.context.ActiveProfiles("test")
class ProductImageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductImageService productImageService;

    @MockBean
    private JwtAuthFilter jwtAuthFilter;

    @BeforeEach
    void setUp() throws Exception {

        Mockito.doAnswer(invocation -> {
            ServletRequest request = invocation.getArgument(0);
            ServletResponse response = invocation.getArgument(1);
            FilterChain chain = invocation.getArgument(2);
            chain.doFilter(request, response); // Przepuść dalej
            return null;
        }).when(jwtAuthFilter).doFilter(any(), any(), any());
    }

    @Test
    @WithMockUser
    void list_returns_images() throws Exception {
        Mockito.when(productImageService.listByProduct(3L)).thenReturn(List.of(
                new ProductImageDTO(1L, 3L, "/u/1.jpg", "alt", false, Instant.now(), Instant.now())
        ));

        mockMvc.perform(get("/api/products/3/images"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].url").value("/u/1.jpg"));
    }

    @Test
    @WithMockUser(roles = {"OWNER"})
    void upload_accepts_multipart_when_owner() throws Exception {

        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "fake image content".getBytes());

        Mockito.when(productImageService.upload(eq(3L), any(), any(), anyBoolean()))
                .thenReturn(new ProductImageDTO(10L, 3L, "/u/test.jpg", null, true, Instant.now(), Instant.now()));

        mockMvc.perform(multipart("/api/products/3/images")
                        .file(file)
                        .param("isThumbnail", "true")
                        .with(csrf())
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.url").value("/u/test.jpg"));
    }

    @Test
    @WithMockUser(roles = {"OWNER"})
    void delete_requires_owner() throws Exception {
        mockMvc.perform(delete("/api/products/3/images/8")
                        .with(csrf()))
                .andExpect(status().isNoContent());
        Mockito.verify(productImageService).delete(3L, 8L);
    }
}