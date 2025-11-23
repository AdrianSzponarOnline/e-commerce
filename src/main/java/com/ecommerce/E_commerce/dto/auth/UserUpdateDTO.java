package com.ecommerce.E_commerce.dto.auth;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserUpdateDTO(
        @Size(max = 100, message = "First name must not exceed 100 characters")
        @Pattern(
                regexp = "^[\\p{L} '.-]*$",
                message = "First name can only contain letters, spaces, apostrophes, periods, and hyphens."
        )
        String firstName,

        @Size(max = 100, message = "Last name must not exceed 100 characters")
        @Pattern(
                regexp = "^[\\p{L} '.-]*$",
                message = "Last name can only contain letters, spaces, apostrophes, periods, and hyphens."
        )
        String lastName
) {}

