package com.ecommerce.E_commerce.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SkuGenerator Tests")
class SkuGeneratorTest {

    private Product product;
    private Category category;
    private List<ProductAttributeValue> attributeValues;

    @BeforeEach
    void setUp() {
        // Setup category
        category = new Category();
        category.setId(1L);
        category.setName("Electronics");
        category.setSeoSlug("electronics");

        // Setup product
        product = new Product();
        product.setId(42L);
        product.setName("Laptop");
        product.setDescription("High-performance laptop");
        product.setPrice(new BigDecimal("999.99"));
        product.setVatRate(new BigDecimal("23.00"));
        product.setCategory(category);

        // Setup attribute values
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
            
            assertEquals("ELE-LAP--42", sku);
        }

        @Test
        @DisplayName("Should generate SKU with null ID")
        void shouldGenerateSkuWithNullId() {
            product.setId(null);
            
            String sku = SkuGenerator.generate(product);
            
            assertEquals("ELE-LAP--000", sku);
        }

        @Test
        @DisplayName("Should generate SKU with long category and product names")
        void shouldGenerateSkuWithLongNames() {
            category.setName("Electronics and Technology");
            product.setName("High-Performance Gaming Laptop");
            
            String sku = SkuGenerator.generate(product);
            
            assertEquals("ELE-HIG--42", sku);
        }

        @Test
        @DisplayName("Should generate SKU with special characters in names")
        void shouldGenerateSkuWithSpecialCharacters() {
            category.setName("Electronics & Technology!");
            product.setName("Laptop-15\" (Intel i7)");
            
            String sku = SkuGenerator.generate(product);
            
            assertEquals("ELE-LAP--42", sku);
        }
    }

    @Nested
    @DisplayName("SKU Generation with Key Attributes Tests")
    class SkuGenerationWithKeyAttributesTests {

        @Test
        @DisplayName("Should generate SKU with single key attribute")
        void shouldGenerateSkuWithSingleKeyAttribute() {
            CategoryAttribute keyAttribute = createKeyAttribute("Screen Size", "15\"");
            ProductAttributeValue attributeValue = createAttributeValue(keyAttribute, "15\"");
            attributeValues.add(attributeValue);
            
            String sku = SkuGenerator.generate(product);
            
            assertEquals("ELE-LAP-15-42", sku);
        }

        @Test
        @DisplayName("Should generate SKU with multiple key attributes")
        void shouldGenerateSkuWithMultipleKeyAttributes() {
            CategoryAttribute screenAttr = createKeyAttribute("Screen Size", "15\"");
            CategoryAttribute processorAttr = createKeyAttribute("Processor", "Intel i7");
            
            ProductAttributeValue screenValue = createAttributeValue(screenAttr, "15\"");
            ProductAttributeValue processorValue = createAttributeValue(processorAttr, "Intel i7");
            
            attributeValues.add(screenValue);
            attributeValues.add(processorValue);
            
            String sku = SkuGenerator.generate(product);
            
            assertEquals("ELE-LAP-15IN-42", sku);
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
            
            assertEquals("ELE-LAP-15-42", sku);
        }

        @Test
        @DisplayName("Should generate SKU with empty attribute values")
        void shouldGenerateSkuWithEmptyAttributeValues() {
            CategoryAttribute keyAttribute = createKeyAttribute("Screen Size", "");
            ProductAttributeValue attributeValue = createAttributeValue(keyAttribute, "");
            attributeValues.add(attributeValue);
            
            String sku = SkuGenerator.generate(product);
            
            assertEquals("ELE-LAP--42", sku);
        }

        @Test
        @DisplayName("Should generate SKU with null attribute values")
        void shouldGenerateSkuWithNullAttributeValues() {
            CategoryAttribute keyAttribute = createKeyAttribute("Screen Size", null);
            ProductAttributeValue attributeValue = createAttributeValue(keyAttribute, null);
            attributeValues.add(attributeValue);
            
            String sku = SkuGenerator.generate(product);
            
            assertEquals("ELE-LAP--42", sku);
        }
    }

    @Nested
    @DisplayName("Abbreviate Method Tests")
    class AbbreviateMethodTests {

        @Test
        @DisplayName("Should abbreviate normal text correctly")
        void shouldAbbreviateNormalTextCorrectly() {
            // Test through public method by checking SKU generation
            category.setName("Electronics");
            product.setName("Laptop");
            
            String sku = SkuGenerator.generate(product);
            
            assertTrue(sku.startsWith("ELE-LAP"));
        }

        @Test
        @DisplayName("Should handle empty and blank strings")
        void shouldHandleEmptyAndBlankStrings() {
            category.setName("");
            product.setName("   ");
            
            String sku = SkuGenerator.generate(product);
            
            assertEquals("---42", sku);
        }

        @Test
        @DisplayName("Should handle null strings")
        void shouldHandleNullStrings() {
            category.setName(null);
            product.setName(null);
            
            String sku = SkuGenerator.generate(product);
            
            assertEquals("---42", sku);
        }

        @Test
        @DisplayName("Should remove special characters and convert to uppercase")
        void shouldRemoveSpecialCharactersAndConvertToUppercase() {
            category.setName("electronics & technology!");
            product.setName("laptop-15\" (intel i7)");
            
            String sku = SkuGenerator.generate(product);
            
            assertEquals("ELE-LAP--42", sku);
        }

        @Test
        @DisplayName("Should handle very short strings")
        void shouldHandleVeryShortStrings() {
            category.setName("E");
            product.setName("L");
            
            String sku = SkuGenerator.generate(product);
            
            assertEquals("E-L--42", sku);
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle product with very long attribute values")
        void shouldHandleProductWithVeryLongAttributeValues() {
            CategoryAttribute keyAttribute = createKeyAttribute("Description", "Very long description that should be abbreviated");
            ProductAttributeValue attributeValue = createAttributeValue(keyAttribute, "Very long description that should be abbreviated");
            attributeValues.add(attributeValue);
            
            String sku = SkuGenerator.generate(product);
            
            assertEquals("ELE-LAP-VE-42", sku);
        }

        @Test
        @DisplayName("Should handle product with numeric attribute values")
        void shouldHandleProductWithNumericAttributeValues() {
            CategoryAttribute keyAttribute = createKeyAttribute("Model Number", "12345");
            ProductAttributeValue attributeValue = createAttributeValue(keyAttribute, "12345");
            attributeValues.add(attributeValue);
            
            String sku = SkuGenerator.generate(product);
            
            assertEquals("ELE-LAP-12-42", sku);
        }

        @Test
        @DisplayName("Should handle product with mixed alphanumeric attribute values")
        void shouldHandleProductWithMixedAlphanumericAttributeValues() {
            CategoryAttribute keyAttribute = createKeyAttribute("Model", "ABC123XYZ");
            ProductAttributeValue attributeValue = createAttributeValue(keyAttribute, "ABC123XYZ");
            attributeValues.add(attributeValue);
            
            String sku = SkuGenerator.generate(product);
            
            assertEquals("ELE-LAP-AB-42", sku);
        }

        @Test
        @DisplayName("Should generate consistent SKU format")
        void shouldGenerateConsistentSkuFormat() {
            String sku = SkuGenerator.generate(product);
            
            // Should contain exactly 3 dashes separating 4 parts
            String[] parts = sku.split("-");
            assertEquals(4, parts.length);
            
            // All parts should be uppercase
            assertEquals(sku, sku.toUpperCase());
            
            // Should not contain special characters except dashes
            assertTrue(sku.matches("^[A-Z0-9-]+$"));
        }
    }

    @Nested
    @DisplayName("Real-world Examples Tests")
    class RealWorldExamplesTests {

        @Test
        @DisplayName("Should generate SKU for electronics laptop")
        void shouldGenerateSkuForElectronicsLaptop() {
            category.setName("Electronics");
            product.setName("Laptop");
            
            // Add key attributes
            CategoryAttribute screenAttr = createKeyAttribute("Screen Size", "15\"");
            CategoryAttribute processorAttr = createKeyAttribute("Processor", "Intel i7");
            
            ProductAttributeValue screenValue = createAttributeValue(screenAttr, "15\"");
            ProductAttributeValue processorValue = createAttributeValue(processorAttr, "Intel i7");
            
            attributeValues.add(screenValue);
            attributeValues.add(processorValue);
            
            String sku = SkuGenerator.generate(product);
            
            assertEquals("ELE-LAP-15IN-42", sku);
        }

        @Test
        @DisplayName("Should generate SKU for clothing item")
        void shouldGenerateSkuForClothingItem() {
            category.setName("Clothing");
            product.setName("T-Shirt");
            product.setId(123L);
            
            CategoryAttribute sizeAttr = createKeyAttribute("Size", "Large");
            CategoryAttribute colorAttr = createKeyAttribute("Color", "Blue");
            
            ProductAttributeValue sizeValue = createAttributeValue(sizeAttr, "Large");
            ProductAttributeValue colorValue = createAttributeValue(colorAttr, "Blue");
            
            attributeValues.add(sizeValue);
            attributeValues.add(colorValue);
            
            String sku = SkuGenerator.generate(product);
            
            assertEquals("CLO-TSH-LABL-123", sku);
        }
    }

    // Helper methods
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
        
        // Add to category's attributes set
        category.getAttributes().add(categoryAttribute);
        
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
        
        // Add to category's attributes set
        category.getAttributes().add(categoryAttribute);
        
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
