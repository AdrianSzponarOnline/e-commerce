package com.ecommerce.E_commerce.model;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.RandomStringUtils;

import java.util.Comparator;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Utility class responsible for generating SKU codes for products.
 * Format:
 * [CATEGORY_CODE]-[PRODUCT_CODE]-[KEY_ATTRIBUTES]-[ID]
 * Example:
 * ELE-LAP-15I7-42
 * → ELE (Electronics)
 * → LAP (Laptop)
 * → 15I7 (15", Intel i7)
 * → 42 (Product ID)
 */

@UtilityClass
public class SkuGenerator {
    public String generate(Product product) {
        if (product == null || product.getCategory() == null) {
            throw new IllegalArgumentException("Product or category cannot be null when generating SKU");
        }

        Category category = product.getCategory();
        Set<Attribute> keyAttributeDefinitions = category.getAttributes().stream()
                .filter(CategoryAttribute::isKeyAttribute)
                .map(CategoryAttribute::getAttribute)
                .collect(Collectors.toSet());

        String categoryCode = abbreviate(product.getCategory().getName(), 3);
        String productCode = abbreviate(product.getName(), 3);

        String keyAttributes = product.getAttributeValues().stream()
                .filter(value -> keyAttributeDefinitions.contains(value.getAttribute()))
                .sorted(Comparator.comparing(value -> value.getAttribute().getName()))
                .map(ProductAttributeValue::getAttributeValue)
                .map(value -> abbreviate(value, 2))
                .collect(Collectors.joining());

        String hashPart = RandomStringUtils.randomAlphanumeric(4).toUpperCase();
        return String.join("-", categoryCode, productCode, keyAttributes, hashPart).toUpperCase();
    }
    private String abbreviate(String text, int length) {
        if (text == null || text.isBlank()) return "";
        String clean = text.replaceAll("[^A-Za-z0-9]", "").toUpperCase();
        return clean.substring(0, Math.min(clean.length(), length));
    }
}
