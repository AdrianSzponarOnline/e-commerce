package com.ecommerce.E_commerce.dto.contact;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ContactRequestDTO(
        @NotBlank(message = "Name is required.")
        String name,

        @Email(message = "Invalid email address format")
        @NotBlank(message = "Email address is required")
        String email,

        @NotBlank(message = "Message cannot be blank")
        String message
) {
}
