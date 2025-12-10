package com.ecommerce.E_commerce.controller;

import com.ecommerce.E_commerce.service.AttributeService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.vertexai.gemini.VertexAiGeminiChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;
import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = "*")
public class ChatController {

    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);
    private final ChatClient.Builder builder;
    private final AttributeService attributeService;
    private final ChatMemory chatMemory;
    private final String model;
    private final Float temperature;
    private ChatClient chatClient;


    public ChatController(
            ChatClient.Builder builder,
            AttributeService attributeService,
            ChatMemory chatMemory,
            @Value("${spring.ai.vertex.ai.gemini.chat.options.model}") String model,
            @Value("${spring.ai.vertex.ai.gemini.chat.options.temperature}") Float temperature) {
        this.builder = builder;
        this.attributeService = attributeService;
        this.chatMemory = chatMemory;
        this.model = model;
        this.temperature = temperature;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void initializeChatClient() {
        try {
            logger.info("Initializing ChatClient with attributes");
            List<String> attributes = attributeService.getAllAttributeNames();
            String attributesString = String.join(", ", attributes);
            String systemPrompt = """
                    Jesteś inteligentnym i pomocnym asystentem sprzedażowym w sklepie e-commerce.
                    Twoim celem jest doprowadzenie klienta do zakupu poprzez znalezienie idealnego produktu.
                    
                    KONTEKST DANYCH (ATRYBUTY):
                    Poniżej znajduje się lista dostępnych atrybutów, po których możesz filtrować.
                    Używaj ich dokładnie jako kluczy w mapie 'attributes' przy wyszukiwaniu:
                    [%s]
                    
                    PROTOKÓŁ DECYZYJNY (Postępuj krok po kroku):
                    
                    KROK 1: ANALIZA ZAPYTANIA
                    Oceń, czy zapytanie użytkownika jest wystarczająco precyzyjne.
                    - ZBYT OGÓLNE (np. "szukam butów", "macie coś fajnego?"): NIE WYWOŁUJ narzędzia wyszukiwania. Przejdź do KROKU 2.
                    - PRECYZYJNE (np. "czerwone buty nike rozmiar 42", "laptop do 3000 zł"): Przejdź do KROKU 3.
                    
                    KROK 2: DOPRECYZOWANIE (Gdy zapytanie jest ogólne lub brak wyników)
                    Zadaj 1-2 krótkie, otwarte pytania, aby zawęzić poszukiwania.
                    Sugeruj się dostępnymi atrybutami (np. zapytaj o kolor, rozmiar, markę lub przeznaczenie).
                    NIE zmyślaj atrybutów spoza listy.
                    
                    KROK 3: WYSZUKIWANIE (Tylko gdy masz konkrety)
                    Wywołaj funkcję 'searchProductsTool' z zidentyfikowanymi parametrami.
                    - Mapuj słowa użytkownika na nasze atrybuty (np. "tanie" -> maxPrice, "czerwone" -> attributes: key='color', value='red').
                    
                    KROK 4: PREZENTACJA WYNIKÓW
                    - Jeśli funkcja zwróci produkty: Wypisz je w formie listy z cenami i linkami markdown: [Nazwa Produktu](/products/<seoSlug>).
                    - Jeśli funkcja zwróci pustą listę: Przeproś i natychmiast zadaj pytanie, które pomoże zmienić kryteria (np. "Nie mam czerwonych, ale może być inny kolor?").
                    
                    ZASADY KRYTYCZNE:
                    1. Minimalizuj liczbę pustych strzałów do bazy danych. Szukaj tylko, gdy masz szansę na trafienie.
                    2. Bądź uprzejmy, ale zwięzły.
                    3. Nigdy nie zwracaj surowego JSON-a, zawsze formatuj odpowiedź dla człowieka.
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
                    .defaultAdvisors(new MessageChatMemoryAdvisor(chatMemory))
                    .build();
            logger.info("ChatClient initialized successfully with {} attributes", attributes.size());
        } catch (Exception e) {
            logger.error("Failed to initialize ChatClient with attributes, using fallback", e);
            this.chatClient = builder
                    .defaultFunctions("searchProductsTool")
                    .defaultOptions(
                            VertexAiGeminiChatOptions.builder()
                                    .withModel(model)
                                    .withTemperature(temperature)
                                    .build()
                    )
                    .build();
        }
    }

    @PostMapping("/chat")
    public String chat(@RequestBody ChatRequest request) {
        logger.debug("POST /api/ai/chat - Processing chat request: conversationId={}", request.conversationId());
        if (chatClient == null) {
            logger.warn("ChatClient not initialized, attempting initialization");
            initializeChatClient();
        }

        String response = chatClient.prompt()
                .user(request.message())
                .advisors(a -> a
                        .param(CHAT_MEMORY_CONVERSATION_ID_KEY, request.conversationId())
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10)
                )
                .call()
                .content();
        logger.debug("POST /api/ai/chat - Chat response generated: conversationId={}", request.conversationId());
        return response;
    }

    public record ChatRequest(String message, String conversationId) {
    }
}
