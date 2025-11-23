package com.ecommerce.E_commerce.service;

import com.ecommerce.E_commerce.dto.product.ProductSearchDTO;
import com.ecommerce.E_commerce.mapper.ProductMapper;
import com.ecommerce.E_commerce.model.Product;
import jakarta.persistence.EntityGraph;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.hibernate.graph.GraphSemantic;
import org.hibernate.search.engine.search.query.SearchResult;
import org.hibernate.search.mapper.orm.Search;
import org.hibernate.search.mapper.orm.session.SearchSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SearchService {
    private final EntityManager em;
    private final ProductMapper productMapper;

    public Page<ProductSearchDTO> search(
            String query,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Map<String, String> attributes,
            Pageable pageable
    ) {
        System.out.println("🔍 SEARCH REQUEST: query='" + query + "', filters=" + attributes);

        SearchSession searchSession = Search.session(em);

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
                .loading(o -> o.graph("Product.withDetails", GraphSemantic.LOAD))
                .fetch((int) pageable.getOffset(), pageable.getPageSize());

        System.out.println("ZNALEZIONO: " + result.total().hitCount());

        List<ProductSearchDTO> dtos = result.hits().stream()
                .map(productMapper::toDTO)
                .toList();

        return new PageImpl<>(dtos, pageable, result.total().hitCount());
    }

}

