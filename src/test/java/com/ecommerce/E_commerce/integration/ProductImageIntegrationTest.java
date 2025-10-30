package com.ecommerce.E_commerce.integration;

import com.ecommerce.E_commerce.model.Category;
import com.ecommerce.E_commerce.model.Product;
import com.ecommerce.E_commerce.repository.CategoryRepository;
import com.ecommerce.E_commerce.repository.ProductRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import com.ecommerce.E_commerce.config.JwtAuthFilter;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@org.springframework.test.context.ActiveProfiles("test")
class ProductImageIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @MockBean
    private JwtAuthFilter jwtAuthFilter; // bypass JWT during tests

    private static Path uploadDir;

    @BeforeAll
    static void initUploadDir() throws Exception {
        uploadDir = Files.createTempDirectory("uploads-test");
    }

    @DynamicPropertySource
    static void registerProps(DynamicPropertyRegistry registry) {
        if (uploadDir == null) {
            try { uploadDir = Files.createTempDirectory("uploads-test"); }
            catch (Exception ignored) {}
        }
        final Path dir = uploadDir;
        registry.add("app.upload-dir", () -> dir.toString());
        registry.add("app.upload-max-bytes", () -> 5_000_000);
        registry.add("app.upload-allowed-types", () -> "image/jpeg,image/png");
    }

    private Long ensureProduct() {
        Category cat = new Category();
        cat.setName("Cat");
        cat.setSeoSlug("cat");
        cat.setCreatedAt(Instant.now());
        cat.setUpdatedAt(Instant.now());
        categoryRepository.save(cat);

        Product p = new Product();
        p.setName("Prod");
        p.setDescription("Desc");
        p.setShortDescription("Short");
        p.setPrice(new BigDecimal("12.34"));
        p.setVatRate(new BigDecimal("23.00"));
        p.setIsFeatured(false);
        p.setShippingCost(BigDecimal.ZERO);
        p.setEstimatedDeliveryTime("2d");
        p.setSeoSlug("prod");
        p.setCategory(cat);
        p.setCreatedAt(Instant.now());
        p.setUpdatedAt(Instant.now());
        p.setIsActive(true);
        productRepository.save(p);
        return p.getId();
    }

    @Test
    @WithMockUser(roles = {"OWNER"})
    void full_flow_upload_list_set_thumbnail_delete() throws Exception {
        Long productId = ensureProduct();

        MockMultipartFile file = new MockMultipartFile("file", "a.jpg", "image/jpeg", new byte[]{1,2,3});
        mockMvc.perform(multipart("/api/products/" + productId + "/images").file(file)
                        .param("altText", "front")
                        .param("isThumbnail", "true"))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/products/" + productId + "/images"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].isThumbnail").value(true));

        // add second image and then set as thumbnail
        MockMultipartFile file2 = new MockMultipartFile("file", "b.jpg", "image/jpeg", new byte[]{1});
        mockMvc.perform(multipart("/api/products/" + productId + "/images").file(file2))
                .andExpect(status().isCreated());

        // list to get second id
        String listJson = mockMvc.perform(get("/api/products/" + productId + "/images"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        // crude parse to get last id
        String lastId = listJson.replaceAll(".*\\{\\\"id\\\":(\\d+)[^}]*}\\s*]$", "$1");
        mockMvc.perform(post("/api/products/" + productId + "/images/" + lastId + "/thumbnail"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isThumbnail").value(true));

        // delete that image
        mockMvc.perform(delete("/api/products/" + productId + "/images/" + lastId))
                .andExpect(status().isNoContent());

        // product should still exist
        assertThat(productRepository.findById(productId)).isPresent();
    }
}


