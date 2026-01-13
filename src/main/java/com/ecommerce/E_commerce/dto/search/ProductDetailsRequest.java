package com.ecommerce.E_commerce.dto.search;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record ProductDetailsRequest(
        @JsonPropertyDescription(
                "Unikalny seo slug" +
                "Kluczowa zasada: AI musi pobrać tę wartość z wyników wcześniejszego wyszukiwania " +
                "(z pola 'id' zwróconego przez funkcję searchProductsTool). " +
                "Wpisuj tudaj dokładnie seo slug")
        String productSlug
) {
}
