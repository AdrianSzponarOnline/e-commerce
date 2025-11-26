package com.ecommerce.E_commerce.service;

import com.ecommerce.E_commerce.dto.product.ProductCreateDTO;
import com.ecommerce.E_commerce.dto.product.ProductDTO;
import com.ecommerce.E_commerce.dto.product.ProductSummaryDTO;
import com.ecommerce.E_commerce.dto.product.ProductUpdateDTO;
import com.ecommerce.E_commerce.dto.productattributevalue.ProductAttributeValueCreateDTO;
import com.ecommerce.E_commerce.exception.ResourceNotFoundException;
import com.ecommerce.E_commerce.mapper.ProductMapper;
import com.ecommerce.E_commerce.model.Category;
import com.ecommerce.E_commerce.model.Product;
import com.ecommerce.E_commerce.model.SkuGenerator;
import com.ecommerce.E_commerce.repository.AttributeRepository;
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


    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final AttributeRepository attributeRepository;
    private final ProductMapper productMapper;
    private final ProductAttributeValueService productAttributeValueService;
    private final ImageUrlService imageUrlService;

    @Autowired
    public ProductServiceImpl(ProductRepository productRepository,
                              CategoryRepository categoryRepository,
                              AttributeRepository attributeRepository,
                              ProductMapper productMapper,
                              ProductAttributeValueService productAttributeValueService,
                              ImageUrlService imageUrlService) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.attributeRepository = attributeRepository;
        this.productMapper = productMapper;
        this.productAttributeValueService = productAttributeValueService;
        this.imageUrlService = imageUrlService;
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
        
        if (dto.attributeValues() != null && !dto.attributeValues().isEmpty()) {
            List<ProductAttributeValueCreateDTO> attributeValueDTOs = dto.attributeValues().stream()
                    .map(attr -> new ProductAttributeValueCreateDTO(
                            savedProduct.getId(),
                            attr.attributeId(),
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
        
        String originalName = product.getName();
        Long originalCategoryId = product.getCategory().getId();
        
        productMapper.updateProductFromDTO(dto, product);
        
        if (dto.categoryId() != null) {
            Category category = categoryRepository.findById(dto.categoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + dto.categoryId()));
            product.setCategory(category);
        }
        
        product.setUpdatedAt(Instant.now());
        
        if (!originalName.equals(product.getName()) || !originalCategoryId.equals(product.getCategory().getId())) {
            product.setSku(SkuGenerator.generate(product));
        }
        
        Product savedProduct = productRepository.save(product);
        
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

        if (product.getAttributeValues() != null) {
            for (var pav : product.getAttributeValues()) {
                pav.setActive(false);
                pav.setDeletedAt(Instant.now());
            }
        }
        
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
    public Page<ProductSummaryDTO> findAll(Pageable pageable) {
        return productRepository.findAll(pageable).map(productMapper::toProductSummaryDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductSummaryDTO> findByCategory(Long categoryId, Pageable pageable) {
        return productRepository.findByCategoryId(categoryId, pageable).map(productMapper::toProductSummaryDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductSummaryDTO> findByCategorySlug(String categorySlug, Pageable pageable) {
        return productRepository.findByCategorySeoSlug(categorySlug, pageable).map(productMapper::toProductSummaryDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductSummaryDTO> findByFeatured(Boolean isFeatured, Pageable pageable) {
        return productRepository.findByIsFeatured(isFeatured, pageable).map(productMapper::toProductSummaryDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductSummaryDTO> findByActive(Boolean isActive, Pageable pageable) {
        return productRepository.findByIsActive(isActive, pageable).map(productMapper::toProductSummaryDTO);
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
    public Page<ProductSummaryDTO> findAll(int page, int size, String sortBy, String sortDir) {
        return findAll(buildPageable(page, size, sortBy, sortDir));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductSummaryDTO> findByCategory(Long categoryId, int page, int size, String sortBy, String sortDir) {
        return findByCategory(categoryId, buildPageable(page, size, sortBy, sortDir));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductSummaryDTO> findByCategorySlug(String categorySlug, int page, int size, String sortBy, String sortDir) {
        return findByCategorySlug(categorySlug, buildPageable(page, size, sortBy, sortDir));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductSummaryDTO> findByFeatured(Boolean isFeatured, int page, int size, String sortBy, String sortDir) {
        return findByFeatured(isFeatured, buildPageable(page, size, sortBy, sortDir));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductSummaryDTO> findByActive(Boolean isActive, int page, int size, String sortBy, String sortDir) {
        return findByActive(isActive, buildPageable(page, size, sortBy, sortDir));
    }

}
