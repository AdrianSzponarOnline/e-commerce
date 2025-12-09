package com.ecommerce.E_commerce.controller;
import com.ecommerce.E_commerce.dto.auth.*;
import com.ecommerce.E_commerce.service.AuthService;
import com.ecommerce.E_commerce.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    @Autowired
    public AuthController(AuthService authService, UserService userService) {
        this.authService = authService;
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody AuthRequestDTO request) {
        return ResponseEntity.ok(authService.authenticate(request));
    }

    @GetMapping("/me")
    public ResponseEntity<UserDto> getCurrentUser() {
        return ResponseEntity.ok(userService.getMyProfile());
    }

    @PostMapping("/register")
    public ResponseEntity<UserDto> register(@Valid @RequestBody RegisterRequestDTO request) {
        UserDto createdUser = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }
    @PostMapping("/activate")
    public ResponseEntity<String> activate(@RequestParam String token) {
        userService.activateAccount(token);
        return ResponseEntity.ok("Konto aktywowane pomyślnie");
    }
    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@Valid @RequestBody ForgotPasswordRequestDTO request) {
        userService.forgotPassword(request.email());
        return ResponseEntity.ok("Link resetujący wysłany (jeśli email istnieje)");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@Valid @RequestBody ResetPasswordRequestDTO request) {
        userService.resetPassword(request.token(), request.newPassword());
        return ResponseEntity.ok("Hasło zmienione pomyślnie");
    }
}
