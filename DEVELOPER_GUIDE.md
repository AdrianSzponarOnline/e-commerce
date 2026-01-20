# Developer Guide - E-commerce System

##  Przegląd techniczny

Ten przewodnik zawiera szczegółowe informacje techniczne dla deweloperów pracujących z systemem e-commerce.

##  Architektura systemu

### Warstwy aplikacji
```
┌─────────────────────────────────────┐
│           Controllers               │ ← REST API Layer
├─────────────────────────────────────┤
│            Services                 │ ← Business Logic Layer
├─────────────────────────────────────┤
│           Repositories              │ ← Data Access Layer
├─────────────────────────────────────┤
│            Entities                 │ ← Data Model Layer
├─────────────────────────────────────┤
│           Database                  │ ← Persistence Layer
└─────────────────────────────────────┘
```

### Wzorce projektowe
- **Repository Pattern** - Abstrakcja dostępu do danych
- **Service Layer Pattern** - Logika biznesowa
- **DTO Pattern** - Transfer danych między warstwami
- **Mapper Pattern** - Mapowanie obiektów (MapStruct)
- **Builder Pattern** - Tworzenie złożonych obiektów
- **Strategy Pattern** - Generowanie SKU

## 🔧 Konfiguracja środowiska

### Wymagania systemowe
- **Java:** 17+
- **Maven:** 3.6+
- **PostgreSQL:** 12+
- **IDE:** IntelliJ IDEA / Eclipse / VS Code

### Konfiguracja IDE
```xml
<!-- IntelliJ IDEA - settings.xml -->
<settings>
    <profiles>
        <profile>
            <id>ecommerce</id>
            <properties>
                <maven.compiler.source>17</maven.compiler.source>
                <maven.compiler.target>17</maven.compiler.target>
            </properties>
        </profile>
    </profiles>
</settings>
```

### Konfiguracja bazy danych
```sql
-- Tworzenie bazy danych
CREATE DATABASE ecommerce;
CREATE USER ecommerce_user WITH PASSWORD 'your_password';
GRANT ALL PRIVILEGES ON DATABASE ecommerce TO ecommerce_user;
```

##  Struktura projektu

### Główne katalogi
```
src/
├── main/
│   ├── java/com/ecommerce/E_commerce/
│   │   ├── config/          # Konfiguracja Spring
│   │   ├── controller/      # REST Controllers
│   │   ├── dto/            # Data Transfer Objects
│   │   ├── exception/      # Custom exceptions
│   │   ├── mapper/         # MapStruct mappers
│   │   ├── model/          # JPA Entities
│   │   ├── repository/     # JPA Repositories
│   │   └── service/        # Business Logic
│   └── resources/
│       ├── application.properties
│       └── db/migration/   # Flyway migrations
└── test/
    └── java/com/ecommerce/E_commerce/
        ├── controller/     # Controller tests
        ├── dto/           # DTO validation tests
        ├── mapper/        # Mapper tests
        ├── model/         # Model tests
        └── service/       # Service tests
```

## System bezpieczeństwa

### Konfiguracja JWT
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Value("${jwt.secret}")
    private String jwtSecret;
    
    @Value("${jwt.expiration}")
    private long jwtExpiration;
    
    // Konfiguracja bezpieczeństwa
}
```

### Role i uprawnienia
```java
public enum ERole {
    USER("USER"),
    ADMIN("ADMIN"), 
    OWNER("OWNER");
}

// Użycie w kontrolerach
@PreAuthorize("hasRole('OWNER')")
@PostMapping
public ResponseEntity<ProductDTO> create(@RequestBody ProductCreateDTO dto) {
    // Tylko OWNER może tworzyć produkty
}
```

### Walidacja danych
```java
public record ProductCreateDTO(
    @NotBlank(message = "Product name cannot be blank")
    @Size(max = 255, message = "Product name cannot exceed 255 characters")
    String name,
    
    @NotNull(message = "Price cannot be null")
    @Positive(message = "Price must be positive")
    BigDecimal price
) {}
```

### Aktywacja konta i reset hasła

System obsługuje aktywację kont użytkowników oraz resetowanie hasła poprzez email. Wszystkie tokeny są przechowywane w tabeli `confirmation_tokens`.

#### Aktywacja konta
```java
@Transactional
public void activateAccount(String token) {
    ConfirmationToken confirmationToken = getAndValidateToken(token);
    
    if (confirmationToken.getConfirmedAt() != null) {
        throw new IllegalStateException("Email has already been activated");
    }
    
    confirmationToken.setConfirmedAt(LocalDateTime.now());
    User user = confirmationToken.getUser();
    user.setEnabled(true);
    userRepository.save(user);
}
```

**Flow aktywacji:**
1. Użytkownik rejestruje się → konto tworzone z `enabled: false`
2. Generowany jest token aktywacyjny (ważny 15 minut)
3. Email z linkiem aktywacyjnym jest wysyłany automatycznie
4. Użytkownik klika link → endpoint `/api/auth/activate?token={token}`
5. Konto jest aktywowane (`enabled: true`)

#### Reset hasła
```java
@Transactional
public void forgotPassword(String email) {
    User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("User with email not found."));
    
    String token = generateAndSaveToken(user, 30); // 30 minut ważności
    String link = "http://localhost:5173/reset-password?token=" + token;
    
    emailService.sendSimpleMail(
        email,
        "Resetowanie hasła",
        "Cześć " + user.getFirstName() + ",\n\n" +
            "Otrzymaliśmy prośbę o zmianę hasła. Kliknij link poniżej:\n" + link
    );
}
```

**Flow resetowania hasła:**
1. Użytkownik wysyła żądanie na `/api/auth/forgot-password` z emailem
2. Generowany jest token resetujący (ważny 30 minut)
3. Email z linkiem resetującym jest wysyłany
4. Użytkownik klika link → endpoint `/api/auth/reset-password` z tokenem i nowym hasłem
5. Hasło jest zmieniane i konto automatycznie aktywowane

#### Konfiguracja email
```properties
spring.mail.host=sandbox.smtp.mailtrap.io
spring.mail.port=587
spring.mail.username=${MAIL_USERNAME}
spring.mail.password=${MAIL_PASSWORD}
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
app.mail.from=sklep@ecommerce.com
```

##  Model danych

### Główne encje

#### User
```java
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String email;
    
    @Column(nullable = false)
    private String password;
    
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>();
}
```

#### Product
```java
@Entity
@Table(name = "products", uniqueConstraints = {
    @UniqueConstraint(columnNames = "sku")
})
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(unique = true, nullable = false)
    private String sku;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;
    
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductAttributeValue> attributeValues = new ArrayList<>();
}
```

### Relacje JPA
```java
// One-to-Many
@OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
private List<ProductAttributeValue> attributeValues;

// Many-to-One
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "category_id", nullable = false)
private Category category;

// Many-to-Many
@ManyToMany(fetch = FetchType.LAZY)
@JoinTable(name = "user_roles",
    joinColumns = @JoinColumn(name = "user_id"),
    inverseJoinColumns = @JoinColumn(name = "role_id"))
private Set<Role> roles;
```

##  MapStruct Configuration

### Podstawowa konfiguracja
```java
@Mapper(componentModel = "spring", 
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ProductMapper {
    
    ProductDTO toProductDTO(Product product);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "sku", ignore = true) // Generowane przez SkuGenerator
    @Mapping(target = "category", ignore = true) // Ustawiane przez service
    Product toProduct(ProductCreateDTO dto);
    
    void updateProductFromDTO(ProductUpdateDTO dto, @MappingTarget Product product);
}
```

### Zaawansowane mapowanie
```java
@Mapper(componentModel = "spring")
public interface ProductAttributeValueMapper {
    
    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "categoryAttributeId", source = "categoryAttribute.id")
    @Mapping(target = "categoryAttributeName", source = "categoryAttribute.name")
    @Mapping(target = "isKeyAttribute", source = "categoryAttribute.keyAttribute")
    ProductAttributeValueDTO toProductAttributeValueDTO(ProductAttributeValue pav);
}
```

##  Testowanie

### Struktura testów
```java
@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {
    
    @Mock
    private ProductRepository productRepository;
    
    @Mock
    private CategoryRepository categoryRepository;
    
    @Mock
    private ProductMapper productMapper;
    
    @InjectMocks
    private ProductServiceImpl productService;
    
    @Test
    void create_ShouldReturnProductDTO_WhenSuccessful() {
        // Given
        ProductCreateDTO dto = new ProductCreateDTO(/*...*/);
        Product product = new Product(/*...*/);
        ProductDTO expectedDTO = new ProductDTO(/*...*/);
        
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(productMapper.toProduct(dto)).thenReturn(product);
        when(productRepository.save(product)).thenReturn(product);
        when(productMapper.toProductDTO(product)).thenReturn(expectedDTO);
        
        // When
        ProductDTO result = productService.create(dto);
        
        // Then
        assertThat(result).isEqualTo(expectedDTO);
        verify(productRepository).save(product);
    }
}
```

### Testy integracyjne
```java
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(locations = "classpath:application-test.properties")
class ProductControllerIntegrationTest {
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Test
    void shouldCreateProduct() {
        // Test integracyjny z prawdziwą bazą danych
    }
}
```

### Mockowanie zależności
```java
// Ręczne wstrzykiwanie mocków
@BeforeEach
void setUp() {
    productService = new ProductServiceImpl();
    // Wstrzykiwanie przez refleksję
    Field field = ProductServiceImpl.class.getDeclaredField("productRepository");
    field.setAccessible(true);
    field.set(productService, productRepository);
}
```

## Generowanie SKU

### Algorytm generowania
```java
@UtilityClass
public class SkuGenerator {
    
    public static String generate(Product product) {
        String categoryPrefix = getCategoryPrefix(product.getCategory());
        String namePrefix = getProductNamePrefix(product.getName());
        String attributeSuffix = getAttributeSuffix(product.getAttributeValues());
        String sequence = getSequence(product);
        
        return String.format("%s-%s-%s-%s", 
            categoryPrefix, namePrefix, attributeSuffix, sequence);
    }
    
    private static String getAttributeSuffix(List<ProductAttributeValue> attributeValues) {
        return attributeValues.stream()
            .filter(pav -> pav.getCategoryAttribute().isKeyAttribute())
            .map(ProductAttributeValue::getValue)
            .collect(Collectors.joining("-"));
    }
}
```

### Testowanie generowania SKU
```java
@Test
void generate_ShouldCreateCorrectSku_WhenProductHasKeyAttributes() {
    // Given
    Product product = createTestProduct();
    product.getAttributeValues().add(createKeyAttribute("15.6", "Screen Size"));
    product.getAttributeValues().add(createKeyAttribute("Black", "Color"));
    
    // When
    String sku = SkuGenerator.generate(product);
    
    // Then
    assertThat(sku).isEqualTo("ELE-LAP-15.6-Black-1");
}
```

## Wyszukiwanie Elasticsearch

### Konfiguracja
System wykorzystuje Hibernate Search z Elasticsearch do zaawansowanego wyszukiwania produktów. Wszystkie produkty są automatycznie indeksowane przy starcie aplikacji.

### Endpoint wyszukiwania
```java
@RestController
@RequestMapping("/api/search")
public class SearchController {
    
    @PostMapping
    public Page<ProductSearchDTO> search(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestBody(required = false) Map<String, String> attributes,
            @PageableDefault(size = 20) Pageable pageable) {
        return searchService.search(query, minPrice, maxPrice, attributes, pageable);
    }
}
```

### Funkcjonalności
- Fuzzy matching dla zapytań tekstowych (tolerancja 2 znaki)
- Wyszukiwanie w polach `name` i `description`
- Filtrowanie po zakresie cen
- Filtrowanie po atrybutach produktów (nested queries)

### Automatyczne indeksowanie
```java
@Component
public class SearchIndexer implements ApplicationListener<ApplicationReadyEvent> {
    
    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        SearchSession searchSession = Search.session(em);
        MassIndexer indexer = searchSession.massIndexer(Product.class)
                .threadsToLoadObjects(4)
                .batchSizeToLoadObjects(25);
        indexer.startAndWait();
    }
}
```

### Konfiguracja analizy tekstu
```java
@Component("AnalysisConfigurer")
public class SearchAnalisisConfig implements ElasticsearchAnalysisConfigurer {
    @Override
    public void configure(ElasticsearchAnalysisConfigurationContext context) {
        context.analyzer("english").type("english");
    }
}
```

## Paginacja i sortowanie

### Implementacja w Repository
```java
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    Page<Product> findByNameContainingIgnoreCaseAndIsActive(
        String name, Boolean isActive, Pageable pageable);
    
    @Query("SELECT p FROM Product p WHERE p.category.id = :categoryId AND p.isActive = :isActive")
    Page<Product> findByCategoryAndActive(
        @Param("categoryId") Long categoryId, 
        @Param("isActive") Boolean isActive, 
        Pageable pageable);
}
```

### Użycie w Service
```java
@Override
public Page<ProductDTO> searchByName(String name, Pageable pageable) {
    Page<Product> products = productRepository.findByNameContainingIgnoreCaseAndIsActive(
        name, true, pageable);
    return products.map(productMapper::toProductDTO);
}
```

### Użycie w Controller
```java
@GetMapping
public ResponseEntity<Page<ProductDTO>> getAllProducts(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "id") String sortBy,
        @RequestParam(defaultValue = "asc") String sortDir) {
    
    Sort sort = sortDir.equalsIgnoreCase("desc") 
        ? Sort.by(sortBy).descending() 
        : Sort.by(sortBy).ascending();
    Pageable pageable = PageRequest.of(page, size, sort);
    
    Page<ProductDTO> products = productService.findAll(pageable);
    return ResponseEntity.ok(products);
}
```

##  Bulk Operations

### Implementacja bulk create
```java
@Override
public List<ProductAttributeValueDTO> createBulk(List<ProductAttributeValueCreateDTO> dtos) {
    return dtos.stream()
        .map(this::create)
        .collect(Collectors.toList());
}
```

### Implementacja bulk update
```java
@Override
public List<ProductAttributeValueDTO> updateByProduct(Long productId, List<ProductAttributeValueUpdateDTO> dtos) {
    List<ProductAttributeValue> existingValues = repository.findByProductIdAndIsActive(productId, true);
    
    if (existingValues.size() != dtos.size()) {
        throw new IllegalArgumentException("Number of update DTOs must match existing attribute values");
    }
    
    for (int i = 0; i < existingValues.size(); i++) {
        ProductAttributeValue existingValue = existingValues.get(i);
        ProductAttributeValueUpdateDTO dto = dtos.get(i);
        
        mapper.updateProductAttributeValueFromDTO(dto, existingValue);
        existingValue.setUpdatedAt(Instant.now());
        repository.save(existingValue);
    }
    
    return existingValues.stream()
        .map(mapper::toProductAttributeValueDTO)
        .collect(Collectors.toList());
}
```

##  Migracje bazy danych

### Dostępne migracje
- `V1__init_schema.sql` - Podstawowa struktura bazy danych (tabele: users, roles, categories, products, addresses, orders, payments, itp.)
- `V2__category_attribute_updates.sql` - Aktualizacje atrybutów kategorii
- `V3__insert_craft_categories.sql` - Wstawienie kategorii rzemieślniczych (rzeźby, ceramika, biżuteria, itp.)
- `V4__add_sku_unique_constraint.sql` - Unikalne ograniczenie dla SKU w tabeli products
- `V5__seed_data.sql` - Dane początkowe (użytkownicy, role, podstawowe dane)
- `V6__add_mock_users.sql` - Dodanie użytkowników testowych (testuser@example.com, owner@example.com)
- `V7__insert_sample_products.sql` - Wstawienie przykładowych produktów z atrybutami
- `V8__add_key_attribute_to_category_attributes.sql` - Dodanie kolumny `key_attribute` do tabeli category_attributes
- `V9__create_inventory.sql` - Utworzenie tabeli inventory do zarządzania stanem magazynowym
- `V10__refactor_attributes_schema.sql` - Refaktoryzacja schematu atrybutów (utworzenie tabeli attributes, migracja danych)
- `V11__add_payment_columns.sql` - Dodanie kolumn `transaction_id` i `notes` do tabeli payments
- `V12__create_confirmation_tokens.sql` - Utworzenie tabeli confirmation_tokens do aktywacji kont użytkowników

### Tworzenie migracji
```sql
-- V5__add_product_images.sql
CREATE TABLE product_images (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL,
    image_url VARCHAR(500) NOT NULL,
    alt_text VARCHAR(255),
    is_primary BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);

CREATE INDEX idx_product_images_product_id ON product_images(product_id);
CREATE INDEX idx_product_images_primary ON product_images(is_primary);
```

## Obrazy produktów – implementacja

### Serwis
- `ProductImageServiceImpl` zapisuje pliki do `${app.upload-dir}/products/{productId}` i wystawia URL `/uploads/products/{productId}/{filename}`.
- Walidacja: typ (`image/jpeg,png,webp`), rozmiar (domyślnie 5 MB), limit zdjęć (10), jedna miniatura (reszta odznaczana).

### Konfiguracja
```properties
app.upload-dir=uploads
app.upload-max-bytes=5242880
app.upload-allowed-types=image/jpeg,image/png,image/webp
app.max-images-per-product=10
```

### Serwowanie statyczne
- `WebConfig` mapuje `/uploads/**` na katalog `${app.upload-dir}`.

### Testy
- Jednostkowe: `ProductImageServiceImplTest` (walidacje, miniatura, zapis URL)
- WebMvc: `ProductImageControllerTest` (multipart, lista, delete)
- Integracyjne (H2): `ProductImageIntegrationTest` (pełny flow) – profil `test` wyłącza security.

### Uruchamianie migracji
```bash
# Sprawdzenie statusu migracji
mvn flyway:info

# Uruchomienie migracji
mvn flyway:migrate

# Cofnięcie migracji
mvn flyway:undo
```

##  Debugging i logowanie

### Strategia logowania

System używa **SLF4J** z implementacją **Logback** do kompleksowego logowania operacji biznesowych i błędów.

#### Architektura logowania

**Kontrolery:**
- Logują tylko **sukcesy** (INFO/DEBUG)
- Nie zawierają bloków try-catch - błędy są obsługiwane przez `GlobalExceptionHandler`
- Przykład: `logger.info("POST /api/orders - Order created successfully: orderId={}", order.id())`

**GlobalExceptionHandler:**
- Centralne miejsce logowania wszystkich błędów
- Błędy biznesowe (ResourceNotFoundException, BadCredentialsException) → `WARN` bez stack trace
- Niespodziewane błędy (Exception) → `ERROR` z pełnym stack trace
- Eliminuje duplikację logów

**Serwisy:**
- Logują operacje biznesowe (INFO)
- Logują ostrzeżenia przy nieprawidłowych operacjach (WARN)
- Szczegóły operacji (DEBUG) dla diagnostyki

#### Poziomy logowania

- **TRACE** - Szczegóły wewnętrzne (rzadko używane)
- **DEBUG** - Szczegóły operacji, diagnostyka
- **INFO** - Kluczowe operacje biznesowe (tworzenie zamówień, produktów, płatności)
- **WARN** - Ostrzeżenia, nieprawidłowe operacje, błędy biznesowe
- **ERROR** - Błędy wymagające uwagi, niespodziewane wyjątki

### Konfiguracja logowania

```properties
# application.properties

# Poziom logowania dla całej aplikacji
logging.level.root=INFO

# Poziom logowania dla pakietu aplikacji
logging.level.com.ecommerce.E_commerce=INFO

# Szczegółowe logowanie dla wybranych komponentów
logging.level.com.ecommerce.E_commerce.service=DEBUG
logging.level.com.ecommerce.E_commerce.controller=INFO

# Logowanie Spring Security (tylko w środowisku deweloperskim)
logging.level.org.springframework.security=DEBUG

# Logowanie Hibernate SQL (tylko w środowisku deweloperskim)
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE

# Format logów
logging.pattern.console=%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n
logging.pattern.file=%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n
```

### Użycie logów w kodzie

#### Kontrolery - logowanie sukcesów

```java
@RestController
@RequestMapping("/api/orders")
public class OrderController {
    
    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);
    
    @PostMapping
    public ResponseEntity<OrderDTO> createOrder(@Valid @RequestBody OrderCreateDTO dto) {
        logger.info("POST /api/orders - Creating order for userId={}", user.getId());
        OrderDTO order = orderService.create(user.getId(), dto);
        logger.info("POST /api/orders - Order created successfully: orderId={}, total={}", order.id(), order.totalAmount());
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
        // Błędy są automatycznie logowane przez GlobalExceptionHandler
    }
}
```

#### Serwisy - logowanie operacji biznesowych

```java
@Service
@Transactional
public class OrderServiceImpl implements OrderService {
    
    private static final Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);
    
    @Override
    public OrderDTO create(Long userId, OrderCreateDTO dto) {
        logger.info("Creating order for userId={}, itemsCount={}", userId, dto.items().size());
        
        // ... logika biznesowa ...
        
        Order savedOrder = orderRepository.save(order);
        logger.info("Order created successfully: orderId={}, userId={}, total={}", 
                   savedOrder.getId(), userId, savedOrder.getTotalAmount());
        
        return orderMapper.toOrderDTO(savedOrder);
    }
}
```

#### GlobalExceptionHandler - logowanie błędów

```java
@ControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Object> handleResourceNotFoundException(ResourceNotFoundException e) {
        logger.warn("Resource not found: {}", e.getMessage());
        return buildErrorResponse(HttpStatus.NOT_FOUND, "Not Found", e.getMessage());
    }
    
    @ExceptionHandler({BadCredentialsException.class, AuthenticationException.class})
    public ResponseEntity<Object> handleAuthenticationException(Exception e) {
        logger.warn("Authentication failed: {}", e.getMessage());
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, "Authentication Failed", "Invalid username or password");
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGlobalException(Exception e) {
        logger.error("Unexpected error occurred", e);
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", "An unexpected error occurred.");
    }
}
```

### Najlepsze praktyki

1. **Kontrolery logują tylko sukcesy** - Błędy są obsługiwane przez `GlobalExceptionHandler`
2. **Brak duplikacji** - Każdy błąd logowany tylko raz
3. **Odpowiednie poziomy** - WARN dla błędów biznesowych, ERROR dla awarii systemu
4. **Kontekst w logach** - Zawsze dodawaj istotne parametry (userId, orderId, productId)
5. **Strukturalne logowanie** - Używaj placeholderów `{}` zamiast konkatenacji stringów

### Przykłady logów

```
2024-01-15 10:30:45 [http-nio-8080-exec-1] INFO  OrderController - POST /api/orders - Creating order for userId=123
2024-01-15 10:30:45 [http-nio-8080-exec-1] INFO  OrderServiceImpl - Creating order for userId=123, itemsCount=3
2024-01-15 10:30:45 [http-nio-8080-exec-1] INFO  OrderServiceImpl - Order created successfully: orderId=456, userId=123, total=299.99
2024-01-15 10:30:45 [http-nio-8080-exec-1] INFO  OrderController - POST /api/orders - Order created successfully: orderId=456, total=299.99

2024-01-15 10:31:20 [http-nio-8080-exec-2] WARN  GlobalExceptionHandler - Resource not found: Product not found with id: 999
2024-01-15 10:31:20 [http-nio-8080-exec-2] WARN  GlobalExceptionHandler - Authentication failed: Bad credentials

2024-01-15 10:32:10 [http-nio-8080-exec-3] ERROR GlobalExceptionHandler - Unexpected error occurred
java.sql.SQLException: Connection timeout
    at com.ecommerce.E_commerce.repository.ProductRepository.findById(ProductRepository.java:45)
    ...
```

##  Performance Optimization

### Lazy Loading
```java
@Entity
public class Product {
    @ManyToOne(fetch = FetchType.LAZY) // Lazy loading
    @JoinColumn(name = "category_id")
    private Category category;
    
    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY)
    private List<ProductAttributeValue> attributeValues;
}
```

### Query Optimization
```java
@Query("SELECT p FROM Product p " +
       "LEFT JOIN FETCH p.category " +
       "LEFT JOIN FETCH p.attributeValues " +
       "WHERE p.id = :id")
Optional<Product> findByIdWithDetails(@Param("id") Long id);
```

### Caching
```java
@Cacheable("products")
@Override
public ProductDTO getById(Long id) {
    // Implementacja z cache
}

@CacheEvict(value = "products", key = "#id")
@Override
public ProductDTO update(Long id, ProductUpdateDTO dto) {
    // Implementacja z evict cache
}
```

##  Troubleshooting

### Częste problemy

#### 1. LazyInitializationException
```java
// Problem
@Transactional(readOnly = true)
public ProductDTO getById(Long id) {
    Product product = productRepository.findById(id).orElseThrow();
    return productMapper.toProductDTO(product); // Błąd przy dostępie do lazy fields
}

// Rozwiązanie
@Query("SELECT p FROM Product p LEFT JOIN FETCH p.category WHERE p.id = :id")
Optional<Product> findByIdWithCategory(@Param("id") Long id);
```

#### 2. Duplicate Key Exception
```java
// Problem - duplikat SKU
@UniqueConstraint(columnNames = "sku")

// Rozwiązanie - sprawdzenie przed zapisem
if (productRepository.existsBySku(sku)) {
    throw new DuplicateKeyException("SKU already exists: " + sku);
}
```

#### 3. Transaction Rollback
```java
@Transactional(rollbackFor = Exception.class)
public ProductDTO create(ProductCreateDTO dto) {
    // Wszystkie operacje w jednej transakcji
    // Rollback przy każdym wyjątku
}
```

##  Monitoring i metryki

### Health Checks
```java
@Component
public class DatabaseHealthIndicator implements HealthIndicator {
    
    @Autowired
    private ProductRepository productRepository;
    
    @Override
    public Health health() {
        try {
            long count = productRepository.count();
            return Health.up()
                .withDetail("products", count)
                .build();
        } catch (Exception e) {
            return Health.down()
                .withDetail("error", e.getMessage())
                .build();
        }
    }
}
```

### Custom Metrics
```java
@Component
public class ProductMetrics {
    
    private final MeterRegistry meterRegistry;
    private final Counter productCreatedCounter;
    private final Timer productCreationTimer;
    
    public ProductMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.productCreatedCounter = Counter.builder("products.created")
            .description("Number of products created")
            .register(meterRegistry);
        this.productCreationTimer = Timer.builder("products.creation.time")
            .description("Time taken to create products")
            .register(meterRegistry);
    }
}
```

## AI Chat Integration

### Konfiguracja
System wykorzystuje Spring AI z integracją Google Gemini do asystenta sprzedażowego.

### Konfiguracja właściwości
```properties
# Google Vertex AI Gemini
spring.ai.vertex.ai.gemini.chat.options.model=gemini-pro
spring.ai.vertex.ai.gemini.chat.options.temperature=0.7
spring.ai.vertex.ai.project-id=your-project-id
spring.ai.vertex.ai.location=us-central1
spring.ai.vertex.ai.credentials.location=classpath:credentials.json
```

### Implementacja ChatController
```java
@RestController
@RequestMapping("/api/ai")
public class ChatController {
    
    private final ChatClient.Builder builder;
    private final AttributeService attributeService;
    private final CategoryService categoryService;
    private final ChatMemory chatMemory;
    
    @EventListener(ApplicationReadyEvent.class)
    public void initializeChatClient() {
        // Pobranie struktury kategorii i atrybutów
        String categoriesTree = categoryService.getCategoryTreeStructure();
        Map<String, List<String>> attributesMap = attributeService.getAllAttributesWithValues();
        
        // Budowa system prompt z kontekstem
        String systemPrompt = buildSystemPrompt(categoriesTree, attributesMap);
        
        // Inicjalizacja ChatClient z funkcjami
        this.chatClient = builder
            .defaultFunctions("searchProductsTool", "productDetailsTool")
            .defaultOptions(VertexAiGeminiChatOptions.builder()
                .withModel(model)
                .withTemperature(temperature)
                .build())
            .defaultSystem(systemPrompt)
            .defaultAdvisors(new MessageChatMemoryAdvisor(chatMemory))
            .build();
    }
}
```

### Funkcje AI (Tools)
System definiuje dwie funkcje dla AI:

1. **searchProductsTool** - Wyszukiwanie produktów
   - Parametry: `query`, `categoryId`, `minPrice`, `maxPrice`, `attributes`
   - Zwraca listę produktów z sugestiami alternatyw

2. **productDetailsTool** - Szczegóły produktu
   - Parametry: `productSlug`
   - Zwraca pełne informacje o produkcie

### Pamięć konwersacji
System wykorzystuje `ChatMemory` do utrzymania kontekstu rozmowy:
- Każda konwersacja ma unikalny `conversationId`
- Pamięć przechowuje ostatnie 10 wiadomości
- Kontekst jest automatycznie przekazywany do AI

### System Prompt
Asystent jest skonfigurowany jako polski asystent sprzedażowy z:
- Dostępem do struktury kategorii
- Listą dostępnych atrybutów produktów
- Instrukcjami dotyczącymi formatowania odpowiedzi
- Protokołem decyzyjnym dla wyszukiwania produktów

## Contact Form Integration

### Implementacja
```java
@RestController
@RequestMapping("/api/contact")
public class ContactController {
    
    private final EmailService emailService;
    
    @Value("${app.contact.admin.email}")
    private String adminEmail;
    
    @PostMapping
    public ResponseEntity<String> sendContactMessage(
            @Valid @RequestBody ContactRequestDTO request) {
        String subject = "Nowa wiadomość od: " + request.name();
        String content = buildEmailContent(request);
        
        emailService.sendSimpleMail(adminEmail, subject, content);
        return ResponseEntity.ok(content);
    }
}
```

### Konfiguracja
```properties
app.contact.admin.email=admin@ecommerce.com
```

### Walidacja
DTO `ContactRequestDTO` zawiera walidację:
- `name` - wymagane, max 255 znaków
- `email` - wymagane, format email
- `message` - wymagane, min 10 znaków

## Rozszerzone API - Filtrowanie i Statystyki

### Filtrowanie zamówień
```java
@GetMapping("/filter")
@PreAuthorize("hasRole('OWNER')")
public ResponseEntity<Page<OrderDTO>> filterOrders(
        @RequestParam(required = false) Long userId,
        @RequestParam(required = false) OrderStatus status,
        @RequestParam(required = false) Boolean isActive,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startDate,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endDate,
        Pageable pageable) {
    return ResponseEntity.ok(orderService.findByMultipleCriteria(
        userId, status, isActive, startDate, endDate, pageable));
}
```

### Filtrowanie płatności
```java
@GetMapping("/filter")
@PreAuthorize("hasRole('OWNER')")
public ResponseEntity<Page<PaymentDTO>> filterPayments(
        @RequestParam(required = false) Long orderId,
        @RequestParam(required = false) String status,
        @RequestParam(required = false) String method,
        @RequestParam(required = false) Boolean isActive,
        @RequestParam(required = false) Instant startDate,
        @RequestParam(required = false) Instant endDate,
        Pageable pageable) {
    return ResponseEntity.ok(paymentService.findByMultipleCriteria(
        orderId, status, method, isActive, startDate, endDate, pageable));
}
```

### Statystyki
Wszystkie kontrolery z rozszerzonym API mają endpointy statystyk:
- `/stats/count` - liczba rekordów z opcjonalnymi filtrami
- Wsparcie dla filtrowania po userId, status, itp.

## 🔧 Development Tools

### Maven Plugins
```xml
<plugins>
    <plugin>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-maven-plugin</artifactId>
    </plugin>
    <plugin>
        <groupId>org.flywaydb</groupId>
        <artifactId>flyway-maven-plugin</artifactId>
    </plugin>
    <plugin>
        <groupId>org.jacoco</groupId>
        <artifactId>jacoco-maven-plugin</artifactId>
    </plugin>
</plugins>
```

### IDE Configuration
```json
// .vscode/settings.json
{
    "java.configuration.updateBuildConfiguration": "automatic",
    "java.compile.nullAnalysis.mode": "automatic",
    "java.format.settings.url": "https://raw.githubusercontent.com/google/styleguide/gh-pages/eclipse-java-google-style.xml"
}
```

## Aktualizacja profilu użytkownika

### Implementacja
```java
@PutMapping("/update")
@PreAuthorize("hasRole('OWNER') or (hasRole('USER'))")
public ResponseEntity<UserDto> update(
        @Valid @RequestBody UserUpdateDTO request,
        @AuthenticationPrincipal User user) {
    UserDto updatedUser = userService.updateUser(user.getId(), request);
    return ResponseEntity.ok(updatedUser);
}
```

### Walidacja
DTO `UserUpdateDTO` zawiera opcjonalne pola:
- `firstName` - imię
- `lastName` - nazwisko
- `email` - email (walidacja formatu)

### Bezpieczeństwo
- USER może aktualizować tylko swój profil
- OWNER może aktualizować dowolny profil
- Email jest walidowany pod kątem unikalności

## Ponowne wysyłanie linku aktywacyjnego

### Implementacja
```java
@PostMapping("/resend-activation")
public ResponseEntity<String> resendActivation(
        @Valid @RequestBody ResendActivationRequestDTO request) {
    userService.resendActivationLink(request.email());
    return ResponseEntity.ok("Nowy link aktywacyjny został wysłany");
}
```

### Flow
1. Użytkownik wysyła żądanie z emailem
2. System generuje nowy token aktywacyjny (ważny 15 minut)
3. Email z nowym linkiem jest wysyłany
4. Stary token jest unieważniany

### Bezpieczeństwo
- Dla bezpieczeństwa zawsze zwracany jest ten sam komunikat (nawet jeśli email nie istnieje)
- Nowy token unieważnia poprzedni token dla danego użytkownika

## Statistics API Implementation

### Przegląd
Statistics API zapewnia szczegółowe statystyki sprzedaży i produktów dla właścicieli sklepu. Wszystkie endpointy wymagają roli `ROLE_OWNER` i są zabezpieczone za pomocą `@PreAuthorize`.

### Architektura

#### Warstwy
```
StatisticsController (REST API)
    ↓
StatisticsService (Business Logic)
    ↓
OrderRepository / OrderItemRepository (Data Access)
    ↓
Database (PostgreSQL)
```

### Implementacja

#### Controller
```java
@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Validated
public class StatisticsController {
    
    @GetMapping("/products/top-by-quantity")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<List<TopProductDTO>> getTopProductsByQuantity(
            @RequestParam(required = false) @PastOrPresent Instant startDate,
            @RequestParam(required = false) @PastOrPresent Instant endDate,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int limit) {
        // Implementacja
    }
}
```

#### Service
```java
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {
    
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    
    private static final List<OrderStatus> COMPLETED_STATUSES = Arrays.asList(
        OrderStatus.CONFIRMED,
        OrderStatus.PROCESSING,
        OrderStatus.SHIPPED,
        OrderStatus.DELIVERED,
        OrderStatus.COMPLETED
    );
}
```

### Zapytania SQL

#### Top produkty według ilości
```sql
SELECT 
    oi.product_id,
    oi.product_name,
    oi.product_sku,
    SUM(oi.quantity) as total_quantity,
    SUM(oi.price * oi.quantity) as total_revenue,
    COUNT(DISTINCT oi.order_id) as order_count
FROM order_items oi
JOIN orders o ON oi.order_id = o.id
WHERE o.status IN ('CONFIRMED', 'PROCESSING', 'SHIPPED', 'DELIVERED', 'COMPLETED')
  AND o.created_at >= :startDate
  AND o.created_at <= :endDate
  AND o.is_active = true
GROUP BY oi.product_id, oi.product_name, oi.product_sku
ORDER BY total_quantity DESC
LIMIT :limit
```

#### Statystyki sprzedaży
```sql
SELECT 
    SUM(o.total_amount) as total_revenue,
    COUNT(o.id) as total_orders,
    AVG(o.total_amount) as average_order_value
FROM orders o
WHERE o.status IN ('CONFIRMED', 'PROCESSING', 'SHIPPED', 'DELIVERED', 'COMPLETED')
  AND o.created_at >= :startDate
  AND o.created_at <= :endDate
  AND o.is_active = true
```

### DTO

#### TopProductDTO
```java
public record TopProductDTO(
    @NotNull @Positive Long productId,
    @NotBlank String productName,
    @NotBlank String productSku,
    @NotNull @PositiveOrZero Long totalQuantitySold,
    @NotNull @PositiveOrZero BigDecimal totalRevenue,
    @NotNull @PositiveOrZero Long orderCount
) {}
```

#### SalesStatisticsDTO
```java
public record SalesStatisticsDTO(
    @NotNull @PositiveOrZero BigDecimal totalRevenue,
    @NotNull @PositiveOrZero Long totalOrders,
    @NotNull @PositiveOrZero Long totalProductsSold,
    @NotNull @PositiveOrZero BigDecimal averageOrderValue,
    @NotNull Instant periodStart,
    @NotNull Instant periodEnd
) {}
```

### Walidacja

#### Parametry kontrolera
- `startDate` / `endDate`: `@PastOrPresent` - daty nie mogą być w przyszłości
- `limit`: `@Min(1)` `@Max(100)` - limit między 1 a 100
- `year`: `@Min(2000)` `@Max(2100)` - rok między 2000 a 2100
- `month`: `@Min(1)` `@Max(12)` - miesiąc między 1 a 12

#### Walidacja biznesowa
- `startDate` musi być przed `endDate` (sprawdzane w kontrolerze)
- Zwraca `400 Bad Request` jeśli daty są nieprawidłowe

### Bezpieczeństwo

- Wszystkie endpointy wymagają roli `ROLE_OWNER`
- Używa `@PreAuthorize("hasRole('OWNER')")` na poziomie klasy i metod
- Walidacja parametrów za pomocą `@Validated` na kontrolerze
- Logowanie wszystkich operacji (INFO dla sukcesów, WARN dla błędów)

### Wydajność

- Używa `@Transactional(readOnly = true)` dla optymalizacji
- Zapytania SQL z agregacjami są wykonywane bezpośrednio w bazie danych
- Domyślne limity zapobiegają przeciążeniu (max 100 produktów)
- Statystyki są obliczane na podstawie zamówień w statusach "zakończonych"

### Logowanie

```java
logger.info("GET /api/statistics/products/top-by-quantity - startDate={}, endDate={}, limit={}", 
    startDate, endDate, limit);
logger.info("GET /api/statistics/products/top-by-quantity - Successfully retrieved {} top products", 
    topProducts.size());
```

### Statusy zamówień

Statystyki uwzględniają tylko zamówienia w następujących statusach:
- `CONFIRMED` - Potwierdzone
- `PROCESSING` - W trakcie realizacji
- `SHIPPED` - Wysłane
- `DELIVERED` - Dostarczone
- `COMPLETED` - Zakończone

Zamówienia w statusach `PENDING`, `CANCELLED`, `REFUNDED` nie są uwzględniane w statystykach.

---

*Przewodnik dewelopera - ostatnia aktualizacja: 2025-01-15*
