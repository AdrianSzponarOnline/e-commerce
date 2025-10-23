# E-commerce API Documentation

## Przegląd
Kompletna dokumentacja API dla systemu e-commerce z obsługą produktów, kategorii, atrybutów, użytkowników i autoryzacji.

## Base URLs
- **Auth API:** `/api/auth`
- **Categories API:** `/api/categories`
- **Category Attributes API:** `/api/categories/{categoryId}/attributes`
- **Products API:** `/api/products`
- **Product Attribute Values API:** `/api/product-attribute-values`

## Autoryzacja
- **Publiczne endpointy** - dostępne dla wszystkich użytkowników
- **Owner endpointy** - wymagają roli `ROLE_OWNER`
- **Uwierzytelnione endpointy** - wymagają ważnego JWT tokena

---

## 1. Authentication API (`/api/auth`)

### 1.1 Logowanie
**Endpoint:** `POST /api/auth/login`  
**Autoryzacja:** Public

**Request Body:**
```json
{
  "email": "user@example.com",
  "password": "password123"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "email": "user@example.com"
}
```

### 1.2 Rejestracja
**Endpoint:** `POST /api/auth/register`  
**Autoryzacja:** Public

**Request Body:**
```json
{
  "email": "user@example.com",
  "password": "password123",
  "firstName": "Jan",
  "lastName": "Kowalski"
}
```

**Response:**
```json
{
  "email": "user@example.com"
}
```

### 1.3 Pobieranie aktualnego użytkownika
**Endpoint:** `GET /api/auth/me`  
**Autoryzacja:** Authenticated

**Response:**
```json
{
  "email": "user@example.com"
}
```

---

## 2. Categories API (`/api/categories`)

### 2.1 Tworzenie kategorii
**Endpoint:** `POST /api/categories`  
**Autoryzacja:** `ROLE_OWNER`

**Request Body:**
```json
{
  "name": "Electronics",
  "description": "Electronic devices and accessories",
  "seoSlug": "electronics",
  "parentId": 1,
  "isActive": true
}
```

**Response:**
```json
{
  "id": 1,
  "name": "Electronics",
  "description": "Electronic devices and accessories",
  "seoSlug": "electronics",
  "parentId": null,
  "isActive": true,
  "createdAt": "2024-01-01T10:00:00Z",
  "updatedAt": "2024-01-01T10:00:00Z",
  "children": []
}
```

### 2.2 Aktualizacja kategorii
**Endpoint:** `PUT /api/categories/{id}`  
**Autoryzacja:** `ROLE_OWNER`

**Request Body:**
```json
{
  "name": "Updated Electronics",
  "description": "Updated description",
  "seoSlug": "updated-electronics",
  "isActive": true
}
```

### 2.3 Pobieranie kategorii po ID
**Endpoint:** `GET /api/categories/{id}`  
**Autoryzacja:** Public

### 2.4 Lista wszystkich kategorii
**Endpoint:** `GET /api/categories`  
**Autoryzacja:** Public

### 2.5 Lista publicznych kategorii
**Endpoint:** `GET /api/categories/public`  
**Autoryzacja:** Public

### 2.6 Pobieranie kategorii po slug
**Endpoint:** `GET /api/categories/slug/{slug}`  
**Autoryzacja:** Public

### 2.7 Usuwanie kategorii
**Endpoint:** `DELETE /api/categories/{id}`  
**Autoryzacja:** `ROLE_OWNER`

---

## 3. Category Attributes API (`/api/categories/{categoryId}/attributes`)

### 3.1 Tworzenie atrybutu kategorii
**Endpoint:** `POST /api/categories/{categoryId}/attributes`  
**Autoryzacja:** `ROLE_OWNER`

**Request Body:**
```json
{
  "name": "Screen Size",
  "type": "TEXT",
  "isActive": true
}
```

**Response:**
```json
{
  "id": 1,
  "name": "Screen Size",
  "type": "TEXT",
  "isActive": true,
  "categoryId": 1,
  "createdAt": "2024-01-01T10:00:00Z",
  "updatedAt": "2024-01-01T10:00:00Z"
}
```

### 3.2 Aktualizacja atrybutu kategorii
**Endpoint:** `PUT /api/categories/{categoryId}/attributes/{id}`  
**Autoryzacja:** `ROLE_OWNER`

### 3.3 Pobieranie atrybutu po ID
**Endpoint:** `GET /api/categories/{categoryId}/attributes/{id}`  
**Autoryzacja:** Public

### 3.4 Lista atrybutów kategorii
**Endpoint:** `GET /api/categories/{categoryId}/attributes`  
**Autoryzacja:** Public

### 3.5 Usuwanie atrybutu kategorii
**Endpoint:** `DELETE /api/categories/{categoryId}/attributes/{id}`  
**Autoryzacja:** `ROLE_OWNER`

---

## 4. Products API (`/api/products`)

### 4.1 Tworzenie produktu
**Endpoint:** `POST /api/products`  
**Autoryzacja:** `ROLE_OWNER`

**Request Body:**
```json
{
  "name": "Laptop Gaming",
  "description": "Wysokiej klasy laptop do gier",
  "shortDescription": "Laptop gaming",
  "price": 2999.99,
  "vatRate": 23.0,
  "shippingCost": 29.99,
  "estimatedDeliveryTime": "3-5 dni",
  "thumbnailUrl": "https://example.com/laptop.jpg",
  "seoSlug": "laptop-gaming",
  "categoryId": 1,
  "isFeatured": true,
  "attributeValues": [
    {
      "categoryAttributeId": 1,
      "value": "15.6 inch"
    },
    {
      "categoryAttributeId": 2,
      "value": "Black"
    }
  ]
}
```

**Response:**
```json
{
  "id": 1,
  "name": "Laptop Gaming",
  "description": "Wysokiej klasy laptop do gier",
  "shortDescription": "Laptop gaming",
  "price": 2999.99,
  "sku": "LAP-GAM-001",
  "vatRate": 23.0,
  "isFeatured": true,
  "shippingCost": 29.99,
  "estimatedDeliveryTime": "3-5 dni",
  "thumbnailUrl": "https://example.com/laptop.jpg",
  "seoSlug": "laptop-gaming",
  "category": {
    "id": 1,
    "name": "Electronics"
  },
  "attributeValues": [
    {
      "id": 1,
      "productId": 1,
      "productName": "Laptop Gaming",
      "categoryAttributeId": 1,
      "categoryAttributeName": "Screen Size",
      "categoryAttributeType": "TEXT",
      "isKeyAttribute": true,
      "value": "15.6 inch",
      "isActive": true
    }
  ],
  "createdAt": "2024-01-01T10:00:00Z",
  "updatedAt": "2024-01-01T10:00:00Z",
  "isActive": true
}
```

### 4.2 Aktualizacja produktu
**Endpoint:** `PUT /api/products/{id}`  
**Autoryzacja:** `ROLE_OWNER`

**Request Body:**
```json
{
  "name": "Updated Laptop Gaming",
  "price": 2799.99,
  "isFeatured": false,
  "attributeValues": [
    {
      "id": 1,
      "categoryAttributeId": 1,
      "value": "17 inch"
    }
  ]
}
```

### 4.3 Usuwanie produktu
**Endpoint:** `DELETE /api/products/{id}`  
**Autoryzacja:** `ROLE_OWNER`

### 4.4 Pobieranie produktu po ID
**Endpoint:** `GET /api/products/{id}`  
**Autoryzacja:** Public

### 4.5 Pobieranie produktu po SKU
**Endpoint:** `GET /api/products/sku/{sku}`  
**Autoryzacja:** Public

### 4.6 Pobieranie produktu po SEO slug
**Endpoint:** `GET /api/products/slug/{slug}`  
**Autoryzacja:** Public

### 4.7 Lista wszystkich produktów (z paginacją)
**Endpoint:** `GET /api/products`  
**Autoryzacja:** Public

**Query Parameters:**
- `page` (int, default: 0) - numer strony
- `size` (int, default: 10) - rozmiar strony
- `sortBy` (string, default: "id") - pole sortowania
- `sortDir` (string, default: "asc") - kierunek sortowania (asc/desc)

### 4.8 Produkty według kategorii
**Endpoint:** `GET /api/products/category/{categoryId}`  
**Autoryzacja:** Public

### 4.9 Produkty według kategorii (slug)
**Endpoint:** `GET /api/products/category-slug/{categorySlug}`  
**Autoryzacja:** Public

### 4.10 Produkty w zakresie cenowym
**Endpoint:** `GET /api/products/price-range`  
**Autoryzacja:** Public

**Query Parameters:**
- `minPrice` (BigDecimal) - minimalna cena
- `maxPrice` (BigDecimal) - maksymalna cena

### 4.11 Produkty polecane
**Endpoint:** `GET /api/products/featured`  
**Autoryzacja:** Public

### 4.12 Produkty aktywne/nieaktywne
**Endpoint:** `GET /api/products/active`  
**Autoryzacja:** Public (aktywne), `ROLE_OWNER` (nieaktywne)

**Query Parameters:**
- `isActive` (Boolean) - status aktywności

### 4.13 Wyszukiwanie produktów
**Endpoint:** `GET /api/products/search`  
**Autoryzacja:** Public

**Query Parameters:**
- `name` (string) - wyszukiwanie po nazwie
- `description` (string) - wyszukiwanie po opisie

### 4.14 Zaawansowane filtrowanie
**Endpoint:** `GET /api/products/filter`  
**Autoryzacja:** Public

**Query Parameters:**
- `categoryId` (Long) - ID kategorii
- `isFeatured` (Boolean) - czy polecany
- `minPrice` (BigDecimal) - minimalna cena
- `maxPrice` (BigDecimal) - maksymalna cena

### 4.15 Statystyki produktów
**Endpoint:** `GET /api/products/stats`  
**Autoryzacja:** Public

**Response:**
```json
{
  "totalProducts": 150,
  "featuredProducts": 25,
  "activeProducts": 140,
  "productsByCategory": {
    "1": 50,
    "2": 30,
    "3": 70
  }
}
```

---

## 5. Product Attribute Values API (`/api/product-attribute-values`)

### 5.1 Tworzenie wartości atrybutu produktu
**Endpoint:** `POST /api/product-attribute-values`  
**Autoryzacja:** `ROLE_OWNER`

**Request Body:**
```json
{
  "productId": 1,
  "categoryAttributeId": 1,
  "value": "15.6 inch"
}
```

**Response:**
```json
{
  "id": 1,
  "productId": 1,
  "productName": "Laptop Gaming",
  "categoryAttributeId": 1,
  "categoryAttributeName": "Screen Size",
  "categoryAttributeType": "TEXT",
  "isKeyAttribute": true,
  "value": "15.6 inch",
  "createdAt": "2024-01-01T10:00:00Z",
  "updatedAt": "2024-01-01T10:00:00Z",
  "isActive": true
}
```

### 5.2 Tworzenie wielu wartości atrybutów (bulk)
**Endpoint:** `POST /api/product-attribute-values/bulk`  
**Autoryzacja:** `ROLE_OWNER`

**Request Body:**
```json
[
  {
    "productId": 1,
    "categoryAttributeId": 1,
    "value": "15.6 inch"
  },
  {
    "productId": 1,
    "categoryAttributeId": 2,
    "value": "Black"
  }
]
```

### 5.3 Aktualizacja wartości atrybutu
**Endpoint:** `PUT /api/product-attribute-values/{id}`  
**Autoryzacja:** `ROLE_OWNER`

**Request Body:**
```json
{
  "id": 1,
  "categoryAttributeId": 1,
  "value": "17 inch"
}
```

### 5.4 Aktualizacja wartości atrybutów produktu (bulk)
**Endpoint:** `PUT /api/product-attribute-values/product/{productId}`  
**Autoryzacja:** `ROLE_OWNER`

### 5.5 Usuwanie wartości atrybutu
**Endpoint:** `DELETE /api/product-attribute-values/{id}`  
**Autoryzacja:** `ROLE_OWNER`

### 5.6 Usuwanie wszystkich wartości atrybutów produktu
**Endpoint:** `DELETE /api/product-attribute-values/product/{productId}`  
**Autoryzacja:** `ROLE_OWNER`

### 5.7 Pobieranie wartości atrybutu po ID
**Endpoint:** `GET /api/product-attribute-values/{id}`  
**Autoryzacja:** Public

### 5.8 Pobieranie wartości atrybutu po produkt i atrybut kategorii
**Endpoint:** `GET /api/product-attribute-values/product/{productId}/category-attribute/{categoryAttributeId}`  
**Autoryzacja:** Public

### 5.9 Lista wszystkich wartości atrybutów (z paginacją)
**Endpoint:** `GET /api/product-attribute-values`  
**Autoryzacja:** Public

### 5.10 Wartości atrybutów według produktu
**Endpoint:** `GET /api/product-attribute-values/product/{productId}`  
**Autoryzacja:** Public

### 5.11 Wartości atrybutów według atrybutu kategorii
**Endpoint:** `GET /api/product-attribute-values/category-attribute/{categoryAttributeId}`  
**Autoryzacja:** Public

### 5.12 Wartości atrybutów według kategorii produktu
**Endpoint:** `GET /api/product-attribute-values/category/{categoryId}`  
**Autoryzacja:** Public

### 5.13 Wyszukiwanie wartości atrybutów
**Endpoint:** `GET /api/product-attribute-values/search/value`  
**Autoryzacja:** Public

**Query Parameters:**
- `value` (string) - wyszukiwana wartość

### 5.14 Wartości atrybutów według typu
**Endpoint:** `GET /api/product-attribute-values/attribute-type/{type}`  
**Autoryzacja:** Public

### 5.15 Kluczowe atrybuty produktu
**Endpoint:** `GET /api/product-attribute-values/product/{productId}/key-attributes`  
**Autoryzacja:** Public

### 5.16 Statystyki wartości atrybutów
**Endpoint:** `GET /api/product-attribute-values/count/product/{productId}`  
**Autoryzacja:** Public

**Response:**
```json
5
```

### 5.17 Unikalne wartości atrybutu kategorii
**Endpoint:** `GET /api/product-attribute-values/distinct-values/category-attribute/{categoryAttributeId}`  
**Autoryzacja:** Public

**Response:**
```json
["15.6 inch", "17 inch", "13.3 inch"]
```

---

## 6. Modele danych

### 6.1 CategoryAttributeType
```json
{
  "TEXT": "Tekst",
  "NUMBER": "Liczba",
  "BOOLEAN": "Prawda/Fałsz",
  "DATE": "Data",
  "SELECT": "Lista wyboru"
}
```

### 6.2 ERole
```json
{
  "USER": "Użytkownik",
  "ADMIN": "Administrator", 
  "OWNER": "Właściciel"
}
```

---

## 7. Kody błędów

### 7.1 HTTP Status Codes
- `200 OK` - Sukces
- `201 Created` - Zasób utworzony
- `204 No Content` - Sukces bez zawartości
- `400 Bad Request` - Błędne żądanie
- `401 Unauthorized` - Brak autoryzacji
- `403 Forbidden` - Brak uprawnień
- `404 Not Found` - Zasób nie znaleziony
- `409 Conflict` - Konflikt (np. duplikat)
- `500 Internal Server Error` - Błąd serwera

### 7.2 Przykłady błędów
```json
{
  "timestamp": "2024-01-01T10:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/products",
  "details": [
    {
      "field": "name",
      "message": "Product name cannot be blank"
    }
  ]
}
```

---

## 8. Przykłady użycia

### 8.1 Tworzenie produktu z atrybutami
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

### 8.2 Wyszukiwanie produktów z filtrami
```bash
curl -X GET "http://localhost:8080/api/products/filter?categoryId=1&minPrice=1000&maxPrice=5000&isFeatured=true&page=0&size=10&sortBy=price&sortDir=asc"
```

### 8.3 Pobieranie atrybutów produktu
```bash
curl -X GET "http://localhost:8080/api/product-attribute-values/product/1"
```

---

## 9. Testy

### 9.1 Pokrycie testami
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

### 9.2 Uruchamianie testów
```bash
# Wszystkie testy
mvn test

# Konkretna klasa testowa
mvn test -Dtest=ProductServiceImplTest

# Testy z raportem pokrycia
mvn test jacoco:report
```

---

## 10. Migracje bazy danych

### 10.1 Dostępne migracje
- `V1__baseline.sql` - Podstawowa struktura bazy danych
- `V2__category_attribute_updates.sql` - Aktualizacje atrybutów kategorii
- `V3__insert_craft_categories.sql` - Wstawienie kategorii rzemieślniczych
- `V4__add_sku_unique_constraint.sql` - Unikalne ograniczenie dla SKU

### 10.2 Uruchamianie migracji
```bash
# Automatycznie przy starcie aplikacji
mvn spring-boot:run

# Ręcznie (jeśli potrzebne)
mvn flyway:migrate
```

---

## 11. Konfiguracja

### 11.1 Wymagane zmienne środowiskowe
```properties
# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/ecommerce
spring.datasource.username=your_username
spring.datasource.password=your_password

# JWT
jwt.secret=your-secret-key
jwt.expiration=86400000

# Flyway
spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration
```

### 11.2 Profile aplikacji
- `default` - Produkcja (PostgreSQL)
- `test` - Testy (H2 in-memory)
- `example` - Przykładowa konfiguracja

---

## 12. Bezpieczeństwo

### 12.1 Role i uprawnienia
- **USER** - Podstawowe operacje (odczyt)
- **ADMIN** - Zarządzanie użytkownikami
- **OWNER** - Pełne uprawnienia (CRUD wszystkich zasobów)

### 12.2 Zabezpieczone endpointy
- Wszystkie operacje CUD wymagają roli `ROLE_OWNER`
- Endpointy odczytu są publiczne
- JWT token wymagany dla operacji wymagających autoryzacji

### 12.3 Walidacja danych
- Wszystkie DTOs mają walidację Bean Validation
- Sprawdzanie uprawnień na poziomie metody
- Sanityzacja danych wejściowych

---

*Dokumentacja wygenerowana automatycznie - ostatnia aktualizacja: 2024-01-01*
