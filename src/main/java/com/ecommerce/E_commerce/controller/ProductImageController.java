package com.ecommerce.E_commerce.controller;

import com.ecommerce.E_commerce.dto.productimage.ProductImageDTO;
import com.ecommerce.E_commerce.service.ProductImageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(ProductImageController.class);
    private final ProductImageService productImageService;

    public ProductImageController(ProductImageService productImageService) {
        this.productImageService = productImageService;
    }

    @GetMapping
    public ResponseEntity<List<ProductImageDTO>> list(@PathVariable("productId") Long productId) {
        return ResponseEntity.ok(productImageService.listByProduct(productId));
    }

    @PostMapping(consumes = {"multipart/form-data"})
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ProductImageDTO> upload(
            @PathVariable("productId") Long productId,
            @RequestPart("file") MultipartFile file,
            @RequestParam(value = "altText", required = false) String altText,
            @RequestParam(value = "isThumbnail", required = false) Boolean isThumbnail) {

        logger.info("POST /api/products/{}/images - Uploading image: fileName={}, isThumbnail={}", productId, file.getOriginalFilename(), isThumbnail);
        ProductImageDTO dto = productImageService.upload(productId, file, altText, Boolean.TRUE.equals(isThumbnail));
        logger.info("POST /api/products/{}/images - Image uploaded successfully: imageId={}", productId, dto.id());
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @DeleteMapping("/{imageId}")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<Void> delete(
            @PathVariable("productId") Long productId,
            @PathVariable("imageId") Long imageId) {

        logger.info("DELETE /api/products/{}/images/{} - Deleting image", productId, imageId);
        productImageService.delete(productId, imageId);
        logger.info("DELETE /api/products/{}/images/{} - Image deleted successfully", productId, imageId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{imageId}/thumbnail")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ProductImageDTO> setThumbnail(
            @PathVariable("productId") Long productId,
            @PathVariable("imageId") Long imageId) {

        logger.info("POST /api/products/{}/images/{}/thumbnail - Setting thumbnail", productId, imageId);
        ProductImageDTO dto = productImageService.setThumbnail(productId, imageId);
        logger.info("POST /api/products/{}/images/{}/thumbnail - Thumbnail set successfully", productId, imageId);
        return ResponseEntity.ok(dto);
    }
}