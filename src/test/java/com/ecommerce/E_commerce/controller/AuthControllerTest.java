package com.ecommerce.E_commerce.controller;

import com.ecommerce.E_commerce.dto.auth.AuthRequestDTO;
import com.ecommerce.E_commerce.dto.auth.AuthResponseDTO;
import com.ecommerce.E_commerce.dto.auth.RegisterRequestDTO;
import com.ecommerce.E_commerce.dto.auth.UserDto;
import com.ecommerce.E_commerce.service.AuthService;
import com.ecommerce.E_commerce.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AuthController.class,
        excludeAutoConfiguration = {SecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class})
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void login_ShouldReturnAuthResponse() throws Exception {
        AuthRequestDTO request = new AuthRequestDTO("test@example.com", "password123");
        AuthResponseDTO response = new AuthResponseDTO(
                "jwt-token",
                "John",
                "Doe",
                "test@example.com",
                List.of("ROLE_USER")
        );

        Mockito.when(authService.authenticate(any(AuthRequestDTO.class))).thenReturn(response);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.roles[0]").value("ROLE_USER"));
    }

    @Test
    void register_ShouldReturnCreatedUser() throws Exception {
        RegisterRequestDTO request = new RegisterRequestDTO(
                "newuser@example.com",
                "Jane",
                "Smith",
                "Password123"
        );
        UserDto response = new UserDto(
                1L,
                "newuser@example.com",
                "Jane",
                "Smith",
                Set.of("ROLE_USER")
        );

        Mockito.when(userService.createUser(any(RegisterRequestDTO.class))).thenReturn(response);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("newuser@example.com"))
                .andExpect(jsonPath("$.firstName").value("Jane"))
                .andExpect(jsonPath("$.lastName").value("Smith"));
    }

    @Test
    void getCurrentUser_ShouldReturnUserDto() throws Exception {
        UserDto userDto = new UserDto(
                1L,
                "test@example.com",
                "John",
                "Doe",
                Set.of("ROLE_USER")
        );

        Mockito.when(userService.getMyProfile()).thenReturn(userDto);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/auth/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"));
    }
}

