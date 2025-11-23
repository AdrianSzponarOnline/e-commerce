package com.ecommerce.E_commerce.controller;

import com.ecommerce.E_commerce.dto.product.ProductSearchDTO;
import org.springframework.data.domain.Page;
import com.ecommerce.E_commerce.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {
    private final SearchService searchService;

    @PostMapping
    public Page<ProductSearchDTO> search(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestBody(required = false) Map<String, String> attributes,
            @PageableDefault(size = 20) Pageable pageable)
    {
        return searchService.search(query, minPrice, maxPrice, attributes, pageable );
    }
}
