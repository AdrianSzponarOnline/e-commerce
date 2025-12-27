package com.ecommerce.E_commerce.config;

import com.ecommerce.E_commerce.dto.product.ProductSearchDTO;
import com.ecommerce.E_commerce.dto.search.ProductSearchRequest;
import com.ecommerce.E_commerce.service.SearchService;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.function.Function;

@Configuration
@Profile("!test")
public class AiConfig {
    @Bean
    public ChatMemory chatMemory() {
        return new InMemoryChatMemory();
    }

    @Bean
    @Description("Wyszukuje produkty. Użyj tego narzędzia, gdy użytkownik pyta o asortyment, chce coś znaleźć lub pyta o cenę.")
    public Function<ProductSearchRequest, ProductSearchResponse> searchProductsTool(SearchService searchService) {
        return request -> {
            System.out.println("Gemini wykonuje funkcję z:" + request);
            Pageable pageable = PageRequest.of(0,5);

            Page<ProductSearchDTO> results = searchService.search(
                    request.query(),
                    request.minPrice(),
                    request.maxPrice(),
                    true,
                    request.attributes(),
                    pageable
            );
            return new ProductSearchResponse(results.getContent());
        };
    }
    public record ProductSearchResponse(List<ProductSearchDTO> products) {}
}
