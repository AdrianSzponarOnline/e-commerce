package com.ecommerce.E_commerce.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JWTServiceTest {

    @InjectMocks
    private JWTService jwtService;

    private UserDetails userDetails;
    private String secretKey;
    private long expirationTime = 86400000; // 24 hours

    @BeforeEach
    void setUp() {
        // Generate a valid secret key for testing
        SecretKey key = Keys.secretKeyFor(io.jsonwebtoken.SignatureAlgorithm.HS256);
        secretKey = Base64.getEncoder().encodeToString(key.getEncoded());

        ReflectionTestUtils.setField(jwtService, "secretKey", secretKey);
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", expirationTime);

        userDetails = mock(UserDetails.class);
        lenient().when(userDetails.getUsername()).thenReturn("test@example.com");
        java.util.Set<GrantedAuthority> authorities = java.util.Set.of(new SimpleGrantedAuthority("ROLE_USER"));
        lenient().doReturn(authorities).when(userDetails).getAuthorities();
    }

    @Test
    void generateToken_ShouldGenerateValidToken() {
        // When
        String token = jwtService.generateToken(userDetails);

        // Then
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void extractUsername_ShouldExtractUsernameFromToken() {
        // Given
        String token = jwtService.generateToken(userDetails);

        // When
        String username = jwtService.extractUsername(token);

        // Then
        assertEquals("test@example.com", username);
    }

    @Test
    void extractRoles_ShouldExtractRolesFromToken() {
        // Given
        String token = jwtService.generateToken(userDetails);

        // When
        List<String> roles = jwtService.extractRoles(token);

        // Then
        assertNotNull(roles);
        assertTrue(roles.contains("ROLE_USER"));
    }

    @Test
    void isTokenValid_ShouldReturnTrue_WhenTokenIsValid() {
        // Given
        String token = jwtService.generateToken(userDetails);

        // When
        boolean isValid = jwtService.isTokenValid(token, userDetails);

        // Then
        assertTrue(isValid);
    }

    @Test
    void isTokenValid_ShouldReturnFalse_WhenUsernameDoesNotMatch() {
        // Given
        String token = jwtService.generateToken(userDetails);
        UserDetails differentUser = mock(UserDetails.class);
        when(differentUser.getUsername()).thenReturn("different@example.com");
        java.util.Set<GrantedAuthority> differentAuthorities = java.util.Set.of(new SimpleGrantedAuthority("ROLE_USER"));
        lenient().doReturn(differentAuthorities).when(differentUser).getAuthorities();

        // When
        boolean isValid = jwtService.isTokenValid(token, differentUser);

        // Then
        assertFalse(isValid);
    }

    @Test
    void getExpirationTime_ShouldReturnExpirationTime() {
        // When
        long expiration = jwtService.getExpirationTime();

        // Then
        assertEquals(expirationTime, expiration);
    }

    @Test
    void generateToken_WithExtraClaims_ShouldIncludeClaimsInToken() {
        // Given
        java.util.Map<String, Object> extraClaims = new java.util.HashMap<>();
        extraClaims.put("customClaim", "customValue");

        // When
        String token = jwtService.generateToken(extraClaims, userDetails);

        // Then
        assertNotNull(token);
        // Verify token can be parsed
        Claims claims = Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(Base64.getDecoder().decode(secretKey)))
                .build()
                .parseSignedClaims(token)
                .getPayload();
        assertEquals("customValue", claims.get("customClaim"));
    }
}

