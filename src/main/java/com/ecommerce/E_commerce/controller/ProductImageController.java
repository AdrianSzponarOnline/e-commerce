package com.ecommerce.E_commerce.controller;

import com.ecommerce.E_commerce.dto.productimage.ProductImageDTO;
import com.ecommerce.E_commerce.service.ProductImageService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/products/{productId}/images")
@CrossOrigin(origins = "*")
public class ProductImageController {

    private final ProductImageService productImageService;

    public ProductImageController(ProductImageService productImageService) {
        this.productImageService = productImageService;
    }

    @GetMapping
    public ResponseEntity<List<ProductImageDTO>> list(@PathVariable Long productId) {
        return ResponseEntity.ok(productImageService.listByProduct(productId));
    }

    @PostMapping(consumes = {"multipart/form-data"})
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ProductImageDTO> upload(@PathVariable Long productId,
                                                  @RequestPart("file") MultipartFile file,
                                                  @RequestPart(value = "altText", required = false) String altText,
                                                  @RequestPart(value = "isThumbnail", required = false) Boolean isThumbnail) {
        ProductImageDTO dto = productImageService.upload(productId, file, altText, Boolean.TRUE.equals(isThumbnail));
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @DeleteMapping("/{imageId}")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<Void> delete(@PathVariable Long productId, @PathVariable Long imageId) {
        productImageService.delete(productId, imageId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{imageId}/thumbnail")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ProductImageDTO> setThumbnail(@PathVariable Long productId, @PathVariable Long imageId) {
        return ResponseEntity.ok(productImageService.setThumbnail(productId, imageId));
    }
}


