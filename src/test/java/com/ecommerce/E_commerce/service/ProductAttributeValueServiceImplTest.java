package com.ecommerce.E_commerce.service;

import com.ecommerce.E_commerce.dto.productattributevalue.ProductAttributeValueCreateDTO;
import com.ecommerce.E_commerce.dto.productattributevalue.ProductAttributeValueDTO;
import com.ecommerce.E_commerce.dto.productattributevalue.ProductAttributeValueUpdateDTO;
import com.ecommerce.E_commerce.exception.ResourceNotFoundException;
import com.ecommerce.E_commerce.mapper.ProductAttributeValueMapper;
import com.ecommerce.E_commerce.model.Category;
import com.ecommerce.E_commerce.model.CategoryAttribute;
import com.ecommerce.E_commerce.model.CategoryAttributeType;
import com.ecommerce.E_commerce.model.Product;
import com.ecommerce.E_commerce.model.ProductAttributeValue;
import com.ecommerce.E_commerce.repository.CategoryAttributeRepository;
import com.ecommerce.E_commerce.repository.ProductAttributeValueRepository;
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

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductAttributeValueServiceImplTest {

    @Mock
    private ProductAttributeValueRepository productAttributeValueRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryAttributeRepository categoryAttributeRepository;

    @Mock
    private ProductAttributeValueMapper productAttributeValueMapper;

    private ProductAttributeValueServiceImpl productAttributeValueService;

    private ProductAttributeValue testProductAttributeValue;
    private Product testProduct;
    private Category testCategory;
    private CategoryAttribute testCategoryAttribute;
    private ProductAttributeValueCreateDTO testCreateDTO;
    private ProductAttributeValueUpdateDTO testUpdateDTO;
    private ProductAttributeValueDTO testProductAttributeValueDTO;

    @BeforeEach
    void setUp() {
        productAttributeValueService = new ProductAttributeValueServiceImpl(
                productRepository,
                productAttributeValueRepository,
                categoryAttributeRepository,
                productAttributeValueMapper
        );

        // Setup test data
        testCategory = new Category();
        testCategory.setId(1L);
        testCategory.setName("Electronics");
        testCategory.setSeoSlug("electronics");

        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setName("Test Laptop");
        testProduct.setCategory(testCategory);

        testCategoryAttribute = new CategoryAttribute();
        testCategoryAttribute.setId(1L);
        testCategoryAttribute.setName("Screen Size");
        testCategoryAttribute.setType(CategoryAttributeType.TEXT);
        testCategoryAttribute.setKeyAttribute(true);
        testCategoryAttribute.setCategory(testCategory);

        testProductAttributeValue = new ProductAttributeValue();
        testProductAttributeValue.setId(1L);
        testProductAttributeValue.setProduct(testProduct);
        testProductAttributeValue.setCategoryAttribute(testCategoryAttribute);
        testProductAttributeValue.setValue("15.6 inches");
        testProductAttributeValue.setCreatedAt(Instant.now());
        testProductAttributeValue.setUpdatedAt(Instant.now());
        testProductAttributeValue.setDeletedAt(null);
        testProductAttributeValue.setIsActive(true);

        testCreateDTO = new ProductAttributeValueCreateDTO(
                1L, // productId
                1L, // categoryAttributeId
                "15.6 inches" // value
        );

        testUpdateDTO = new ProductAttributeValueUpdateDTO(
                "17.3 inches", // value
                true // isActive
        );

        testProductAttributeValueDTO = new ProductAttributeValueDTO(
                1L, // id
                1L, // productId
                "Test Laptop", // productName
                1L, // categoryAttributeId
                "Screen Size", // categoryAttributeName
                "TEXT", // categoryAttributeType
                true, // isKeyAttribute
                "15.6 inches", // value
                Instant.now(), // createdAt
                Instant.now(), // updatedAt
                null, // deletedAt
                true // isActive
        );
    }

    // CRUD Operations Tests

    @Test
    void create_ShouldCreateProductAttributeValueSuccessfully() {
        // Given
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(categoryAttributeRepository.findById(1L)).thenReturn(Optional.of(testCategoryAttribute));
        when(productAttributeValueRepository.findByProductIdAndCategoryAttributeId(1L, 1L)).thenReturn(Optional.empty());
        when(productAttributeValueMapper.toProductAttributeValue(testCreateDTO)).thenReturn(testProductAttributeValue);
        when(productAttributeValueRepository.save(any(ProductAttributeValue.class))).thenReturn(testProductAttributeValue);
        when(productAttributeValueMapper.toProductAttributeValueDTO(testProductAttributeValue)).thenReturn(testProductAttributeValueDTO);

        // When
        ProductAttributeValueDTO result = productAttributeValueService.create(testCreateDTO);

        // Then
        assertNotNull(result);
        assertEquals(testProductAttributeValueDTO, result);
        verify(productRepository).findById(1L);
        verify(categoryAttributeRepository).findById(1L);
        verify(productAttributeValueRepository).findByProductIdAndCategoryAttributeId(1L, 1L);
        verify(productAttributeValueMapper).toProductAttributeValue(testCreateDTO);
        verify(productAttributeValueRepository).save(any(ProductAttributeValue.class));
        verify(productAttributeValueMapper).toProductAttributeValueDTO(testProductAttributeValue);
    }

    @Test
    void create_ShouldThrowException_WhenProductNotFound() {
        // Given
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> productAttributeValueService.create(testCreateDTO));
        assertEquals("Product not found with id: 1", exception.getMessage());
        verify(productRepository).findById(1L);
        verify(productAttributeValueRepository, never()).save(any(ProductAttributeValue.class));
    }

    @Test
    void create_ShouldThrowException_WhenCategoryAttributeNotFound() {
        // Given
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(categoryAttributeRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> productAttributeValueService.create(testCreateDTO));
        assertEquals("Category attribute not found with id: 1", exception.getMessage());
        verify(productRepository).findById(1L);
        verify(categoryAttributeRepository).findById(1L);
        verify(productAttributeValueRepository, never()).save(any(ProductAttributeValue.class));
    }

    @Test
    void create_ShouldThrowException_WhenCombinationAlreadyExists() {
        // Given
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(categoryAttributeRepository.findById(1L)).thenReturn(Optional.of(testCategoryAttribute));
        when(productAttributeValueRepository.findByProductIdAndCategoryAttributeId(1L, 1L)).thenReturn(Optional.of(testProductAttributeValue));

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> productAttributeValueService.create(testCreateDTO));
        assertEquals("Product attribute value already exists for this product and category attribute", exception.getMessage());
        verify(productRepository).findById(1L);
        verify(categoryAttributeRepository).findById(1L);
        verify(productAttributeValueRepository).findByProductIdAndCategoryAttributeId(1L, 1L);
        verify(productAttributeValueRepository, never()).save(any(ProductAttributeValue.class));
    }

    @Test
    void update_ShouldUpdateProductAttributeValueSuccessfully() {
        // Given
        when(productAttributeValueRepository.findById(1L)).thenReturn(Optional.of(testProductAttributeValue));
        when(productAttributeValueRepository.save(any(ProductAttributeValue.class))).thenReturn(testProductAttributeValue);
        when(productAttributeValueMapper.toProductAttributeValueDTO(testProductAttributeValue)).thenReturn(testProductAttributeValueDTO);

        // When
        ProductAttributeValueDTO result = productAttributeValueService.update(1L, testUpdateDTO);

        // Then
        assertNotNull(result);
        assertEquals(testProductAttributeValueDTO, result);
        verify(productAttributeValueRepository).findById(1L);
        verify(productAttributeValueMapper).updateProductAttributeValueFromDTO(testUpdateDTO, testProductAttributeValue);
        verify(productAttributeValueRepository).save(testProductAttributeValue);
        verify(productAttributeValueMapper).toProductAttributeValueDTO(testProductAttributeValue);
    }

    @Test
    void update_ShouldThrowException_WhenProductAttributeValueNotFound() {
        // Given
        when(productAttributeValueRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> productAttributeValueService.update(1L, testUpdateDTO));
        assertEquals("Product attribute value not found with id: 1", exception.getMessage());
        verify(productAttributeValueRepository).findById(1L);
        verify(productAttributeValueRepository, never()).save(any(ProductAttributeValue.class));
    }

    @Test
    void delete_ShouldDeleteProductAttributeValueSuccessfully() {
        // Given
        when(productAttributeValueRepository.findById(1L)).thenReturn(Optional.of(testProductAttributeValue));

        // When
        productAttributeValueService.delete(1L);

        // Then
        verify(productAttributeValueRepository).findById(1L);
        verify(productAttributeValueRepository).save(testProductAttributeValue);
        assertNotNull(testProductAttributeValue.getDeletedAt());
        assertFalse(testProductAttributeValue.getIsActive());
    }

    @Test
    void delete_ShouldThrowException_WhenProductAttributeValueNotFound() {
        // Given
        when(productAttributeValueRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> productAttributeValueService.delete(1L));
        assertEquals("Product attribute value not found with id: 1", exception.getMessage());
        verify(productAttributeValueRepository).findById(1L);
        verify(productAttributeValueRepository, never()).save(any(ProductAttributeValue.class));
    }

    // Single ProductAttributeValue Retrieval Tests

    @Test
    void getById_ShouldReturnProductAttributeValue_WhenProductAttributeValueExists() {
        // Given
        when(productAttributeValueRepository.findById(1L)).thenReturn(Optional.of(testProductAttributeValue));
        when(productAttributeValueMapper.toProductAttributeValueDTO(testProductAttributeValue)).thenReturn(testProductAttributeValueDTO);

        // When
        ProductAttributeValueDTO result = productAttributeValueService.getById(1L);

        // Then
        assertNotNull(result);
        assertEquals(testProductAttributeValueDTO, result);
        verify(productAttributeValueRepository).findById(1L);
        verify(productAttributeValueMapper).toProductAttributeValueDTO(testProductAttributeValue);
    }

    @Test
    void getById_ShouldThrowException_WhenProductAttributeValueNotFound() {
        // Given
        when(productAttributeValueRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> productAttributeValueService.getById(1L));
        assertEquals("Product attribute value not found with id: 1", exception.getMessage());
        verify(productAttributeValueRepository).findById(1L);
    }

    @Test
    void getByProductId_ShouldReturnListOfProductAttributeValues() {
        // Given
        List<ProductAttributeValue> productAttributeValues = Arrays.asList(testProductAttributeValue);
        when(productAttributeValueRepository.findByProductIdAndIsActive(1L, true)).thenReturn(productAttributeValues);
        when(productAttributeValueMapper.toProductAttributeValueDTO(testProductAttributeValue)).thenReturn(testProductAttributeValueDTO);

        // When
        List<ProductAttributeValueDTO> result = productAttributeValueService.getByProductId(1L);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testProductAttributeValueDTO, result.get(0));
        verify(productAttributeValueRepository).findByProductIdAndIsActive(1L, true);
        verify(productAttributeValueMapper).toProductAttributeValueDTO(testProductAttributeValue);
    }

    @Test
    void getByCategoryAttributeId_ShouldReturnListOfProductAttributeValues() {
        // Given
        List<ProductAttributeValue> productAttributeValues = Arrays.asList(testProductAttributeValue);
        when(productAttributeValueRepository.findByCategoryAttributeIdAndIsActive(1L, true)).thenReturn(productAttributeValues);
        when(productAttributeValueMapper.toProductAttributeValueDTO(testProductAttributeValue)).thenReturn(testProductAttributeValueDTO);

        // When
        List<ProductAttributeValueDTO> result = productAttributeValueService.getByCategoryAttributeId(1L);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testProductAttributeValueDTO, result.get(0));
        verify(productAttributeValueRepository).findByCategoryAttributeIdAndIsActive(1L, true);
        verify(productAttributeValueMapper).toProductAttributeValueDTO(testProductAttributeValue);
    }

    @Test
    void getByProductAndCategoryAttribute_ShouldReturnProductAttributeValue() {
        // Given
        when(productAttributeValueRepository.findByProductIdAndCategoryAttributeId(1L, 1L)).thenReturn(Optional.of(testProductAttributeValue));
        when(productAttributeValueMapper.toProductAttributeValueDTO(testProductAttributeValue)).thenReturn(testProductAttributeValueDTO);

        // When
        ProductAttributeValueDTO result = productAttributeValueService.getByProductAndCategoryAttribute(1L, 1L);

        // Then
        assertNotNull(result);
        assertEquals(testProductAttributeValueDTO, result);
        verify(productAttributeValueRepository).findByProductIdAndCategoryAttributeId(1L, 1L);
        verify(productAttributeValueMapper).toProductAttributeValueDTO(testProductAttributeValue);
    }

    @Test
    void getByProductAndCategoryAttribute_ShouldThrowException_WhenNotFound() {
        // Given
        when(productAttributeValueRepository.findByProductIdAndCategoryAttributeId(1L, 1L)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> productAttributeValueService.getByProductAndCategoryAttribute(1L, 1L));
        assertEquals("Product attribute value not found for product id: 1 and category attribute id: 1", exception.getMessage());
        verify(productAttributeValueRepository).findByProductIdAndCategoryAttributeId(1L, 1L);
    }

    // Paginated Lists Tests

    @Test
    void findAll_ShouldReturnPageOfProductAttributeValues() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<ProductAttributeValue> productAttributeValuePage = new PageImpl<>(Arrays.asList(testProductAttributeValue));
        when(productAttributeValueRepository.findAll(pageable)).thenReturn(productAttributeValuePage);
        when(productAttributeValueMapper.toProductAttributeValueDTO(testProductAttributeValue)).thenReturn(testProductAttributeValueDTO);

        // When
        Page<ProductAttributeValueDTO> result = productAttributeValueService.findAll(pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(testProductAttributeValueDTO, result.getContent().get(0));
        verify(productAttributeValueRepository).findAll(pageable);
        verify(productAttributeValueMapper).toProductAttributeValueDTO(testProductAttributeValue);
    }

    @Test
    void findByProductId_ShouldReturnPageOfProductAttributeValues() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<ProductAttributeValue> productAttributeValuePage = new PageImpl<>(Arrays.asList(testProductAttributeValue));
        when(productAttributeValueRepository.findByProductId(1L, pageable)).thenReturn(productAttributeValuePage);
        when(productAttributeValueMapper.toProductAttributeValueDTO(testProductAttributeValue)).thenReturn(testProductAttributeValueDTO);

        // When
        Page<ProductAttributeValueDTO> result = productAttributeValueService.findByProductId(1L, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(testProductAttributeValueDTO, result.getContent().get(0));
        verify(productAttributeValueRepository).findByProductId(1L, pageable);
        verify(productAttributeValueMapper).toProductAttributeValueDTO(testProductAttributeValue);
    }

    @Test
    void findByCategoryAttributeId_ShouldReturnPageOfProductAttributeValues() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<ProductAttributeValue> productAttributeValuePage = new PageImpl<>(Arrays.asList(testProductAttributeValue));
        when(productAttributeValueRepository.findByCategoryAttributeId(1L, pageable)).thenReturn(productAttributeValuePage);
        when(productAttributeValueMapper.toProductAttributeValueDTO(testProductAttributeValue)).thenReturn(testProductAttributeValueDTO);

        // When
        Page<ProductAttributeValueDTO> result = productAttributeValueService.findByCategoryAttributeId(1L, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(testProductAttributeValueDTO, result.getContent().get(0));
        verify(productAttributeValueRepository).findByCategoryAttributeId(1L, pageable);
        verify(productAttributeValueMapper).toProductAttributeValueDTO(testProductAttributeValue);
    }

    @Test
    void findByCategoryId_ShouldReturnPageOfProductAttributeValues() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<ProductAttributeValue> productAttributeValuePage = new PageImpl<>(Arrays.asList(testProductAttributeValue));
        when(productAttributeValueRepository.findByProductCategoryId(1L, pageable)).thenReturn(productAttributeValuePage);
        when(productAttributeValueMapper.toProductAttributeValueDTO(testProductAttributeValue)).thenReturn(testProductAttributeValueDTO);

        // When
        Page<ProductAttributeValueDTO> result = productAttributeValueService.findByCategoryId(1L, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(testProductAttributeValueDTO, result.getContent().get(0));
        verify(productAttributeValueRepository).findByProductCategoryId(1L, pageable);
        verify(productAttributeValueMapper).toProductAttributeValueDTO(testProductAttributeValue);
    }

    @Test
    void findByValue_ShouldReturnPageOfProductAttributeValues() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<ProductAttributeValue> productAttributeValuePage = new PageImpl<>(Arrays.asList(testProductAttributeValue));
        when(productAttributeValueRepository.findByValueContainingIgnoreCase("15.6", pageable)).thenReturn(productAttributeValuePage);
        when(productAttributeValueMapper.toProductAttributeValueDTO(testProductAttributeValue)).thenReturn(testProductAttributeValueDTO);

        // When
        Page<ProductAttributeValueDTO> result = productAttributeValueService.findByValue("15.6", pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(testProductAttributeValueDTO, result.getContent().get(0));
        verify(productAttributeValueRepository).findByValueContainingIgnoreCase("15.6", pageable);
        verify(productAttributeValueMapper).toProductAttributeValueDTO(testProductAttributeValue);
    }

    // Search and Filter Tests

    @Test
    void searchByValue_ShouldReturnPageOfProductAttributeValues() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<ProductAttributeValue> productAttributeValuePage = new PageImpl<>(Arrays.asList(testProductAttributeValue));
        when(productAttributeValueRepository.findByValueContainingIgnoreCase("15.6", pageable)).thenReturn(productAttributeValuePage);
        when(productAttributeValueMapper.toProductAttributeValueDTO(testProductAttributeValue)).thenReturn(testProductAttributeValueDTO);

        // When
        Page<ProductAttributeValueDTO> result = productAttributeValueService.searchByValue("15.6", pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(testProductAttributeValueDTO, result.getContent().get(0));
        verify(productAttributeValueRepository).findByValueContainingIgnoreCase("15.6", pageable);
        verify(productAttributeValueMapper).toProductAttributeValueDTO(testProductAttributeValue);
    }

    @Test
    void findByAttributeType_ShouldReturnPageOfProductAttributeValues() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<ProductAttributeValue> productAttributeValuePage = new PageImpl<>(Arrays.asList(testProductAttributeValue));
        when(productAttributeValueRepository.findByCategoryAttributeType("TEXT", pageable)).thenReturn(productAttributeValuePage);
        when(productAttributeValueMapper.toProductAttributeValueDTO(testProductAttributeValue)).thenReturn(testProductAttributeValueDTO);

        // When
        Page<ProductAttributeValueDTO> result = productAttributeValueService.findByAttributeType("TEXT", pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(testProductAttributeValueDTO, result.getContent().get(0));
        verify(productAttributeValueRepository).findByCategoryAttributeType("TEXT", pageable);
        verify(productAttributeValueMapper).toProductAttributeValueDTO(testProductAttributeValue);
    }

    // Statistics Tests

    @Test
    void countByProductId_ShouldReturnCount() {
        // Given
        when(productAttributeValueRepository.countByProductIdAndIsActive(1L, true)).thenReturn(5L);

        // When
        long result = productAttributeValueService.countByProductId(1L);

        // Then
        assertEquals(5L, result);
        verify(productAttributeValueRepository).countByProductIdAndIsActive(1L, true);
    }

    @Test
    void countByCategoryAttributeId_ShouldReturnCount() {
        // Given
        when(productAttributeValueRepository.countByCategoryAttributeIdAndIsActive(1L, true)).thenReturn(3L);

        // When
        long result = productAttributeValueService.countByCategoryAttributeId(1L);

        // Then
        assertEquals(3L, result);
        verify(productAttributeValueRepository).countByCategoryAttributeIdAndIsActive(1L, true);
    }

    @Test
    void countByCategoryId_ShouldReturnCount() {
        // Given
        when(productAttributeValueRepository.countByProductCategoryId(1L)).thenReturn(10L);

        // When
        long result = productAttributeValueService.countByCategoryId(1L);

        // Then
        assertEquals(10L, result);
        verify(productAttributeValueRepository).countByProductCategoryId(1L);
    }

    // Utility Methods Tests

    @Test
    void getDistinctValuesByCategoryAttribute_ShouldReturnDistinctValues() {
        // Given
        List<String> distinctValues = Arrays.asList("15.6 inches", "17.3 inches", "13.3 inches");
        when(productAttributeValueRepository.findDistinctValuesByCategoryAttribute(1L)).thenReturn(distinctValues);

        // When
        List<String> result = productAttributeValueService.getDistinctValuesByCategoryAttribute(1L);

        // Then
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals(distinctValues, result);
        verify(productAttributeValueRepository).findDistinctValuesByCategoryAttribute(1L);
    }

    @Test
    void getKeyAttributesByProduct_ShouldReturnKeyAttributes() {
        // Given
        List<ProductAttributeValue> keyAttributes = Arrays.asList(testProductAttributeValue);
        when(productAttributeValueRepository.findKeyAttributesByProduct(1L)).thenReturn(keyAttributes);
        when(productAttributeValueMapper.toProductAttributeValueDTO(testProductAttributeValue)).thenReturn(testProductAttributeValueDTO);

        // When
        List<ProductAttributeValueDTO> result = productAttributeValueService.getKeyAttributesByProduct(1L);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testProductAttributeValueDTO, result.get(0));
        verify(productAttributeValueRepository).findKeyAttributesByProduct(1L);
        verify(productAttributeValueMapper).toProductAttributeValueDTO(testProductAttributeValue);
    }

    @Test
    void getByProductAndAttributeType_ShouldReturnProductAttributeValues() {
        // Given
        List<ProductAttributeValue> productAttributeValues = Arrays.asList(testProductAttributeValue);
        when(productAttributeValueRepository.findByProductAndAttributeType(1L, "TEXT")).thenReturn(productAttributeValues);
        when(productAttributeValueMapper.toProductAttributeValueDTO(testProductAttributeValue)).thenReturn(testProductAttributeValueDTO);

        // When
        List<ProductAttributeValueDTO> result = productAttributeValueService.getByProductAndAttributeType(1L, "TEXT");

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testProductAttributeValueDTO, result.get(0));
        verify(productAttributeValueRepository).findByProductAndAttributeType(1L, "TEXT");
        verify(productAttributeValueMapper).toProductAttributeValueDTO(testProductAttributeValue);
    }

    // Bulk Operations Tests

    @Test
    void createBulk_ShouldCreateMultipleProductAttributeValues() {
        // Given
        List<ProductAttributeValueCreateDTO> dtos = Arrays.asList(testCreateDTO);
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(categoryAttributeRepository.findById(1L)).thenReturn(Optional.of(testCategoryAttribute));
        when(productAttributeValueRepository.findByProductIdAndCategoryAttributeId(1L, 1L)).thenReturn(Optional.empty());
        when(productAttributeValueMapper.toProductAttributeValue(testCreateDTO)).thenReturn(testProductAttributeValue);
        when(productAttributeValueRepository.save(any(ProductAttributeValue.class))).thenReturn(testProductAttributeValue);
        when(productAttributeValueMapper.toProductAttributeValueDTO(testProductAttributeValue)).thenReturn(testProductAttributeValueDTO);

        // When
        List<ProductAttributeValueDTO> result = productAttributeValueService.createBulk(dtos);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testProductAttributeValueDTO, result.get(0));
        verify(productRepository).findById(1L);
        verify(categoryAttributeRepository).findById(1L);
        verify(productAttributeValueRepository).save(any(ProductAttributeValue.class));
    }

    @Test
    void updateByProduct_ShouldUpdateMultipleProductAttributeValues() {
        // Given
        List<ProductAttributeValueUpdateDTO> dtos = Arrays.asList(testUpdateDTO);
        List<ProductAttributeValue> existingValues = Arrays.asList(testProductAttributeValue);
        when(productAttributeValueRepository.findByProductIdAndIsActive(1L, true)).thenReturn(existingValues);
        when(productAttributeValueRepository.save(any(ProductAttributeValue.class))).thenReturn(testProductAttributeValue);
        when(productAttributeValueMapper.toProductAttributeValueDTO(testProductAttributeValue)).thenReturn(testProductAttributeValueDTO);

        // When
        List<ProductAttributeValueDTO> result = productAttributeValueService.updateByProduct(1L, dtos);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testProductAttributeValueDTO, result.get(0));
        verify(productAttributeValueRepository).findByProductIdAndIsActive(1L, true);
        verify(productAttributeValueRepository).save(any(ProductAttributeValue.class));
    }

    @Test
    void updateByProduct_ShouldThrowException_WhenSizeMismatch() {
        // Given
        List<ProductAttributeValueUpdateDTO> dtos = Arrays.asList(testUpdateDTO, testUpdateDTO);
        List<ProductAttributeValue> existingValues = Arrays.asList(testProductAttributeValue);
        when(productAttributeValueRepository.findByProductIdAndIsActive(1L, true)).thenReturn(existingValues);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> productAttributeValueService.updateByProduct(1L, dtos));
        assertEquals("Number of update DTOs must match existing attribute values", exception.getMessage());
        verify(productAttributeValueRepository).findByProductIdAndIsActive(1L, true);
        verify(productAttributeValueRepository, never()).save(any(ProductAttributeValue.class));
    }

    @Test
    void deleteByProduct_ShouldDeleteAllProductAttributeValuesForProduct() {
        // Given
        List<ProductAttributeValue> productAttributeValues = Arrays.asList(testProductAttributeValue);
        when(productAttributeValueRepository.findByProductIdAndIsActive(1L, true)).thenReturn(productAttributeValues);
        when(productAttributeValueRepository.saveAll(anyList())).thenReturn(productAttributeValues);

        // When
        productAttributeValueService.deleteByProduct(1L);

        // Then
        verify(productAttributeValueRepository).findByProductIdAndIsActive(1L, true);
        verify(productAttributeValueRepository).saveAll(anyList());
        assertNotNull(testProductAttributeValue.getDeletedAt());
        assertFalse(testProductAttributeValue.getIsActive());
    }

    // Edge Cases Tests

    @Test
    void findAll_ShouldReturnEmptyPage_WhenNoProductAttributeValuesExist() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<ProductAttributeValue> emptyPage = new PageImpl<>(Arrays.asList());
        when(productAttributeValueRepository.findAll(pageable)).thenReturn(emptyPage);

        // When
        Page<ProductAttributeValueDTO> result = productAttributeValueService.findAll(pageable);

        // Then
        assertNotNull(result);
        assertTrue(result.getContent().isEmpty());
        verify(productAttributeValueRepository).findAll(pageable);
    }

    @Test
    void getByProductId_ShouldReturnEmptyList_WhenNoProductAttributeValuesExist() {
        // Given
        when(productAttributeValueRepository.findByProductIdAndIsActive(1L, true)).thenReturn(Arrays.asList());

        // When
        List<ProductAttributeValueDTO> result = productAttributeValueService.getByProductId(1L);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(productAttributeValueRepository).findByProductIdAndIsActive(1L, true);
    }
}
