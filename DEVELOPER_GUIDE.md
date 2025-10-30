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

##  Paginacja i sortowanie

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

### Konfiguracja logowania
```properties
# application.properties
logging.level.com.ecommerce.E_commerce=DEBUG
logging.level.org.springframework.security=DEBUG
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE
```

### Użycie logów w kodzie
```java
@Slf4j
@Service
public class ProductServiceImpl implements ProductService {
    
    @Override
    public ProductDTO create(ProductCreateDTO dto) {
        log.info("Creating product with name: {}", dto.name());
        
        try {
            Product product = productMapper.toProduct(dto);
            Product savedProduct = productRepository.save(product);
            
            log.info("Product created successfully with ID: {}", savedProduct.getId());
            return productMapper.toProductDTO(savedProduct);
        } catch (Exception e) {
            log.error("Error creating product: {}", e.getMessage(), e);
            throw e;
        }
    }
}
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

---

*Przewodnik dewelopera - ostatnia aktualizacja: 2025-10-30*
