package com.ecommerce.E_commerce.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ForgotPasswordRequestDTO(
        @NotBlank(message = "Email jest wymagany")
        @Email(message = "Nieprawidłowy format adresu email")
        String email
) {
}
