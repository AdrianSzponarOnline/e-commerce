package com.ecommerce.E_commerce.controller;

import com.ecommerce.E_commerce.dto.address.AddressDTO;
import com.ecommerce.E_commerce.service.AddressService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AddressController.class,
        excludeAutoConfiguration = {SecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class})
@ActiveProfiles("test")
class AddressControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AddressService addressService;

    @Test
    void create_ShouldReturnCreatedStatus() throws Exception {
        AddressDTO addressDTO = new AddressDTO(
                1L, 1L, "123 Main St", null, "Warsaw", null, "00-001", "Poland",
                Instant.now(), Instant.now(), null, true
        );

        Mockito.when(addressService.create(any())).thenReturn(addressDTO);

        String jsonRequest = """
                {
                    "userId": 1,
                    "line1": "123 Main St",
                    "city": "Warsaw",
                    "postalCode": "00-001",
                    "country": "Poland",
                    "isActive": true
                }
                """;

        mockMvc.perform(MockMvcRequestBuilders.post("/api/addresses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.city").value("Warsaw"));
    }

    @Test
    void getById_ShouldReturnAddress() throws Exception {
        AddressDTO addressDTO = new AddressDTO(
                1L, 1L, "123 Main St", null, "Warsaw", null, "00-001", "Poland",
                Instant.now(), Instant.now(), null, true
        );

        Mockito.when(addressService.getById(1L)).thenReturn(addressDTO);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/addresses/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.city").value("Warsaw"));
    }

    @Test
    void getByUserId_ShouldReturnList() throws Exception {
        List<AddressDTO> addresses = List.of(
                new AddressDTO(1L, 1L, "123 Main St", null, "Warsaw", null, "00-001", "Poland",
                        Instant.now(), Instant.now(), null, true),
                new AddressDTO(2L, 1L, "456 Other St", null, "Krakow", null, "30-001", "Poland",
                        Instant.now(), Instant.now(), null, true)
        );

        Mockito.when(addressService.getByUserId(1L)).thenReturn(addresses);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/addresses/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void update_ShouldReturnUpdatedAddress() throws Exception {
        AddressDTO addressDTO = new AddressDTO(
                1L, 1L, "456 New St", null, "Krakow", null, "30-001", "Poland",
                Instant.now(), Instant.now(), null, true
        );

        Mockito.when(addressService.update(eq(1L), any())).thenReturn(addressDTO);

        String jsonRequest = """
                {
                    "line1": "456 New St",
                    "city": "Krakow",
                    "postalCode": "30-001",
                    "country": "Poland"
                }
                """;

        mockMvc.perform(MockMvcRequestBuilders.put("/api/addresses/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.city").value("Krakow"));
    }

    @Test
    void delete_ShouldReturnNoContent() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/addresses/1"))
                .andExpect(status().isNoContent());

        Mockito.verify(addressService).delete(1L);
    }
}

