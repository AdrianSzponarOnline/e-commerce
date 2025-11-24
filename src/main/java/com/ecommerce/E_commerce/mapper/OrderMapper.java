package com.ecommerce.E_commerce.mapper;

import com.ecommerce.E_commerce.dto.order.OrderCreateDTO;
import com.ecommerce.E_commerce.dto.order.OrderDTO;
import com.ecommerce.E_commerce.dto.order.OrderUpdateDTO;
import com.ecommerce.E_commerce.model.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",
        uses = {AddressMapper.class, OrderItemMapper.class, PaymentMapper.class},
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface OrderMapper {

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "address", source = "address")
    @Mapping(target = "items", source = "items")
    @Mapping(target = "payments", source = "payments")
    @Mapping(target = "status", expression = "java(order.getStatus() != null ? order.getStatus().name() : null)")
    OrderDTO toOrderDTO(Order order);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "address", ignore = true)
    @Mapping(target = "items", ignore = true)
    @Mapping(target = "totalAmount", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "payments", ignore = true)
    @Mapping(target = "isActive", constant = "true")
    Order toOrder(OrderCreateDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "address", ignore = true)
    @Mapping(target = "items", ignore = true)
    @Mapping(target = "totalAmount", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "payments", ignore = true)
    void updateOrderFromDTO(OrderUpdateDTO dto, @MappingTarget Order order);
}