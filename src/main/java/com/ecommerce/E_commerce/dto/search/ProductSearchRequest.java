package com.ecommerce.E_commerce.dto.search;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.math.BigDecimal;
import java.util.Map;

public record ProductSearchRequest(
        @JsonPropertyDescription("Tekst wyszukiwania, np. 'czerwona sukienka', 'laptop gamingowy'.")
        String query,

        @JsonPropertyDescription("Minimalna cena (opcjonalnie).")
        BigDecimal minPrice,

        @JsonPropertyDescription("Maksymalna cena (opcjonalnie).")
        BigDecimal maxPrice,

        @JsonPropertyDescription("Mapa precyzyjnych filtrów. Klucz to nazwa atrybutu (musi być dokładna, np. 'Materiał rzeźby', 'Kolor'), a wartość to szukana cecha (np. 'Metal', 'Czerwony').")
        Map<String, String> attributes
) {
}
