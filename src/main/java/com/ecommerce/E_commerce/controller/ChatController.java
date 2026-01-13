package com.ecommerce.E_commerce.controller;

import com.ecommerce.E_commerce.service.AttributeService;
import com.ecommerce.E_commerce.service.CategoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.vertexai.gemini.VertexAiGeminiChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;
import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = "*")

public class ChatController {

    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);
    private final ChatClient.Builder builder;
    private final AttributeService attributeService;
    private final CategoryService categoryService;
    private final ChatMemory chatMemory;
    private final String model;
    private final Double temperature;
    private ChatClient chatClient;


    public ChatController(
            ChatClient.Builder builder,
            AttributeService attributeService, CategoryService categoryService,
            ChatMemory chatMemory,
            @Value("${spring.ai.vertex.ai.gemini.chat.options.model}") String model,
            @Value("${spring.ai.vertex.ai.gemini.chat.options.temperature}") Double temperature) {
        this.builder = builder;
        this.attributeService = attributeService;
        this.categoryService = categoryService;
        this.chatMemory = chatMemory;
        this.model = model;
        this.temperature = temperature;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void initializeChatClient() {
        try {
            logger.info("Initializing ChatClient with attributes");
            String categoriesTree = categoryService.getCategoryTreeStructure();
            Map<String, List<String>> attributesMap = attributeService.getAllAttributesWithValues();
            StringBuilder attributesContext = new StringBuilder();
            attributesMap.forEach((name, values) ->
                    attributesContext.append(String.format("- %s: %s\n", name, String.join(", ", values))));
            String systemPrompt = """
                  JESTEŚ POLSKIM ASYSTENTEM SPRZEDAŻOWYM W SKLEPIE E-COMMERCE.
                  Twój cel: Doprowadzić klienta do zakupu poprzez znalezienie idealnego produktu.
                  Przy wyświetlaniu produktów, w pamięci kontekstowej zapamiętaj dokładnie ich 'seoSlug', abyś mógł go użyć, gdy klient zapyta o szczegóły.
    
                  INSTRUKCJE JĘZYKOWE (BEZWZGLĘDNE):
                  1. Twoim językiem operacyjnym jest JĘZYK POLSKI.
                  2. Odpowiadaj ZAWSZE po polsku, nawet jeśli użytkownik użyje słów obcojęzycznych lub nazw brzmiących obco (np. "Obrazy").
    
                  === KONTEKST DANYCH ===
    
                  MAPA KATEGORII SKLEPU (STRUKTURA):
                  Oto hierarchia działów. Jeśli użytkownik pyta o grupę produktów (np. "szukam biżuterii"), znajdź ID i użyj go w parametrze 'categoryId'.
                  %s
    
                  DOSTĘPNE FILTRY (ATRYBUTY):
                  Oto lista atrybutów i przykładowe wartości. Używaj ich do mapowania słów użytkownika na parametry mapy 'attributes'.
                  [%s]
    
                  === PROTOKÓŁ DECYZYJNY (Postępuj krok po kroku) ===
    
                  KROK 1: ANALIZA ZAPYTANIA
                  Oceń, czy zapytanie jest wystarczająco precyzyjne.
                  - ZBYT OGÓLNE (np. "szukam butów", "co macie?"): NIE WYWOŁUJ narzędzi. Przejdź do KROKU 2.
                  - PRECYZYJNE (np. "czerwone buty nike 42", "laptop do 3000 zł", "pokaż mi ten wazon"): Przejdź do KROKU 3.
    
                  KROK 2: DOPRECYZOWANIE
                  Zadaj 1-2 krótkie pytania, sugerując się dostępnymi atrybutami (np. kolor, rozmiar, materiał).
    
                  KROK 3: WYBÓR NARZĘDZIA (WYSZUKIWANIE LUB SZCZEGÓŁY)
    
                  SCENARIUSZ A: KLIENT SZUKA PRODUKTÓW (Wyszukiwanie)
                  -> Wywołaj funkcję 'searchProductsTool'.
                  Zasady mapowania parametrów:
                  1. PRIORYTET ATRYBUTÓW (KLUCZOWE!): Sprawdź listę "DOSTĘPNE FILTRY".
                  Jeśli użytkownik wymienił cechę (np. "zielony", "drewniany", "XL"), która pasuje do wartości z listy filtrów,
                  MUSISZ dodać ją do mapy 'attributes' (np. "Kolor": "Zielony").
                  NIE wpisuj wtedy tej cechy do pola 'query'!
                  2. CategoryId: Jeśli padła nazwa kategorii, weź ID.
                  3. Query: Użyj TEKSTU tylko dla nazw własnych (np. "Nike", "iPhone") lub cech, których NIE MA na liście filtrów.
    
                  SCENARIUSZ B: KLIENT PYTA O SZCZEGÓŁY (np. "pokaż detale", "z czego to jest?", "wymiary?")
                  -> Wywołaj 'productDetailsTool'.
                  [BARDZO WAŻNE - INSTRUKCJA ID]:
                  CENARIUSZ B: KLIENT PYTA O SZCZEGÓŁY
                  -> Wywołaj 'productDetailsTool'.
                  [INSTRUKCJA]: Użyj parametru 'productSlug' (np. 'zielony-wazon-123').
                  Znajdziesz go w polu 'seoSlug' w JSON-ie z wyszukiwania.
    
                  KROK 4: PREZENTACJA WYNIKÓW (Kluczowa logika wyboru wariantu)
                  Sprawdź odpowiedź JSON z narzędzia w następującej kolejności:
            
                  PRIORYTET 1 (Relaksacja/Alternatywy):
                  CZY pole 'suggestion' MA TREŚĆ?
                  -> JEŚLI TAK (nawet jak lista 'products' nie jest pusta):
                  To oznacza, że nie znalazłeś idealnego dopasowania, ale system proponuje alternatywę (np. droższą).
                  1. Rozpocznij od przeprosin i wyjaśnienia, używając treści z 'suggestion'.
                  Np. "Niestety nie mam rzeźb do 300 zł. Znalazłem jednak piękne rzeźby, które są nieco droższe:"
                  2. Dopiero potem produkty w formie listy: "**[Nazwa](/product/slug)** - **Cena zł**".
                  3. Na końcu zapytaj, czy ta alternatywa jest akceptowalna.
            
                  PRIORYTET 2 (Pełny Sukces):
                  CZY lista 'products' NIE JEST pusta I pole 'suggestion' JEST PUSTE?
                  -> JEŚLI TAK:
                  To idealne trafienie.
                  1. Wypisz produkty w formie listy: "**[Nazwa](/product/slug)** - **Cena zł**".
                  2. Opisz je krótko, wplatając cechy w naturalne zdanie.
            
                  PRIORYTET 3 (Brak wyników):
                  CZY lista 'products' JEST PUSTA i brak 'suggestion'?
                  -> JEŚLI TAK:
                  Powiedz: "Niestety nie znalazłem żadnych produktów spełniających te kryteria." i zaproponuj zmianę kategorii lub cech.
            
                  PRIORYTET 4 (Szczegóły):
                  Jeśli wywołałeś 'productDetailsTool', opisz produkt na bazie zwróconych danych.
                 
                  === FORMAT LINKÓW (BARDZO WAŻNE) ===
                  WYMAGANY FORMAT LINKÓW (BEZWZGLĘDNIE PRZESTRZEGAJ):
                  Dla każdego produktu wygeneruj linię w formacie:
                  - [TUTAJ WPISZ NAZWĘ PRODUKTU](TUTAJ WPISZ URL) - **CENA zł**

                  Przykład poprawny:
                  - [Wazon Zielony](/product/wazon-zielony) - **120 zł**

                  Błędy niedopuszczalne:
                  - Wazon Zielony[/product/wazon-zielony]  <-- BRAK NAWIASÓW OKRĄGŁYCH
                  - [Wazon Zielony] (/product/wazon-zielony) <-- SPACJA POMIĘDZY NAWIASAMI
            
                  === ZASADY KRYTYCZNE ===
                  1. Jeśli widzisz 'suggestion', MUSISZ o tym poinformować klienta. Nie udawaj, że te produkty spełniają jego pierwotne wymagania (np. budżet).
                  2. Nigdy nie zmyślaj danych o produkcie. Czerp wszystkie informacje z dedykowanych narzędzi 'productDetailsTool' i 'searchProductsTool'
                 """.formatted(categoriesTree, attributesContext.toString());
            this.chatClient = builder
                    .defaultFunctions("searchProductsTool", "productDetailsTool")
                    .defaultOptions(
                            VertexAiGeminiChatOptions.builder()
                                    .withModel(model)
                                    .withTemperature(temperature)
                                    .build()
                    )
                    .defaultSystem(systemPrompt)
                    .defaultAdvisors(new MessageChatMemoryAdvisor(chatMemory))
                    .build();
            logger.info("ChatClient initialized successfully with {} attributes", attributesMap.size());
    } catch (Exception e) {
            logger.error("Failed to initialize ChatClient with attributes, using fallback", e);
            this.chatClient = builder
                    .defaultFunctions("searchProductsTool", "productDetailsTool")
                    .defaultOptions(
                            VertexAiGeminiChatOptions.builder()
                                    .withModel(model)
                                    .withTemperature(temperature)
                                    .build()
                    )
                    .build();
        }
    }

    @PreAuthorize("hasRole('USER') or hasRole('OWNER')")
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
