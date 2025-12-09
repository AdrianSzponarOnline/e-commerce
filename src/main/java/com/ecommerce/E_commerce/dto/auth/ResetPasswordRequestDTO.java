package com.ecommerce.E_commerce.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequestDTO(
        @NotBlank(message = "Token is required")
        String token,

        @NotBlank
        @Size(min = 6 ,message = "Password must be at least 6 characters long")
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$",
                message = "Password must contain at least one uppercase letter, one lowercase letter and one number")
        String newPassword
) {
}
