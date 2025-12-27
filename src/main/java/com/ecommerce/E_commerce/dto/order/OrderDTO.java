package com.ecommerce.E_commerce.dto.order;

import com.ecommerce.E_commerce.dto.address.AddressDTO;
import com.ecommerce.E_commerce.dto.orderitem.OrderItemDTO;
import com.ecommerce.E_commerce.dto.payment.PaymentDTO;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrderDTO(
        Long id,
        Long userId,
        String firstName,
        String lastName,
        AddressDTO address,
        String status,
        BigDecimal totalAmount,
        List<OrderItemDTO> items,
        List<PaymentDTO> payments,
        Instant createdAt,
        Instant updatedAt,
        Boolean isActive
) {
}

