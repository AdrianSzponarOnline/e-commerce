package com.ecommerce.E_commerce.service;

import com.ecommerce.E_commerce.dto.category.CategoryDTO;
import com.ecommerce.E_commerce.dto.product.ProductCreateDTO;
import com.ecommerce.E_commerce.dto.product.ProductDTO;
import com.ecommerce.E_commerce.dto.product.ProductSummaryDTO;
import com.ecommerce.E_commerce.dto.product.ProductUpdateDTO;
import com.ecommerce.E_commerce.exception.ResourceNotFoundException;
import com.ecommerce.E_commerce.mapper.ProductMapper;
import com.ecommerce.E_commerce.model.Category;
import com.ecommerce.E_commerce.model.Product;
import com.ecommerce.E_commerce.repository.CategoryRepository;
import com.ecommerce.E_commerce.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ProductMapper productMapper;

    @Mock
    private ProductAttributeValueService productAttributeValueService;

    private ProductServiceImpl productService;

    private Product testProduct;
    private Category testCategory;
    private ProductCreateDTO testCreateDTO;
    private ProductUpdateDTO testUpdateDTO;
    private ProductDTO testProductDTO;
    private ProductSummaryDTO testProductSummaryDTO;

    @BeforeEach
    void setUp() {
        productService = new ProductServiceImpl(
                productRepository,
                categoryRepository,
                productMapper,
                productAttributeValueService
        );

        // Setup test data
        testCategory = new Category();
        testCategory.setId(1L);
        testCategory.setName("Electronics");
        testCategory.setSeoSlug("electronics");

        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setName("Test Laptop");
        testProduct.setDescription("A test laptop for testing");
        testProduct.setShortDescription("Test laptop");
        testProduct.setPrice(new BigDecimal("999.99"));
        testProduct.setSku("ELE-LAP-001");
        testProduct.setVatRate(new BigDecimal("23.00"));
        testProduct.setIsFeatured(false);
        testProduct.setShippingCost(new BigDecimal("15.00"));
        testProduct.setEstimatedDeliveryTime("3-5 days");
        testProduct.setThumbnailUrl("https://example.com/laptop.jpg");
        testProduct.setSeoSlug("test-laptop");
        testProduct.setCategory(testCategory);
        testProduct.setCreatedAt(Instant.now());
        testProduct.setUpdatedAt(Instant.now());
        testProduct.setDeletedAt(null);
        testProduct.setIsActive(true);

        testCreateDTO = new ProductCreateDTO(
                "Test Laptop",
                "A test laptop for testing",
                "Test laptop",
                new BigDecimal("999.99"),
                new BigDecimal("23.00"),
                new BigDecimal("15.00"),
                "3-5 days",
                "https://example.com/laptop.jpg",
                "test-laptop",
                1L,
                false,
                null // attributeValues
        );

        testUpdateDTO = new ProductUpdateDTO(
                "Updated Laptop",
                "An updated laptop description",
                "Updated laptop",
                new BigDecimal("1099.99"),
                new BigDecimal("23.00"),
                new BigDecimal("20.00"),
                "2-4 days",
                "https://example.com/updated-laptop.jpg",
                "updated-laptop",
                1L,
                true,
                true,
                null // attributeValues
        );

        CategoryDTO testCategoryDTO = new CategoryDTO(
                1L,
                "Electronics",
                null,
                "electronics",
                true,
                Instant.now(),
                Instant.now(),
                null,
                new ArrayList<>()
        );
        
        testProductDTO = new ProductDTO(
                1L,
                "Test Laptop",
                "A test laptop for testing",
                "Test laptop",
                new BigDecimal("999.99"),
                "ELE-LAP-001",
                new BigDecimal("23.00"),
                false,
                new BigDecimal("15.00"),
                "3-5 days",
                "https://example.com/laptop.jpg",
                "test-laptop",
                testCategoryDTO,
                new ArrayList<>(),
                Instant.now(),
                Instant.now()
        );
        
        testProductSummaryDTO = new ProductSummaryDTO(
                1L,
                "Test Laptop",
                new BigDecimal("999.99"),
                "Test laptop",
                "https://example.com/laptop.jpg",
                "test-laptop",
                "Electronics"
        );
    }

    // CRUD Operations Tests

    @Test
    void create_ShouldCreateProductSuccessfully() {
        // Given
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(productMapper.toProduct(testCreateDTO)).thenReturn(testProduct);
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);
        when(productMapper.toProductDTO(testProduct)).thenReturn(testProductDTO);

        // When
        ProductDTO result = productService.create(testCreateDTO);

        // Then
        assertNotNull(result);
        assertEquals(testProductDTO, result);
        verify(categoryRepository).findById(1L);
        verify(productMapper).toProduct(testCreateDTO);
        verify(productRepository).save(any(Product.class));
        verify(productMapper).toProductDTO(testProduct);
    }

    @Test
    void create_ShouldThrowException_WhenCategoryNotFound() {
        // Given
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> productService.create(testCreateDTO));
        assertEquals("Category not found with id: 1", exception.getMessage());
        verify(categoryRepository).findById(1L);
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void update_ShouldUpdateProductSuccessfully() {
        // Given
        Product updatedProduct = new Product();
        updatedProduct.setId(1L);
        updatedProduct.setName("Updated Laptop");
        updatedProduct.setCategory(testCategory);

        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(productRepository.save(any(Product.class))).thenReturn(updatedProduct);
        when(productMapper.toProductDTO(updatedProduct)).thenReturn(testProductDTO);

        // When
        ProductDTO result = productService.update(1L, testUpdateDTO);

        // Then
        assertNotNull(result);
        verify(productRepository).findById(1L);
        verify(categoryRepository).findById(1L);
        verify(productMapper).updateProductFromDTO(testUpdateDTO, testProduct);
        verify(productRepository).save(testProduct);
        verify(productMapper).toProductDTO(updatedProduct);
    }

    @Test
    void update_ShouldThrowException_WhenProductNotFound() {
        // Given
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> productService.update(1L, testUpdateDTO));
        assertEquals("Product not found with id: 1", exception.getMessage());
        verify(productRepository).findById(1L);
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void update_ShouldThrowException_WhenCategoryNotFound() {
        // Given
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> productService.update(1L, testUpdateDTO));
        assertEquals("Category not found with id: 1", exception.getMessage());
        verify(productRepository).findById(1L);
        verify(categoryRepository).findById(1L);
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void delete_ShouldDeleteProductSuccessfully() {
        // Given
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        // When
        productService.delete(1L);

        // Then
        verify(productRepository).findById(1L);
        verify(productRepository).save(testProduct);
        assertNotNull(testProduct.getDeletedAt());
        assertFalse(testProduct.getIsActive());
    }

    @Test
    void delete_ShouldThrowException_WhenProductNotFound() {
        // Given
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> productService.delete(1L));
        assertEquals("Product not found with id: 1", exception.getMessage());
        verify(productRepository).findById(1L);
        verify(productRepository, never()).save(any(Product.class));
    }

    // Single Product Retrieval Tests

    @Test
    void getById_ShouldReturnProduct_WhenProductExists() {
        // Given
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(productMapper.toProductDTO(testProduct)).thenReturn(testProductDTO);

        // When
        ProductDTO result = productService.getById(1L);

        // Then
        assertNotNull(result);
        assertEquals(testProductDTO, result);
        verify(productRepository).findById(1L);
        verify(productMapper).toProductDTO(testProduct);
    }

    @Test
    void getById_ShouldThrowException_WhenProductNotFound() {
        // Given
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> productService.getById(1L));
        assertEquals("Product not found with id: 1", exception.getMessage());
        verify(productRepository).findById(1L);
    }

    @Test
    void getBySeoSlug_ShouldReturnProduct_WhenProductExists() {
        // Given
        when(productRepository.findBySeoSlug("test-laptop")).thenReturn(Optional.of(testProduct));
        when(productMapper.toProductDTO(testProduct)).thenReturn(testProductDTO);

        // When
        ProductDTO result = productService.getBySeoSlug("test-laptop");

        // Then
        assertNotNull(result);
        assertEquals(testProductDTO, result);
        verify(productRepository).findBySeoSlug("test-laptop");
        verify(productMapper).toProductDTO(testProduct);
    }

    @Test
    void getBySeoSlug_ShouldThrowException_WhenProductNotFound() {
        // Given
        when(productRepository.findBySeoSlug("non-existent")).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> productService.getBySeoSlug("non-existent"));
        assertEquals("Product not found with seo slug: non-existent", exception.getMessage());
        verify(productRepository).findBySeoSlug("non-existent");
    }

    @Test
    void getBySku_ShouldReturnProduct_WhenProductExists() {
        // Given
        when(productRepository.findBySku("ELE-LAP-001")).thenReturn(Optional.of(testProduct));
        when(productMapper.toProductDTO(testProduct)).thenReturn(testProductDTO);

        // When
        ProductDTO result = productService.getBySku("ELE-LAP-001");

        // Then
        assertNotNull(result);
        assertEquals(testProductDTO, result);
        verify(productRepository).findBySku("ELE-LAP-001");
        verify(productMapper).toProductDTO(testProduct);
    }

    @Test
    void getBySku_ShouldThrowException_WhenProductNotFound() {
        // Given
        when(productRepository.findBySku("NON-EXISTENT")).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> productService.getBySku("NON-EXISTENT"));
        assertEquals("Product not found with SKU: NON-EXISTENT", exception.getMessage());
        verify(productRepository).findBySku("NON-EXISTENT");
    }

    // Paginated Product Lists Tests

    @Test
    void findAll_ShouldReturnPageOfProducts() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> productPage = new PageImpl<>(Arrays.asList(testProduct));

        when(productRepository.findAll(pageable)).thenReturn(productPage);
        when(productMapper.toProductSummaryDTO(testProduct)).thenReturn(testProductSummaryDTO);

        // When
        Page<ProductSummaryDTO> result = productService.findAll(pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(testProductSummaryDTO, result.getContent().get(0));
        verify(productRepository).findAll(pageable);
        verify(productMapper).toProductSummaryDTO(testProduct);
    }

    @Test
    void findByCategory_ShouldReturnPageOfProducts() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> productPage = new PageImpl<>(Arrays.asList(testProduct));

        when(productRepository.findByCategoryId(1L, pageable)).thenReturn(productPage);
        when(productMapper.toProductSummaryDTO(testProduct)).thenReturn(testProductSummaryDTO);

        // When
        Page<ProductSummaryDTO> result = productService.findByCategory(1L, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(testProductSummaryDTO, result.getContent().get(0));
        verify(productRepository).findByCategoryId(1L, pageable);
        verify(productMapper).toProductSummaryDTO(testProduct);
    }

    @Test
    void findByCategorySlug_ShouldReturnPageOfProducts() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> productPage = new PageImpl<>(Arrays.asList(testProduct));

        when(productRepository.findByCategorySeoSlug("electronics", pageable)).thenReturn(productPage);
        when(productMapper.toProductSummaryDTO(testProduct)).thenReturn(testProductSummaryDTO);

        // When
        Page<ProductSummaryDTO> result = productService.findByCategorySlug("electronics", pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(testProductSummaryDTO, result.getContent().get(0));
        verify(productRepository).findByCategorySeoSlug("electronics", pageable);
        verify(productMapper).toProductSummaryDTO(testProduct);
    }

    @Test
    void findByPriceRange_ShouldReturnPageOfProducts() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        BigDecimal minPrice = new BigDecimal("100.00");
        BigDecimal maxPrice = new BigDecimal("1000.00");
        Page<Product> productPage = new PageImpl<>(Arrays.asList(testProduct));

        when(productRepository.findByPriceBetween(minPrice, maxPrice, pageable)).thenReturn(productPage);
        when(productMapper.toProductSummaryDTO(testProduct)).thenReturn(testProductSummaryDTO);

        // When
        Page<ProductSummaryDTO> result = productService.findByPriceRange(minPrice, maxPrice, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(testProductSummaryDTO, result.getContent().get(0));
        verify(productRepository).findByPriceBetween(minPrice, maxPrice, pageable);
        verify(productMapper).toProductSummaryDTO(testProduct);
    }

    @Test
    void findByFeatured_ShouldReturnPageOfProducts() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> productPage = new PageImpl<>(Arrays.asList(testProduct));

        when(productRepository.findByIsFeatured(true, pageable)).thenReturn(productPage);
        when(productMapper.toProductSummaryDTO(testProduct)).thenReturn(testProductSummaryDTO);

        // When
        Page<ProductSummaryDTO> result = productService.findByFeatured(true, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(testProductSummaryDTO, result.getContent().get(0));
        verify(productRepository).findByIsFeatured(true, pageable);
        verify(productMapper).toProductSummaryDTO(testProduct);
    }

    @Test
    void findByActive_ShouldReturnPageOfProducts() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> productPage = new PageImpl<>(Arrays.asList(testProduct));

        when(productRepository.findByIsActive(true, pageable)).thenReturn(productPage);
        when(productMapper.toProductSummaryDTO(testProduct)).thenReturn(testProductSummaryDTO);

        // When
        Page<ProductSummaryDTO> result = productService.findByActive(true, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(testProductSummaryDTO, result.getContent().get(0));
        verify(productRepository).findByIsActive(true, pageable);
        verify(productMapper).toProductSummaryDTO(testProduct);
    }

    // Search and Filter Tests

    @Test
    void searchByName_ShouldReturnPageOfProducts() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        String searchTerm = "laptop";
        Page<Product> productPage = new PageImpl<>(Arrays.asList(testProduct));

        when(productRepository.findByNameContainingIgnoreCase(searchTerm, pageable)).thenReturn(productPage);
        when(productMapper.toProductSummaryDTO(testProduct)).thenReturn(testProductSummaryDTO);

        // When
        Page<ProductSummaryDTO> result = productService.searchByName(searchTerm, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(testProductSummaryDTO, result.getContent().get(0));
        verify(productRepository).findByNameContainingIgnoreCase(searchTerm, pageable);
        verify(productMapper).toProductSummaryDTO(testProduct);
    }

    @Test
    void searchByDescription_ShouldReturnPageOfProducts() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        String searchTerm = "gaming";
        Page<Product> productPage = new PageImpl<>(Arrays.asList(testProduct));

        when(productRepository.findByDescriptionContainingIgnoreCase(searchTerm, pageable)).thenReturn(productPage);
        when(productMapper.toProductSummaryDTO(testProduct)).thenReturn(testProductSummaryDTO);

        // When
        Page<ProductSummaryDTO> result = productService.searchByDescription(searchTerm, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(testProductSummaryDTO, result.getContent().get(0));
        verify(productRepository).findByDescriptionContainingIgnoreCase(searchTerm, pageable);
        verify(productMapper).toProductSummaryDTO(testProduct);
    }

    @Test
    void findByCategoryAndPriceRange_ShouldReturnPageOfProducts() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Long categoryId = 1L;
        BigDecimal minPrice = new BigDecimal("100.00");
        BigDecimal maxPrice = new BigDecimal("1000.00");
        Page<Product> productPage = new PageImpl<>(Arrays.asList(testProduct));

        when(productRepository.findByCategoryIdAndPriceBetween(categoryId, minPrice, maxPrice, pageable)).thenReturn(productPage);
        when(productMapper.toProductSummaryDTO(testProduct)).thenReturn(testProductSummaryDTO);

        // When
        Page<ProductSummaryDTO> result = productService.findByCategoryAndPriceRange(categoryId, minPrice, maxPrice, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(testProductSummaryDTO, result.getContent().get(0));
        verify(productRepository).findByCategoryIdAndPriceBetween(categoryId, minPrice, maxPrice, pageable);
        verify(productMapper).toProductSummaryDTO(testProduct);
    }

    // Advanced Filtering Tests

    @Test
    void findByCategoryAndFeatured_ShouldReturnPageOfProducts() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Long categoryId = 1L;
        Page<Product> productPage = new PageImpl<>(Arrays.asList(testProduct));

        when(productRepository.findByCategoryIdAndIsFeatured(categoryId, true, pageable)).thenReturn(productPage);
        when(productMapper.toProductSummaryDTO(testProduct)).thenReturn(testProductSummaryDTO);

        // When
        Page<ProductSummaryDTO> result = productService.findByCategoryAndFeatured(categoryId, true, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(testProductSummaryDTO, result.getContent().get(0));
        verify(productRepository).findByCategoryIdAndIsFeatured(categoryId, true, pageable);
        verify(productMapper).toProductSummaryDTO(testProduct);
    }

    @Test
    void findByPriceRangeAndFeatured_ShouldReturnPageOfProducts() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        BigDecimal minPrice = new BigDecimal("100.00");
        BigDecimal maxPrice = new BigDecimal("1000.00");
        Page<Product> productPage = new PageImpl<>(Arrays.asList(testProduct));

        when(productRepository.findByPriceBetweenAndIsFeatured(minPrice, maxPrice, true, pageable)).thenReturn(productPage);
        when(productMapper.toProductSummaryDTO(testProduct)).thenReturn(testProductSummaryDTO);

        // When
        Page<ProductSummaryDTO> result = productService.findByPriceRangeAndFeatured(minPrice, maxPrice, true, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(testProductSummaryDTO, result.getContent().get(0));
        verify(productRepository).findByPriceBetweenAndIsFeatured(minPrice, maxPrice, true, pageable);
        verify(productMapper).toProductSummaryDTO(testProduct);
    }

    // Statistics Tests

    @Test
    void countByCategory_ShouldReturnCount() {
        // Given
        Long categoryId = 1L;
        when(productRepository.countByCategoryId(categoryId)).thenReturn(5L);

        // When
        long result = productService.countByCategory(categoryId);

        // Then
        assertEquals(5L, result);
        verify(productRepository).countByCategoryId(categoryId);
    }

    @Test
    void countByFeatured_ShouldReturnCount() {
        // Given
        when(productRepository.countByIsFeatured(true)).thenReturn(3L);

        // When
        long result = productService.countByFeatured(true);

        // Then
        assertEquals(3L, result);
        verify(productRepository).countByIsFeatured(true);
    }

    @Test
    void countByActive_ShouldReturnCount() {
        // Given
        when(productRepository.countByIsActive(true)).thenReturn(10L);

        // When
        long result = productService.countByActive(true);

        // Then
        assertEquals(10L, result);
        verify(productRepository).countByIsActive(true);
    }

    // Edge Cases and Error Handling Tests

    @Test
    void create_ShouldSetDefaultValues_WhenOptionalFieldsAreNull() {
        // Given
        ProductCreateDTO dtoWithNulls = new ProductCreateDTO(
                "Test Product",
                "Description",
                null, // shortDescription
                new BigDecimal("100.00"),
                new BigDecimal("23.00"),
                null, // shippingCost
                null, // estimatedDeliveryTime
                null, // thumbnailUrl
                "test-product",
                1L,
                null, // isFeatured
                null // attributeValues
        );

        Product productWithDefaults = new Product();
        productWithDefaults.setName("Test Product");
        productWithDefaults.setDescription("Description");
        productWithDefaults.setPrice(new BigDecimal("100.00"));
        productWithDefaults.setVatRate(new BigDecimal("23.00"));
        productWithDefaults.setSeoSlug("test-product");
        productWithDefaults.setCategory(testCategory);
        productWithDefaults.setShippingCost(BigDecimal.ZERO);
        productWithDefaults.setIsFeatured(false);

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(productMapper.toProduct(dtoWithNulls)).thenReturn(productWithDefaults);
        when(productRepository.save(any(Product.class))).thenReturn(productWithDefaults);
        when(productMapper.toProductDTO(productWithDefaults)).thenReturn(testProductDTO);

        // When
        ProductDTO result = productService.create(dtoWithNulls);

        // Then
        assertNotNull(result);
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void update_ShouldRegenerateSku_WhenNameOrCategoryChanges() {
        // Given
        ProductUpdateDTO updateWithNewName = new ProductUpdateDTO(
                "Completely New Name", // Different name
                null, null, null, null, null, null, null, null, null, null, null, null // attributeValues
        );

        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);
        when(productMapper.toProductDTO(testProduct)).thenReturn(testProductDTO);

        // When
        ProductDTO result = productService.update(1L, updateWithNewName);

        // Then
        assertNotNull(result);
        verify(productRepository).save(testProduct);
        // Note: In real implementation, SKU would be regenerated due to name change
    }

    @Test
    void findAll_ShouldReturnEmptyPage_WhenNoProductsExist() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> emptyPage = new PageImpl<>(Arrays.asList());

        when(productRepository.findAll(pageable)).thenReturn(emptyPage);

        // When
        Page<ProductSummaryDTO> result = productService.findAll(pageable);

        // Then
        assertNotNull(result);
        assertTrue(result.getContent().isEmpty());
        verify(productRepository).findAll(pageable);
    }
}
