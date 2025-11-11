package com.ecommerce.E_commerce.dto.auth;

import java.util.List;

public record AuthResponseDTO(
        String token,
        String firstName,
        String lastName,
        String email,
        List<String> roles) {
}
