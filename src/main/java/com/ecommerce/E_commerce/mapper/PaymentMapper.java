package com.ecommerce.E_commerce.mapper;

import com.ecommerce.E_commerce.dto.payment.PaymentCreateDTO;
import com.ecommerce.E_commerce.dto.payment.PaymentDTO;
import com.ecommerce.E_commerce.dto.payment.PaymentUpdateDTO;
import com.ecommerce.E_commerce.model.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface PaymentMapper {
    
    @Mapping(target = "orderId", source = "order.id")
    @Mapping(target = "method", expression = "java(payment.getMethod() != null ? payment.getMethod().name() : null)")
    @Mapping(target = "status", expression = "java(payment.getStatus() != null ? payment.getStatus().name() : null)")
    PaymentDTO toPaymentDTO(Payment payment);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "order", ignore = true)
    @Mapping(target = "method", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "transactionDate", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "isActive", constant = "true")
    Payment toPayment(PaymentCreateDTO dto);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "order", ignore = true)
    @Mapping(target = "method", ignore = true)
    @Mapping(target = "amount", ignore = true)
    @Mapping(target = "transactionDate", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    void updatePaymentFromDTO(PaymentUpdateDTO dto, @MappingTarget Payment payment);
}

