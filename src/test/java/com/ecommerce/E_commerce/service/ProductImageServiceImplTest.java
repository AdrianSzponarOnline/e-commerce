package com.ecommerce.E_commerce.service;

import com.ecommerce.E_commerce.dto.productimage.ProductImageDTO;
import com.ecommerce.E_commerce.model.Product;
import com.ecommerce.E_commerce.model.ProductImage;
import com.ecommerce.E_commerce.repository.ProductImageRepository;
import com.ecommerce.E_commerce.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ProductImageServiceImplTest {

    @TempDir
    Path temp;

    private ProductImageServiceImpl buildService(ProductRepository productRepository,
                                                 ProductImageRepository imageRepository) throws IOException {
        ProductImageServiceImpl service = new ProductImageServiceImpl(productRepository, imageRepository, temp.toString());
        ReflectionTestUtils.setField(service, "maxUploadBytes", 10_000_000L);
        ReflectionTestUtils.setField(service, "allowedTypesCsv", "image/jpeg,image/png");
        ReflectionTestUtils.setField(service, "allowedExtensionsCsv", "jpg,jpeg,png,webp");
        ReflectionTestUtils.setField(service, "maxImagesPerProduct", 10);
        return service;
    }

    @Test
    void upload_saves_file_and_creates_thumbnail_and_updates_product() throws IOException {
        // Mock SecurityContext
        SecurityContext securityContext = mock(org.springframework.security.core.context.SecurityContext.class);
        Authentication authentication = mock(org.springframework.security.core.Authentication.class);
        when(authentication.getName()).thenReturn("owner@example.com");
        Set<GrantedAuthority> authorities = java.util.Set.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_OWNER"));
        doReturn(authorities).when(authentication).getAuthorities();
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        ProductRepository productRepository = mock(ProductRepository.class);
        ProductImageRepository imageRepository = mock(ProductImageRepository.class);
        Product product = new Product();
        product.setId(5L);
        product.setName("Test");
        when(productRepository.findById(5L)).thenReturn(Optional.of(product));
        when(imageRepository.findByProductId(5L)).thenReturn(List.of());
        when(imageRepository.findByProductIdAndIsThumbnailTrue(5L)).thenReturn(List.of());

        ArgumentCaptor<ProductImage> imageCaptor = ArgumentCaptor.forClass(ProductImage.class);
        when(imageRepository.save(imageCaptor.capture())).thenAnswer(inv -> {
            ProductImage img = inv.getArgument(0);
            img.setId(11L);
            return img;
        });

        ProductImageServiceImpl service = buildService(productRepository, imageRepository);
        MockMultipartFile file = new MockMultipartFile("file", "a.jpg", "image/jpeg", new byte[]{1,2,3});

        ProductImageDTO dto = service.upload(5L, file, "alt", true);

        // repo save called with proper data
        ProductImage saved = imageCaptor.getValue();
        assertThat(saved.getProduct().getId()).isEqualTo(5L);
        assertThat(saved.getAltText()).isEqualTo("alt");
        assertThat(saved.getIsThumbnail()).isTrue();
        assertThat(saved.getUrl()).startsWith("/uploads/products/5/");

        verify(productRepository, times(1)).save(any(Product.class));
        assertThat(dto.productId()).isEqualTo(5L);
        assertThat(dto.isThumbnail()).isTrue();
    }

    @Test
    void upload_rejects_wrong_content_type_and_too_large() throws IOException {
        ProductRepository productRepository = mock(ProductRepository.class);
        ProductImageRepository imageRepository = mock(ProductImageRepository.class);
        when(productRepository.findById(1L)).thenReturn(Optional.of(new Product()));

        ProductImageServiceImpl service = buildService(productRepository, imageRepository);

        MockMultipartFile wrongType = new MockMultipartFile("file", "a.gif", "image/gif", new byte[]{1});
        assertThrows(com.ecommerce.E_commerce.exception.InvalidOperationException.class, () -> service.upload(1L, wrongType, null, false));

        ReflectionTestUtils.setField(service, "maxUploadBytes", 1L);
        MockMultipartFile tooLarge = new MockMultipartFile("file", "a.jpg", "image/jpeg", new byte[]{1,2});
        assertThrows(com.ecommerce.E_commerce.exception.InvalidOperationException.class, () -> service.upload(1L, tooLarge, null, false));
    }

    @Test
    void setThumbnail_unsets_others_and_updates_product() throws IOException {
        ProductRepository productRepository = mock(ProductRepository.class);
        ProductImageRepository imageRepository = mock(ProductImageRepository.class);
        Product product = new Product();
        product.setId(9L);
        when(productRepository.findById(9L)).thenReturn(Optional.of(product));

        ProductImage existingThumb = new ProductImage();
        existingThumb.setId(1L);
        existingThumb.setProduct(product);
        existingThumb.setIsThumbnail(true);
        existingThumb.setUpdatedAt(Instant.now());

        ProductImage target = new ProductImage();
        target.setId(2L);
        target.setProduct(product);
        target.setUrl("/uploads/products/9/x.jpg");

        when(imageRepository.findByProductIdAndIsThumbnailTrue(9L)).thenReturn(List.of(existingThumb));
        when(imageRepository.findById(2L)).thenReturn(Optional.of(target));

        ProductImageServiceImpl service = buildService(productRepository, imageRepository);
        ProductImageDTO dto = service.setThumbnail(9L, 2L);

        assertThat(existingThumb.getIsThumbnail()).isFalse();
        assertThat(dto.isThumbnail()).isTrue();
        verify(productRepository, times(1)).save(any(Product.class));
    }
}


