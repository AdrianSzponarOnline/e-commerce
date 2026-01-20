package com.ecommerce.E_commerce.controller;

import com.ecommerce.E_commerce.dto.product.ProductCreateDTO;
import com.ecommerce.E_commerce.dto.product.ProductDTO;
import com.ecommerce.E_commerce.dto.product.ProductSummaryDTO;
import com.ecommerce.E_commerce.dto.product.ProductUpdateDTO;
import com.ecommerce.E_commerce.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class ProductController {

    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);
    private final ProductService productService;

    @PostMapping
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ProductDTO> createProduct(@Valid @RequestBody ProductCreateDTO dto) {
        logger.info("POST /api/products - Creating product: name={}, categoryId={}", dto.name(), dto.categoryId());
        ProductDTO product = productService.create(dto);
        logger.info("POST /api/products - Product created successfully: productId={}, name={}, sku={}", product.id(), product.name(), product.sku());
        return ResponseEntity.status(HttpStatus.CREATED).body(product);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ProductDTO> updateProduct(@PathVariable Long id, @Valid @RequestBody ProductUpdateDTO dto) {
        logger.info("PUT /api/products/{} - Updating product", id);
        ProductDTO product = productService.update(id, dto);
        logger.info("PUT /api/products/{} - Product updated successfully: name={}", id, product.name());
        return ResponseEntity.ok(product);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        logger.info("DELETE /api/products/{} - Deleting product", id);
        productService.delete(id);
        logger.info("DELETE /api/products/{} - Product deleted successfully", id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable Long id) {
        logger.info("GET /api/products/{} - Retrieving product", id);
        ProductDTO product = productService.getById(id);
        return ResponseEntity.ok(product);
    }

    @GetMapping("/slug/{seoSlug}")
    public ResponseEntity<ProductDTO> getProductBySlug(@PathVariable String seoSlug) {
        logger.info("GET /api/products/{} - Retrieving product", seoSlug);
        ProductDTO product = productService.getBySeoSlug(seoSlug);
        return ResponseEntity.ok(product);
    }

    @GetMapping("/sku/{sku}")
    public ResponseEntity<ProductDTO> getProductBySku(@PathVariable String sku) {
        ProductDTO product = productService.getBySku(sku);
        return ResponseEntity.ok(product);
    }

    @GetMapping
    public ResponseEntity<Page<ProductSummaryDTO>> getAllProducts(
            @PageableDefault(sort = "name", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<ProductSummaryDTO> products = productService.findAll(pageable);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<Page<ProductSummaryDTO>> getProductsByCategory(
            @PathVariable Long categoryId,
            @PageableDefault(sort = "name", direction = Sort.Direction.DESC) Pageable pageable
            ) {
        Page<ProductSummaryDTO> products = productService.findByCategory(categoryId, pageable);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/category-slug/{categorySlug}")
    public ResponseEntity<Page<ProductSummaryDTO>> getProductsByCategorySlug(
            @PathVariable String categorySlug,
            @PageableDefault(sort = "name", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<ProductSummaryDTO> products = productService.findByCategorySlug(categorySlug, pageable);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/featured")
    public ResponseEntity<Page<ProductSummaryDTO>> getFeaturedProducts(
            @RequestParam(defaultValue = "true") Boolean isFeatured,
            @PageableDefault(sort = "name", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<ProductSummaryDTO> products = productService.findByFeatured(isFeatured, pageable);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/active")
    public ResponseEntity<Page<ProductSummaryDTO>> getActiveProducts(
            @RequestParam(defaultValue = "true") Boolean isActive,
            @PageableDefault(sort = "name", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<ProductSummaryDTO> products = productService.findByActive(isActive, pageable);
        return ResponseEntity.ok(products);
    }

    // Statistics
    @GetMapping("/stats/category/{categoryId}/count")
    public ResponseEntity<Long> getProductCountByCategory(@PathVariable Long categoryId) {
        long count = productService.countByCategory(categoryId);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/stats/featured/count")
    public ResponseEntity<Long> getFeaturedProductCount(@RequestParam(defaultValue = "true") Boolean isFeatured) {
        long count = productService.countByFeatured(isFeatured);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/stats/active/count")
    public ResponseEntity<Long> getActiveProductCount(@RequestParam(defaultValue = "true") Boolean isActive) {
        long count = productService.countByActive(isActive);
        return ResponseEntity.ok(count);
    }
}
