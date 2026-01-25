package com.ecommerce.E_commerce.dto.page;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PageCreateDTO(
        @NotBlank(message = "Slug is required")
        @Size(max = 100, message = "Slug must not exceed 100 characters")
        String slug,
        
        @NotBlank(message = "Title is required")
        @Size(max = 100, message = "Title must not exceed 100 characters")
        String title,
        
        String content,
        
        Boolean isSystem
) {
}
