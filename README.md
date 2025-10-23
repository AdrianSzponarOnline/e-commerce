# E-commerce System

## Przegląd
Kompleksowy system e-commerce zbudowany w Spring Boot z obsługą produktów, kategorii, atrybutów, użytkowników i autoryzacji JWT.

## Funkcjonalności

### Zaimplementowane
- **Autoryzacja i uwierzytelnianie** - JWT tokeny, role użytkowników
- **Zarządzanie kategoriami** - Hierarchiczne kategorie z atrybutami
- **Zarządzanie produktami** - Pełne CRUD z dynamicznymi atrybutami
- **System atrybutów** - Dynamiczne atrybuty produktów (rozmiar, kolor, etc.)
- **Generowanie SKU** - Automatyczne SKU na podstawie atrybutów
- **Wyszukiwanie i filtrowanie** - Zaawansowane zapytania z paginacją
- **Bezpieczeństwo** - Role-based access control (RBAC)
- **Testy** - 130+ testów jednostkowych i integracyjnych
- **Migracje bazy danych** - Flyway migrations
- **MapStruct** - Automatyczne mapowanie DTO ↔ Entity

### W trakcie rozwoju
- System zamówień
- System płatności
- Newsletter
- Zarządzanie obrazami produktów

##  Architektura

### Technologie
- **Backend:** Spring Boot 3.x, Spring Security, Spring Data JPA
- **Baza danych:** PostgreSQL (produkcja), H2 (testy)
- **Mapowanie:** MapStruct
- **Migracje:** Flyway
- **Testy:** JUnit 5, Mockito
- **Build:** Maven

### Struktura pakietów
```
com.ecommerce.E_commerce/
├── config/          # Konfiguracja (Security, JWT)
├── controller/      # REST Controllers
├── dto/            # Data Transfer Objects
├── exception/      # Custom exceptions
├── mapper/         # MapStruct mappers
├── model/          # JPA Entities
├── repository/     # JPA Repositories
└── service/        # Business Logic
```

##  Modele danych

### Główne encje
- **User** - Użytkownicy systemu
- **Role** - Role użytkowników (USER, ADMIN, OWNER)
- **Category** - Kategorie produktów (hierarchiczne)
- **CategoryAttribute** - Atrybuty kategorii
- **Product** - Produkty
- **ProductAttributeValue** - Wartości atrybutów produktów
- **Order** - Zamówienia (w przygotowaniu)
- **OrderItem** - Pozycje zamówień (w przygotowaniu)

### Relacje
- User ↔ Role (Many-to-Many)
- Category ↔ CategoryAttribute (One-to-Many)
- Category ↔ Product (One-to-Many)
- Product ↔ ProductAttributeValue (One-to-Many)
- CategoryAttribute ↔ ProductAttributeValue (One-to-Many)

##  Bezpieczeństwo

### Role i uprawnienia
- **USER** - Podstawowe operacje (odczyt)
- **ADMIN** - Zarządzanie użytkownikami
- **OWNER** - Pełne uprawnienia (CRUD wszystkich zasobów)

### Zabezpieczone endpointy
- Wszystkie operacje CUD wymagają roli `ROLE_OWNER`
- Endpointy odczytu są publiczne
- JWT token wymagany dla operacji wymagających autoryzacji

##  Uruchamianie

### Wymagania
- Java 17+
- Maven 3.6+
- PostgreSQL 12+ (produkcja)

### Konfiguracja
1. Skopiuj `application-example.properties` do `application.properties`
2. Skonfiguruj połączenie z bazą danych
3. Ustaw klucz JWT

```properties
# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/ecommerce
spring.datasource.username=your_username
spring.datasource.password=your_password

# JWT
jwt.secret=your-secret-key
jwt.expiration=86400000
```

### Uruchamianie
```bash
# Kompilacja i testy
mvn clean test

# Uruchomienie aplikacji
mvn spring-boot:run

# Uruchomienie z profilem testowym
mvn spring-boot:run -Dspring.profiles.active=test
```

##  API Documentation

### Główne API
- **Auth API:** `/api/auth` - Logowanie, rejestracja
- **Categories API:** `/api/categories` - Zarządzanie kategoriami
- **Category Attributes API:** `/api/categories/{categoryId}/attributes` - Atrybuty kategorii
- **Products API:** `/api/products` - Zarządzanie produktami
- **Product Attribute Values API:** `/api/product-attribute-values` - Wartości atrybutów

### Dokumentacja
- [Kompletna dokumentacja API](API_DOCUMENTATION.md)
- [Dokumentacja produktów](PRODUCT_API_DOCUMENTATION.md)

##  Testy

### Pokrycie testami
- **SkuGeneratorTest:** 8 testów
- **ProductServiceImplTest:** 72 testy  
- **ProductAttributeValueServiceImplTest:** 33 testy
- **CategoryMapperTest:** 4 testy
- **CategoryAttributeMapperTest:** 2 testy
- **CategoryServiceImplTest:** 4 testy
- **CategoryAttributeServiceImplTest:** 4 testy
- **CategoryAttributeControllerTest:** 1 test
- **CategoryDtoValidationTest:** 1 test
- **ECommerceApplicationTests:** 1 test

**Łącznie:** 130 testów

### Uruchamianie testów
```bash
# Wszystkie testy
mvn test

# Konkretna klasa testowa
mvn test -Dtest=ProductServiceImplTest

# Testy z raportem pokrycia
mvn test jacoco:report
```

## Baza danych

### Migracje
- `V1__baseline.sql` - Podstawowa struktura bazy danych
- `V2__category_attribute_updates.sql` - Aktualizacje atrybutów kategorii
- `V3__insert_craft_categories.sql` - Wstawienie kategorii rzemieślniczych
- `V4__add_sku_unique_constraint.sql` - Unikalne ograniczenie dla SKU

### Uruchamianie migracji
```bash
# Automatycznie przy starcie aplikacji
mvn spring-boot:run

# Ręcznie (jeśli potrzebne)
mvn flyway:migrate
```

##  Przykłady użycia

### Tworzenie produktu z atrybutami
```bash
curl -X POST "http://localhost:8080/api/products" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "iPhone 15",
    "description": "Najnowszy iPhone",
    "price": 3999.99,
    "vatRate": 23.0,
    "seoSlug": "iphone-15",
    "categoryId": 1,
    "attributeValues": [
      {
        "categoryAttributeId": 1,
        "value": "6.1 inch"
      },
      {
        "categoryAttributeId": 2,
        "value": "Space Black"
      }
    ]
  }'
```

### Wyszukiwanie produktów z filtrami
```bash
curl -X GET "http://localhost:8080/api/products/filter?categoryId=1&minPrice=1000&maxPrice=5000&isFeatured=true&page=0&size=10&sortBy=price&sortDir=asc"
```

### Pobieranie atrybutów produktu
```bash
curl -X GET "http://localhost:8080/api/product-attribute-values/product/1"
```

##  Konfiguracja

### Profile aplikacji
- `default` - Produkcja (PostgreSQL)
- `test` - Testy (H2 in-memory)
- `example` - Przykładowa konfiguracja

### Zmienne środowiskowe
```bash
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/ecommerce
export SPRING_DATASOURCE_USERNAME=your_username
export SPRING_DATASOURCE_PASSWORD=your_password
export JWT_SECRET=your-secret-key
export JWT_EXPIRATION=86400000
```

##  Changelog

### v1.0.0 (2024-01-01)
- ✅ Implementacja systemu autoryzacji JWT
- ✅ Zarządzanie kategoriami i atrybutami kategorii
- ✅ Kompleksowy system produktów z dynamicznymi atrybutami
- ✅ Automatyczne generowanie SKU
- ✅ Zaawansowane wyszukiwanie i filtrowanie
- ✅ 130+ testów jednostkowych i integracyjnych
- ✅ Kompletna dokumentacja API



## Autorzy

- **Adrian Szponar** - *Główny deweloper* - [AdrianSzponarOnline](https://github.com/AdrianSzponarOnline)



*Dokumentacja wygenerowana automatycznie - ostatnia aktualizacja: 2024-01-01*