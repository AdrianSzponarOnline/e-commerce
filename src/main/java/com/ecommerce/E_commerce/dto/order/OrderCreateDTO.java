package com.ecommerce.E_commerce.dto.order;

import com.ecommerce.E_commerce.dto.orderitem.OrderItemCreateDTO;
import com.ecommerce.E_commerce.model.OrderStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record OrderCreateDTO(
        @NotNull(message = "Address ID is required")
        Long addressId,

        @NotEmpty(message = "Order must contain at least one item")
        @Valid
        List<OrderItemCreateDTO> items
) {
}

