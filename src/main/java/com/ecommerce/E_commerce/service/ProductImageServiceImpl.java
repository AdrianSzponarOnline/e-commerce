package com.ecommerce.E_commerce.service;

import com.ecommerce.E_commerce.dto.productimage.ProductImageDTO;
import com.ecommerce.E_commerce.exception.InvalidOperationException;
import com.ecommerce.E_commerce.exception.ResourceNotFoundException;
import com.ecommerce.E_commerce.model.Product;
import com.ecommerce.E_commerce.model.ProductImage;
import com.ecommerce.E_commerce.repository.ProductImageRepository;
import com.ecommerce.E_commerce.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

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
    public List<ProductImageDTO> listByProduct(Long productId) {
        verifyProduct(productId);
        return productImageRepository.findByProductId(productId).stream().map(this::toDTO).toList();
    }

    @Override
    public ProductImageDTO upload(Long productId, MultipartFile file, String altText, boolean isThumbnail) {
        Product product = verifyProduct(productId);
        
        checkImagePermission(product);

        if (file == null || file.isEmpty()) {
            throw new InvalidOperationException("Plik jest wymagany");
        }
        if (file.getSize() > maxUploadBytes) {
            throw new InvalidOperationException("Plik jest za duży (limit " + maxUploadBytes + " B)");
        }
        
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            throw new InvalidOperationException("Nazwa pliku jest wymagana");
        }
        
        String contentType = file.getContentType() == null ? "" : file.getContentType();
        if (!isAllowedContentType(contentType)) {
            throw new InvalidOperationException("Niedozwolony typ pliku: " + contentType);
        }
        
        String sanitizedFilename = sanitizeFilename(originalFilename);
        String extension = getFileExtension(sanitizedFilename);
        
        if (extension.isEmpty() && !contentType.isEmpty()) {
            extension = getExtensionFromContentType(contentType);
        }
        
        if (extension.isEmpty()) {
            throw new InvalidOperationException("Nie można określić rozszerzenia pliku. Upewnij się, że plik ma rozszerzenie (jpg, png, webp) lub poprawny Content-Type");
        }
        
        if (!isAllowedExtension(extension)) {
            throw new InvalidOperationException("Niedozwolone rozszerzenie pliku: " + extension);
        }

        long currentCount = productImageRepository.findByProductId(productId).size();
        if (currentCount >= maxImagesPerProduct) {
            throw new InvalidOperationException("Przekroczono limit zdjęć dla produktu (" + maxImagesPerProduct + ")");
        }

        String filename = UUID.randomUUID() + "." + extension;

        Path productDir = uploadRoot.resolve("products").resolve(String.valueOf(productId));
        try {
            Files.createDirectories(productDir);
            Path target = productDir.resolve(filename);
            file.transferTo(target);
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file", e);
        }

        String url = "/uploads/products/" + productId + "/" + filename;

        Instant now = Instant.now();
        ProductImage image = new ProductImage();
        image.setProduct(product);
        image.setUrl(url);
        image.setAltText(altText);
        image.setIsThumbnail(isThumbnail);
        image.setIsActive(true);
        image.setCreatedAt(now);
        image.setUpdatedAt(now);

        ProductImage saved = productImageRepository.save(image);

        if (isThumbnail) {
            setThumbnailAtomically(productId, saved.getId(), url);
        }

        return toDTO(saved);
    }

    @Override
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
        
        // Security check: User can only set thumbnails for products (unless OWNER)
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
    

    private String sanitizeFilename(String filename) {
        if (filename == null) {
            return "image";
        }
        
        String sanitized = StringUtils.cleanPath(filename);
        
        // Usuwamy niebezpieczne sekwencje (../, ..\, etc.) ale zachowujemy kropki w nazwie pliku
        sanitized = sanitized.replaceAll("\\.\\./", "");
        sanitized = sanitized.replaceAll("\\.\\.\\\\", "");
        sanitized = sanitized.replaceAll("[/\\\\]", "");
        
        if (sanitized.trim().isEmpty()) {
            return "image";
        }
        
        return sanitized;
    }
    
    /**
     * Extracts file extension from filename (without the dot).
     */
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        String ext = filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
        return ext;
    }
    
    /**
     * Validates if file extension is allowed.
     */
    private boolean isAllowedExtension(String extension) {
        if (extension == null || extension.isEmpty()) {
            return false;
        }
        Set<String> allowed = Arrays.stream(allowedExtensionsCsv.split(","))
                .map(String::trim)
                .map(String::toLowerCase)
                .collect(Collectors.toSet());
        return allowed.contains(extension.toLowerCase());
    }
    
    /**
     * Validates if Content-Type is allowed.
     */
    private boolean isAllowedContentType(String contentType) {
        if (contentType == null || contentType.isEmpty()) {
            return false;
        }
        List<String> allowed = Arrays.stream(allowedTypesCsv.split(","))
                .map(String::trim)
                .collect(Collectors.toList());
        return allowed.stream().anyMatch(contentType::equalsIgnoreCase);
    }
    
    /**
     * Extracts file extension from Content-Type.
     */
    private String getExtensionFromContentType(String contentType) {
        if (contentType == null || contentType.isEmpty()) {
            return "";
        }
        
        // Mapowanie Content-Type na rozszerzenie
        if (contentType.equalsIgnoreCase("image/jpeg") || contentType.equalsIgnoreCase("image/jpg")) {
            return "jpg";
        } else if (contentType.equalsIgnoreCase("image/png")) {
            return "png";
        } else if (contentType.equalsIgnoreCase("image/webp")) {
            return "webp";
        }
        
        return "";
    }
    
    private Product verifyProduct(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));
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


