package com.ecommerce.E_commerce.integration;

import com.ecommerce.E_commerce.config.JwtAuthFilter;
import com.ecommerce.E_commerce.controller.ChatController;
import com.ecommerce.E_commerce.model.Address;
import com.ecommerce.E_commerce.model.User;
import com.ecommerce.E_commerce.repository.AddressRepository;
import com.ecommerce.E_commerce.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.transaction.Transactional;
import lombok.With;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;


import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
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
@Transactional
class AddressIntegrationTest {

    @MockBean
    private ChatModel chatModel;

    @MockBean
    private ChatController chatController;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AddressRepository addressRepository;

    @MockBean
    private JwtAuthFilter jwtAuthFilter;

    @Autowired
    private UserRepository userRepository;

    private Authentication auth;
    private User testUser;

    @BeforeEach
    void setUp() throws Exception{
        doAnswer(invocation -> {
            ServletRequest request = invocation.getArgument(0);
            ServletResponse response = invocation.getArgument(1);
            FilterChain chain = invocation.getArgument(2);

            chain.doFilter(request, response);
            return null;
        }).when(jwtAuthFilter).doFilter(any(), any(), any());

        addressRepository.deleteAll();
        userRepository.deleteAll();

        testUser = new User();
        testUser.setEmail("test@example.com");
        testUser.setPassword("$2a$10$test");
        testUser.setEnabled(true);
        testUser = userRepository.save(testUser);

        this.auth = new UsernamePasswordAuthenticationToken(
                testUser,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
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
                        .with(authentication(auth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.city").value("Warsaw"))
                .andReturn().getResponse().getContentAsString();

        Long addressId = Long.parseLong(response.split("\"id\":")[1].split(",")[0].trim());

        mockMvc.perform(get("/api/addresses/" + addressId)
                        .with(authentication(auth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.city").value("Warsaw"));

        mockMvc.perform(get("/api/addresses/user/" + testUser.getId())
                        .with(authentication(auth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].city").value("Warsaw"));

        String updateJson = """
                {
                    "city": "Krakow",
                    "postalCode": "30-001"
                }
                """;

        mockMvc.perform(put("/api/addresses/" + addressId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson)
                        .with(authentication(auth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.city").value("Krakow"));

        Address updated = addressRepository.findById(addressId).orElseThrow();
        assertThat(updated.getCity()).isEqualTo("Krakow");

        mockMvc.perform(delete("/api/addresses/" + addressId)
                        .with(authentication(auth)))
                .andExpect(status().isNoContent());

        // Verify soft delete
        Optional<Address> deleted = addressRepository.findById(addressId);
        assertThat(deleted).isEmpty();
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
        mockMvc.perform(get("/api/addresses/user/" + testUser.getId() + "/active")
                        .with(authentication(auth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].isActive").value(true));
    }
}

