package com.ecommerce.E_commerce.dto.category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;

public record CategoryUpdateDTO(
        @NotBlank
        @Size(max = 100)
        @Pattern(regexp = "^[^<>]*$", message = "must not contain HTML or script characters")
        String name,

        @Size(max = 255)
        @Pattern(regexp = "^[^<>]*$", message = "must not contain HTML or script characters")
        String description,

        @NotBlank
        @Size(max = 255)
        @Pattern(regexp = "^[a-z0-9]+(?:-[a-z0-9]+)*$", message = "must be a URL-safe slug: lowercase letters, numbers, and hyphens")
        String seoSlug,

        Long parentId,       // opcjonalny
        @NotNull
        Boolean isActive
) {

}
