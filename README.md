# E-commerce System

## Przegląd
Kompleksowy system e-commerce zbudowany w Spring Boot z obsługą produktów, kategorii, atrybutów, użytkowników i autoryzacji JWT.

## Funkcjonalności

### Zaimplementowane
- **Autoryzacja i uwierzytelnianie** - JWT tokeny, role użytkowników
- **Aktywacja konta przez email** - Automatyczna wysyłka maila z linkiem aktywacyjnym przy rejestracji
- **Reset hasła przez email** - Wysyłka maila z linkiem resetującym hasło
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
- **Zamówienia gości** - Możliwość składania zamówień przez niezalogowanych użytkowników
- **Płatności gości** - Możliwość opłacania zamówień przez gości z weryfikacją email
- **System płatności** - Obsługa płatności z różnymi metodami i statusami
- **System adresów** - Zarządzanie adresami użytkowników z zabezpieczeniami
- **System magazynu** - Zarządzanie stanem magazynowym z pesymistyczną blokadą
- **Domain-Driven Design** - Logika biznesowa enkapsulowana w encjach
- **Logowanie SLF4J** - Kompleksowe logowanie operacji biznesowych i błędów

### W trakcie rozwoju
- Newsletter
- Rozszerzona integracja AI dla rekomendacji produktów

##  Architektura

### Technologie
- **Backend:** Spring Boot 3.x, Spring Security, Spring Data JPA
- **Baza danych:** PostgreSQL (produkcja), H2 (testy)
- **Wyszukiwanie:** Elasticsearch (Hibernate Search)
- **Mapowanie:** MapStruct
- **Migracje:** Flyway
- **Logowanie:** SLF4J z logback
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
- **Publiczne endpointy** - Dostępne bez autoryzacji:
  - `POST /api/orders/guest` - Tworzenie zamówień przez gości
  - `POST /api/payments/guest` - Tworzenie płatności przez gości
  - `POST /api/payments/guest/{paymentId}/simulate` - Symulacja płatności gości
  - `POST /api/auth/register`, `POST /api/auth/login` - Rejestracja i logowanie
  - `GET /api/products/**`, `GET /api/categories/**` - Przeglądanie produktów i kategorii
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
4. Skonfiguruj serwer SMTP do wysyłki emaili (aktywacja konta, reset hasła)

```properties
# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/ecommerce
spring.datasource.username=your_username
spring.datasource.password=your_password

# JWT
security.jwt.secret-key=your-secret-key
security.jwt.expiration-time=3600000

# Email (Mailtrap lub inny SMTP)
spring.mail.host=sandbox.smtp.mailtrap.io
spring.mail.port=587
spring.mail.username=your_mailtrap_username
spring.mail.password=your_mailtrap_password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
app.mail.from=sklep@ecommerce.com
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
- **Auth API:** `/api/auth` - Logowanie, rejestracja, aktywacja konta, reset hasła, aktualizacja profilu
- **Categories API:** `/api/categories` - Zarządzanie kategoriami
- **Category Attributes API:** `/api/categories/{categoryId}/attributes` - Atrybuty kategorii
- **Products API:** `/api/products` - Zarządzanie produktami
- **Search API:** `/api/search` - Wyszukiwanie produktów (Elasticsearch)
- **Product Attribute Values API:** `/api/product-attribute-values` - Wartości atrybutów
- **Product Images API:** `/api/products/{productId}/images` - Zarządzanie obrazami produktów
- **Orders API:** `/api/orders` - Zarządzanie zamówieniami z filtrowaniem i statystykami
  - `/api/orders/guest` - Tworzenie zamówień przez gości (bez autoryzacji)
- **Payments API:** `/api/payments` - Zarządzanie płatnościami z symulacją bramki płatniczej
  - `/api/payments/guest` - Tworzenie płatności przez gości (bez autoryzacji)
  - `/api/payments/guest/{paymentId}/simulate` - Symulacja płatności gości (bez autoryzacji)
- **Addresses API:** `/api/addresses` - Zarządzanie adresami użytkowników
- **Inventory API:** `/api/inventory` - Zarządzanie stanem magazynowym
- **AI Chat API:** `/api/ai/chat` - Asystent sprzedażowy z AI (Gemini)
- **Contact API:** `/api/contact` - Formularz kontaktowy
- **Statistics API:** `/api/statistics` - Statystyki sprzedaży i produktów (tylko dla OWNER)

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
- **AddressDtoValidationTest:** 10 testów
- **UserServiceTest:** 6 testów
- **ECommerceApplicationTests:** 1 test

**Łącznie:** 146 testów

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

### Konfiguracja email
System wymaga skonfigurowanego serwera SMTP do wysyłki emaili z aktywacją konta i resetowaniem hasła. W przykładzie użyto Mailtrap (sandbox do testów).

**Wymagane właściwości:**
- `spring.mail.host` - Host serwera SMTP
- `spring.mail.port` - Port serwera SMTP (zwykle 587 dla TLS)
- `spring.mail.username` - Nazwa użytkownika SMTP
- `spring.mail.password` - Hasło SMTP
- `app.mail.from` - Adres email nadawcy

### Konfiguracja logowania
System używa SLF4J z implementacją Logback. Logowanie jest skonfigurowane automatycznie, ale można dostosować poziomy logowania w `application.properties`:

```properties
# Poziom logowania dla całej aplikacji
logging.level.root=INFO
logging.level.com.ecommerce.E_commerce=INFO

# Szczegółowe logowanie (tylko w środowisku deweloperskim)
logging.level.com.ecommerce.E_commerce.service=DEBUG
logging.level.org.springframework.security=DEBUG
logging.level.org.hibernate.SQL=DEBUG
```

Więcej informacji o strategii logowania znajdziesz w `DEVELOPER_GUIDE.md`.

### Zmienne środowiskowe
```bash
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/ecommerce
export SPRING_DATASOURCE_USERNAME=your_username
export SPRING_DATASOURCE_PASSWORD=your_password
export JWT_SECRET_KEY=your-secret-key
export MAIL_HOST=sandbox.smtp.mailtrap.io
export MAIL_PORT=587
export MAIL_USERNAME=your_mailtrap_username
export MAIL_PASSWORD=your_mailtrap_password
export MAIL_FROM=sklep@ecommerce.com
```

##  Changelog

### v1.5.0
- **Rozszerzone API płatności**
  - Endpoint `/api/payments/me` - pobieranie własnych płatności
  - Endpoint `/api/payments/user/{userId}` - płatności użytkownika
  - Endpoint `/api/payments/order/{orderId}` - płatności zamówienia
  - Endpoint `/api/payments/status/{status}` - filtrowanie po statusie
  - Endpoint `/api/payments/filter` - zaawansowane filtrowanie
  - Endpoint `/api/payments/stats/count` - statystyki płatności
  - Endpoint `/api/payments/{paymentId}/simulate` - symulacja bramki płatniczej
  - Endpoint `/api/payments/guest` - tworzenie płatności przez gości (bez autoryzacji)
  - Endpoint `/api/payments/guest/{paymentId}/simulate` - symulacja płatności gości (bez autoryzacji)
- **Rozszerzone API zamówień**
  - Endpoint `/api/orders/me` - pobieranie własnych zamówień
  - Endpoint `/api/orders/user/{userId}` - zamówienia użytkownika
  - Endpoint `/api/orders/status/{status}` - filtrowanie po statusie
  - Endpoint `/api/orders/filter` - zaawansowane filtrowanie z datami
  - Endpoint `/api/orders/stats/count` - statystyki zamówień
  - Endpoint `/api/orders/guest` - tworzenie zamówień przez gości (bez autoryzacji)
- **Zamówienia gości (Guest Orders)**
  - Obsługa zamówień przez niezalogowanych użytkowników
  - Automatyczne tworzenie adresów dostawy dla gości
  - Przechowywanie danych kontaktowych gości (email, imię, nazwisko, telefon) w zamówieniu
  - Notyfikacje email dla zamówień gości
  - Migracja bazy danych: `V16__make_address_user_id_nullable.sql` - adresy mogą być bez użytkownika
  - Rozszerzenie modelu Order o pola: `guestEmail`, `guestFirstName`, `guestLastName`, `guestPhone`
  - Aktualizacja serwisów notyfikacji do obsługi zamówień gości
- **Płatności gości (Guest Payments)**
  - Weryfikacja email gościa przy tworzeniu i symulacji płatności
  - Obsługa płatności dla zamówień gości z pełną funkcjonalnością (symulacja, zmiana statusów)
  - Aktualizacja metod `isPaymentOwner` i `isOrderOwner` do obsługi zamówień gości
- **Nowe funkcjonalności**
  - AI Chat API (`/api/ai/chat`) - asystent sprzedażowy z integracją Gemini
  - Contact API (`/api/contact`) - formularz kontaktowy z wysyłką emaili
  - Aktualizacja profilu użytkownika (`PUT /api/auth/update`)
  - Ponowne wysyłanie linku aktywacyjnego (`POST /api/auth/resend-activation`)
  - Rozszerzone API magazynu z podsumowaniami i sprawdzaniem dostępności

### v1.4.0 
- **Dodano kompleksowe logowanie SLF4J**
  - Logowanie operacji biznesowych w kontrolerach i serwisach
  - Centralne logowanie błędów w GlobalExceptionHandler
  - Eliminacja duplikacji logów (błędy logowane tylko raz)
  - Zastąpienie System.err.println logowaniem SLF4J
  - Strategia logowania: kontrolery logują sukcesy, GlobalExceptionHandler loguje błędy

### v1.1.0 
- System zamówień (Order, OrderItem) z pełnym flow
- System płatności z różnymi metodami i statusami
- System adresów użytkowników z zabezpieczeniami
- System magazynu z pesymistyczną blokadą (zapobieganie race conditions)
- Anulowanie zamówień przez użytkowników (tylko status NEW/CONFIRMED)
- Domain-Driven Design - logika biznesowa w encjach
- Automatyczne zarządzanie stanem magazynowym przy zamówieniach
- Poprawki bezpieczeństwa - walidacja właściciela zasobów
- Enum dla statusów zamówień i płatności (zamiast magic strings)

### v1.2.0 
- Integracja Elasticsearch (Hibernate Search) dla wyszukiwania produktów
- Usunięcie wyszukiwania z bazy danych na rzecz Elasticsearch
- Endpoint `/api/search` z obsługą fuzzy matching i filtrowania
- Automatyczne indeksowanie produktów przy starcie aplikacji
- Walidacja XSS w DTOs

### v1.2.1 
- Dodano walidację `@NotNull` dla pola `userId` w `AddressCreateDTO`
- Naprawiono testy jednostkowe - dodano brakujące zależności w `UserServiceTest`
- Rozszerzono testy walidacji DTO - dodano `AddressDtoValidationTest` z 10 testami
- Ujednolicono konfigurację properties

### v1.3.0
- System aktywacji kont użytkowników przez email
- System resetowania hasła przez email
- Automatyczna wysyłka maili z linkami aktywacyjnymi i resetującymi
- Tokeny aktywacyjne i resetujące z określonym czasem ważności (15 min aktywacja, 30 min reset)
- Tabela `confirmation_tokens` do przechowywania tokenów
- Integracja z serwerem SMTP (Mailtrap) do wysyłki emaili

### v1.0.0 
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
- **Zalogowany użytkownik:** USER tworzy zamówienie z pozycjami przez `POST /api/orders`
- **Gość:** Gość tworzy zamówienie przez `POST /api/orders/guest` (bez autoryzacji)
- Automatyczna rezerwacja magazynu (pesymistyczna blokada)
- Status: `NEW`

### 2. Płatność
- **Zalogowany użytkownik:** USER tworzy płatność dla zamówienia przez `POST /api/payments`
- **Gość:** Gość tworzy płatność przez `POST /api/payments/guest` (bez autoryzacji, wymaga weryfikacji email)
- Walidacja: kwota musi odpowiadać `totalAmount` zamówienia
- Status płatności: `PENDING`

### 2.1 Symulacja płatności (Mock Payment Gateway)
- **Zalogowany użytkownik:** `POST /api/payments/{paymentId}/simulate?scenario=SUCCESS`
- **Gość:** `POST /api/payments/guest/{paymentId}/simulate?email=guest@example.com&scenario=SUCCESS` (bez autoryzacji, wymaga weryfikacji email)
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

