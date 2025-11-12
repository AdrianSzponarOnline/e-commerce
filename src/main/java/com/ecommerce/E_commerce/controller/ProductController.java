package com.ecommerce.E_commerce.controller;

import com.ecommerce.E_commerce.dto.product.ProductCreateDTO;
import com.ecommerce.E_commerce.dto.product.ProductDTO;
import com.ecommerce.E_commerce.dto.product.ProductSummaryDTO;
import com.ecommerce.E_commerce.dto.product.ProductUpdateDTO;
import com.ecommerce.E_commerce.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "*")
public class ProductController {

    private final ProductService productService;

    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    // CRUD Operations - Owner only
    @PostMapping
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ProductDTO> createProduct(@Valid @RequestBody ProductCreateDTO dto) {
        ProductDTO product = productService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(product);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ProductDTO> updateProduct(@PathVariable Long id, @Valid @RequestBody ProductUpdateDTO dto) {
        ProductDTO product = productService.update(id, dto);
        return ResponseEntity.ok(product);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // Single Product Retrieval
    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable Long id) {
        ProductDTO product = productService.getById(id);
        return ResponseEntity.ok(product);
    }

    @GetMapping("/slug/{seoSlug}")
    public ResponseEntity<ProductDTO> getProductBySlug(@PathVariable String seoSlug) {
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
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        Page<ProductSummaryDTO> products = productService.findAll(page, size, sortBy, sortDir);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<Page<ProductSummaryDTO>> getProductsByCategory(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        Page<ProductSummaryDTO> products = productService.findByCategory(categoryId, page, size, sortBy, sortDir);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/category-slug/{categorySlug}")
    public ResponseEntity<Page<ProductSummaryDTO>> getProductsByCategorySlug(
            @PathVariable String categorySlug,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        Page<ProductSummaryDTO> products = productService.findByCategorySlug(categorySlug, page, size, sortBy, sortDir);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/price-range")
    public ResponseEntity<Page<ProductSummaryDTO>> getProductsByPriceRange(
            @RequestParam BigDecimal minPrice,
            @RequestParam BigDecimal maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "price") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        Page<ProductSummaryDTO> products = productService.findByPriceRange(minPrice, maxPrice, page, size, sortBy, sortDir);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/featured")
    public ResponseEntity<Page<ProductSummaryDTO>> getFeaturedProducts(
            @RequestParam(defaultValue = "true") Boolean isFeatured,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        Page<ProductSummaryDTO> products = productService.findByFeatured(isFeatured, page, size, sortBy, sortDir);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/active")
    public ResponseEntity<Page<ProductSummaryDTO>> getActiveProducts(
            @RequestParam(defaultValue = "true") Boolean isActive,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        Page<ProductSummaryDTO> products = productService.findByActive(isActive, page, size, sortBy, sortDir);
        return ResponseEntity.ok(products);
    }

    // Search and Filter
    @GetMapping("/search/name")
    public ResponseEntity<Page<ProductSummaryDTO>> searchProductsByName(
            @RequestParam String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        Page<ProductSummaryDTO> products = productService.searchByName(name, page, size, sortBy, sortDir);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/search/description")
    public ResponseEntity<Page<ProductSummaryDTO>> searchProductsByDescription(
            @RequestParam String description,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        Page<ProductSummaryDTO> products = productService.searchByDescription(description, page, size, sortBy, sortDir);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/category/{categoryId}/price-range")
    public ResponseEntity<Page<ProductSummaryDTO>> getProductsByCategoryAndPriceRange(
            @PathVariable Long categoryId,
            @RequestParam BigDecimal minPrice,
            @RequestParam BigDecimal maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "price") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        Page<ProductSummaryDTO> products = productService.findByCategoryAndPriceRange(categoryId, minPrice, maxPrice, page, size, sortBy, sortDir);
        return ResponseEntity.ok(products);
    }

    // Advanced Filtering
    @GetMapping("/category/{categoryId}/featured")
    public ResponseEntity<Page<ProductSummaryDTO>> getProductsByCategoryAndFeatured(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "true") Boolean isFeatured,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        Page<ProductSummaryDTO> products = productService.findByCategoryAndFeatured(categoryId, isFeatured, page, size, sortBy, sortDir);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/price-range/featured")
    public ResponseEntity<Page<ProductSummaryDTO>> getProductsByPriceRangeAndFeatured(
            @RequestParam BigDecimal minPrice,
            @RequestParam BigDecimal maxPrice,
            @RequestParam(defaultValue = "true") Boolean isFeatured,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "price") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        Page<ProductSummaryDTO> products = productService.findByPriceRangeAndFeatured(minPrice, maxPrice, isFeatured, page, size, sortBy, sortDir);
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
