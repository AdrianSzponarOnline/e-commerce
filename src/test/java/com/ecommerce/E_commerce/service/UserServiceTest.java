package com.ecommerce.E_commerce.service;

import com.ecommerce.E_commerce.dto.auth.RegisterRequestDTO;
import com.ecommerce.E_commerce.dto.auth.UserDto;
import com.ecommerce.E_commerce.exception.EmailAlreadyExistsException;
import com.ecommerce.E_commerce.exception.RoleNotFountException;
import com.ecommerce.E_commerce.model.ERole;
import com.ecommerce.E_commerce.model.Role;
import com.ecommerce.E_commerce.model.User;
import com.ecommerce.E_commerce.repository.ConfirmationTokenRepository;
import com.ecommerce.E_commerce.repository.RoleRepository;
import com.ecommerce.E_commerce.repository.UserRepository;
import com.ecommerce.E_commerce.service.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailService emailService;

    @Mock
    private ConfirmationTokenRepository confirmationTokenRepository;

    private UserService userService;

    private User testUser;
    private Role userRole;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, roleRepository, passwordEncoder, emailService, confirmationTokenRepository);

        userRole = new Role(ERole.ROLE_USER);

        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setPassword("encodedPassword");
        testUser.setRoles(Set.of(userRole));
        testUser.setEnabled(true);
    }

    @Test
    void createUser_ShouldCreateUserSuccessfully() {
        // Given
        RegisterRequestDTO request = new RegisterRequestDTO(
                "newuser@example.com",
                "Jane",
                "Smith",
                "Password123"
        );

        User newUser = new User();
        newUser.setEmail("newuser@example.com");
        newUser.setFirstName("Jane");
        newUser.setLastName("Smith");
        newUser.setPassword("encodedPassword");
        newUser.setRoles(Set.of(userRole));
        newUser.setEnabled(true);

        when(userRepository.findByEmail("newuser@example.com")).thenReturn(Optional.empty());
        when(roleRepository.findByRole(ERole.ROLE_USER)).thenReturn(Optional.of(userRole));
        when(passwordEncoder.encode("Password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(newUser);

        // When
        UserDto result = userService.createUser(request);

        // Then
        assertNotNull(result);
        assertEquals("newuser@example.com", result.email());
        assertEquals("Jane", result.firstName());
        assertEquals("Smith", result.lastName());
        verify(userRepository).findByEmail("newuser@example.com");
        verify(roleRepository).findByRole(ERole.ROLE_USER);
        verify(passwordEncoder).encode("Password123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createUser_ShouldThrowException_WhenEmailAlreadyExists() {
        // Given
        RegisterRequestDTO request = new RegisterRequestDTO(
                "existing@example.com",
                "Jane",
                "Smith",
                "Password123"
        );
        when(userRepository.findByEmail("existing@example.com")).thenReturn(Optional.of(testUser));

        // When & Then
        EmailAlreadyExistsException exception = assertThrows(EmailAlreadyExistsException.class,
                () -> userService.createUser(request));
        assertEquals("existing@example.com", exception.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void createUser_ShouldThrowException_WhenRoleNotFound() {
        // Given
        RegisterRequestDTO request = new RegisterRequestDTO(
                "newuser@example.com",
                "Jane",
                "Smith",
                "Password123"
        );
        when(userRepository.findByEmail("newuser@example.com")).thenReturn(Optional.empty());
        when(roleRepository.findByRole(ERole.ROLE_USER)).thenReturn(Optional.empty());

        // When & Then
        RoleNotFountException exception = assertThrows(RoleNotFountException.class,
                () -> userService.createUser(request));
        assertTrue(exception.getMessage().contains("ROLE_USER not found"));
        verify(userRepository, never()).save(any());
    }

    @Test
    void loadUserByUsername_ShouldReturnUser_WhenUserExists() {
        // Given
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        // When
        User result = userService.loadUserByUsername("test@example.com");

        // Then
        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
        verify(userRepository).findByEmail("test@example.com");
    }

    @Test
    void loadUserByUsername_ShouldThrowException_WhenUserNotFound() {
        // Given
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // When & Then
        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class,
                () -> userService.loadUserByUsername("nonexistent@example.com"));
        assertTrue(exception.getMessage().contains("User details not found"));
    }

    @Test
    void loadUserByUsername_ShouldThrowException_WhenUserDisabled() {
        // Given
        testUser.setEnabled(false);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        // When & Then
        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class,
                () -> userService.loadUserByUsername("test@example.com"));
        assertTrue(exception.getMessage().contains("User account is disabled"));
    }
}

