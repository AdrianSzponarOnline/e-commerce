package com.ecommerce.E_commerce.controller;
import com.ecommerce.E_commerce.dto.auth.*;
import com.ecommerce.E_commerce.model.User;
import com.ecommerce.E_commerce.service.AuthService;
import com.ecommerce.E_commerce.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;



@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private final AuthService authService;
    private final UserService userService;

    @Autowired
    public AuthController(AuthService authService, UserService userService) {
        this.authService = authService;
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody AuthRequestDTO request) {
        logger.info("POST /api/auth/login - Attempting login for email: {}", request.email());
        AuthResponseDTO response = authService.authenticate(request);
        logger.info("POST /api/auth/login - Login successful for email: {}", request.email());
        return ResponseEntity.ok(response);
    }
    @PreAuthorize("hasRole('OWNER') or (hasRole('USER'))")
    @PutMapping("/update")
    public ResponseEntity<UserDto>  update(
            @Valid @RequestBody UserUpdateDTO request,
            @AuthenticationPrincipal User user) {
        logger.info("PUT /api/auth/update - Attempting to update user: {}", user.getId());
        UserDto updatedUser = userService.updateUser(user.getId(), request);
        logger.info("PUT /api/auth/update - User successfully updated for id: {}", user.getId());
        return ResponseEntity.ok(updatedUser);
    }

    @GetMapping("/me")
    public ResponseEntity<UserDto> getCurrentUser() {
        logger.debug("GET /api/auth/me - Getting current user profile");
        UserDto user = userService.getMyProfile();
        logger.debug("GET /api/auth/me - Profile retrieved for user: {}", user.email());
        return ResponseEntity.ok(user);
    }

    @PostMapping("/register")
    public ResponseEntity<UserDto> register(@Valid @RequestBody RegisterRequestDTO request) {
        logger.info("POST /api/auth/register - Registering new user with email: {}", request.email());
        UserDto createdUser = userService.createUser(request);
        logger.info("POST /api/auth/register - User registered successfully: userId={}, email={}", createdUser.id(), createdUser.email());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }
    @PostMapping("/activate")
    public ResponseEntity<String> activate(@RequestParam String token) {
        logger.info("POST /api/auth/activate - Attempting account activation with token");
        userService.activateAccount(token);
        logger.info("POST /api/auth/activate - Account activated successfully");
        return ResponseEntity.ok("Konto aktywowane pomyślnie");
    }
    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@Valid @RequestBody ForgotPasswordRequestDTO request) {
        logger.info("POST /api/auth/forgot-password - Password reset requested for email: {}", request.email());
        userService.forgotPassword(request.email());
        logger.info("POST /api/auth/forgot-password - Password reset link sent (if email exists): {}", request.email());
        return ResponseEntity.ok("Link resetujący wysłany (jeśli email istnieje)");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@Valid @RequestBody ResetPasswordRequestDTO request) {
        logger.info("POST /api/auth/reset-password - Attempting password reset");
        userService.resetPassword(request.token(), request.newPassword());
        logger.info("POST /api/auth/reset-password - Password reset successful");
        return ResponseEntity.ok("Hasło zmienione pomyślnie");
    }
}
