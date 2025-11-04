package com.ecommerce.E_commerce.dto.address;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AddressUpdateDTO(
        @Size(max = 255, message = "Address line 1 must not exceed 255 characters")
        @Pattern(regexp = "^[^<>]*$", message = "Address line 1 must not contain HTML or script characters")
        String line1,

        @Size(max = 255, message = "Address line 2 must not exceed 255 characters")
        @Pattern(regexp = "^[^<>]*$", message = "Address line 2 must not contain HTML or script characters")
        String line2,

        @Size(max = 100, message = "City must not exceed 100 characters")
        @Pattern(regexp = "^[^<>]*$", message = "City must not contain HTML or script characters")
        String city,

        @Size(max = 100, message = "Region must not exceed 100 characters")
        @Pattern(regexp = "^[^<>]*$", message = "Region must not contain HTML or script characters")
        String region,

        @Size(max = 20, message = "Postal code must not exceed 20 characters")
        @Pattern(regexp = "^[^<>]*$", message = "Postal code must not contain HTML or script characters")
        String postalCode,

        @Size(max = 100, message = "Country must not exceed 100 characters")
        @Pattern(regexp = "^[^<>]*$", message = "Country must not contain HTML or script characters")
        String country,

        Boolean isActive
) {}

