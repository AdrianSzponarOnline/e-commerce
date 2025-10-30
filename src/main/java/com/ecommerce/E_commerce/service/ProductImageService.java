package com.ecommerce.E_commerce.service;

import com.ecommerce.E_commerce.dto.productimage.ProductImageDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ProductImageService {

    List<ProductImageDTO> listByProduct(Long productId);

    ProductImageDTO upload(Long productId, MultipartFile file, String altText, boolean isThumbnail);

    void delete(Long productId, Long imageId);

    ProductImageDTO setThumbnail(Long productId, Long imageId);
}


