package com.ecommerce.E_commerce.dto.product;

import com.ecommerce.E_commerce.dto.productattributevalue.ProductAttributeValueUpdateDTO;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

public record ProductUpdateDTO(
        @Size(max = 255, message = "Product name must not exceed 255 characters")
        @Pattern(regexp = "^[^<>]*$", message = "Product name must not contain HTML or script characters")
        String name,
        
        @Size(max = 10000, message = "Product description must not exceed 10000 characters")
        @Pattern(regexp = "^[^<>]*$", message = "Product description must not contain HTML or script characters")
        String description,
        
        @Size(max = 255, message = "Short description must not exceed 255 characters")
        @Pattern(regexp = "^[^<>]*$", message = "Short description must not contain HTML or script characters")
        String shortDescription,
        
        @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
        BigDecimal price,
        
        @DecimalMin(value = "0.0", message = "VAT rate must be non-negative")
        BigDecimal vatRate,
        
        @DecimalMin(value = "0.0", message = "Shipping cost must be non-negative")
        BigDecimal shippingCost,
        
        @Size(max = 100, message = "Estimated delivery time must not exceed 100 characters")
        @Pattern(regexp = "^[^<>]*$", message = "Estimated delivery time must not contain HTML or script characters")
        String estimatedDeliveryTime,
        
        @Size(max = 500, message = "Thumbnail URL must not exceed 500 characters")
        @Pattern(regexp = "^https?://[^<>\\s]+$", message = "Thumbnail URL must be a valid HTTP/HTTPS URL")
        String thumbnailUrl,
        
        @Size(max = 255, message = "SEO slug must not exceed 255 characters")
        @Pattern(regexp = "^[a-z0-9]+(?:-[a-z0-9]+)*$", message = "SEO slug must be a URL-safe slug: lowercase letters, numbers, and hyphens")
        String seoSlug,
        
        Long categoryId,
        
        Boolean isFeatured,
        
        Boolean isActive,
        
        List<ProductAttributeValueUpdateDTO> attributeValues
) {
}
