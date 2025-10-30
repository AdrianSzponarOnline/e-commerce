package com.ecommerce.E_commerce.repository;

import com.ecommerce.E_commerce.model.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {

    List<ProductImage> findByProductId(Long productId);

    long countByProductIdAndIsThumbnailTrue(Long productId);

    List<ProductImage> findByProductIdAndIsThumbnailTrue(Long productId);
}


