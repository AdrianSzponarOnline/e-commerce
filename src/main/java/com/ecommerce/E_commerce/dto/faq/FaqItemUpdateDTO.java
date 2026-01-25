package com.ecommerce.E_commerce.dto.faq;

import jakarta.validation.constraints.Size;

public record FaqItemUpdateDTO(
        @Size(max = 500, message = "Question must not exceed 500 characters")
        String question,
        
        @Size(max = 2000, message = "Answer must not exceed 2000 characters")
        String answer,
        
        Integer sortOrder,
        
        Boolean isActive
) {
}
