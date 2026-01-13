package com.ecommerce.E_commerce.dto.search;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.math.BigDecimal;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL) // Opcjonalnie: nie wysyła nulli do AI, oszczędza tokeny
public record ProductSearchRequest(

        @JsonPropertyDescription("Słowa kluczowe do wyszukiwania w nazwie lub opisie produktu, np. 'czerwona sukienka', 'laptop gamingowy'.")
        String query,

        @JsonPropertyDescription("ID kategorii do filtrowania (Long). Wypełnij tylko, jeśli znasz ID konkretnej kategorii, o którą prosi użytkownik.")
        Long categoryId,

        @JsonPropertyDescription("Minimalna cena produktu (opcjonalnie).")
        BigDecimal minPrice,

        @JsonPropertyDescription("Maksymalna cena produktu (opcjonalnie).")
        BigDecimal maxPrice,

        @JsonPropertyDescription("Mapa precyzyjnych filtrów (atrybutów). Klucz to nazwa atrybutu (np. 'Kolor', 'Rozmiar', 'Materiał'), a wartość to poszukiwana cecha.")
        Map<String, String> attributes
) {
}