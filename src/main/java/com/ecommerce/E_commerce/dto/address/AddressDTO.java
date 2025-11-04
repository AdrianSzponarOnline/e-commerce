package com.ecommerce.E_commerce.dto.address;

import java.time.Instant;

public record AddressDTO(
        Long id,
        Long userId,
        String line1,
        String line2,
        String city,
        String region,
        String postalCode,
        String country,
        Instant createdAt,
        Instant updatedAt,
        Instant deletedAt,
        Boolean isActive
) {}

