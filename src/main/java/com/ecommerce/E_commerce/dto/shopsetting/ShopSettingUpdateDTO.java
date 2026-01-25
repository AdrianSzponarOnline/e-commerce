package com.ecommerce.E_commerce.dto.shopsetting;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ShopSettingUpdateDTO(
        @Size(max = 1000, message = "Value must not exceed 1000 characters")
        @Pattern(regexp = "^[^<>]*$", message = "Value cannot contain HTML tags (<, >)")
        String value,

        @Size(max = 500, message = "Description must not exceed 500 characters")
        @Pattern(regexp = "^[^<>]*$", message = "Description cannot contain HTML tags")
        String description
) {
}
