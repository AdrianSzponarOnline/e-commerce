package com.ecommerce.E_commerce.service;

import com.ecommerce.E_commerce.dto.auth.AuthRequestDTO;
import com.ecommerce.E_commerce.dto.auth.AuthResponseDTO;
import com.ecommerce.E_commerce.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JWTService jwtService;

    private AuthService authService;

    private User testUser;

    @BeforeEach
    void setUp() {
        authService = new AuthService(authenticationManager, jwtService);

        testUser = new User();
        testUser.setEmail("test@example.com");
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setEnabled(true);
    }

    @Test
    void authenticate_ShouldReturnAuthResponse_WhenCredentialsAreValid() {
        // Given
        AuthRequestDTO request = new AuthRequestDTO("test@example.com", "password123");
        String token = "jwt-token";
        java.util.Set<GrantedAuthority> authorities = java.util.Set.of(new SimpleGrantedAuthority("ROLE_USER"));

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(testUser);
        @SuppressWarnings("unchecked")
        java.util.Collection<? extends GrantedAuthority> authoritiesCollection = authorities;
        when(authentication.getAuthorities()).thenReturn(authoritiesCollection);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtService.generateToken(testUser)).thenReturn(token);

        // When
        AuthResponseDTO response = authService.authenticate(request);

        // Then
        assertNotNull(response);
        assertEquals(token, response.token());
        assertEquals("test@example.com", response.email());
        assertEquals("John", response.firstName());
        assertEquals("Doe", response.lastName());
        assertEquals(1, response.roles().size());
        assertEquals("ROLE_USER", response.roles().get(0));
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService).generateToken(testUser);
    }

    @Test
    void authenticate_ShouldThrowIllegalArgumentException_WhenRequestIsNull() {
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> authService.authenticate(null));
        assertEquals("Email and password cannot be null or empty", exception.getMessage());
        verify(authenticationManager, never()).authenticate(any());
    }

    @Test
    void authenticate_ShouldThrowIllegalArgumentException_WhenEmailIsNull() {
        // Given
        AuthRequestDTO request = new AuthRequestDTO(null, "password123");

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> authService.authenticate(request));
        assertEquals("Email and password cannot be null or empty", exception.getMessage());
        verify(authenticationManager, never()).authenticate(any());
    }

    @Test
    void authenticate_ShouldThrowIllegalArgumentException_WhenPasswordIsNull() {
        // Given
        AuthRequestDTO request = new AuthRequestDTO("test@example.com", null);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> authService.authenticate(request));
        assertEquals("Email and password cannot be null or empty", exception.getMessage());
        verify(authenticationManager, never()).authenticate(any());
    }

    @Test
    void authenticate_ShouldThrowBadCredentialsException_WhenCredentialsAreInvalid() {
        // Given
        AuthRequestDTO request = new AuthRequestDTO("test@example.com", "wrongpassword");
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        // When & Then
        BadCredentialsException exception = assertThrows(BadCredentialsException.class,
                () -> authService.authenticate(request));
        assertEquals("Invalid email or password", exception.getMessage());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    void authenticate_ShouldThrowIllegalStateException_WhenPrincipalIsNotUserDetails() {
        // Given
        AuthRequestDTO request = new AuthRequestDTO("test@example.com", "password123");
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn("not a UserDetails");
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);

        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> authService.authenticate(request));
        assertEquals("Authentication principal is not UserDetails", exception.getMessage());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService, never()).generateToken(any());
    }
}

