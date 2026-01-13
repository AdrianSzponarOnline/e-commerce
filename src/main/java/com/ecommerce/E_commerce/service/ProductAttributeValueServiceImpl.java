package com.ecommerce.E_commerce.service;

import com.ecommerce.E_commerce.dto.productattributevalue.ProductAttributeValueCreateDTO;
import com.ecommerce.E_commerce.dto.productattributevalue.ProductAttributeValueDTO;
import com.ecommerce.E_commerce.dto.productattributevalue.ProductAttributeValueUpdateDTO;
import com.ecommerce.E_commerce.exception.DuplicateResourceException;
import com.ecommerce.E_commerce.exception.InvalidOperationException;
import com.ecommerce.E_commerce.exception.ResourceNotFoundException;
import com.ecommerce.E_commerce.mapper.ProductAttributeValueMapper;
import com.ecommerce.E_commerce.model.Attribute;
import com.ecommerce.E_commerce.model.CategoryAttributeType;
import com.ecommerce.E_commerce.model.Product;
import com.ecommerce.E_commerce.model.ProductAttributeValue;
import com.ecommerce.E_commerce.repository.AttributeRepository;
import com.ecommerce.E_commerce.repository.ProductAttributeValueRepository;
import com.ecommerce.E_commerce.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class ProductAttributeValueServiceImpl implements ProductAttributeValueService {

    private final ProductRepository productRepository;
    private final ProductAttributeValueRepository productAttributeValueRepository;
    private final AttributeRepository attributeRepository;
    private final ProductAttributeValueMapper productAttributeValueMapper;

    private static final Logger logger = LoggerFactory.getLogger(ProductAttributeValueServiceImpl.class);

    @Override
    @CacheEvict(value = "product_attributes", allEntries = true)
    public ProductAttributeValueDTO create(ProductAttributeValueCreateDTO dto) {
        Product product = productRepository.findById(dto.productId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + dto.productId()));
        
        Attribute attribute = attributeRepository.findById(dto.attributeId())
                .orElseThrow(() -> new ResourceNotFoundException("Attribute not found with id: " + dto.attributeId()));
        
        if (productAttributeValueRepository.findByProductIdAndAttributeId(dto.productId(), dto.attributeId()).isPresent()) {
            throw new DuplicateResourceException("Product attribute value already exists for this product and attribute");
        }
        
      
        validateAttributeValue(dto.attributeValue(), attribute.getType(), attribute.getName());
        
        ProductAttributeValue productAttributeValue = productAttributeValueMapper.toProductAttributeValue(dto);
        productAttributeValue.setProduct(product);
        productAttributeValue.setAttribute(attribute);
        
        ProductAttributeValue savedProductAttributeValue = productAttributeValueRepository.save(productAttributeValue);
        return productAttributeValueMapper.toProductAttributeValueDTO(savedProductAttributeValue);
    }

    @Override
    @CacheEvict(value = "product_attributes", allEntries = true)
    public ProductAttributeValueDTO update(Long id, ProductAttributeValueUpdateDTO dto) {
        ProductAttributeValue productAttributeValue = productAttributeValueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product attribute value not found with id: " + id));
        
        if (dto.attributeValue() != null) {
            Attribute attribute = productAttributeValue.getAttribute();
            validateAttributeValue(dto.attributeValue(), attribute.getType(), attribute.getName());
        }
        
        productAttributeValueMapper.updateProductAttributeValueFromDTO(dto, productAttributeValue);
        
        if (dto.isActive() != null) {
            productAttributeValue.setActive(dto.isActive());
        }
        
        ProductAttributeValue savedProductAttributeValue = productAttributeValueRepository.save(productAttributeValue);
        return productAttributeValueMapper.toProductAttributeValueDTO(savedProductAttributeValue);
    }

    @Override
    @CacheEvict(value = "product_attributes", allEntries = true)
    public void delete(Long id) {
        ProductAttributeValue productAttributeValue = productAttributeValueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product attribute value not found with id: " + id));
        
        productAttributeValueRepository.delete(productAttributeValue);
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
    @Cacheable(value = "product_attributes", key = "#productId")
    public List<ProductAttributeValueDTO> getByProductId(Long productId) {
        List<ProductAttributeValue> productAttributeValues = productAttributeValueRepository.findByProductIdAndIsActive(productId, true);
        return productAttributeValues.stream()
                .map(productAttributeValueMapper::toProductAttributeValueDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductAttributeValueDTO> getByAttributeId(Long attributeId) {
        List<ProductAttributeValue> productAttributeValues = productAttributeValueRepository.findByAttributeIdAndIsActive(attributeId, true);
        return productAttributeValues.stream()
                .map(productAttributeValueMapper::toProductAttributeValueDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ProductAttributeValueDTO getByProductAndAttribute(Long productId, Long attributeId) {
        ProductAttributeValue productAttributeValue = productAttributeValueRepository.findByProductIdAndAttributeId(productId, attributeId)
                .orElseThrow(() -> new ResourceNotFoundException("Product attribute value not found for product id: " + productId + " and attribute id: " + attributeId));
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
    public Page<ProductAttributeValueDTO> findByAttributeId(Long attributeId, Pageable pageable) {
        Page<ProductAttributeValue> productAttributeValues = productAttributeValueRepository.findByAttributeId(attributeId, pageable);
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
        Page<ProductAttributeValue> productAttributeValues = productAttributeValueRepository.findByAttributeValueContainingIgnoreCase(value, pageable);
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
        Page<ProductAttributeValue> productAttributeValues = productAttributeValueRepository.findByAttributeType(attributeType, pageable);
        return productAttributeValues.map(productAttributeValueMapper::toProductAttributeValueDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductAttributeValueDTO> findByKeyAttributes(Long productId, Pageable pageable) {
        List<ProductAttributeValue> productAttributeValues = productAttributeValueRepository.findByProductIdAndKeyAttributeAndIsActive(productId, true, true);
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), productAttributeValues.size());
        List<ProductAttributeValue> pageContent = productAttributeValues.subList(start, end);
        
        Page<ProductAttributeValue> page = new org.springframework.data.domain.PageImpl<>(
                pageContent, pageable, productAttributeValues.size());
        
        return page.map(productAttributeValueMapper::toProductAttributeValueDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductAttributeValueDTO> findByMultipleCriteria(Long productId, Long attributeId, String value, Boolean isActive, Pageable pageable) {
        Page<ProductAttributeValue> productAttributeValues = productAttributeValueRepository.findByMultipleCriteria(productId, attributeId, value, isActive, pageable);
        return productAttributeValues.map(productAttributeValueMapper::toProductAttributeValueDTO);
    }

    @Override
    @CacheEvict(value = "product_attributes", allEntries = true)
    public List<ProductAttributeValueDTO> createBulk(List<ProductAttributeValueCreateDTO> dtos) {
        for (ProductAttributeValueCreateDTO dto : dtos) {
            Product product = productRepository.findById(dto.productId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + dto.productId()));
            
            Attribute attribute = attributeRepository.findById(dto.attributeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Attribute not found with id: " + dto.attributeId()));
            
            if (productAttributeValueRepository.findByProductIdAndAttributeId(dto.productId(), dto.attributeId()).isPresent()) {
                throw new DuplicateResourceException("Product attribute value already exists for product id: " +
                        dto.productId() + " and attribute id: " + dto.attributeId());
            }
            validateAttributeValue(dto.attributeValue(), attribute.getType(), attribute.getName());
        }

        List<ProductAttributeValue> entities = dtos.stream()
                .map(dto -> {
                    Product product = productRepository.findById(dto.productId())
                            .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + dto.productId()));
                    Attribute attribute = attributeRepository.findById(dto.attributeId())
                            .orElseThrow(() -> new ResourceNotFoundException("Attribute not found with id: " + dto.attributeId()));
                    
                    ProductAttributeValue productAttributeValue = productAttributeValueMapper.toProductAttributeValue(dto);

                    logger.info("DEBUG MAPPER: DTO value='{}' -> Entity value='{}'",
                            dto.attributeValue(),
                            productAttributeValue.getAttributeValue());

                    productAttributeValue.setProduct(product);
                    productAttributeValue.setAttribute(attribute);
                    return productAttributeValue;
                })
                .collect(Collectors.toList());
        
        List<ProductAttributeValue> savedEntities = productAttributeValueRepository.saveAll(entities);
        
        return savedEntities.stream()
                .map(productAttributeValueMapper::toProductAttributeValueDTO)
                .collect(Collectors.toList());
    }

    @Override
    @CacheEvict(value = "product_attributes", allEntries = true)
    public List<ProductAttributeValueDTO> updateByProduct(Long productId, List<ProductAttributeValueUpdateDTO> dtos) {
        List<ProductAttributeValue> existingValues = productAttributeValueRepository.findByProductIdAndIsActive(productId, true);
        
    
        Map<Long, ProductAttributeValue> existingById = existingValues.stream()
                .collect(Collectors.toMap(ProductAttributeValue::getId, v -> v));
        
        Map<Long, ProductAttributeValue> existingByAttributeId = existingValues.stream()
                .collect(Collectors.toMap(v -> v.getAttribute().getId(), v -> v, (v1, v2) -> v1));
        
        List<ProductAttributeValue> toUpdate = new java.util.ArrayList<>();
        List<ProductAttributeValue> toCreate = new java.util.ArrayList<>();
        
        for (ProductAttributeValueUpdateDTO dto : dtos) {
            ProductAttributeValue existingValue = null;
            
            if (dto.id() != null) {
                existingValue = existingById.get(dto.id());
                if (existingValue == null) {
                    throw new ResourceNotFoundException("Product attribute value not found with id: " + dto.id());
                }
            } 
        
            else if (dto.attributeId() != null) {
                existingValue = existingByAttributeId.get(dto.attributeId());
            }
            
            if (existingValue != null) {
             
                if (dto.attributeValue() != null) {
                    Attribute attribute = existingValue.getAttribute();
                    validateAttributeValue(dto.attributeValue(), attribute.getType(), attribute.getName());
                }
                
                productAttributeValueMapper.updateProductAttributeValueFromDTO(dto, existingValue);
                
                if (dto.isActive() != null) {
                    existingValue.setActive(dto.isActive());
                }
                
                toUpdate.add(existingValue);
            } else {

                if (dto.attributeId() == null) {
                    throw new IllegalArgumentException("Cannot create new attribute value: attributeId is required when id is not provided");
                }
                
                Product product = productRepository.findById(productId)
                        .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));
                
                Attribute attribute = attributeRepository.findById(dto.attributeId())
                        .orElseThrow(() -> new ResourceNotFoundException("Attribute not found with id: " + dto.attributeId()));
                
                if (productAttributeValueRepository.findByProductIdAndAttributeId(productId, dto.attributeId()).isPresent()) {
                    throw new DuplicateResourceException("Product attribute value already exists for this product and attribute");
                }
                
                if (dto.attributeValue() != null) {
                    validateAttributeValue(dto.attributeValue(), attribute.getType(), attribute.getName());
                }
                
                ProductAttributeValue newValue = new ProductAttributeValue();
                newValue.setProduct(product);
                newValue.setAttribute(attribute);
                newValue.setAttributeValue(dto.attributeValue());
                newValue.setActive(dto.isActive() != null ? dto.isActive() : true);
                
                toCreate.add(newValue);
            }
        }
        
        List<ProductAttributeValue> savedValues = new java.util.ArrayList<>();
        if (!toUpdate.isEmpty()) {
            savedValues.addAll(productAttributeValueRepository.saveAll(toUpdate));
        }
        if (!toCreate.isEmpty()) {
            savedValues.addAll(productAttributeValueRepository.saveAll(toCreate));
        }
        
        return savedValues.stream()
                .map(productAttributeValueMapper::toProductAttributeValueDTO)
                .collect(Collectors.toList());
    }

    @Override
    @CacheEvict(value = "product_attributes", allEntries = true)
    public void deleteByProduct(Long productId) {
        List<ProductAttributeValue> productAttributeValues = productAttributeValueRepository.findByProductIdAndIsActive(productId, true);
        
        productAttributeValueRepository.deleteAll(productAttributeValues);
    }

    @Override
    @Transactional(readOnly = true)
    public long countByProductId(Long productId) {
        return productAttributeValueRepository.countByProductIdAndIsActive(productId, true);
    }

    @Override
    @Transactional(readOnly = true)
    public long countByAttributeId(Long attributeId) {
        return productAttributeValueRepository.countByAttributeIdAndIsActive(attributeId, true);
    }

    @Override
    @Transactional(readOnly = true)
    public long countByCategoryId(Long categoryId) {
        return productAttributeValueRepository.countByProductCategoryId(categoryId);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "product_attributes", key = "'distinct_' + #attributeId")
    public List<String> getDistinctValuesByAttribute(Long attributeId) {
        return productAttributeValueRepository.findDistinctValuesByAttribute(attributeId);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "product_attributes", key = "'key_' + #productId")
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

    /**
     * Validates that the provided value matches the expected attribute type.
     * 
     * @param value the value to validate
     * @param attributeType the expected type of the attribute
     * @param attributeName the name of the attribute (for error messages)
     * @throws InvalidOperationException if the value doesn't match the attribute type
     */
    private void validateAttributeValue(String value, CategoryAttributeType attributeType, String attributeName) {
        if (value == null || value.trim().isEmpty()) {
            return;
        }
        switch (attributeType) {
            case NUMBER:
                validateNumberValue(value, attributeName);
                break;
            case BOOLEAN:
                validateBooleanValue(value, attributeName);
                break;
            case TEXT:
            case SELECT:
                break;
            default:
                throw new InvalidOperationException("Unknown attribute type: " + attributeType);
        }
    }

    private void validateNumberValue(String value, String attributeName) {
        try {
            Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            throw new InvalidOperationException(
                String.format("Invalid value for attribute '%s' (type: NUMBER). Expected a number, but got: '%s'", 
                    attributeName, value)
            );
        }
    }

    private void validateBooleanValue(String value, String attributeName) {
        String trimmedValue = value.trim().toLowerCase();
        if (!trimmedValue.equals("true") && !trimmedValue.equals("false") && 
            !trimmedValue.equals("1") && !trimmedValue.equals("0") &&
            !trimmedValue.equals("yes") && !trimmedValue.equals("no")) {
            throw new InvalidOperationException(
                String.format("Invalid value for attribute '%s' (type: BOOLEAN). Expected 'true' or 'false', but got: '%s'", 
                    attributeName, value)
            );
        }
    }
}
