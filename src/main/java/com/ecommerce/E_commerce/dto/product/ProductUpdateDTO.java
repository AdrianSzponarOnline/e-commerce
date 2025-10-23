package com.ecommerce.E_commerce.dto.product;

import com.ecommerce.E_commerce.dto.productattributevalue.ProductAttributeValueUpdateDTO;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

public record ProductUpdateDTO(
        @Size(max = 255, message = "Product name must not exceed 255 characters")
        String name,
        
        String description,
        
        @Size(max = 255, message = "Short description must not exceed 255 characters")
        String shortDescription,
        
        @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
        BigDecimal price,
        
        @DecimalMin(value = "0.0", message = "VAT rate must be non-negative")
        BigDecimal vatRate,
        
        @DecimalMin(value = "0.0", message = "Shipping cost must be non-negative")
        BigDecimal shippingCost,
        
        @Size(max = 100, message = "Estimated delivery time must not exceed 100 characters")
        String estimatedDeliveryTime,
        
        String thumbnailUrl,
        
        @Size(max = 255, message = "SEO slug must not exceed 255 characters")
        String seoSlug,
        
        Long categoryId,
        
        Boolean isFeatured,
        
        Boolean isActive,
        
        List<ProductAttributeValueUpdateDTO> attributeValues
) {
}
