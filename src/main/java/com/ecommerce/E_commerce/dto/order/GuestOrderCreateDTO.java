package com.ecommerce.E_commerce.dto.order;

import com.ecommerce.E_commerce.dto.orderitem.OrderItemCreateDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

public record GuestOrderCreateDTO(
        @NotEmpty(message = "Order must contain at least one item")
        @Valid
        List<OrderItemCreateDTO> items,

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        @Size(max = 255, message = "Email must not exceed 255 characters")
        String email,

        @NotBlank(message = "First name is required")
        @Size(max = 100, message = "First name must not exceed 100 characters")
        @Pattern(regexp = "^[^<>]*$", message = "First name must not contain HTML or script characters")
        String firstName,

        @Size(max = 100, message = "Last name must not exceed 100 characters")
        @Pattern(regexp = "^[^<>]*$", message = "Last name must not contain HTML or script characters")
        String lastName,

        @Size(max = 20, message = "Phone must not exceed 20 characters")
        @Pattern(regexp = "^[^<>]*$", message = "Phone must not contain HTML or script characters")
        String phone,

        @NotBlank(message = "Address line 1 is required")
        @Size(max = 255, message = "Address line 1 must not exceed 255 characters")
        @Pattern(regexp = "^[^<>]*$", message = "Address line 1 must not contain HTML or script characters")
        String addressLine1,

        @Size(max = 255, message = "Address line 2 must not exceed 255 characters")
        @Pattern(regexp = "^[^<>]*$", message = "Address line 2 must not contain HTML or script characters")
        String addressLine2,

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
        String country
) {
}
