package com.ecommerce.E_commerce.integration;

import com.ecommerce.E_commerce.model.Address;
import com.ecommerce.E_commerce.model.User;
import com.ecommerce.E_commerce.repository.AddressRepository;
import com.ecommerce.E_commerce.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;


import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
@org.springframework.test.context.TestPropertySource(properties = {
        "app.upload-dir=/tmp/uploads-test"
})
class AddressIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private UserRepository userRepository;

    @MockBean
    private com.ecommerce.E_commerce.config.JwtAuthFilter jwtAuthFilter;

    private User testUser;

    @BeforeEach
    void setUp() {
        addressRepository.deleteAll();
        userRepository.deleteAll();

        testUser = new User();
        testUser.setEmail("test@example.com");
        testUser.setPassword("$2a$10$test");
        testUser.setEnabled(true);
        testUser = userRepository.save(testUser);
    }

    @Test
    void full_flow_create_get_update_delete() throws Exception {
        // Create address
        String createJson = """
                {
                    "userId": %d,
                    "line1": "123 Main St",
                    "line2": "Apt 4",
                    "city": "Warsaw",
                    "region": "Mazovia",
                    "postalCode": "00-001",
                    "country": "Poland",
                    "isActive": true
                }
                """.formatted(testUser.getId());

        String response = mockMvc.perform(post("/api/addresses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.city").value("Warsaw"))
                .andReturn().getResponse().getContentAsString();

        // Extract ID (simple parse for test)
        Long addressId = Long.parseLong(response.split("\"id\":")[1].split(",")[0].trim());

        // Get by ID
        mockMvc.perform(get("/api/addresses/" + addressId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.city").value("Warsaw"));

        // Get by user ID
        mockMvc.perform(get("/api/addresses/user/" + testUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].city").value("Warsaw"));

        // Update
        String updateJson = """
                {
                    "city": "Krakow",
                    "postalCode": "30-001"
                }
                """;

        mockMvc.perform(put("/api/addresses/" + addressId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.city").value("Krakow"));

        // Verify in DB
        Address updated = addressRepository.findById(addressId).orElseThrow();
        assertThat(updated.getCity()).isEqualTo("Krakow");

        // Delete (soft delete)
        mockMvc.perform(delete("/api/addresses/" + addressId))
                .andExpect(status().isNoContent());

        // Verify soft delete
        Address deleted = addressRepository.findById(addressId).orElseThrow();
        assertThat(deleted.getDeletedAt()).isNotNull();
        assertThat(deleted.getIsActive()).isFalse();
    }

    @Test
    void getActiveByUserId_ShouldReturnOnlyActive() throws Exception {
        // Create active address
        Address active = new Address();
        active.setUser(testUser);
        active.setLine1("Active St");
        active.setCity("Warsaw");
        active.setPostalCode("00-001");
        active.setCountry("Poland");
        active.setIsActive(true);
        active.setCreatedAt(Instant.now());
        active.setUpdatedAt(Instant.now());
        addressRepository.save(active);

        // Create inactive address
        Address inactive = new Address();
        inactive.setUser(testUser);
        inactive.setLine1("Inactive St");
        inactive.setCity("Warsaw");
        inactive.setPostalCode("00-002");
        inactive.setCountry("Poland");
        inactive.setIsActive(false);
        inactive.setCreatedAt(Instant.now());
        inactive.setUpdatedAt(Instant.now());
        addressRepository.save(inactive);

        // Get active addresses
        mockMvc.perform(get("/api/addresses/user/" + testUser.getId() + "/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].isActive").value(true));
    }
}

