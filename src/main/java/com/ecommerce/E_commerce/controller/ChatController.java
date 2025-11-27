package com.ecommerce.E_commerce.controller;

import com.ecommerce.E_commerce.service.AttributeService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.vertexai.gemini.VertexAiGeminiChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = "*")
public class ChatController {


    private final ChatClient chatClient;

    public ChatController(
            ChatClient.Builder builder,
            AttributeService attributeService,
            @Value("${spring.ai.vertex.ai.gemini.chat.options.model}") String model,
            @Value("${spring.ai.vertex.ai.gemini.chat.options.temperature}") Float temperature)
    {

        List<String> attributes = attributeService.getAllAttributeNames();
        String attributesString = String.join(", ", attributes);
        String systemPrompt = """
                Jesteś inteligentnym asystentem sprzedażowym w sklepie.
                Twoim celem jest pomóc klientom znaleźć produkty, używając narzędzia 'searchProductsTool'.
                
                BARDZO WAŻNE - Kiedy identyfikujesz cechy produktu w pytaniu użytkownika, 
                musisz mapować je na konkretne nazwy atrybutów dostępne w naszym sklepie.
                
                DOSTĘPNE NAZWY ATRYBUTÓW (Używaj ich dokładnie jako kluczy w mapie 'attributes'):
                %s
                ZASADY KRYTYCZNE:
                
                     1. NIE OPOWIADAJ o tym, co zamierzasz zrobić.
                     2. Jeśli użytkownik pyta o produkt -> NATYCHMIAST wywołaj funkcję 'searchProductsTool'.
                     3. Nie pisz zdań w stylu "Poszukam tego dla Ciebie". Po prostu szukaj.
                INSTRUKCJA:
                 1. Gdy klient pyta o produkt -> NATYCHMIAST wywołaj 'searchProductsTool'.
                 2. Po otrzymaniu danych -> Wypisz listę produktów z cenami i linkami.
                                                                                                               
                  Format linku: [Nazwa](/products/<seoSlug>)
                """.formatted(attributesString);
        this.chatClient = builder
                .defaultFunctions("searchProductsTool")
                .defaultOptions(
                        VertexAiGeminiChatOptions.builder()
                                .withModel(model)
                                .withTemperature(temperature)
                                .build()
                )
                .defaultSystem(systemPrompt)
                .build();
    }
    @PostMapping("/chat")
    public String chat(@RequestBody String message) {
        return chatClient.prompt()
                .user(message)
                .call()
                .content();
    }

}
