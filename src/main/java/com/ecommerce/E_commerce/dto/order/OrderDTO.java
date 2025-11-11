package com.ecommerce.E_commerce.dto.order;

import com.ecommerce.E_commerce.dto.address.AddressDTO;
import com.ecommerce.E_commerce.dto.orderitem.OrderItemDTO;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrderDTO(
        Long id,
        Long userId,
        AddressDTO address,
        String status,
        BigDecimal totalAmount,
        List<OrderItemDTO> items,
        Instant createdAt,
        Instant updatedAt,
        Boolean isActive
) {
}

