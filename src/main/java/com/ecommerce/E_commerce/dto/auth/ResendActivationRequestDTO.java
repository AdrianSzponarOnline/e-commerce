package com.ecommerce.E_commerce.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ResendActivationRequestDTO(
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        String email
) {
}
