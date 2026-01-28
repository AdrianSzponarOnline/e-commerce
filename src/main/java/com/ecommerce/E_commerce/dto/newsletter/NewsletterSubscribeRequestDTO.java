package com.ecommerce.E_commerce.dto.newsletter;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record NewsletterSubscribeRequestDTO(
        @NotBlank(message = "Email jest wymagany")
        @Email(message = "Nieprawidłowy format adresu e-mail")
        String email
) {
}
