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
- **Wyszukiwanie Elasticsearch** - Zaawansowane wyszukiwanie produktów z obsługą fuzzy matching i filtrowania
- **Bezpieczeństwo** - Role-based access control (RBAC) z walidacją właściciela zasobów
- **Testy** - 130+ testów jednostkowych i integracyjnych
- **Obrazy produktów** - upload, lista, usuwanie, miniatura, serwowanie z `/uploads/**`
- **Migracje bazy danych** - Flyway migrations
- **MapStruct** - Automatyczne mapowanie DTO ↔ Entity
- **System zamówień** - Pełny system zamówień z pozycjami (Order, OrderItem)
- **System płatności** - Obsługa płatności z różnymi metodami i statusami
- **System adresów** - Zarządzanie adresami użytkowników z zabezpieczeniami
- **System magazynu** - Zarządzanie stanem magazynowym z pesymistyczną blokadą
- **Domain-Driven Design** - Logika biznesowa enkapsulowana w encjach

### W trakcie rozwoju
- Newsletter

##  Architektura

### Technologie
- **Backend:** Spring Boot 3.x, Spring Security, Spring Data JPA
- **Baza danych:** PostgreSQL (produkcja), H2 (testy)
- **Wyszukiwanie:** Elasticsearch (Hibernate Search)
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
- **Order** - Zamówienia z statusami (NEW, CONFIRMED, PROCESSING, SHIPPED, DELIVERED, CANCELLED, REFUNDED)
- **OrderItem** - Pozycje zamówień
- **Payment** - Płatności z metodami (CREDIT_CARD, DEBIT_CARD, PAYPAL, BANK_TRANSFER, CASH_ON_DELIVERY, BLIK, APPLE_PAY, GOOGLE_PAY)
- **PaymentStatus** - Statusy płatności (PENDING, PROCESSING, COMPLETED, FAILED, CANCELLED, REFUNDED)
- **Address** - Adresy użytkowników
- **Inventory** - Stan magazynowy produktów z rezerwacjami

### Relacje
- User ↔ Role (Many-to-Many)
- User ↔ Address (One-to-Many)
- User ↔ Order (One-to-Many)
- Category ↔ CategoryAttribute (One-to-Many)
- Category ↔ Product (One-to-Many)
- Product ↔ ProductAttributeValue (One-to-Many)
- Product ↔ Inventory (One-to-One)
- CategoryAttribute ↔ ProductAttributeValue (One-to-Many)
- Order ↔ OrderItem (One-to-Many)
- Order ↔ Payment (One-to-Many)
- Order ↔ Address (Many-to-One)

##  Bezpieczeństwo

### Role i uprawnienia
- **USER** - Podstawowe operacje:
  - Tworzenie i zarządzanie własnymi zamówieniami
  - Tworzenie płatności dla własnych zamówień
  - Zarządzanie własnymi adresami
  - Anulowanie własnych zamówień (tylko status NEW/CONFIRMED)
  - Przeglądanie własnych zamówień i płatności
- **ADMIN** - Zarządzanie użytkownikami
- **OWNER** - Pełne uprawnienia (CRUD wszystkich zasobów, zmiana statusów zamówień i płatności)

### Zabezpieczone endpointy
- **Operacje administracyjne** - Wymagają roli `ROLE_OWNER` (zmiana statusów, zarządzanie produktami, kategoriami)
- **Operacje użytkownika** - USER może operować tylko na swoich zasobach (zamówienia, płatności, adresy)
- **Walidacja właściciela** - Automatyczna walidacja czy USER jest właścicielem zasobu
- **JWT token** - Wymagany dla wszystkich operacji wymagających autoryzacji

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
- **Search API:** `/api/search` - Wyszukiwanie produktów (Elasticsearch)
- **Product Attribute Values API:** `/api/product-attribute-values` - Wartości atrybutów
- **Product Images API:** `/api/products/{productId}/images` - Zarządzanie obrazami produktów
- **Orders API:** `/api/orders` - Zarządzanie zamówieniami
- **Payments API:** `/api/payments` - Zarządzanie płatnościami
- **Addresses API:** `/api/addresses` - Zarządzanie adresami użytkowników
- **Inventory API:** `/api/inventory` - Zarządzanie stanem magazynowym

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

##  Obrazy produktów

### Endpoints
- `GET /api/products/{productId}/images` – lista obrazów
- `POST /api/products/{productId}/images` – upload (multipart form: `file`, opcjonalnie `altText`, `isThumbnail`)
- `POST /api/products/{productId}/images/{imageId}/thumbnail` – ustaw miniaturę
- `DELETE /api/products/{productId}/images/{imageId}` – usuń obraz

Wymagania uprawnień: operacje modyfikujące wymagają roli OWNER.

### Walidacja uploadu
- Dozwolone typy: `image/jpeg,image/png,image/webp`
- Maks. rozmiar: `5 MB`
- Limit na produkt: `10` obrazów
- Jedna miniatura na produkt – ustawienie nowej zdejmuje flagę z poprzedniej

### Konfiguracja
```properties
app.upload-dir=uploads
app.upload-max-bytes=5242880
app.upload-allowed-types=image/jpeg,image/png,image/webp
app.max-images-per-product=10
```

Pliki są zapisywane w `${app.upload-dir}/products/{productId}/...` i serwowane pod `/uploads/**`.

### Testy
- Jednostkowe serwisu: `ProductImageServiceImplTest`
- Web MVC kontrolera: `ProductImageControllerTest`
- Integracyjne (H2): `ProductImageIntegrationTest`

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

### Wyszukiwanie produktów z Elasticsearch
```bash
curl -X POST "http://localhost:8080/api/search?query=laptop&minPrice=1000&maxPrice=5000&page=0&size=20" \
  -H "Content-Type: application/json" \
  -d '{
    "Color": "Black",
    "Screen Size": "15.6 inch"
  }'
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

### v1.1.0 (2024-01-XX)
- System zamówień (Order, OrderItem) z pełnym flow
- System płatności z różnymi metodami i statusami
- System adresów użytkowników z zabezpieczeniami
- System magazynu z pesymistyczną blokadą (zapobieganie race conditions)
- Anulowanie zamówień przez użytkowników (tylko status NEW/CONFIRMED)
- Domain-Driven Design - logika biznesowa w encjach
- Automatyczne zarządzanie stanem magazynowym przy zamówieniach
- Poprawki bezpieczeństwa - walidacja właściciela zasobów
- Enum dla statusów zamówień i płatności (zamiast magic strings)

### v1.2.0 (2024-01-XX)
- Integracja Elasticsearch (Hibernate Search) dla wyszukiwania produktów
- Usunięcie wyszukiwania z bazy danych na rzecz Elasticsearch
- Endpoint `/api/search` z obsługą fuzzy matching i filtrowania
- Automatyczne indeksowanie produktów przy starcie aplikacji
- Walidacja XSS w DTOs

### v1.0.0 (2024-01-01)
-  Implementacja systemu autoryzacji JWT
-  Zarządzanie kategoriami i atrybutami kategorii
-  Kompleksowy system produktów z dynamicznymi atrybutami
-  Automatyczne generowanie SKU
-  130+ testów jednostkowych i integracyjnych
-  Kompletna dokumentacja API



## Autorzy

- **Adrian Szponar** - *Główny deweloper* - [AdrianSzponarOnline](https://github.com/AdrianSzponarOnline)



## Flow zamówienia

### 1. Tworzenie zamówienia
- USER tworzy zamówienie z pozycjami
- Automatyczna rezerwacja magazynu (pesymistyczna blokada)
- Status: `NEW`

### 2. Płatność
- USER tworzy płatność dla zamówienia
- Walidacja: kwota musi odpowiadać `totalAmount` zamówienia
- Status płatności: `PENDING`

### 2.1 Symulacja płatności (Mock Payment Gateway)
- Endpoint: `POST /api/payments/{paymentId}/simulate?scenario=SUCCESS`
- Dostępne scenariusze:
  - `SUCCESS` - Płatność udana (status → `COMPLETED`, zamówienie → `CONFIRMED`)
  - `FAIL` - Odmowa banku (status → `FAILED`, zamówienie → `CANCELLED`)
  - `ERROR` - Błąd połączenia (status → `FAILED`, zamówienie → `CANCELLED`)
- Automatycznie generuje mock `transactionId` i aktualizuje status zamówienia
- Opóźnienie 1 sekundy symuluje czas oczekiwania na odpowiedź bramki

### 3. Finalizacja płatności
- Gdy płatność zmienia status na `COMPLETED`:
  - Automatyczna zmiana statusu zamówienia na `CONFIRMED`
  - Finalizacja rezerwacji magazynu (stock sprzedany)
- Gdy płatność zmienia status na `FAILED`/`CANCELLED`:
  - Automatyczna zmiana statusu zamówienia na `CANCELLED` (jeśli było `NEW`)
  - Zwolnienie rezerwacji magazynu

### 4. Anulowanie
- USER może anulować zamówienie (tylko status NEW/CONFIRMED)
- OWNER może anulować w dowolnym momencie
- Automatyczne zwolnienie rezerwacji magazynu

### 5. Statusy zamówienia
- `NEW` → `CONFIRMED` → `PROCESSING` → `SHIPPED` → `DELIVERED`
- `CANCELLED` - anulowane (zwolnienie magazynu)
- `REFUNDED` - zwrócone

*Dokumentacja wygenerowana automatycznie - ostatnia aktualizacja: 2024-01-XX*