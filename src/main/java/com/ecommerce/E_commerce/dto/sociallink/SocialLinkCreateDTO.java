package com.ecommerce.E_commerce.dto.sociallink;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SocialLinkCreateDTO(
        @NotBlank(message = "Platform name is required")
        @Size(max = 100, message = "Platform name must not exceed 100 characters")
        String platformName,
        
        @NotBlank(message = "URL is required")
        @Size(max = 500, message = "URL must not exceed 500 characters")
        String url,
        
        @Size(max = 100, message = "Icon code must not exceed 100 characters")
        String iconCode,
        
        Integer sortOrder
) {
}
