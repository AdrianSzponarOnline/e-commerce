package com.ecommerce.E_commerce.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet; // Dodano
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SkuGenerator Tests")
class SkuGeneratorTest {

    private Product product;
    private Category category;
    private List<ProductAttributeValue> attributeValues;

    @BeforeEach
    void setUp() {
        category = new Category();
        category.setId(1L);
        category.setName("Electronics");
        category.setSeoSlug("electronics");
        category.setAttributes(new HashSet<>());

        product = new Product();
        product.setId(42L);
        product.setName("Laptop");
        product.setDescription("High-performance laptop");
        product.setPrice(new BigDecimal("999.99"));
        product.setVatRate(new BigDecimal("23.00"));
        product.setCategory(category);

        attributeValues = new ArrayList<>();
        product.setAttributeValues(attributeValues);
    }

    @Nested
    @DisplayName("Validation Tests")
    class ValidationTests {

        @Test
        @DisplayName("Should throw exception when product is null")
        void shouldThrowExceptionWhenProductIsNull() {
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> SkuGenerator.generate(null)
            );
            assertEquals("Product or category cannot be null when generating SKU", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when category is null")
        void shouldThrowExceptionWhenCategoryIsNull() {
            product.setCategory(null);

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> SkuGenerator.generate(product)
            );
            assertEquals("Product or category cannot be null when generating SKU", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Basic SKU Generation Tests")
    class BasicSkuGenerationTests {

        @Test
        @DisplayName("Should generate basic SKU without attributes")
        void shouldGenerateBasicSkuWithoutAttributes() {
            String sku = SkuGenerator.generate(product);

            // Format: ELE-LAP--HASH
            assertTrue(sku.startsWith("ELE-LAP--"));
            String[] parts = sku.split("-");
            assertEquals(4, parts.length);
            assertEquals("ELE", parts[0]);
            assertEquals("LAP", parts[1]);
            assertEquals("", parts[2]); // puste atrybuty
            assertEquals(4, parts[3].length());
            assertTrue(parts[3].matches("[A-Z0-9]{4}"), "Hash should be alphanumeric");
        }
    }

    @Nested
    @DisplayName("SKU Generation with Key Attributes Tests")
    class SkuGenerationWithKeyAttributesTests {

        @Test
        @DisplayName("Should generate SKU with multiple key attributes sorted alphabetically")
        void shouldGenerateSkuWithMultipleKeyAttributes() {

            CategoryAttribute screenAttr = createKeyAttribute("Screen Size", "15\"");
            CategoryAttribute processorAttr = createKeyAttribute("Processor", "Intel i7");

            ProductAttributeValue screenValue = createAttributeValue(screenAttr, "15\"");
            ProductAttributeValue processorValue = createAttributeValue(processorAttr, "Intel i7");

            attributeValues.add(screenValue);
            attributeValues.add(processorValue);

            // When
            String sku = SkuGenerator.generate(product);

            // Then
            String[] parts = sku.split("-");
            assertEquals("ELE", parts[0]);
            assertEquals("LAP", parts[1]);

            assertEquals("IN15", parts[2], "Attributes should be sorted by name: Processor(IN) then Screen(15)");

            assertTrue(parts[3].matches("[A-Z0-9]{4}"));
        }

        @Test
        @DisplayName("Should generate SKU with mixed key and non-key attributes")
        void shouldGenerateSkuWithMixedAttributes() {
            CategoryAttribute keyAttribute = createKeyAttribute("Screen Size", "15\"");
            CategoryAttribute nonKeyAttribute = createNonKeyAttribute("Color", "Black");

            ProductAttributeValue keyValue = createAttributeValue(keyAttribute, "15\"");
            ProductAttributeValue nonKeyValue = createAttributeValue(nonKeyAttribute, "Black");

            attributeValues.add(keyValue);
            attributeValues.add(nonKeyValue);

            String sku = SkuGenerator.generate(product);

            String[] parts = sku.split("-");
            assertEquals("15", parts[2], "Only key attribute (15) should be present");
        }
    }

    @Nested
    @DisplayName("Abbreviate Method Tests")
    class AbbreviateMethodTests {


        @Test
        @DisplayName("Should handle empty and blank strings")
        void shouldHandleEmptyAndBlankStrings() {
            category.setName("");
            product.setName("   ");

            String sku = SkuGenerator.generate(product);

            String[] parts = sku.split("-");
            assertEquals(4, parts.length);
            assertEquals("", parts[0]);
            assertEquals("", parts[1]);
            assertEquals("", parts[2]);
        }
    }



    private CategoryAttribute createKeyAttribute(String name, String value) {
        Attribute attribute = new Attribute();
        attribute.setId(1L);
        attribute.setName(name);
        attribute.setType(CategoryAttributeType.TEXT);

        CategoryAttribute categoryAttribute = new CategoryAttribute();
        categoryAttribute.setId(1L);
        categoryAttribute.setCategory(category);
        categoryAttribute.setAttribute(attribute);
        categoryAttribute.setKeyAttribute(true);

        if (category.getAttributes() != null) {
            category.getAttributes().add(categoryAttribute);
        }

        return categoryAttribute;
    }

    private CategoryAttribute createNonKeyAttribute(String name, String value) {
        Attribute attribute = new Attribute();
        attribute.setId(2L);
        attribute.setName(name);
        attribute.setType(CategoryAttributeType.TEXT);

        CategoryAttribute categoryAttribute = new CategoryAttribute();
        categoryAttribute.setId(2L);
        categoryAttribute.setCategory(category);
        categoryAttribute.setAttribute(attribute);
        categoryAttribute.setKeyAttribute(false);

        if (category.getAttributes() != null) {
            category.getAttributes().add(categoryAttribute);
        }

        return categoryAttribute;
    }

    private ProductAttributeValue createAttributeValue(CategoryAttribute categoryAttribute, String value) {
        ProductAttributeValue attributeValue = new ProductAttributeValue();
        attributeValue.setId(1L);
        attributeValue.setProduct(product);
        attributeValue.setAttribute(categoryAttribute.getAttribute());
        attributeValue.setValue(value);
        return attributeValue;
    }
}