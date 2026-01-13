package com.ecommerce.E_commerce.service;

import com.ecommerce.E_commerce.dto.productimage.ProductImageDTO;
import com.ecommerce.E_commerce.exception.InvalidOperationException;
import com.ecommerce.E_commerce.exception.ResourceNotFoundException;
import com.ecommerce.E_commerce.model.Product;
import com.ecommerce.E_commerce.model.ProductImage;
import com.ecommerce.E_commerce.repository.ProductImageRepository;
import com.ecommerce.E_commerce.repository.ProductRepository;
import lombok.Getter;
import lombok.Setter;
import org.apache.tika.Tika;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
@Getter
@Setter
public class ProductImageServiceImpl implements ProductImageService {

    private static final Logger logger = LoggerFactory.getLogger(ProductImageServiceImpl.class);
    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final Tika tika = new Tika();

    private final Path uploadRoot;

    @Value("${app.upload-max-bytes:5242880}")
    private long maxUploadBytes;

    @Value("${app.upload-allowed-types:image/jpeg,image/png,image/webp}")
    private String allowedTypesCsv;

    @Value("${app.upload-allowed-extensions:jpg,jpeg,png,webp}")
    private String allowedExtensionsCsv;

    @Value("${app.max-images-per-product:10}")
    private int maxImagesPerProduct;

    private final ImageUrlService imageUrlService;

    public ProductImageServiceImpl(ProductRepository productRepository,
                                   ProductImageRepository productImageRepository,
                                   ImageUrlService imageUrlService,
                                   @Value("${app.upload-dir:uploads}") String uploadDir) throws IOException {
        this.productRepository = productRepository;
        this.productImageRepository = productImageRepository;
        this.imageUrlService = imageUrlService;
        this.uploadRoot = Paths.get(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(this.uploadRoot.resolve("products"));
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "product_images", key = "#productId")
    public List<ProductImageDTO> listByProduct(Long productId) {
        verifyProduct(productId);
        return productImageRepository.findByProductId(productId).stream().map(this::toDTO).toList();
    }

    @Override
    @CacheEvict(value = "product_images", key = "#productId")
    public ProductImageDTO upload(Long productId, MultipartFile file, String altText, boolean isThumbnail) {
        logger.info("Starting upload: productId={}, originalName={}, isThumbnail={}",
                productId, file.getOriginalFilename(), isThumbnail);


        Product product = verifyProduct(productId);
        checkImagePermission(product);
        checkProductImageLimit(productId);

        String extension = validateAndAnalyzeFile(file);

        String storedFilename = storeFileOnDisk(file, productId, extension);

        ProductImage savedImage = saveImageMetadata(product, storedFilename, altText, isThumbnail);

        if (isThumbnail) {
            setThumbnailAtomically(productId, savedImage.getId(), savedImage.getUrl());
        }

        return toDTO(savedImage);
    }

    @Override
    @CacheEvict(value = "product_images", key = "#productId")
    public void delete(Long productId, Long imageId) {
        Product product = verifyProduct(productId);
        
        checkImagePermission(product);
        
        ProductImage image = productImageRepository.findById(imageId)
                .orElseThrow(() -> new ResourceNotFoundException("Image not found: " + imageId));
        if (!image.getProduct().getId().equals(productId)) {
            throw new ResourceNotFoundException("Image does not belong to product");
        }

        if (Boolean.TRUE.equals(image.getIsThumbnail())) {
            product.setThumbnailUrl(null);
            productRepository.save(product);
        }

     
        productImageRepository.delete(image);
    }

    @Override
    public ProductImageDTO setThumbnail(Long productId, Long imageId) {
        Product product = verifyProduct(productId);
        
        checkImagePermission(product);
        
        ProductImage image = productImageRepository.findById(imageId)
                .orElseThrow(() -> new ResourceNotFoundException("Image not found: " + imageId));
        if (!image.getProduct().getId().equals(productId)) {
            throw new ResourceNotFoundException("Image does not belong to product");
        }

        setThumbnailAtomically(productId, imageId, image.getUrl());

        return toDTO(image);
    }
    
   
    private void setThumbnailAtomically(Long productId, Long newThumbnailId, String thumbnailUrl) {
        List<ProductImage> existingThumbnails = productImageRepository.findByProductIdAndIsThumbnailTrue(productId);
        for (ProductImage img : existingThumbnails) {
            if (!img.getId().equals(newThumbnailId)) {
                img.setIsThumbnail(false);
            }
        }
        productImageRepository.saveAll(existingThumbnails);

     
        ProductImage newThumbnail = productImageRepository.findById(newThumbnailId)
                .orElseThrow(() -> new ResourceNotFoundException("Image not found: " + newThumbnailId));
        newThumbnail.setIsThumbnail(true);
        productImageRepository.save(newThumbnail);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));
        product.setThumbnailUrl(thumbnailUrl);
        productRepository.save(product);
    }

    
    private void checkImagePermission(Product product) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new AccessDeniedException("User not authenticated");
        }
        
        boolean isOwner = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(auth -> auth.equals("ROLE_OWNER"));
        
     
        if (!isOwner) {
            throw new AccessDeniedException("Only OWNER can manage product images");
        }
    }


    private String getExtensionFromMimeType(String mimeType) {
        if (mimeType == null) return null;

        return switch (mimeType.toLowerCase()) {
            case "image/jpeg", "image/jpg" -> "jpg";
            case "image/png" -> "png";
            case "image/webp" -> "webp";
            default -> null;
        };
    }

    private boolean isAllowedContentType(String contentType) {
        if (contentType == null || contentType.isEmpty()) {
            return false;
        }
        List<String> allowed = Arrays.stream(allowedTypesCsv.split(","))
                .map(String::trim)
                .toList();
        return allowed.stream().anyMatch(contentType::equalsIgnoreCase);
    }
    
    private Product verifyProduct(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));
    }

    private void checkProductImageLimit(Long productId) {
        long currentCount = productImageRepository.findByProductId(productId).size();
        if (currentCount >= maxImagesPerProduct) {
            throw new InvalidOperationException("Image limit for this product exceeded (" + maxImagesPerProduct + ")");
        }
    }

    private String validateAndAnalyzeFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidOperationException("File is required");
        }
        if (file.getSize() > maxUploadBytes) {
            throw new InvalidOperationException("File too big (limit " + maxUploadBytes + " bytes)");
        }

        String detectedType;
        try {
            detectedType = tika.detect(file.getInputStream());
        } catch (IOException e) {
            throw new InvalidOperationException("Failed to analyze file content");
        }

        if (!isAllowedContentType(detectedType)) {
            throw new InvalidOperationException("Invalid file content type. Detected: " + detectedType);
        }

        String extension = getExtensionFromMimeType(detectedType);
        if (extension == null) {
            throw new InvalidOperationException("Could not determine extension for MIME type: " + detectedType);
        }

        return extension;
    }

    private String storeFileOnDisk(MultipartFile file, Long productId, String extension) {
        String filename = UUID.randomUUID() + "." + extension;
        Path productDir = uploadRoot.resolve("products").resolve(String.valueOf(productId));

        try {
            Files.createDirectories(productDir);
            Path target = productDir.resolve(filename);
            file.transferTo(target);
            return filename;
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file on disk", e);
        }
    }
    private ProductImage saveImageMetadata(Product product, String filename, String altText, boolean isThumbnail) {
        String url = "/uploads/products/" + product.getId() + "/" + filename;
        Instant now = Instant.now();

        ProductImage image = new ProductImage();
        image.setProduct(product);
        image.setUrl(url);
        image.setAltText(altText);
        image.setIsThumbnail(isThumbnail);
        image.setIsActive(true);
        image.setCreatedAt(now);
        image.setUpdatedAt(now);

        return productImageRepository.save(image);
    }

    private ProductImageDTO toDTO(ProductImage image) {
        String fullUrl = imageUrlService.buildFullUrl(image.getUrl());
        
        return new ProductImageDTO(
                image.getId(),
                image.getProduct().getId(),
                fullUrl,
                image.getAltText(),
                image.getIsThumbnail(),
                image.getCreatedAt(),
                image.getUpdatedAt()
        );
    }
}


