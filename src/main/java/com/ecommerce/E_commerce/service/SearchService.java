package com.ecommerce.E_commerce.service;

import com.ecommerce.E_commerce.dto.product.ProductSearchDTO;
import com.ecommerce.E_commerce.mapper.ProductMapper;
import com.ecommerce.E_commerce.model.Category;
import com.ecommerce.E_commerce.model.Product;
import com.ecommerce.E_commerce.model.ProductAttributeValue;
import com.ecommerce.E_commerce.repository.CategoryRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.hibernate.graph.GraphSemantic;
import org.hibernate.search.engine.search.predicate.dsl.SearchPredicateFactory;
import org.hibernate.search.engine.search.predicate.dsl.PredicateFinalStep;
import org.hibernate.search.engine.search.query.SearchResult;
import org.hibernate.search.engine.search.sort.dsl.SearchSortFactory;
import org.hibernate.search.engine.search.sort.dsl.SortFinalStep;
import org.hibernate.search.mapper.orm.Search;
import org.hibernate.search.mapper.orm.session.SearchSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchService {

    private static final Logger logger = LoggerFactory.getLogger(SearchService.class);
    private final EntityManager em;
    private final ProductMapper productMapper;
    private final CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public Page<ProductSearchDTO> search(
            String query,
            Long categoryId,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Boolean isActive,
            Map<String, String> attributes,
            Pageable pageable
    ) {
        logger.info("Search req: query='{}', catId={}, price={}-{}", query, categoryId, minPrice, maxPrice);

        SearchSession searchSession = Search.session(em);

        Set<Long> categoryIdsToFilter = resolveTargetCategoryIds(categoryId);

        SearchResult<Product> result = searchSession.search(Product.class)
                .where(f -> buildSearchPredicate(
                        f, query, categoryIdsToFilter, minPrice, maxPrice, isActive, attributes))
                .sort(f -> buildSort(f, pageable))
                .loading(o -> o.graph("Product.withDetails", GraphSemantic.LOAD))
                .fetch((int) pageable.getOffset(), pageable.getPageSize());

        List<ProductSearchDTO> dtos = result.hits().stream()
                .map(product -> {
                    // standardowe mapowanie (id, nazwa, cena)
                    ProductSearchDTO baseDto = productMapper.toDTO(product);


                    Map<String, String> attributesMap = product.getAttributeValues().stream()
                            .filter(pav -> pav.getAttribute() != null)
                            .collect(Collectors.toMap(
                                    pav -> pav.getAttribute().getName(), // Klucz
                                    pav -> pav.getAttributeValue() != null ? pav.getAttributeValue() : "",   // Wartość
                                    (existing, replacement) -> existing
                            ));

                    // 3. rekord z wypełnioną mapą
                    return new ProductSearchDTO(
                            baseDto.id(),
                            baseDto.name(),
                            baseDto.price(),
                            baseDto.shortDescription(),
                            baseDto.thumbnailUrl(),
                            baseDto.seoSlug(),
                            baseDto.categoryName(),
                            baseDto.isActive(),
                            attributesMap
                    );
                })
                .toList();

        return new PageImpl<>(dtos, pageable, result.total().hitCount());
    }

    // metody pomocnicze

    private PredicateFinalStep buildSearchPredicate(
            SearchPredicateFactory f,
            String query,
            Set<Long> categoryIds,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Boolean isActive,
            Map<String, String> attributes
    ) {
        var bool = f.bool();
        boolean hasCondition = false;

        if (categoryIds != null && !categoryIds.isEmpty()) {
            bool.filter(f.terms().field("category.id").matchingAny(categoryIds));
            hasCondition = true;
        }
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
        if (isActive != null) {
            bool.filter(f.match().field("isActive").matching(isActive));
            hasCondition = true;
        }

        if (attributes != null && !attributes.isEmpty()) {
            attributes.forEach((key, value) -> {
                bool.filter(f.nested().objectField("attributeValues")
                        .nest(f.bool()
                                .must(f.bool()
                                        .should(f.match().field("attributeValues.attribute.name")
                                                .matching(key).fuzzy(1))
                                        .should(f.wildcard().field("attributeValues.attribute.name")
                                                .matching("*" + key + "*"))
                                        .should(f.wildcard().field("attributeValues.attribute.name")
                                                .matching("*" + key.toLowerCase() + "*"))
                                )

                                .must(f.bool()
                                        .should(f.match().field("attributeValues.attributeValue")
                                                .matching(value).fuzzy(2))

                                        .should(f.match().field("attributeValues.attributeValue")
                                                .matching(value.toLowerCase()).fuzzy(2))
                                        .should(f.wildcard().field("attributeValues.attributeValue")
                                                .matching("*" + value.toLowerCase() + "*"))
                                )
                        ));
            });
            hasCondition = true;
        }
        return bool;
    }


    private Set<Long> resolveTargetCategoryIds(Long categoryId) {
        if (categoryId == null) {
            return Collections.emptySet();
        }

        Set<Long> ids = new HashSet<>();
        categoryRepository.findById(categoryId).ifPresent(rootCategory ->
                collectCategoryIdsRecursively(rootCategory, ids)
        );
        logger.info("Dla kategorii {} znaleziono IDs: {}", categoryId, ids);

        if (ids.isEmpty()) {
            ids.add(-1L);
        }
        return ids;
    }

    private void collectCategoryIdsRecursively(Category category, Set<Long> ids) {
        ids.add(category.getId());
        if (category.getChildren() != null) {
            for (Category child : category.getChildren()) {
                collectCategoryIdsRecursively(child, ids);
            }
        }
    }


    private SortFinalStep buildSort(
            SearchSortFactory f,
            Pageable pageable
    ) {
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
    }
}