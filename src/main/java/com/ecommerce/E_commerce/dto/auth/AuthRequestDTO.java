package com.ecommerce.E_commerce.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AuthRequestDTO(
        @NotBlank(message = "Email is required") @Email(message = "Email should be valid") @Size(max = 255) @Pattern(regexp = "^(?!.*[<>]).*$", message = "Email contains invalid characters") String email,
        @NotBlank(message = "Password is required") @Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters") String password
) {
}
