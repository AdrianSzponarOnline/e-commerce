package com.ecommerce.E_commerce.dto.newsletter;

import jakarta.validation.constraints.NotBlank;

public record NewsletterSendRequestDTO(
        @NotBlank(message = "Temat jest wymagany")
        String subject,

        @NotBlank(message = "Treść jest wymagana")
        String content
) {
}
