package com.ecommerce.E_commerce.dto.page;

import jakarta.validation.constraints.Size;

public record PageUpdateDTO(
        @Size(max = 100, message = "Slug must not exceed 100 characters")
        String slug,
        
        @Size(max = 100, message = "Title must not exceed 100 characters")
        String title,
        
        String content,
        
        Boolean isActive
) {
}
