package com.ecommerce.E_commerce.service;

import com.ecommerce.E_commerce.dto.productattributevalue.ProductAttributeValueCreateDTO;
import com.ecommerce.E_commerce.dto.productattributevalue.ProductAttributeValueDTO;
import com.ecommerce.E_commerce.dto.productattributevalue.ProductAttributeValueUpdateDTO;
import com.ecommerce.E_commerce.exception.ResourceNotFoundException;
import com.ecommerce.E_commerce.mapper.ProductAttributeValueMapper;
import com.ecommerce.E_commerce.model.CategoryAttribute;
import com.ecommerce.E_commerce.model.Product;
import com.ecommerce.E_commerce.model.ProductAttributeValue;
import com.ecommerce.E_commerce.repository.CategoryAttributeRepository;
import com.ecommerce.E_commerce.repository.ProductAttributeValueRepository;
import com.ecommerce.E_commerce.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProductAttributeValueServiceImpl implements ProductAttributeValueService {

    private final ProductRepository productRepository;
    private final ProductAttributeValueRepository productAttributeValueRepository;
    private final CategoryAttributeRepository categoryAttributeRepository;
    private final ProductAttributeValueMapper productAttributeValueMapper;

    @Override
    public ProductAttributeValueDTO create(ProductAttributeValueCreateDTO dto) {
        Product product = productRepository.findById(dto.productId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + dto.productId()));
        
        CategoryAttribute categoryAttribute = categoryAttributeRepository.findById(dto.categoryAttributeId())
                .orElseThrow(() -> new ResourceNotFoundException("Category attribute not found with id: " + dto.categoryAttributeId()));
        
        // Check if this combination already exists
        if (productAttributeValueRepository.findByProductIdAndCategoryAttributeId(dto.productId(), dto.categoryAttributeId()).isPresent()) {
            throw new IllegalArgumentException("Product attribute value already exists for this product and category attribute");
        }
        
        ProductAttributeValue productAttributeValue = productAttributeValueMapper.toProductAttributeValue(dto);
        productAttributeValue.setProduct(product);
        productAttributeValue.setCategoryAttribute(categoryAttribute);
        
        ProductAttributeValue savedProductAttributeValue = productAttributeValueRepository.save(productAttributeValue);
        return productAttributeValueMapper.toProductAttributeValueDTO(savedProductAttributeValue);
    }

    @Override
    public ProductAttributeValueDTO update(Long id, ProductAttributeValueUpdateDTO dto) {
        ProductAttributeValue productAttributeValue = productAttributeValueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product attribute value not found with id: " + id));
        
        productAttributeValueMapper.updateProductAttributeValueFromDTO(dto, productAttributeValue);
        
        if (dto.isActive() != null) {
            productAttributeValue.setIsActive(dto.isActive());
        }
        
        ProductAttributeValue savedProductAttributeValue = productAttributeValueRepository.save(productAttributeValue);
        return productAttributeValueMapper.toProductAttributeValueDTO(savedProductAttributeValue);
    }

    @Override
    public void delete(Long id) {
        ProductAttributeValue productAttributeValue = productAttributeValueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product attribute value not found with id: " + id));
        
        productAttributeValue.setDeletedAt(Instant.now());
        productAttributeValue.setIsActive(false);
        
        productAttributeValueRepository.save(productAttributeValue);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductAttributeValueDTO getById(Long id) {
        ProductAttributeValue productAttributeValue = productAttributeValueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product attribute value not found with id: " + id));
        return productAttributeValueMapper.toProductAttributeValueDTO(productAttributeValue);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductAttributeValueDTO> getByProductId(Long productId) {
        List<ProductAttributeValue> productAttributeValues = productAttributeValueRepository.findByProductIdAndIsActive(productId, true);
        return productAttributeValues.stream()
                .map(productAttributeValueMapper::toProductAttributeValueDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductAttributeValueDTO> getByCategoryAttributeId(Long categoryAttributeId) {
        List<ProductAttributeValue> productAttributeValues = productAttributeValueRepository.findByCategoryAttributeIdAndIsActive(categoryAttributeId, true);
        return productAttributeValues.stream()
                .map(productAttributeValueMapper::toProductAttributeValueDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ProductAttributeValueDTO getByProductAndCategoryAttribute(Long productId, Long categoryAttributeId) {
        ProductAttributeValue productAttributeValue = productAttributeValueRepository.findByProductIdAndCategoryAttributeId(productId, categoryAttributeId)
                .orElseThrow(() -> new ResourceNotFoundException("Product attribute value not found for product id: " + productId + " and category attribute id: " + categoryAttributeId));
        return productAttributeValueMapper.toProductAttributeValueDTO(productAttributeValue);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductAttributeValueDTO> findAll(Pageable pageable) {
        Page<ProductAttributeValue> productAttributeValues = productAttributeValueRepository.findAll(pageable);
        return productAttributeValues.map(productAttributeValueMapper::toProductAttributeValueDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductAttributeValueDTO> findByProductId(Long productId, Pageable pageable) {
        Page<ProductAttributeValue> productAttributeValues = productAttributeValueRepository.findByProductId(productId, pageable);
        return productAttributeValues.map(productAttributeValueMapper::toProductAttributeValueDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductAttributeValueDTO> findByCategoryAttributeId(Long categoryAttributeId, Pageable pageable) {
        Page<ProductAttributeValue> productAttributeValues = productAttributeValueRepository.findByCategoryAttributeId(categoryAttributeId, pageable);
        return productAttributeValues.map(productAttributeValueMapper::toProductAttributeValueDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductAttributeValueDTO> findByCategoryId(Long categoryId, Pageable pageable) {
        Page<ProductAttributeValue> productAttributeValues = productAttributeValueRepository.findByProductCategoryId(categoryId, pageable);
        return productAttributeValues.map(productAttributeValueMapper::toProductAttributeValueDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductAttributeValueDTO> findByValue(String value, Pageable pageable) {
        Page<ProductAttributeValue> productAttributeValues = productAttributeValueRepository.findByValueContainingIgnoreCase(value, pageable);
        return productAttributeValues.map(productAttributeValueMapper::toProductAttributeValueDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductAttributeValueDTO> searchByValue(String value, Pageable pageable) {
        return findByValue(value, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductAttributeValueDTO> findByAttributeType(String attributeType, Pageable pageable) {
        Page<ProductAttributeValue> productAttributeValues = productAttributeValueRepository.findByCategoryAttributeType(attributeType, pageable);
        return productAttributeValues.map(productAttributeValueMapper::toProductAttributeValueDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductAttributeValueDTO> findByKeyAttributes(Long productId, Pageable pageable) {
        List<ProductAttributeValue> productAttributeValues = productAttributeValueRepository.findByProductIdAndCategoryAttributeIsKeyAttributeAndIsActive(productId, true, true);
        // Convert to page manually since we have a list
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), productAttributeValues.size());
        List<ProductAttributeValue> pageContent = productAttributeValues.subList(start, end);
        
        Page<ProductAttributeValue> page = new org.springframework.data.domain.PageImpl<>(
                pageContent, pageable, productAttributeValues.size());
        
        return page.map(productAttributeValueMapper::toProductAttributeValueDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductAttributeValueDTO> findByMultipleCriteria(Long productId, Long categoryAttributeId, String value, Boolean isActive, Pageable pageable) {
        Page<ProductAttributeValue> productAttributeValues = productAttributeValueRepository.findByMultipleCriteria(productId, categoryAttributeId, value, isActive, pageable);
        return productAttributeValues.map(productAttributeValueMapper::toProductAttributeValueDTO);
    }

    @Override
    public List<ProductAttributeValueDTO> createBulk(List<ProductAttributeValueCreateDTO> dtos) {
        return dtos.stream()
                .map(this::create)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductAttributeValueDTO> updateByProduct(Long productId, List<ProductAttributeValueUpdateDTO> dtos) {
        // This is a simplified implementation - in practice, you might want to match by category attribute
        List<ProductAttributeValue> existingValues = productAttributeValueRepository.findByProductIdAndIsActive(productId, true);
        
        if (dtos.size() != existingValues.size()) {
            throw new IllegalArgumentException("Number of update DTOs must match existing attribute values");
        }
        
        for (int i = 0; i < existingValues.size(); i++) {
            productAttributeValueMapper.updateProductAttributeValueFromDTO(dtos.get(i), existingValues.get(i));
            productAttributeValueRepository.save(existingValues.get(i));
        }
        
        return existingValues.stream()
                .map(productAttributeValueMapper::toProductAttributeValueDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteByProduct(Long productId) {
        List<ProductAttributeValue> productAttributeValues = productAttributeValueRepository.findByProductIdAndIsActive(productId, true);
        Instant now = Instant.now();
        
        for (ProductAttributeValue pav : productAttributeValues) {
            pav.setDeletedAt(now);
            pav.setIsActive(false);
        }
        
        productAttributeValueRepository.saveAll(productAttributeValues);
    }

    @Override
    @Transactional(readOnly = true)
    public long countByProductId(Long productId) {
        return productAttributeValueRepository.countByProductIdAndIsActive(productId, true);
    }

    @Override
    @Transactional(readOnly = true)
    public long countByCategoryAttributeId(Long categoryAttributeId) {
        return productAttributeValueRepository.countByCategoryAttributeIdAndIsActive(categoryAttributeId, true);
    }

    @Override
    @Transactional(readOnly = true)
    public long countByCategoryId(Long categoryId) {
        return productAttributeValueRepository.countByProductCategoryId(categoryId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getDistinctValuesByCategoryAttribute(Long categoryAttributeId) {
        return productAttributeValueRepository.findDistinctValuesByCategoryAttribute(categoryAttributeId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductAttributeValueDTO> getKeyAttributesByProduct(Long productId) {
        List<ProductAttributeValue> productAttributeValues = productAttributeValueRepository.findKeyAttributesByProduct(productId);
        return productAttributeValues.stream()
                .map(productAttributeValueMapper::toProductAttributeValueDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductAttributeValueDTO> getByProductAndAttributeType(Long productId, String attributeType) {
        List<ProductAttributeValue> productAttributeValues = productAttributeValueRepository.findByProductAndAttributeType(productId, attributeType);
        return productAttributeValues.stream()
                .map(productAttributeValueMapper::toProductAttributeValueDTO)
                .collect(Collectors.toList());
    }

    @Autowired
    public ProductAttributeValueServiceImpl(ProductRepository productRepository,
                                            ProductAttributeValueRepository productAttributeValueRepository,
                                            CategoryAttributeRepository categoryAttributeRepository,
                                            ProductAttributeValueMapper productAttributeValueMapper) {
        this.productRepository = productRepository;
        this.productAttributeValueRepository = productAttributeValueRepository;
        this.categoryAttributeRepository = categoryAttributeRepository;
        this.productAttributeValueMapper = productAttributeValueMapper;
    }

}
