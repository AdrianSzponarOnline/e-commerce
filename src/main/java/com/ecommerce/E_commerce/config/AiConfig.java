package com.ecommerce.E_commerce.config;

import com.ecommerce.E_commerce.dto.product.ProductDTO;
import com.ecommerce.E_commerce.dto.product.ProductSearchDTO;
import com.ecommerce.E_commerce.dto.search.ProductDetailsRequest;
import com.ecommerce.E_commerce.dto.search.ProductDetailsResponse;
import com.ecommerce.E_commerce.dto.search.ProductSearchRequest;
import com.ecommerce.E_commerce.service.ProductService;
import com.ecommerce.E_commerce.service.SearchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Configuration
@Profile("!test")
public class AiConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(AiConfig.class);
    
    @Bean
    public ChatMemory chatMemory() {
        return new InMemoryChatMemory();
    }

    @Bean
    @Description("Wyszukuje produkty. Użyj tego narzędzia, gdy użytkownik pyta o asortyment, chce coś znaleźć lub pyta o cenę.")
    public Function<ProductSearchRequest, ProductSearchResponse> searchProductsTool(SearchService searchService) {
        return request -> {
            logger.info("TOOL: SearchProducts called via Gemini. Query: '{}', CategoryId: {}, Attributes: {}",
                    request.query(), request.categoryId(), request.attributes());  Pageable pageable = PageRequest.of(0,5);
            try {
                Page<ProductSearchDTO> results = searchService.search(
                        request.query(),
                        request.categoryId(),
                        request.minPrice(),
                        request.maxPrice(),
                        true,
                        request.attributes(),
                        pageable
                );
                if (results.hasContent()) {
                    logger.info("TOOL: Strict search success. Found {} products.", results.getTotalElements());
                    return new ProductSearchResponse(results.getContent(), null);
                }
                logger.info("TOOL: Strict search returned 0 results. Starting relaxation strategies...");
                String suggestion;

                if (request.maxPrice() != null || request.minPrice() != null) {
                    logger.debug("TOOL: Attempting Price Relaxation...");
                    Page<ProductSearchDTO> priceCheck = searchService.search(
                            request.query(),
                            request.categoryId(),
                            null,
                            null,
                            true,
                            request.attributes(),
                            PageRequest.of(0, 1));
                    if (priceCheck.hasContent()) {
                        logger.info("TOOL: Price Relaxation HIT. Suggesting different price range.");
                        suggestion = "Znaleziono produkty spełniające kryteria, ale są poza podanym zakresem cenowym";
                        return new ProductSearchResponse(priceCheck.getContent(), suggestion);
                    }
                }
                if (request.attributes() != null && !request.attributes().isEmpty()) {
                    if (request.attributes().size() > 1) {
                        logger.debug("TOOL: Attempting Iterative Attribute Relaxation ({} attributes)...", request.attributes().size());
                        for (String keyToRemove : request.attributes().keySet()) {
                            logger.trace("TOOL: Checking without attribute: {}", keyToRemove);
                            Map<String, String> relaxedAttributes = new HashMap<>(request.attributes());
                            relaxedAttributes.remove(keyToRemove);

                            Page<ProductSearchDTO> partialCheck = searchService.search(
                                    request.query(),
                                    request.categoryId(),
                                    request.minPrice(),
                                    request.maxPrice(),
                                    true,
                                    relaxedAttributes,
                                    PageRequest.of(0, 1)
                            );
                            if (partialCheck.hasContent()) {
                                logger.info("TOOL: Attribute Relaxation HIT. Removed key: {}", keyToRemove);
                                String ignoredValue = request.attributes().get(keyToRemove);
                                suggestion = String.format(
                                        "Znalazłem produkty spełniające większość kryteriów, ale niestety nie mamy wariantu: %s=%s. " +
                                                "Znalazłem jednak pozostałe pasujące cechy.",
                                        keyToRemove, ignoredValue
                                );
                                return new ProductSearchResponse(List.of(), suggestion);
                            }
                        }
                    }
                    logger.debug("TOOL: Attempting Full Attribute Removal...");
                    Page<ProductSearchDTO> allAttrsRemovedCheck = searchService.search(
                            request.query(),
                            request.categoryId(),
                            request.minPrice(),
                            request.maxPrice(),
                            true,
                            null,
                            PageRequest.of(0, 1)
                    );
                    if (allAttrsRemovedCheck.hasContent()) {
                        logger.info("TOOL: Full Attribute Removal HIT.");
                        suggestion = "Znaleziono produkty tego typu (kategoria/nazwa), ale niestety nie posiadamy ich w wybranych wariantach (kolor/rozmiar itp.).";
                        return new ProductSearchResponse(allAttrsRemovedCheck.getContent(), suggestion);
                    }
                }
                if (request.query() != null && !request.query().isBlank() && request.categoryId() != null) {
                    logger.debug("TOOL: Attempting Category Fallback (ignoring query name)...");
                    Page<ProductSearchDTO> catCheck = searchService.search(
                            null,
                            request.categoryId(),
                            null, null, true, null,
                            PageRequest.of(0, 1)
                    );
                    if (catCheck.hasContent()) {
                        logger.info("TOOL: Category Fallback HIT.");
                        suggestion = "Nie znaleziono dokładnie tego modelu, ale mamy inne produkty w tej kategorii.";
                        return new ProductSearchResponse(List.of(), suggestion);
                    }
                }


                logger.warn("TOOL: All relaxation strategies failed. Returning empty result.");
                return new ProductSearchResponse(List.of(), "Brak produktów spełniających jakiekolwiek zbliżone kryteria.");
            }catch (Exception e){
                logger.error("Tool critical error: ", e);
                return new ProductSearchResponse(List.of(), "Wystąpił wewnętrzny błąd wyszukiwarki (np. problem z połączeniem)." +
                        " Przeproś klienta i poproś o spróbowanie później.");
            }
        };
    }
    @Bean
    @Description("Pobiera SZCZEGÓŁY produktu (pełny opis, specyfikacja techniczna, koszt dostawy). Użyj, gdy klient dopytuje o konkrety wybranego produktu.")
    public Function<ProductDetailsRequest, ProductDetailsResponse> productDetailsTool(ProductService productService) {
        return request -> {
            logger.debug("AI requesting details for slug: {}", request.productSlug());

            try {
                ProductDTO product = productService.getBySeoSlug(request.productSlug());

                String specs = product.attributeValues().stream()
                        .map(val -> val.attributeName() + ": " + val.attributeValue())
                        .collect(Collectors.joining(", "));

                return new ProductDetailsResponse(
                        product.name(),
                        product.description(),
                        product.price(),
                        product.category() != null ? product.category().name() : "Brak",
                        specs,
                        product.shippingCost(),
                        product.estimatedDeliveryTime()
                );
            } catch (Exception e) {
                logger.error("TOOL: Error fetching product details for slug: {}", request.productSlug(), e);
                throw e;
            }
        };
    }
    public record ProductSearchResponse(List<ProductSearchDTO> products,
                                        String suggestion) {}
}
