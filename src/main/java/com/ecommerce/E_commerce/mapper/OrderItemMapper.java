package com.ecommerce.E_commerce.mapper;

import com.ecommerce.E_commerce.dto.orderitem.OrderItemCreateDTO;
import com.ecommerce.E_commerce.dto.orderitem.OrderItemDTO;
import com.ecommerce.E_commerce.dto.orderitem.OrderItemUpdateDTO;
import com.ecommerce.E_commerce.model.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",
        uses = {ProductMapper.class},
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface OrderItemMapper {
    
    @Mapping(target = "orderId", source = "order.id")
    @Mapping(target = "product", source = "product")
    OrderItemDTO toOrderItemDTO(OrderItem orderItem);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "order", ignore = true)
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "price", ignore = true) 
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "isActive", constant = "true")
    OrderItem toOrderItem(OrderItemCreateDTO dto);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "order", ignore = true)
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "price", ignore = true) 
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    void updateOrderItemFromDTO(OrderItemUpdateDTO dto, @MappingTarget OrderItem orderItem);
}

