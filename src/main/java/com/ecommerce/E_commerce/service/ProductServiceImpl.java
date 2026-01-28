package com.ecommerce.E_commerce.service;

import com.ecommerce.E_commerce.dto.inventory.InventoryCreateDTO;
import com.ecommerce.E_commerce.dto.product.ProductCreateDTO;
import com.ecommerce.E_commerce.dto.product.ProductDTO;
import com.ecommerce.E_commerce.dto.product.ProductSummaryDTO;
import com.ecommerce.E_commerce.dto.product.ProductUpdateDTO;
import com.ecommerce.E_commerce.dto.productattributevalue.ProductAttributeValueCreateDTO;
import com.ecommerce.E_commerce.exception.DuplicateResourceException;
import com.ecommerce.E_commerce.exception.ResourceNotFoundException;
import com.ecommerce.E_commerce.mapper.ProductMapper;
import com.ecommerce.E_commerce.model.Category;
import com.ecommerce.E_commerce.model.Product;
import com.ecommerce.E_commerce.model.SkuGenerator;
import com.ecommerce.E_commerce.repository.CategoryRepository;
import com.ecommerce.E_commerce.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private static final Logger logger = LoggerFactory.getLogger(ProductServiceImpl.class);
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;
    private final ProductAttributeValueService productAttributeValueService;
    private final InventoryService inventoryService;

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "products", allEntries = true),
            @CacheEvict(value = "product_lists", allEntries = true),
            @CacheEvict(value = "product_counts", allEntries = true)
    })
    public ProductDTO create(ProductCreateDTO dto) {
        logger.info("Creating product: name={}, categoryId={}", dto.name(), dto.categoryId());

        Category category = fetchCategory(dto.categoryId());
        Product product = prepareNewProduct(dto, category);

        Product savedProduct = productRepository.save(product);
        logger.info("Product created successfully: productId={}, name={}, sku={}",
                savedProduct.getId(), savedProduct.getName(), savedProduct.getSku());

        processAttributes(savedProduct, dto.attributeValues());
        initializeInventory(savedProduct);

        return fetchAndMapToDTO(savedProduct.getId());
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "products", allEntries = true),
            @CacheEvict(value = "product_lists", allEntries = true),
            @CacheEvict(value = "product_counts", allEntries = true)
    })
    public ProductDTO update(Long id, ProductUpdateDTO dto) {
        logger.info("Updating product: productId={}", id);
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
            logger.debug("Regenerating SKU due to name/category change: productId={}", id);
            product.setSku(SkuGenerator.generate(product));
        }

        Product savedProduct = productRepository.save(product);
        logger.info("Product updated successfully: productId={}, name={}", id, savedProduct.getName());

        if (dto.attributeValues() != null && !dto.attributeValues().isEmpty()) {
            logger.debug("Updating attribute values for product: productId={}, attributesCount={}", id, dto.attributeValues().size());
            productAttributeValueService.updateByProduct(savedProduct.getId(), dto.attributeValues());
        }

        return productMapper.toProductDTO(savedProduct);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "products", allEntries = true),
            @CacheEvict(value = "product_lists", allEntries = true),
            @CacheEvict(value = "product_counts", allEntries = true)
    })
    public void delete(Long id) {
        logger.info("Deleting product: productId={}", id);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        
        product.setDeletedAt(Instant.now());
        product.setIsActive(false);
        product.setUpdatedAt(Instant.now());

        if (product.getAttributeValues() != null) {
            logger.debug("Deactivating {} attribute values for deleted product: productId={}", 
                        product.getAttributeValues().size(), id);
            for (var pav : product.getAttributeValues()) {
                pav.setActive(false);
                pav.setDeletedAt(Instant.now());
            }
        }
        
        productRepository.save(product);
        logger.info("Product deleted successfully (soft delete): productId={}", id);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "products", key = "#id")
    public ProductDTO getById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        return productMapper.toProductDTO(product);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "products", key = "#seoSlug")
    public ProductDTO getBySeoSlug(String seoSlug) {
        Product product = productRepository.findBySeoSlug(seoSlug)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with seo slug: " + seoSlug));
        return productMapper.toProductDTO(product);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "products", key = "#sku")
    public ProductDTO getBySku(String sku) {
        Product product = productRepository.findBySku(sku)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with SKU: " + sku));
        return productMapper.toProductDTO(product);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "product_lists", key = "'all_' + #pageable.pageNumber + '_' + #pageable.sort.toString()")
    public Page<ProductSummaryDTO> findAll(Pageable pageable) {
        return productRepository.findAll(pageable).map(productMapper::toProductSummaryDTO);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "product_lists", key = "'cat_' + #categoryId + '_' + #pageable.pageNumber + '_' + #pageable.sort.toString()")
    public Page<ProductSummaryDTO> findByCategory(Long categoryId, Pageable pageable) {
        List<Long> categoryIds = categoryRepository.findAllSubcategoryIds(categoryId);
        return productRepository.findByCategoryIdIn(categoryIds, pageable).map(productMapper::toProductSummaryDTO);
    }

    @Override
    @Transactional(readOnly = true)

    public Page<ProductSummaryDTO> findByCategorySlug(String categorySlug, Pageable pageable) {
        return productRepository.findByCategorySeoSlug(categorySlug, pageable).map(productMapper::toProductSummaryDTO);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "product_lists", key = "'featured_' + #isFeatured + '_' + #pageable.pageNumber + '_' + #pageable.sort.toString()")
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
    @Cacheable(value = "product_counts", key = "'count_cat_' + #categoryId")
    public long countByCategory(Long categoryId) {
        return productRepository.countByCategoryId(categoryId);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "product_counts", key = "'count_feat_' + #isFeatured")
    public long countByFeatured(Boolean isFeatured) {
        return productRepository.countByIsFeatured(isFeatured);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "product_counts", key = "'count_active_' + #isActive")
    public long countByActive(Boolean isActive) {
        return productRepository.countByIsActive(isActive);
    }


    private Category fetchCategory(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + categoryId));
    }

    private Product prepareNewProduct(ProductCreateDTO dto, Category category) {
        Product product = productMapper.toProduct(dto);
        product.setCategory(category);

        product.setShippingCost(dto.shippingCost() != null ? dto.shippingCost() : BigDecimal.ZERO);
        product.setIsFeatured(dto.isFeatured() != null ? dto.isFeatured() : false);
        product.setCreatedAt(Instant.now());
        product.setUpdatedAt(Instant.now());

        product.setSku(SkuGenerator.generate(product));

        return product;
    }

    private void processAttributes(Product product, List<ProductAttributeValueCreateDTO> attributeValues) {
        if (attributeValues == null || attributeValues.isEmpty()) {
            return;
        }

        logger.debug("Creating attribute values for product: productId={}, attributesCount={}",
                product.getId(), attributeValues.size());

        List<ProductAttributeValueCreateDTO> attributeValueDTOs = attributeValues.stream()
                .map(attr -> new ProductAttributeValueCreateDTO(
                        product.getId(),
                        attr.attributeId(),
                        attr.attributeValue()
                ))
                .collect(Collectors.toList());

        productAttributeValueService.createBulk(attributeValueDTOs);
    }

    private void initializeInventory(Product product) {
        try {
            logger.debug("Creating inventory for new product: productId={}", product.getId());
            inventoryService.create(new InventoryCreateDTO(
                    product.getId(),
                    0,  // availableQuantity
                    0,  // reservedQuantity
                    0   // minimumStockLevel
            ));
            logger.debug("Inventory created successfully for product: productId={}", product.getId());
        } catch (DuplicateResourceException e) {
            logger.debug("Inventory already exists for product: productId={}", product.getId());
        }
    }

    private ProductDTO fetchAndMapToDTO(Long productId) {
        Product refreshedProduct = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        return productMapper.toProductDTO(refreshedProduct);
    }

}
