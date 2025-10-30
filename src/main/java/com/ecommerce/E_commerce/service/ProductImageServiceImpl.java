package com.ecommerce.E_commerce.service;

import com.ecommerce.E_commerce.dto.productimage.ProductImageDTO;
import com.ecommerce.E_commerce.exception.ResourceNotFoundException;
import com.ecommerce.E_commerce.model.Product;
import com.ecommerce.E_commerce.model.ProductImage;
import com.ecommerce.E_commerce.repository.ProductImageRepository;
import com.ecommerce.E_commerce.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ProductImageServiceImpl implements ProductImageService {

    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;

    private final Path uploadRoot;

    @Value("${app.upload-max-bytes:5242880}")
    private long maxUploadBytes;

    @Value("${app.upload-allowed-types:image/jpeg,image/png,image/webp}")
    private String allowedTypesCsv;

    @Value("${app.max-images-per-product:10}")
    private int maxImagesPerProduct;

    public ProductImageServiceImpl(ProductRepository productRepository,
                                   ProductImageRepository productImageRepository,
                                   @Value("${app.upload-dir:uploads}") String uploadDir) throws IOException {
        this.productRepository = productRepository;
        this.productImageRepository = productImageRepository;
        this.uploadRoot = Paths.get(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(this.uploadRoot.resolve("products"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductImageDTO> listByProduct(Long productId) {
        verifyProduct(productId);
        return productImageRepository.findByProductId(productId).stream().map(this::toDTO).toList();
    }

    @Override
    public ProductImageDTO upload(Long productId, MultipartFile file, String altText, boolean isThumbnail) {
        Product product = verifyProduct(productId);

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Plik jest wymagany");
        }
        if (file.getSize() > maxUploadBytes) {
            throw new IllegalArgumentException("Plik jest za duży (limit " + maxUploadBytes + " B)");
        }
        String contentType = file.getContentType() == null ? "" : file.getContentType();
        boolean allowed = List.of(allowedTypesCsv.split(",")).stream().map(String::trim).anyMatch(contentType::equalsIgnoreCase);
        if (!allowed) {
            throw new IllegalArgumentException("Niedozwolony typ pliku: " + contentType);
        }

        long currentCount = productImageRepository.findByProductId(productId).size();
        if (currentCount >= maxImagesPerProduct) {
            throw new IllegalArgumentException("Przekroczono limit zdjęć dla produktu (" + maxImagesPerProduct + ")");
        }

        String original = StringUtils.cleanPath(file.getOriginalFilename() == null ? "image" : file.getOriginalFilename());
        String extension = original.contains(".") ? original.substring(original.lastIndexOf('.')) : "";
        String filename = UUID.randomUUID() + extension;

        Path productDir = uploadRoot.resolve("products").resolve(String.valueOf(productId));
        try {
            Files.createDirectories(productDir);
            Path target = productDir.resolve(filename);
            file.transferTo(target);
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file", e);
        }

        String url = "/uploads/products/" + productId + "/" + filename;

        ProductImage image = new ProductImage();
        image.setProduct(product);
        image.setUrl(url);
        image.setAltText(altText);
        image.setIsThumbnail(isThumbnail);
        image.setCreatedAt(Instant.now());
        image.setUpdatedAt(Instant.now());
        image.setIsActive(true);

        ProductImage saved = productImageRepository.save(image);

        if (isThumbnail) {
            // Ensure single thumbnail per product
            productImageRepository.findByProductIdAndIsThumbnailTrue(productId)
                    .stream()
                    .filter(img -> !img.getId().equals(saved.getId()))
                    .forEach(img -> { img.setIsThumbnail(false); img.setUpdatedAt(Instant.now()); });
            product.setThumbnailUrl(url);
            product.setUpdatedAt(Instant.now());
            productRepository.save(product);
        }

        return toDTO(saved);
    }

    @Override
    public void delete(Long productId, Long imageId) {
        verifyProduct(productId);
        ProductImage image = productImageRepository.findById(imageId)
                .orElseThrow(() -> new ResourceNotFoundException("Image not found: " + imageId));
        if (!image.getProduct().getId().equals(productId)) {
            throw new ResourceNotFoundException("Image does not belong to product");
        }

        productImageRepository.delete(image);
        // Note: we don't delete the physical file to keep example simple
    }

    @Override
    public ProductImageDTO setThumbnail(Long productId, Long imageId) {
        Product product = verifyProduct(productId);
        ProductImage image = productImageRepository.findById(imageId)
                .orElseThrow(() -> new ResourceNotFoundException("Image not found: " + imageId));
        if (!image.getProduct().getId().equals(productId)) {
            throw new ResourceNotFoundException("Image does not belong to product");
        }

        // unset existing thumbnails
        productImageRepository.findByProductIdAndIsThumbnailTrue(productId)
                .forEach(img -> { img.setIsThumbnail(false); img.setUpdatedAt(Instant.now()); });

        image.setIsThumbnail(true);
        image.setUpdatedAt(Instant.now());
        productImageRepository.save(image);

        product.setThumbnailUrl(image.getUrl());
        product.setUpdatedAt(Instant.now());
        productRepository.save(product);

        return toDTO(image);
    }

    private Product verifyProduct(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));
    }

    private ProductImageDTO toDTO(ProductImage image) {
        return new ProductImageDTO(
                image.getId(),
                image.getProduct().getId(),
                image.getUrl(),
                image.getAltText(),
                image.getIsThumbnail(),
                image.getCreatedAt(),
                image.getUpdatedAt()
        );
    }
}


