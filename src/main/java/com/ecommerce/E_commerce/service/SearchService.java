package com.ecommerce.E_commerce.service;

import com.ecommerce.E_commerce.dto.product.ProductSearchDTO;
import com.ecommerce.E_commerce.mapper.ProductMapper;
import com.ecommerce.E_commerce.model.Product;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.hibernate.graph.GraphSemantic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.hibernate.search.engine.search.query.SearchResult;
import org.hibernate.search.mapper.orm.Search;
import org.hibernate.search.mapper.orm.session.SearchSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SearchService {
    
    private static final Logger logger = LoggerFactory.getLogger(SearchService.class);
    private final EntityManager em;
    private final ProductMapper productMapper;
    private final ImageUrlService imageUrlService;

    public Page<ProductSearchDTO> search(
            String query,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Boolean isActive,
            Map<String, String> attributes,
            Pageable pageable
    ) {

        logger.info("Executing product search: query='{}', minPrice={}, maxPrice={}, isActive={}, attributesCount={}", 
                   query, minPrice, maxPrice, isActive, attributes != null ? attributes.size() : 0);

        SearchSession searchSession = Search.session(em);

        final Boolean finalIsActive = isActive;

        SearchResult<Product> result = searchSession.search(Product.class)
                .where(f -> {
                    var bool = f.bool();
                    boolean hasCondition = false;

                    if (query != null && !query.isBlank()) {
                        bool.must(f.match()
                                .fields("name", "description")
                                .matching(query)
                                .fuzzy(2));
                        hasCondition = true;
                    }

                    if (minPrice != null) {
                        bool.filter(f.range().field("price").greaterThan(minPrice));
                        hasCondition = true;
                    }
                    if (maxPrice != null) {
                        bool.filter(f.range().field("price").lessThan(maxPrice));
                        hasCondition = true;
                    }
                    if (finalIsActive != null) {
                        bool.filter(f.match().field("isActive").matching(finalIsActive));
                        hasCondition = true;
                    }

                    if (attributes != null && !attributes.isEmpty()) {
                        attributes.forEach((key, value) -> {
                            bool.filter(f.nested().objectField("attributeValues")
                                    .nest(f.bool()
                                            .must(f.match().field("attributeValues.attribute.name").matching(key))

                                            .must(f.match().field("attributeValues.value").matching(value).fuzzy(1))
                                    ));
                        });
                        hasCondition = true;
                    }

                    if (!hasCondition) {
                        bool.must(f.matchAll());
                    }

                    return bool;
                })
                .sort(f -> {
                    if (pageable.getSort().isSorted()) {
                        var composite = f.composite();
                        for (Sort.Order order : pageable.getSort()) {
                            var fieldSort = f.field(order.getProperty());
                            if (order.isDescending()) {
                                fieldSort.desc();
                            } else {
                                fieldSort.asc();
                            }
                            composite.add(fieldSort);
                        }
                        return composite;
                    }
                    return f.score();
                })
                .loading(o -> o.graph("Product.withDetails", GraphSemantic.LOAD))
                .fetch((int) pageable.getOffset(), pageable.getPageSize());

        logger.info("Search completed: found {} products", result.total().hitCount());

        List<ProductSearchDTO> dtos = result.hits().stream()
                .map(productMapper::toDTO)
                .toList();

        logger.debug("Returning {} products for page {} (size: {})", 
                    dtos.size(), pageable.getPageNumber(), pageable.getPageSize());

        return new PageImpl<>(dtos, pageable, result.total().hitCount());
    }

}

