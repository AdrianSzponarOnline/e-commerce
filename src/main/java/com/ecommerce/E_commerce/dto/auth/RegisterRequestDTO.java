package com.ecommerce.E_commerce.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequestDTO(
        @NotBlank @Email
        String email,

        @NotBlank
        @Size(
                max = 100,
                message = "First name must be at least one character long and not longer than 100 characters.")
        @Pattern(
                regexp = "^[\\p{L} '.-]+$",
                message = "First name can only contain letters, spaces, apostrophes, periods, and hyphens."
        )
        String firstName,

        @NotBlank
        @Size(max = 100)
        @Pattern(

                regexp = "^[\\p{L} '.-]+$",
                message = "Last name can only contain letters, spaces, apostrophes, periods, and hyphens."
        )
        String lastName,

        @NotBlank
        @Size(min = 6 ,message = "Password must be at least 6 characters long")
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$",
                message = "Password must contain at least one uppercase letter, one lowercase letter and one number")
        String password
) {
}