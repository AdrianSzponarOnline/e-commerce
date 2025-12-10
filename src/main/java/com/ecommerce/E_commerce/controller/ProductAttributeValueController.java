package com.ecommerce.E_commerce.controller;

import com.ecommerce.E_commerce.dto.productattributevalue.ProductAttributeValueCreateDTO;
import com.ecommerce.E_commerce.dto.productattributevalue.ProductAttributeValueDTO;
import com.ecommerce.E_commerce.dto.productattributevalue.ProductAttributeValueUpdateDTO;
import com.ecommerce.E_commerce.service.ProductAttributeValueService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/product-attribute-values")
@CrossOrigin(origins = "*")
public class ProductAttributeValueController {

    private static final Logger logger = LoggerFactory.getLogger(ProductAttributeValueController.class);
    private final ProductAttributeValueService productAttributeValueService;

    @Autowired
    public ProductAttributeValueController(ProductAttributeValueService productAttributeValueService) {
        this.productAttributeValueService = productAttributeValueService;
    }

    // CRUD Operations - Owner only
    @PostMapping
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ProductAttributeValueDTO> createProductAttributeValue(@Valid @RequestBody ProductAttributeValueCreateDTO dto) {
        logger.info("POST /api/product-attribute-values - Creating product attribute value: productId={}, attributeId={}", dto.productId(), dto.attributeId());
        ProductAttributeValueDTO productAttributeValue = productAttributeValueService.create(dto);
        logger.info("POST /api/product-attribute-values - Product attribute value created successfully: id={}", productAttributeValue.id());
        return ResponseEntity.status(HttpStatus.CREATED).body(productAttributeValue);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ProductAttributeValueDTO> updateProductAttributeValue(@PathVariable Long id, @Valid @RequestBody ProductAttributeValueUpdateDTO dto) {
        logger.info("PUT /api/product-attribute-values/{} - Updating product attribute value", id);
        ProductAttributeValueDTO productAttributeValue = productAttributeValueService.update(id, dto);
        logger.info("PUT /api/product-attribute-values/{} - Product attribute value updated successfully", id);
        return ResponseEntity.ok(productAttributeValue);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<Void> deleteProductAttributeValue(@PathVariable Long id) {
        logger.info("DELETE /api/product-attribute-values/{} - Deleting product attribute value", id);
        productAttributeValueService.delete(id);
        logger.info("DELETE /api/product-attribute-values/{} - Product attribute value deleted successfully", id);
        return ResponseEntity.noContent().build();
    }

    // Single ProductAttributeValue Retrieval
    @GetMapping("/{id}")
    public ResponseEntity<ProductAttributeValueDTO> getProductAttributeValueById(@PathVariable Long id) {
        ProductAttributeValueDTO productAttributeValue = productAttributeValueService.getById(id);
        return ResponseEntity.ok(productAttributeValue);
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<List<ProductAttributeValueDTO>> getProductAttributeValuesByProduct(@PathVariable Long productId) {
        List<ProductAttributeValueDTO> productAttributeValues = productAttributeValueService.getByProductId(productId);
        return ResponseEntity.ok(productAttributeValues);
    }

    @GetMapping("/attribute/{attributeId}")
    public ResponseEntity<List<ProductAttributeValueDTO>> getProductAttributeValuesByAttribute(@PathVariable Long attributeId) {
        List<ProductAttributeValueDTO> productAttributeValues = productAttributeValueService.getByAttributeId(attributeId);
        return ResponseEntity.ok(productAttributeValues);
    }

    @GetMapping("/product/{productId}/attribute/{attributeId}")
    public ResponseEntity<ProductAttributeValueDTO> getProductAttributeValueByProductAndAttribute(
            @PathVariable Long productId, 
            @PathVariable Long attributeId) {
        ProductAttributeValueDTO productAttributeValue = productAttributeValueService.getByProductAndAttribute(productId, attributeId);
        return ResponseEntity.ok(productAttributeValue);
    }

    // Paginated Lists
    @GetMapping
    public ResponseEntity<Page<ProductAttributeValueDTO>> getAllProductAttributeValues(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<ProductAttributeValueDTO> productAttributeValues = productAttributeValueService.findAll(pageable);
        return ResponseEntity.ok(productAttributeValues);
    }

    @GetMapping("/product/{productId}/paginated")
    public ResponseEntity<Page<ProductAttributeValueDTO>> getProductAttributeValuesByProductPaginated(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<ProductAttributeValueDTO> productAttributeValues = productAttributeValueService.findByProductId(productId, pageable);
        return ResponseEntity.ok(productAttributeValues);
    }

    @GetMapping("/attribute/{attributeId}/paginated")
    public ResponseEntity<Page<ProductAttributeValueDTO>> getProductAttributeValuesByAttributePaginated(
            @PathVariable Long attributeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<ProductAttributeValueDTO> productAttributeValues = productAttributeValueService.findByAttributeId(attributeId, pageable);
        return ResponseEntity.ok(productAttributeValues);
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<Page<ProductAttributeValueDTO>> getProductAttributeValuesByCategory(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<ProductAttributeValueDTO> productAttributeValues = productAttributeValueService.findByCategoryId(categoryId, pageable);
        return ResponseEntity.ok(productAttributeValues);
    }

    // Search and Filter
    @GetMapping("/search/value")
    public ResponseEntity<Page<ProductAttributeValueDTO>> searchProductAttributeValuesByValue(
            @RequestParam String value,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<ProductAttributeValueDTO> productAttributeValues = productAttributeValueService.searchByValue(value, pageable);
        return ResponseEntity.ok(productAttributeValues);
    }

    @GetMapping("/attribute-type/{attributeType}")
    public ResponseEntity<Page<ProductAttributeValueDTO>> getProductAttributeValuesByAttributeType(
            @PathVariable String attributeType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<ProductAttributeValueDTO> productAttributeValues = productAttributeValueService.findByAttributeType(attributeType, pageable);
        return ResponseEntity.ok(productAttributeValues);
    }

    @GetMapping("/product/{productId}/key-attributes")
    public ResponseEntity<Page<ProductAttributeValueDTO>> getKeyAttributesByProduct(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<ProductAttributeValueDTO> productAttributeValues = productAttributeValueService.findByKeyAttributes(productId, pageable);
        return ResponseEntity.ok(productAttributeValues);
    }

    // Advanced Filtering
    @GetMapping("/search/advanced")
    public ResponseEntity<Page<ProductAttributeValueDTO>> searchProductAttributeValuesAdvanced(
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) Long attributeId,
            @RequestParam(required = false) String value,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<ProductAttributeValueDTO> productAttributeValues = productAttributeValueService.findByMultipleCriteria(
                productId, attributeId, value, isActive, pageable);
        return ResponseEntity.ok(productAttributeValues);
    }

    // Bulk Operations - Owner only
    @PostMapping("/bulk")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<List<ProductAttributeValueDTO>> createBulkProductAttributeValues(@Valid @RequestBody List<ProductAttributeValueCreateDTO> dtos) {
        logger.info("POST /api/product-attribute-values/bulk - Creating bulk product attribute values: count={}", dtos.size());
        List<ProductAttributeValueDTO> productAttributeValues = productAttributeValueService.createBulk(dtos);
        logger.info("POST /api/product-attribute-values/bulk - Bulk product attribute values created successfully: count={}", productAttributeValues.size());
        return ResponseEntity.status(HttpStatus.CREATED).body(productAttributeValues);
    }

    @PutMapping("/product/{productId}/bulk")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<List<ProductAttributeValueDTO>> updateProductAttributeValuesByProduct(
            @PathVariable Long productId, 
            @Valid @RequestBody List<ProductAttributeValueUpdateDTO> dtos) {
        logger.info("PUT /api/product-attribute-values/product/{}/bulk - Updating bulk product attribute values: count={}", productId, dtos.size());
        List<ProductAttributeValueDTO> productAttributeValues = productAttributeValueService.updateByProduct(productId, dtos);
        logger.info("PUT /api/product-attribute-values/product/{}/bulk - Bulk product attribute values updated successfully: count={}", productId, productAttributeValues.size());
        return ResponseEntity.ok(productAttributeValues);
    }

    @DeleteMapping("/product/{productId}")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<Void> deleteProductAttributeValuesByProduct(@PathVariable Long productId) {
        logger.info("DELETE /api/product-attribute-values/product/{} - Deleting all product attribute values for product", productId);
        productAttributeValueService.deleteByProduct(productId);
        logger.info("DELETE /api/product-attribute-values/product/{} - All product attribute values deleted successfully", productId);
        return ResponseEntity.noContent().build();
    }

    // Statistics
    @GetMapping("/stats/product/{productId}")
    public ResponseEntity<Long> getProductAttributeValueCountByProduct(@PathVariable Long productId) {
        long count = productAttributeValueService.countByProductId(productId);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/stats/attribute/{attributeId}")
    public ResponseEntity<Long> getProductAttributeValueCountByAttribute(@PathVariable Long attributeId) {
        long count = productAttributeValueService.countByAttributeId(attributeId);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/stats/category/{categoryId}")
    public ResponseEntity<Long> getProductAttributeValueCountByCategory(@PathVariable Long categoryId) {
        long count = productAttributeValueService.countByCategoryId(categoryId);
        return ResponseEntity.ok(count);
    }

    // Utility Methods
    @GetMapping("/distinct-values/attribute/{attributeId}")
    public ResponseEntity<List<String>> getDistinctValuesByAttribute(@PathVariable Long attributeId) {
        List<String> values = productAttributeValueService.getDistinctValuesByAttribute(attributeId);
        return ResponseEntity.ok(values);
    }

    @GetMapping("/product/{productId}/key-attributes/list")
    public ResponseEntity<List<ProductAttributeValueDTO>> getKeyAttributesByProductList(@PathVariable Long productId) {
        List<ProductAttributeValueDTO> productAttributeValues = productAttributeValueService.getKeyAttributesByProduct(productId);
        return ResponseEntity.ok(productAttributeValues);
    }

    @GetMapping("/product/{productId}/attribute-type/{attributeType}")
    public ResponseEntity<List<ProductAttributeValueDTO>> getProductAttributeValuesByProductAndAttributeType(
            @PathVariable Long productId, 
            @PathVariable String attributeType) {
        List<ProductAttributeValueDTO> productAttributeValues = productAttributeValueService.getByProductAndAttributeType(productId, attributeType);
        return ResponseEntity.ok(productAttributeValues);
    }
}
