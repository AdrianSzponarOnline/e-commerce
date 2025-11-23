package com.ecommerce.E_commerce.dto.auth;

import java.util.Set;

public record UserDto(
        Long id,
        String email,
        String firstName,
        String lastName,
        Set<String> roles) {
}
