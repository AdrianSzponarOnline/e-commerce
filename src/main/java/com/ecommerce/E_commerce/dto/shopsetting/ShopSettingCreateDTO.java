package com.ecommerce.E_commerce.dto.shopsetting;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ShopSettingCreateDTO(
        @NotBlank(message = "Key is required")
        @Size(max = 100, message = "Key must not exceed 100 characters")
        @Pattern(regexp = "^[a-z0-9_]+$", message = "Key can only contain lowercase letters, numbers and underscores (e.g. shop_name)")
        String key,
        
        @Size(max = 1000, message = "Value must not exceed 1000 characters")
        @Pattern(regexp = "^[^<>]*$", message = "Value cannot contain HTML tags (<, >)") 
        String value,
        
        @Size(max = 500, message = "Description must not exceed 500 characters")
        @Pattern(regexp = "^[^<>]*$", message = "Description cannot contain HTML tags")
        String description
) {
}
