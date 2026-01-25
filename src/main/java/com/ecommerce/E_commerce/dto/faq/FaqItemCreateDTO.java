package com.ecommerce.E_commerce.dto.faq;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record FaqItemCreateDTO(
        @NotBlank(message = "Question is required")
        @Size(max = 500, message = "Question must not exceed 500 characters")
        String question,
        
        @NotBlank(message = "Answer is required")
        @Size(max = 2000, message = "Answer must not exceed 2000 characters")
        String answer,
        
        Integer sortOrder
) {
}
