package com.ecommerce.E_commerce.dto.address;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AddressCreateDTO(
        Long userId,

        @NotBlank(message = "Address line 1 is required")
        @Size(max = 255, message = "Address line 1 must not exceed 255 characters")
        @Pattern(regexp = "^[^<>]*$", message = "Address line 1 must not contain HTML or script characters")
        String line1,

        @Size(max = 255, message = "Address line 2 must not exceed 255 characters")
        @Pattern(regexp = "^[^<>]*$", message = "Address line 2 must not contain HTML or script characters")
        String line2,

        @NotBlank(message = "City is required")
        @Size(max = 100, message = "City must not exceed 100 characters")
        @Pattern(regexp = "^[^<>]*$", message = "City must not contain HTML or script characters")
        String city,

        @Size(max = 100, message = "Region must not exceed 100 characters")
        @Pattern(regexp = "^[^<>]*$", message = "Region must not contain HTML or script characters")
        String region,

        @NotBlank(message = "Postal code is required")
        @Size(max = 20, message = "Postal code must not exceed 20 characters")
        @Pattern(regexp = "^[^<>]*$", message = "Postal code must not contain HTML or script characters")
        String postalCode,

        @NotBlank(message = "Country is required")
        @Size(max = 100, message = "Country must not exceed 100 characters")
        @Pattern(regexp = "^[^<>]*$", message = "Country must not contain HTML or script characters")
        String country,

        Boolean isActive
) {}

