package com.ecommerce.E_commerce.service;

import com.ecommerce.E_commerce.dto.productattributevalue.ProductAttributeValueCreateDTO;
import com.ecommerce.E_commerce.dto.productattributevalue.ProductAttributeValueDTO;
import com.ecommerce.E_commerce.dto.productattributevalue.ProductAttributeValueUpdateDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

public interface ProductAttributeValueService {
    
    // CRUD Operations - Owner only
    @PreAuthorize("hasRole('OWNER')")
    ProductAttributeValueDTO create(ProductAttributeValueCreateDTO dto);
    
    @PreAuthorize("hasRole('OWNER')")
    ProductAttributeValueDTO update(Long id, ProductAttributeValueUpdateDTO dto);
    
    @PreAuthorize("hasRole('OWNER')")
    void delete(Long id);
    
    // Single ProductAttributeValue Retrieval
    ProductAttributeValueDTO getById(Long id);
    List<ProductAttributeValueDTO> getByProductId(Long productId);
    List<ProductAttributeValueDTO> getByAttributeId(Long attributeId);
    ProductAttributeValueDTO getByProductAndAttribute(Long productId, Long attributeId);
    
    // Paginated Lists
    Page<ProductAttributeValueDTO> findAll(Pageable pageable);
    Page<ProductAttributeValueDTO> findByProductId(Long productId, Pageable pageable);
    Page<ProductAttributeValueDTO> findByAttributeId(Long attributeId, Pageable pageable);
    Page<ProductAttributeValueDTO> findByCategoryId(Long categoryId, Pageable pageable);
    Page<ProductAttributeValueDTO> findByValue(String value, Pageable pageable);
    
    // Search and Filter
    Page<ProductAttributeValueDTO> searchByValue(String value, Pageable pageable);
    Page<ProductAttributeValueDTO> findByAttributeType(String attributeType, Pageable pageable);
    Page<ProductAttributeValueDTO> findByKeyAttributes(Long productId, Pageable pageable);
    
    // Advanced Filtering
    Page<ProductAttributeValueDTO> findByMultipleCriteria(
            Long productId, 
            Long attributeId, 
            String value, 
            Boolean isActive, 
            Pageable pageable
    );
    
    // Bulk Operations - Owner only
    @PreAuthorize("hasRole('OWNER')")
    List<ProductAttributeValueDTO> createBulk(List<ProductAttributeValueCreateDTO> dtos);
    
    @PreAuthorize("hasRole('OWNER')")
    List<ProductAttributeValueDTO> updateByProduct(Long productId, List<ProductAttributeValueUpdateDTO> dtos);
    
    @PreAuthorize("hasRole('OWNER')")
    void deleteByProduct(Long productId);
    

    long countByProductId(Long productId);
    long countByAttributeId(Long attributeId);
    long countByCategoryId(Long categoryId);
    
    List<String> getDistinctValuesByAttribute(Long attributeId);
    List<ProductAttributeValueDTO> getKeyAttributesByProduct(Long productId);
    List<ProductAttributeValueDTO> getByProductAndAttributeType(Long productId, String attributeType);
}
