package com.ecommerce.E_commerce.controller;

import com.ecommerce.E_commerce.dto.product.ProductSearchDTO;
import com.ecommerce.E_commerce.model.User;
import org.springframework.data.domain.Page;
import com.ecommerce.E_commerce.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {
    
    private static final Logger logger = LoggerFactory.getLogger(SearchController.class);
    private final SearchService searchService;

    @PostMapping
    public Page<ProductSearchDTO> search(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Boolean isActive,
            @RequestBody(required = false) Map<String, String> attributes,
            @PageableDefault(size = 20) Pageable pageable)
    {
        logger.debug("POST /api/search - Searching products: query={}, minPrice={}, maxPrice={}, attributesCount={}", 
                    query, minPrice, maxPrice, attributes != null ? attributes.size() : 0);
        Page<ProductSearchDTO> results = searchService.search(query, minPrice, maxPrice, isActive, attributes, pageable);
        logger.debug("POST /api/search - Search completed: resultsCount={}", results.getTotalElements());
        return results;
    }
}
