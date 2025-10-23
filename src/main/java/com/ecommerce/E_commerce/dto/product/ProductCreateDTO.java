package com.ecommerce.E_commerce.dto.product;

import com.ecommerce.E_commerce.dto.productattributevalue.ProductAttributeValueCreateDTO;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

public record ProductCreateDTO(
        @NotBlank(message = "Product name is required")
        @Size(max = 255, message = "Product name must not exceed 255 characters")
        String name,
        
        @NotBlank(message = "Product description is required")
        String description,
        
        @Size(max = 255, message = "Short description must not exceed 255 characters")
        String shortDescription,
        
        @NotNull(message = "Price is required")
        @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
        BigDecimal price,
        
        @NotNull(message = "VAT rate is required")
        @DecimalMin(value = "0.0", message = "VAT rate must be non-negative")
        BigDecimal vatRate,
        
        @DecimalMin(value = "0.0", message = "Shipping cost must be non-negative")
        BigDecimal shippingCost,
        
        @Size(max = 100, message = "Estimated delivery time must not exceed 100 characters")
        String estimatedDeliveryTime,
        
        String thumbnailUrl,
        
        @NotBlank(message = "SEO slug is required")
        @Size(max = 255, message = "SEO slug must not exceed 255 characters")
        String seoSlug,
        
        @NotNull(message = "Category ID is required")
        Long categoryId,
        
        Boolean isFeatured,
        
        List<ProductAttributeValueCreateDTO> attributeValues
) {
}
