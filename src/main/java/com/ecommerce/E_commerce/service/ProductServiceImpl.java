package com.ecommerce.E_commerce.service;

import com.ecommerce.E_commerce.dto.product.ProductCreateDTO;
import com.ecommerce.E_commerce.dto.product.ProductDTO;
import com.ecommerce.E_commerce.dto.product.ProductUpdateDTO;
import com.ecommerce.E_commerce.dto.productattributevalue.ProductAttributeValueCreateDTO;
import com.ecommerce.E_commerce.exception.ResourceNotFoundException;
import com.ecommerce.E_commerce.mapper.ProductMapper;
import com.ecommerce.E_commerce.model.Category;
import com.ecommerce.E_commerce.model.Product;
import com.ecommerce.E_commerce.model.SkuGenerator;
import com.ecommerce.E_commerce.repository.CategoryRepository;
import com.ecommerce.E_commerce.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    @Autowired
    private ProductMapper productMapper;
    
    @Autowired
    private ProductAttributeValueService productAttributeValueService;

    @Autowired
    public ProductServiceImpl(ProductRepository productRepository,
                              CategoryRepository categoryRepository,
                              ProductMapper productMapper,
                              ProductAttributeValueService productAttributeValueService) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.productMapper = productMapper;
        this.productAttributeValueService = productAttributeValueService;
    }

    @Override
    public ProductDTO create(ProductCreateDTO dto) {
        Category category = categoryRepository.findById(dto.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + dto.categoryId()));
        
        Product product = productMapper.toProduct(dto);
        product.setCategory(category);
        product.setShippingCost(dto.shippingCost() != null ? dto.shippingCost() : BigDecimal.ZERO);
        product.setIsFeatured(dto.isFeatured() != null ? dto.isFeatured() : false);
        product.setCreatedAt(Instant.now());
        product.setUpdatedAt(Instant.now());
        
        product.setSku(SkuGenerator.generate(product));
        
        Product savedProduct = productRepository.save(product);
        
        // Handle attribute values if provided
        if (dto.attributeValues() != null && !dto.attributeValues().isEmpty()) {
            List<ProductAttributeValueCreateDTO> attributeValueDTOs = dto.attributeValues().stream()
                    .map(attr -> new ProductAttributeValueCreateDTO(
                            savedProduct.getId(),
                            attr.categoryAttributeId(),
                            attr.value()
                    ))
                    .collect(Collectors.toList());
            
            productAttributeValueService.createBulk(attributeValueDTOs);
        }
        
        return productMapper.toProductDTO(savedProduct);
    }

    @Override
    public ProductDTO update(Long id, ProductUpdateDTO dto) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        
        // Store original values to check if SKU needs regeneration
        String originalName = product.getName();
        Long originalCategoryId = product.getCategory().getId();
        
        // Update product using mapper
        productMapper.updateProductFromDTO(dto, product);
        
        // Handle category update separately
        if (dto.categoryId() != null) {
            Category category = categoryRepository.findById(dto.categoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + dto.categoryId()));
            product.setCategory(category);
        }
        
        product.setUpdatedAt(Instant.now());
        
        // Regenerate SKU if category or name changed
        if (!originalName.equals(product.getName()) || !originalCategoryId.equals(product.getCategory().getId())) {
            product.setSku(SkuGenerator.generate(product));
        }
        
        Product savedProduct = productRepository.save(product);
        
        // Handle attribute values if provided
        if (dto.attributeValues() != null && !dto.attributeValues().isEmpty()) {
            productAttributeValueService.updateByProduct(savedProduct.getId(), dto.attributeValues());
        }
        
        return productMapper.toProductDTO(savedProduct);
    }

    @Override
    public void delete(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        
        product.setDeletedAt(Instant.now());
        product.setIsActive(false);
        product.setUpdatedAt(Instant.now());
        
        // Delete associated attribute values
        productAttributeValueService.deleteByProduct(id);
        
        productRepository.save(product);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDTO getById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        return productMapper.toProductDTO(product);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDTO getBySeoSlug(String seoSlug) {
        Product product = productRepository.findBySeoSlug(seoSlug)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with seo slug: " + seoSlug));
        return productMapper.toProductDTO(product);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDTO getBySku(String sku) {
        Product product = productRepository.findBySku(sku)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with SKU: " + sku));
        return productMapper.toProductDTO(product);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductDTO> findAll(Pageable pageable) {
        return productRepository.findAll(pageable).map(productMapper::toProductDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductDTO> findByCategory(Long categoryId, Pageable pageable) {
        return productRepository.findByCategoryId(categoryId, pageable).map(productMapper::toProductDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductDTO> findByCategorySlug(String categorySlug, Pageable pageable) {
        return productRepository.findByCategorySeoSlug(categorySlug, pageable).map(productMapper::toProductDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductDTO> findByPriceRange(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable) {
        return productRepository.findByPriceBetween(minPrice, maxPrice, pageable).map(productMapper::toProductDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductDTO> findByFeatured(Boolean isFeatured, Pageable pageable) {
        return productRepository.findByIsFeatured(isFeatured, pageable).map(productMapper::toProductDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductDTO> findByActive(Boolean isActive, Pageable pageable) {
        return productRepository.findByIsActive(isActive, pageable).map(productMapper::toProductDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductDTO> searchByName(String name, Pageable pageable) {
        return productRepository.findByNameContainingIgnoreCase(name, pageable).map(productMapper::toProductDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductDTO> searchByDescription(String description, Pageable pageable) {
        return productRepository.findByDescriptionContainingIgnoreCase(description, pageable).map(productMapper::toProductDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductDTO> findByCategoryAndPriceRange(Long categoryId, BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable) {
        return productRepository.findByCategoryIdAndPriceBetween(categoryId, minPrice, maxPrice, pageable).map(productMapper::toProductDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductDTO> findByCategoryAndFeatured(Long categoryId, Boolean isFeatured, Pageable pageable) {
        return productRepository.findByCategoryIdAndIsFeatured(categoryId, isFeatured, pageable).map(productMapper::toProductDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductDTO> findByPriceRangeAndFeatured(BigDecimal minPrice, BigDecimal maxPrice, Boolean isFeatured, Pageable pageable) {
        return productRepository.findByPriceBetweenAndIsFeatured(minPrice, maxPrice, isFeatured, pageable).map(productMapper::toProductDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public long countByCategory(Long categoryId) {
        return productRepository.countByCategoryId(categoryId);
    }

    @Override
    @Transactional(readOnly = true)
    public long countByFeatured(Boolean isFeatured) {
        return productRepository.countByIsFeatured(isFeatured);
    }

    @Override
    @Transactional(readOnly = true)
    public long countByActive(Boolean isActive) {
        return productRepository.countByIsActive(isActive);
    }

    private Pageable buildPageable(int page, int size, String sortBy, String sortDir) {
        Sort sort = (sortDir != null && sortDir.equalsIgnoreCase("desc"))
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        return PageRequest.of(page, size, sort);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductDTO> findAll(int page, int size, String sortBy, String sortDir) {
        return findAll(buildPageable(page, size, sortBy, sortDir));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductDTO> findByCategory(Long categoryId, int page, int size, String sortBy, String sortDir) {
        return findByCategory(categoryId, buildPageable(page, size, sortBy, sortDir));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductDTO> findByCategorySlug(String categorySlug, int page, int size, String sortBy, String sortDir) {
        return findByCategorySlug(categorySlug, buildPageable(page, size, sortBy, sortDir));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductDTO> findByPriceRange(BigDecimal minPrice, BigDecimal maxPrice, int page, int size, String sortBy, String sortDir) {
        return findByPriceRange(minPrice, maxPrice, buildPageable(page, size, sortBy, sortDir));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductDTO> findByFeatured(Boolean isFeatured, int page, int size, String sortBy, String sortDir) {
        return findByFeatured(isFeatured, buildPageable(page, size, sortBy, sortDir));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductDTO> findByActive(Boolean isActive, int page, int size, String sortBy, String sortDir) {
        return findByActive(isActive, buildPageable(page, size, sortBy, sortDir));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductDTO> searchByName(String name, int page, int size, String sortBy, String sortDir) {
        return searchByName(name, buildPageable(page, size, sortBy, sortDir));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductDTO> searchByDescription(String description, int page, int size, String sortBy, String sortDir) {
        return searchByDescription(description, buildPageable(page, size, sortBy, sortDir));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductDTO> findByCategoryAndPriceRange(Long categoryId, BigDecimal minPrice, BigDecimal maxPrice, int page, int size, String sortBy, String sortDir) {
        return findByCategoryAndPriceRange(categoryId, minPrice, maxPrice, buildPageable(page, size, sortBy, sortDir));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductDTO> findByCategoryAndFeatured(Long categoryId, Boolean isFeatured, int page, int size, String sortBy, String sortDir) {
        return findByCategoryAndFeatured(categoryId, isFeatured, buildPageable(page, size, sortBy, sortDir));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductDTO> findByPriceRangeAndFeatured(BigDecimal minPrice, BigDecimal maxPrice, Boolean isFeatured, int page, int size, String sortBy, String sortDir) {
        return findByPriceRangeAndFeatured(minPrice, maxPrice, isFeatured, buildPageable(page, size, sortBy, sortDir));
    }

}
