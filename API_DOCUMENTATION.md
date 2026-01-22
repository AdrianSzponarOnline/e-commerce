# E-commerce API Documentation

## Przegląd
Kompletna dokumentacja API dla systemu e-commerce z obsługą produktów, kategorii, atrybutów, użytkowników i autoryzacji.

## Base URLs
- **Auth API:** `/api/auth`
- **Categories API:** `/api/categories`
- **Category Attributes API:** `/api/categories/{categoryId}/attributes`
- **Products API:** `/api/products`
- **Search API:** `/api/search` - Wyszukiwanie produktów (Elasticsearch)
- **Product Attribute Values API:** `/api/product-attribute-values`
- **Product Images API:** `/api/products/{productId}/images`
- **Orders API:** `/api/orders`
- **Payments API:** `/api/payments`
- **Addresses API:** `/api/addresses`
- **Inventory API:** `/api/inventory`
- **AI Chat API:** `/api/ai/chat`
- **Contact API:** `/api/contact`
- **Statistics API:** `/api/statistics` - Statystyki sprzedaży i produktów (tylko dla OWNER)

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
  "firstName": "Jan",
  "lastName": "Kowalski",
  "email": "user@example.com",
  "roles": ["ROLE_USER"]
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
  "id": 1,
  "email": "user@example.com",
  "firstName": "Jan",
  "lastName": "Kowalski",
  "roles": ["ROLE_USER"]
}
```

**Uwagi:**
- Konto jest tworzone z statusem `enabled: false` (nieaktywne)
- Automatycznie wysyłany jest email z linkiem aktywacyjnym
- Link aktywacyjny jest ważny przez 15 minut
- Link ma format: `http://localhost:5173/activate?token={token}`
- Użytkownik musi aktywować konto przed zalogowaniem

### 1.3 Aktywacja konta
**Endpoint:** `POST /api/auth/activate`  
**Autoryzacja:** Public

**Query Parameters:**
- `token` (String, required) - Token aktywacyjny otrzymany w emailu

**Response:**
```
Konto aktywowane pomyślnie
```

**Status codes:**
- `200 OK` - Konto zostało aktywowane
- `400 Bad Request` - Token już wykorzystany lub wygasł
- `404 Not Found` - Nieprawidłowy token

**Uwagi:**
- Token może być użyty tylko raz
- Po aktywacji konto jest automatycznie włączone (`enabled: true`)
- Token wygasa po 15 minutach od utworzenia

### 1.4 Zapomniane hasło
**Endpoint:** `POST /api/auth/forgot-password`  
**Autoryzacja:** Public

**Request Body:**
```json
{
  "email": "user@example.com"
}
```

**Response:**
```
Link resetujący wysłany (jeśli email istnieje)
```

**Status codes:**
- `200 OK` - Zawsze zwracany (dla bezpieczeństwa, nawet jeśli email nie istnieje)
- `400 Bad Request` - Błędne dane wejściowe

**Uwagi:**
- Jeśli email istnieje w systemie, wysyłany jest email z linkiem resetującym hasło
- Link resetujący jest ważny przez 30 minut
- Link ma format: `http://localhost:5173/reset-password?token={token}`
- Dla bezpieczeństwa zawsze zwracany jest ten sam komunikat (nawet jeśli email nie istnieje)

### 1.5 Reset hasła
**Endpoint:** `POST /api/auth/reset-password`  
**Autoryzacja:** Public

**Request Body:**
```json
{
  "token": "abc123...",
  "newPassword": "newPassword123"
}
```

**Response:**
```
Hasło zmienione pomyślnie
```

**Status codes:**
- `200 OK` - Hasło zostało zmienione
- `400 Bad Request` - Token już wykorzystany, wygasł lub hasło nie spełnia wymagań
- `404 Not Found` - Nieprawidłowy token

**Uwagi:**
- Token może być użyty tylko raz
- Po zmianie hasła konto jest automatycznie aktywowane (`enabled: true`)
- Token wygasa po 30 minutach od utworzenia
- Hasło jest automatycznie hashowane przed zapisem

### 1.6 Pobieranie aktualnego użytkownika
**Endpoint:** `GET /api/auth/me`  
**Autoryzacja:** Authenticated

**Response:**
```json
{
  "id": 1,
  "email": "user@example.com",
  "firstName": "Jan",
  "lastName": "Kowalski",
  "roles": ["ROLE_USER"]
}
```

### 1.7 Aktualizacja profilu użytkownika
**Endpoint:** `PUT /api/auth/update`  
**Autoryzacja:** Authenticated (USER lub OWNER)

**Request Body:**
```json
{
  "firstName": "Jan",
  "lastName": "Kowalski",
  "email": "newemail@example.com"
}
```

**Response:**
```json
{
  "id": 1,
  "email": "newemail@example.com",
  "firstName": "Jan",
  "lastName": "Kowalski",
  "roles": ["ROLE_USER"]
}
```

**Status codes:**
- `200 OK` - Profil został zaktualizowany
- `400 Bad Request` - Błędne dane wejściowe
- `401 Unauthorized` - Brak autoryzacji

### 1.8 Ponowne wysłanie linku aktywacyjnego
**Endpoint:** `POST /api/auth/resend-activation`  
**Autoryzacja:** Public

**Request Body:**
```json
{
  "email": "user@example.com"
}
```

**Response:**
```
Nowy link aktywacyjny został wysłany
```

**Status codes:**
- `200 OK` - Link został wysłany (jeśli email istnieje i konto nie jest aktywne)
- `400 Bad Request` - Błędne dane wejściowe

**Uwagi:**
- Link jest wysyłany tylko jeśli konto nie jest jeszcze aktywne
- Nowy token aktywacyjny jest generowany i wysyłany na email
- Token jest ważny przez 15 minut

---

## 2. Categories API (`/api/categories`)

API kategorii obsługuje hierarchiczną strukturę kategorii z relacjami rodzic-dziecko. Kategorie są zwracane w formie drzewa, gdzie każda kategoria może mieć podkategorie.

**Uwaga:** Pojedyncze kategorie (GET by ID/Slug) zwracają kategorię bez drzewa dzieci (pusta lista `children`). Listy kategorii (GET all/active/byParent) zwracają pełną strukturę drzewa z zagnieżdżonymi podkategoriami.

### 2.1 Tworzenie kategorii
**Endpoint:** `POST /api/categories`  
**Autoryzacja:** `ROLE_OWNER`

**Request Body:**
```json
{
  "name": "Electronics",
  "description": "Electronic devices and accessories",
  "seoSlug": "electronics",
  "parentId": null,
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

**Status codes:**
- `201 Created` - Kategoria została utworzona
- `400 Bad Request` - Błędne dane wejściowe (np. duplikat seoSlug)
- `401 Unauthorized` - Brak autoryzacji
- `403 Forbidden` - Brak uprawnień (wymagana rola OWNER)
- `404 Not Found` - Kategoria nadrzędna nie istnieje (jeśli podano parentId)

### 2.2 Aktualizacja kategorii
**Endpoint:** `PUT /api/categories/{id}`  
**Autoryzacja:** `ROLE_OWNER`

**Request Body:**
```json
{
  "name": "Updated Electronics",
  "description": "Updated description",
  "seoSlug": "updated-electronics",
  "parentId": null,
  "isActive": true
}
```

**Response:**
```json
{
  "id": 1,
  "name": "Updated Electronics",
  "description": "Updated description",
  "seoSlug": "updated-electronics",
  "parentId": null,
  "isActive": true,
  "createdAt": "2024-01-01T10:00:00Z",
  "updatedAt": "2024-01-01T11:00:00Z",
  "children": []
}
```

**Status codes:**
- `200 OK` - Kategoria została zaktualizowana
- `400 Bad Request` - Błędne dane wejściowe lub próba utworzenia cyklicznej zależności
- `401 Unauthorized` - Brak autoryzacji
- `403 Forbidden` - Brak uprawnień (wymagana rola OWNER)
- `404 Not Found` - Kategoria nie istnieje

**Uwaga:** System zapobiega tworzeniu cyklicznych zależności (kategoria nie może być swoim własnym rodzicem lub przodkiem).

### 2.3 Pobieranie kategorii po ID
**Endpoint:** `GET /api/categories/{id}`  
**Autoryzacja:** Public

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

**Uwaga:** Pojedyncza kategoria zwracana jest bez drzewa dzieci (pusta lista `children`) dla optymalizacji.

**Status codes:**
- `200 OK` - Kategoria znaleziona
- `404 Not Found` - Kategoria nie istnieje

### 2.4 Lista wszystkich kategorii (drzewo)
**Endpoint:** `GET /api/categories`  
**Autoryzacja:** Public

**Response:**
```json
[
  {
    "id": 1,
    "name": "Electronics",
    "description": "Electronic devices",
    "seoSlug": "electronics",
    "parentId": null,
    "isActive": true,
    "createdAt": "2024-01-01T10:00:00Z",
    "updatedAt": "2024-01-01T10:00:00Z",
    "children": [
      {
        "id": 2,
        "name": "Laptops",
        "description": "Laptop computers",
        "seoSlug": "laptops",
        "parentId": 1,
        "isActive": true,
        "createdAt": "2024-01-01T11:00:00Z",
        "updatedAt": "2024-01-01T11:00:00Z",
        "children": []
      }
    ]
  }
]
```

**Uwaga:** Zwraca pełną strukturę drzewa kategorii z zagnieżdżonymi podkategoriami. Tylko kategorie główne (bez parentId) są na najwyższym poziomie.

**Status codes:**
- `200 OK` - Lista kategorii

### 2.5 Lista aktywnych kategorii (drzewo)
**Endpoint:** `GET /api/categories/active`  
**Autoryzacja:** Public

**Response:**
```json
[
  {
    "id": 1,
    "name": "Electronics",
    "description": "Electronic devices",
    "seoSlug": "electronics",
    "parentId": null,
    "isActive": true,
    "createdAt": "2024-01-01T10:00:00Z",
    "updatedAt": "2024-01-01T10:00:00Z",
    "children": [
      {
        "id": 2,
        "name": "Laptops",
        "seoSlug": "laptops",
        "parentId": 1,
        "isActive": true,
        "createdAt": "2024-01-01T11:00:00Z",
        "updatedAt": "2024-01-01T11:00:00Z",
        "children": []
      }
    ]
  }
]
```

**Uwaga:** Zwraca tylko aktywne kategorie (`isActive: true`) w formie drzewa. Nieaktywne kategorie są pomijane.

**Status codes:**
- `200 OK` - Lista aktywnych kategorii

### 2.6 Kategorie według rodzica
**Endpoint:** `GET /api/categories/parent/{parentId}`  
**Autoryzacja:** Public

**Parametry:**
- `parentId` (path parameter) - ID kategorii nadrzędnej

**Response:**
```json
[
  {
    "id": 2,
    "name": "Laptops",
    "description": "Laptop computers",
    "seoSlug": "laptops",
    "parentId": 1,
    "isActive": true,
    "createdAt": "2024-01-01T11:00:00Z",
    "updatedAt": "2024-01-01T11:00:00Z",
    "children": []
  }
]
```

**Uwaga:** Zwraca bezpośrednie podkategorie danej kategorii w formie drzewa (mogą mieć własne podkategorie).

**Status codes:**
- `200 OK` - Lista podkategorii

### 2.7 Pobieranie kategorii po slug
**Endpoint:** `GET /api/categories/slug/{slug}`  
**Autoryzacja:** Public

**Parametry:**
- `slug` (path parameter) - SEO slug kategorii

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

**Uwaga:** Pojedyncza kategoria zwracana jest bez drzewa dzieci (pusta lista `children`) dla optymalizacji.

**Status codes:**
- `200 OK` - Kategoria znaleziona
- `404 Not Found` - Kategoria nie istnieje

### 2.8 Usuwanie kategorii (soft delete)
**Endpoint:** `DELETE /api/categories/{id}`  
**Autoryzacja:** `ROLE_OWNER`

**Parametry:**
- `id` (path parameter) - ID kategorii

**Response:**
- `204 No Content` - Kategoria została usunięta

**Uwaga:** Usuwanie jest typu "soft delete" - kategoria jest oznaczana jako nieaktywna (`isActive: false`) i otrzymuje datę usunięcia (`deletedAt`), ale nie jest fizycznie usuwana z bazy danych.

**Status codes:**
- `204 No Content` - Kategoria została usunięta
- `401 Unauthorized` - Brak autoryzacji
- `403 Forbidden` - Brak uprawnień (wymagana rola OWNER)
- `404 Not Found` - Kategoria nie istnieje

---

## 3. Category Attributes API (`/api/categories/{categoryId}/attributes`)

### 3.1 Tworzenie atrybutu kategorii
**Endpoint:** `POST /api/categories/{categoryId}/attributes`  
**Autoryzacja:** `ROLE_OWNER`

**Request Body:**
```json
{
  "attributeId": 1,
  "isKeyAttribute": true,
  "isActive": true
}
```

**Response:**
```json
{
  "id": 1,
  "categoryId": 1,
  "attributeId": 1,
  "attributeName": "Screen Size",
  "attributeType": "TEXT",
  "isKeyAttribute": true,
  "isActive": true
}
```

**Uwaga:** Atrybut musi istnieć w systemie (tabela `attributes`). `categoryId` jest automatycznie ustawiane z path parameter.

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
      "attributeId": 1,
      "value": "15.6 inch"
    },
    {
      "attributeId": 2,
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
      "attributeId": 1,
      "attributeName": "Screen Size",
      "attributeType": "TEXT",
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
      "attributeId": 1,
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

### 4.10 Produkty polecane
**Endpoint:** `GET /api/products/featured`  
**Autoryzacja:** Public

### 4.11 Produkty aktywne/nieaktywne
**Endpoint:** `GET /api/products/active`  
**Autoryzacja:** Public (aktywne), `ROLE_OWNER` (nieaktywne)

**Query Parameters:**
- `isActive` (Boolean) - status aktywności

### 4.12 Statystyki produktów

#### 4.16.1 Liczba produktów w kategorii
**Endpoint:** `GET /api/products/stats/category/{categoryId}/count`  
**Autoryzacja:** Public

**Response:** `Long` - liczba produktów

#### 4.16.2 Liczba produktów wyróżnionych
**Endpoint:** `GET /api/products/stats/featured/count`  
**Autoryzacja:** Public

**Query Parameters:**
- `isFeatured` (Boolean, default: true)

**Response:** `Long` - liczba produktów

#### 4.16.3 Liczba produktów aktywnych
**Endpoint:** `GET /api/products/stats/active/count`  
**Autoryzacja:** Public

**Query Parameters:**
- `isActive` (Boolean, default: true)

**Response:** `Long` - liczba produktów

---

## 5. Search API (`/api/search`)

### 5.1 Wyszukiwanie produktów
**Endpoint:** `POST /api/search`  
**Autoryzacja:** Public

**Query Parameters:**
- `query` (String, optional) - Tekst do wyszukania w nazwie i opisie produktu
- `minPrice` (BigDecimal, optional) - Minimalna cena
- `maxPrice` (BigDecimal, optional) - Maksymalna cena
- `page` (int, default: 0) - Numer strony
- `size` (int, default: 20) - Rozmiar strony
- `sort` (String, optional) - Sortowanie (np. "name,asc", "price,desc")

**Request Body (optional):**
```json
{
  "Color": "Black",
  "Screen Size": "15.6 inch"
}
```

**Response:**
```json
{
  "content": [
    {
      "id": 1,
      "name": "Laptop Gaming",
      "price": 2999.99,
      "shortDescription": "Wysokiej klasy laptop do gier",
      "thumbnailUrl": "https://example.com/image.jpg",
      "seoSlug": "laptop-gaming",
      "categoryName": "Elektronika"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 20
  },
  "totalElements": 15,
  "totalPages": 1
}
```

**Funkcjonalności:**
- Fuzzy matching dla zapytań tekstowych (tolerancja 2 znaki)
- Wyszukiwanie w polach `name` i `description`
- Filtrowanie po zakresie cen
- Filtrowanie po atrybutach produktów (nazwa atrybutu -> wartość)
- Automatyczne indeksowanie produktów przy starcie aplikacji

**Uwagi:**
- Wyszukiwanie wykorzystuje Elasticsearch (Hibernate Search)
- Wszystkie produkty są automatycznie indeksowane przy starcie aplikacji
- Fuzzy matching pozwala na znalezienie produktów nawet przy drobnych błędach w zapytaniu

---

## 6. Product Attribute Values API (`/api/product-attribute-values`)

### 5.1 Tworzenie wartości atrybutu produktu
**Endpoint:** `POST /api/product-attribute-values`  
**Autoryzacja:** `ROLE_OWNER`

**Request Body:**
```json
{
  "productId": 1,
  "attributeId": 1,
  "value": "15.6 inch"
}
```

**Response:**
```json
{
  "id": 1,
  "attributeName": "Screen Size",
  "attributeType": "TEXT",
  "isKeyAttribute": true,
  "value": "15.6 inch"
}
```

**Uwaga:** `attributeId` odnosi się do atrybutu z tabeli `attributes`, nie do `categoryAttribute`.

### 5.2 Tworzenie wielu wartości atrybutów (bulk)
**Endpoint:** `POST /api/product-attribute-values/bulk`  
**Autoryzacja:** `ROLE_OWNER`

**Request Body:**
```json
[
  {
    "productId": 1,
    "attributeId": 1,
    "value": "15.6 inch"
  },
  {
    "productId": 1,
    "attributeId": 2,
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
  "value": "17 inch"
}
```

**Uwaga:** Można aktualizować tylko wartość atrybutu. `attributeId` nie może być zmienione.

### 5.4 Aktualizacja wartości atrybutów produktu (bulk)
**Endpoint:** `PUT /api/product-attribute-values/product/{productId}/bulk`  
**Autoryzacja:** `ROLE_OWNER`

**Request Body:**
```json
[
  {
    "id": 1,
    "value": "17 inch"
  },
  {
    "id": 2,
    "value": "White"
  }
]
```

**Uwaga:** Lista musi zawierać wszystkie atrybuty produktu. Liczba elementów musi odpowiadać liczbie istniejących atrybutów.

### 5.5 Usuwanie wartości atrybutu
**Endpoint:** `DELETE /api/product-attribute-values/{id}`  
**Autoryzacja:** `ROLE_OWNER`

### 5.6 Usuwanie wszystkich wartości atrybutów produktu
**Endpoint:** `DELETE /api/product-attribute-values/product/{productId}`  
**Autoryzacja:** `ROLE_OWNER`

### 5.7 Pobieranie wartości atrybutu po ID
**Endpoint:** `GET /api/product-attribute-values/{id}`  
**Autoryzacja:** Public

### 5.8 Pobieranie wartości atrybutu po produkt i atrybut
**Endpoint:** `GET /api/product-attribute-values/product/{productId}/attribute/{attributeId}`  
**Autoryzacja:** Public

### 5.9 Lista wszystkich wartości atrybutów (z paginacją)
**Endpoint:** `GET /api/product-attribute-values`  
**Autoryzacja:** Public

**Query Parameters:**
- `page` (int, default: 0) - numer strony
- `size` (int, default: 10) - rozmiar strony
- `sortBy` (string, default: "id") - pole sortowania
- `sortDir` (string, default: "asc") - kierunek sortowania

**Response:** `Page<ProductAttributeValueDTO>`

### 5.10 Wartości atrybutów według produktu
**Endpoint:** `GET /api/product-attribute-values/product/{productId}`  
**Autoryzacja:** Public

**Response:** `List<ProductAttributeValueDTO>`

### 5.11 Wartości atrybutów według produktu (paginated)
**Endpoint:** `GET /api/product-attribute-values/product/{productId}/paginated`  
**Autoryzacja:** Public

**Query Parameters:**
- `page` (int, default: 0) - numer strony
- `size` (int, default: 10) - rozmiar strony
- `sortBy` (string, default: "id") - pole sortowania
- `sortDir` (string, default: "asc") - kierunek sortowania

**Response:** `Page<ProductAttributeValueDTO>`

### 5.12 Wartości atrybutów według atrybutu
**Endpoint:** `GET /api/product-attribute-values/attribute/{attributeId}`  
**Autoryzacja:** Public

**Response:** `List<ProductAttributeValueDTO>`

### 5.13 Wartości atrybutów według atrybutu (paginated)
**Endpoint:** `GET /api/product-attribute-values/attribute/{attributeId}/paginated`  
**Autoryzacja:** Public

**Query Parameters:**
- `page` (int, default: 0) - numer strony
- `size` (int, default: 10) - rozmiar strony
- `sortBy` (string, default: "id") - pole sortowania
- `sortDir` (string, default: "asc") - kierunek sortowania

**Response:** `Page<ProductAttributeValueDTO>`

### 5.14 Wartości atrybutów według kategorii produktu
**Endpoint:** `GET /api/product-attribute-values/category/{categoryId}`  
**Autoryzacja:** Public

**Query Parameters:**
- `page` (int, default: 0) - numer strony
- `size` (int, default: 10) - rozmiar strony
- `sortBy` (string, default: "id") - pole sortowania
- `sortDir` (string, default: "asc") - kierunek sortowania

**Response:** `Page<ProductAttributeValueDTO>`

### 5.15 Wyszukiwanie wartości atrybutów
**Endpoint:** `GET /api/product-attribute-values/search/value`  
**Autoryzacja:** Public

**Query Parameters:**
- `value` (string, required) - wyszukiwana wartość
- `page` (int, default: 0) - numer strony
- `size` (int, default: 10) - rozmiar strony
- `sortBy` (string, default: "id") - pole sortowania
- `sortDir` (string, default: "asc") - kierunek sortowania

**Response:** `Page<ProductAttributeValueDTO>`

### 5.16 Wartości atrybutów według typu
**Endpoint:** `GET /api/product-attribute-values/attribute-type/{attributeType}`  
**Autoryzacja:** Public

**Path Parameters:**
- `attributeType` (String) - Typ atrybutu (TEXT, NUMBER, BOOLEAN, SELECT)

**Query Parameters:**
- `page` (int, default: 0) - numer strony
- `size` (int, default: 10) - rozmiar strony
- `sortBy` (string, default: "id") - pole sortowania
- `sortDir` (string, default: "asc") - kierunek sortowania

**Response:** `Page<ProductAttributeValueDTO>`

### 5.17 Kluczowe atrybuty produktu (paginated)
**Endpoint:** `GET /api/product-attribute-values/product/{productId}/key-attributes`  
**Autoryzacja:** Public

**Query Parameters:**
- `page` (int, default: 0) - numer strony
- `size` (int, default: 10) - rozmiar strony
- `sortBy` (string, default: "id") - pole sortowania
- `sortDir` (string, default: "asc") - kierunek sortowania

**Response:** `Page<ProductAttributeValueDTO>`

### 5.18 Kluczowe atrybuty produktu (lista)
**Endpoint:** `GET /api/product-attribute-values/product/{productId}/key-attributes/list`  
**Autoryzacja:** Public

**Response:** `List<ProductAttributeValueDTO>`

### 5.19 Wartości atrybutów według produktu i typu
**Endpoint:** `GET /api/product-attribute-values/product/{productId}/attribute-type/{attributeType}`  
**Autoryzacja:** Public

**Response:** `List<ProductAttributeValueDTO>`

### 5.20 Zaawansowane wyszukiwanie
**Endpoint:** `GET /api/product-attribute-values/search/advanced`  
**Autoryzacja:** Public

**Query Parameters:**
- `productId` (Long, optional) - ID produktu
- `attributeId` (Long, optional) - ID atrybutu
- `value` (String, optional) - Wartość atrybutu
- `isActive` (Boolean, optional) - Status aktywności
- `page` (int, default: 0) - numer strony
- `size` (int, default: 10) - rozmiar strony
- `sortBy` (string, default: "id") - pole sortowania
- `sortDir` (string, default: "asc") - kierunek sortowania

**Response:** `Page<ProductAttributeValueDTO>`

### 5.21 Statystyki wartości atrybutów

#### 5.21.1 Liczba wartości atrybutów dla produktu
**Endpoint:** `GET /api/product-attribute-values/stats/product/{productId}`  
**Autoryzacja:** Public

**Response:** `Long` - liczba wartości atrybutów

#### 5.21.2 Liczba wartości atrybutów dla atrybutu
**Endpoint:** `GET /api/product-attribute-values/stats/attribute/{attributeId}`  
**Autoryzacja:** Public

**Response:** `Long` - liczba wartości atrybutów

#### 5.21.3 Liczba wartości atrybutów dla kategorii
**Endpoint:** `GET /api/product-attribute-values/stats/category/{categoryId}`  
**Autoryzacja:** Public

**Response:** `Long` - liczba wartości atrybutów

### 5.22 Unikalne wartości atrybutu
**Endpoint:** `GET /api/product-attribute-values/distinct-values/attribute/{attributeId}`  
**Autoryzacja:** Public

**Response:**
```json
["15.6 inch", "17 inch", "13.3 inch"]
```

---

## 7. Product Images API (`/api/products/{productId}/images`)

### 6.1 Lista obrazów
**Endpoint:** `GET /api/products/{productId}/images`  
**Autoryzacja:** Public

**Response:**
```json
[
  {
    "id": 10,
    "productId": 3,
    "url": "/uploads/products/3/abc.jpg",
    "altText": "front",
    "isThumbnail": true,
    "createdAt": "2025-10-11T17:10:58Z",
    "updatedAt": "2025-10-11T17:10:58Z"
  }
]
```

### 6.2 Upload obrazu
**Endpoint:** `POST /api/products/{productId}/images`  
**Autoryzacja:** `ROLE_OWNER`

**Multipart form-data:**
- `file` (wymagany)
- `altText` (opcjonalny)
- `isThumbnail` (opcjonalny, domyślnie false)

**Walidacja:**
- Typy: `image/jpeg,image/png,image/webp`
- Max rozmiar: `5 MB`
- Limit na produkt: `10`

**Response:** `201 Created` + `ProductImageDTO`

### 6.3 Ustawienie miniatury
**Endpoint:** `POST /api/products/{productId}/images/{imageId}/thumbnail`  
**Autoryzacja:** `ROLE_OWNER`

Zdejmuje flagę `isThumbnail` z innych obrazów produktu i ustawia `thumbnail_url` w produkcie.

### 6.4 Usunięcie obrazu
**Endpoint:** `DELETE /api/products/{productId}/images/{imageId}`  
**Autoryzacja:** `ROLE_OWNER`

---

## 8. Modele danych

### 7.1 CategoryAttributeType
```json
{
  "TEXT": "Tekst",
  "NUMBER": "Liczba",
  "BOOLEAN": "Prawda/Fałsz",
  "DATE": "Data",
  "SELECT": "Lista wyboru"
}
```

### 7.2 ERole
```json
{
  "USER": "Użytkownik",
  "ADMIN": "Administrator", 
  "OWNER": "Właściciel"
}
```

---

## 9. Kody błędów

### 8.1 HTTP Status Codes
- `200 OK` - Sukces
- `201 Created` - Zasób utworzony
- `204 No Content` - Sukces bez zawartości
- `400 Bad Request` - Błędne żądanie
- `401 Unauthorized` - Brak autoryzacji
- `403 Forbidden` - Brak uprawnień
- `404 Not Found` - Zasób nie znaleziony
- `409 Conflict` - Konflikt (np. duplikat)
- `500 Internal Server Error` - Błąd serwera

### 8.2 Przykłady błędów
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

## 10. Przykłady użycia

### 9.1 Tworzenie produktu z atrybutami
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
        "attributeId": 1,
        "value": "6.1 inch"
      },
      {
        "attributeId": 2,
        "value": "Space Black"
      }
    ]
  }'
```

### 10.2 Wyszukiwanie produktów z Elasticsearch
```bash
curl -X POST "http://localhost:8080/api/search?query=laptop&minPrice=1000&maxPrice=5000&page=0&size=20" \
  -H "Content-Type: application/json" \
  -d '{
    "Color": "Black",
    "Screen Size": "15.6 inch"
  }'
```

### 10.3 Pobieranie atrybutów produktu
```bash
curl -X GET "http://localhost:8080/api/product-attribute-values/product/1"
```

---

## 11. Testy

### 10.1 Pokrycie testami
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

### 10.2 Uruchamianie testów
```bash
# Wszystkie testy
mvn test

# Konkretna klasa testowa
mvn test -Dtest=ProductServiceImplTest

# Testy z raportem pokrycia
mvn test jacoco:report
```

---

## 12. Migracje bazy danych

### 12.1 Dostępne migracje
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
- `V16__make_address_user_id_nullable.sql` - Wsparcie dla zamówień gości: `user_id` w tabeli `addresses` jest teraz nullable, dodano pola kontaktowe gości w tabeli `orders` (`guest_email`, `guest_first_name`, `guest_last_name`, `guest_phone`)

### 12.2 Uruchamianie migracji
```bash
# Automatycznie przy starcie aplikacji
mvn spring-boot:run

# Ręcznie (jeśli potrzebne)
mvn flyway:migrate
```

---

## 13. Konfiguracja

### 12.1 Wymagane zmienne środowiskowe
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

### 12.2 Profile aplikacji
- `default` - Produkcja (PostgreSQL)
- `test` - Testy (H2 in-memory)
- `example` - Przykładowa konfiguracja

---

## 14. Bezpieczeństwo

### 13.1 Role i uprawnienia
- **USER** - Podstawowe operacje (odczyt)
- **ADMIN** - Zarządzanie użytkownikami
- **OWNER** - Pełne uprawnienia (CRUD wszystkich zasobów)

### 13.2 Zabezpieczone endpointy
- Wszystkie operacje CUD wymagają roli `ROLE_OWNER`
- Endpointy odczytu są publiczne
- JWT token wymagany dla operacji wymagających autoryzacji
- **Publiczne endpointy:**
  - `POST /api/orders/guest` - Tworzenie zamówień przez gości (bez autoryzacji)
  - `POST /api/payments/guest` - Tworzenie płatności przez gości (bez autoryzacji)
  - `POST /api/payments/guest/{paymentId}/simulate` - Symulacja płatności gości (bez autoryzacji)
  - `POST /api/auth/register`, `POST /api/auth/login` - Rejestracja i logowanie
  - `GET /api/products/**`, `GET /api/categories/**` - Przeglądanie produktów i kategorii

### 13.3 Walidacja danych
- Wszystkie DTOs mają walidację Bean Validation
- Sprawdzanie uprawnień na poziomie metody
- Sanityzacja danych wejściowych

---

## 15. Orders API (`/api/orders`)

### 14.1 Tworzenie zamówienia
**Endpoint:** `POST /api/orders`  
**Autoryzacja:** USER lub OWNER (tylko własne zamówienia)

**Request Body:**
```json
{
  "addressId": 1,
  "status": "NEW",
  "items": [
    {
      "productId": 1,
      "quantity": 2
    }
  ]
}
```

**Response:** `201 Created`
```json
{
  "id": 1,
  "userId": 1,
  "address": {...},
  "status": "NEW",
  "totalAmount": 199.98,
  "items": [...],
  "createdAt": "2024-01-01T10:00:00Z"
}
```

**Uwagi:**
- Automatyczna rezerwacja magazynu (pesymistyczna blokada)
- Automatyczne obliczenie `totalAmount` na podstawie pozycji
- Cena pozycji pobierana z produktu (zabezpieczenie przed manipulacją)

### 14.1.1 Tworzenie zamówienia przez gościa (bez logowania)
**Endpoint:** `POST /api/orders/guest`  
**Autoryzacja:** Brak (endpoint publiczny)

**Request Body:**
```json
{
  "email": "guest@example.com",
  "firstName": "Jan",
  "lastName": "Kowalski",
  "phone": "123456789",
  "addressLine1": "ul. Przykładowa 1",
  "addressLine2": "Mieszkanie 5",
  "city": "Warszawa",
  "region": "Mazowieckie",
  "postalCode": "00-001",
  "country": "Polska",
  "items": [
    {
      "productId": 1,
      "quantity": 2
    },
    {
      "productId": 3,
      "quantity": 1
    }
  ]
}
```

**Pola wymagane:**
- `email` (String) - Email kontaktowy gościa (walidacja formatu email)
- `firstName` (String) - Imię gościa (max 100 znaków)
- `lastName` (String, opcjonalne) - Nazwisko gościa (max 100 znaków)
- `phone` (String, opcjonalne) - Numer telefonu (max 20 znaków)
- `addressLine1` (String) - Pierwsza linia adresu (max 255 znaków)
- `addressLine2` (String, opcjonalne) - Druga linia adresu (max 255 znaków)
- `city` (String) - Miasto (max 100 znaków)
- `region` (String, opcjonalne) - Region/województwo (max 100 znaków)
- `postalCode` (String) - Kod pocztowy (max 20 znaków)
- `country` (String) - Kraj (max 100 znaków)
- `items` (List<OrderItemCreateDTO>) - Lista produktów w zamówieniu (min 1 pozycja)

**Response:** `201 Created`
```json
{
  "id": 123,
  "userId": null,
  "firstName": "Jan",
  "lastName": "Kowalski",
  "address": {
    "id": 456,
    "line1": "ul. Przykładowa 1",
    "line2": "Mieszkanie 5",
    "city": "Warszawa",
    "region": "Mazowieckie",
    "postalCode": "00-001",
    "country": "Polska"
  },
  "status": "NEW",
  "totalAmount": 299.98,
  "items": [
    {
      "id": 1,
      "productId": 1,
      "product": {...},
      "quantity": 2,
      "price": 99.99
    }
  ],
  "payments": [],
  "createdAt": "2024-01-01T10:00:00Z",
  "updatedAt": "2024-01-01T10:00:00Z",
  "isActive": true
}
```

**Uwagi:**
- Endpoint dostępny bez autoryzacji - umożliwia składanie zamówień przez niezalogowanych użytkowników
- Automatyczna rezerwacja magazynu (pesymistyczna blokada)
- Automatyczne obliczenie `totalAmount` na podstawie pozycji
- Cena pozycji pobierana z produktu (zabezpieczenie przed manipulacją)
- Adres dostawy jest automatycznie tworzony i powiązany z zamówieniem
- Dane kontaktowe gościa (email, imię, nazwisko, telefon) są przechowywane w zamówieniu
- Notyfikacje email są wysyłane na adres podany w polu `email`
- W odpowiedzi `userId` będzie `null` dla zamówień gości
- Wszystkie pola tekstowe są walidowane pod kątem bezpieczeństwa (brak HTML/script)

**Status codes:**
- `201 Created` - Zamówienie utworzone pomyślnie
- `400 Bad Request` - Błędy walidacji (brak produktów, nieprawidłowy email, brak wymaganych pól)
- `404 Not Found` - Jeden z produktów nie został znaleziony
- `409 Conflict` - Brak wystarczającej ilości produktów w magazynie

**Przykład użycia:**
```bash
curl -X POST http://localhost:8080/api/orders/guest \
  -H "Content-Type: application/json" \
  -d '{
    "email": "jan.kowalski@example.com",
    "firstName": "Jan",
    "lastName": "Kowalski",
    "phone": "+48123456789",
    "addressLine1": "ul. Przykładowa 1",
    "city": "Warszawa",
    "postalCode": "00-001",
    "country": "Polska",
    "items": [
      {
        "productId": 1,
        "quantity": 1
      }
    ]
  }'
```

### 14.2 Anulowanie zamówienia
**Endpoint:** `PATCH /api/orders/{id}/cancel`  
**Autoryzacja:** USER (tylko własne, status NEW/CONFIRMED) lub OWNER (dowolne)

**Response:** `200 OK`
```json
{
  "id": 1,
  "status": "CANCELLED",
  ...
}
```

**Błędy:**
- `400 Bad Request` - Zamówienie już anulowane
- `403 Forbidden` - USER próbuje anulować SHIPPED/DELIVERED

### 14.3 Pobieranie zamówienia
**Endpoint:** `GET /api/orders/{id}`  
**Autoryzacja:** USER (tylko własne) lub OWNER

### 14.4 Lista zamówień użytkownika
**Endpoint:** `GET /api/orders/user/{userId}`  
**Autoryzacja:** USER (tylko własne) lub OWNER

**Query Parameters:**
- `page` (int, default: 0) - numer strony
- `size` (int, default: 10) - rozmiar strony
- `sortBy` (string, default: "createdAt") - pole sortowania
- `sortDir` (string, default: "desc") - kierunek sortowania (asc/desc)

**Response:** `Page<OrderDTO>`

### 14.5 Pobieranie własnych zamówień
**Endpoint:** `GET /api/orders/me`  
**Autoryzacja:** USER lub OWNER

**Query Parameters:**
- `page` (int, default: 0) - numer strony
- `size` (int, default: 10) - rozmiar strony
- `sortBy` (string, default: "createdAt") - pole sortowania
- `sortDir` (string, default: "desc") - kierunek sortowania (asc/desc)

**Response:** `Page<OrderDTO>`

**Uwagi:**
- Zwraca zamówienia zalogowanego użytkownika
- Automatycznie używa ID z tokena JWT

### 14.6 Zamówienia według statusu
**Endpoint:** `GET /api/orders/status/{status}`  
**Autoryzacja:** OWNER

**Path Parameters:**
- `status` (OrderStatus) - Status zamówienia (NEW, CONFIRMED, PROCESSING, SHIPPED, DELIVERED, CANCELLED, REFUNDED)

**Query Parameters:**
- `page` (int, default: 0) - numer strony
- `size` (int, default: 10) - rozmiar strony
- `sortBy` (string, default: "createdAt") - pole sortowania
- `sortDir` (string, default: "desc") - kierunek sortowania (asc/desc)

**Response:** `Page<OrderDTO>`

### 14.7 Zamówienia użytkownika według statusu
**Endpoint:** `GET /api/orders/user/{userId}/status/{status}`  
**Autoryzacja:** USER (tylko własne) lub OWNER

**Path Parameters:**
- `userId` (Long) - ID użytkownika
- `status` (OrderStatus) - Status zamówienia

**Query Parameters:**
- `page` (int, default: 0) - numer strony
- `size` (int, default: 10) - rozmiar strony
- `sortBy` (string, default: "createdAt") - pole sortowania
- `sortDir` (string, default: "desc") - kierunek sortowania (asc/desc)

**Response:** `Page<OrderDTO>`

### 14.8 Zaawansowane filtrowanie zamówień
**Endpoint:** `GET /api/orders/filter`  
**Autoryzacja:** OWNER

**Query Parameters:**
- `userId` (Long, optional) - ID użytkownika
- `status` (OrderStatus, optional) - Status zamówienia
- `isActive` (Boolean, optional) - Status aktywności
- `startDate` (Instant, optional) - Data początkowa (ISO 8601)
- `endDate` (Instant, optional) - Data końcowa (ISO 8601)
- `page` (int, default: 0) - numer strony
- `size` (int, default: 10) - rozmiar strony
- `sortBy` (string, default: "createdAt") - pole sortowania
- `sortDir` (string, default: "desc") - kierunek sortowania (asc/desc)

**Response:** `Page<OrderDTO>`

**Przykład:**
```bash
GET /api/orders/filter?userId=1&status=CONFIRMED&startDate=2024-01-01T00:00:00Z&endDate=2024-12-31T23:59:59Z
```

### 14.9 Statystyki zamówień
**Endpoint:** `GET /api/orders/stats/count`  
**Autoryzacja:** USER (tylko własne) lub OWNER

**Query Parameters:**
- `userId` (Long, optional) - ID użytkownika
- `status` (OrderStatus, optional) - Status zamówienia

**Response:** `Long` - liczba zamówień

**Uwagi:**
- Można podać `userId`, `status` lub oba parametry
- Jeśli podano oba, zwraca liczbę zamówień użytkownika o danym statusie
- Jeśli podano tylko `userId`, zwraca liczbę wszystkich zamówień użytkownika
- Jeśli podano tylko `status`, zwraca liczbę wszystkich zamówień o danym statusie

### 14.10 Aktualizacja zamówienia (tylko OWNER)
**Endpoint:** `PUT /api/orders/{id}`  
**Autoryzacja:** OWNER

**Request Body:**
```json
{
  "status": "SHIPPED",
  "isActive": true
}
```

**Uwagi:**
- Automatyczne zarządzanie magazynem przy zmianie statusu
- `CANCELLED` → zwolnienie rezerwacji
- `CONFIRMED/SHIPPED/DELIVERED` → finalizacja rezerwacji

---

## 16. Payments API (`/api/payments`)

### 15.1 Tworzenie płatności
**Endpoint:** `POST /api/payments`  
**Autoryzacja:** USER (tylko własne zamówienia) lub OWNER

**Request Body:**
```json
{
  "orderId": 1,
  "amount": 199.98,
  "method": "CREDIT_CARD",
  "transactionId": "TXN-123456",
  "notes": "Payment notes"
}
```

**Response:** `201 Created`
```json
{
  "id": 1,
  "orderId": 1,
  "amount": 199.98,
  "method": "CREDIT_CARD",
  "status": "PENDING",
  "transactionDate": "2024-01-01T10:00:00Z"
}
```

**Walidacja:**
- Kwota musi odpowiadać `order.totalAmount`
- Zamówienie musi mieć status NEW lub CONFIRMED
- USER może płacić tylko za swoje zamówienia

### 15.1.1 Tworzenie płatności przez gościa (bez logowania)
**Endpoint:** `POST /api/payments/guest`  
**Autoryzacja:** Brak (endpoint publiczny)

**Request Body:**
```json
{
  "orderId": 13,
  "email": "guest@example.com",
  "amount": 299.98,
  "method": "CREDIT_CARD",
  "transactionId": "TXN-123456",
  "notes": "Guest payment"
}
```

**Pola wymagane:**
- `orderId` (Long) - ID zamówienia gościa
- `email` (String) - Email gościa (walidacja formatu email, musi się zgadzać z emailem zamówienia)
- `amount` (BigDecimal) - Kwota płatności (musi się zgadzać z `order.totalAmount`)
- `method` (PaymentMethod) - Metoda płatności (CREDIT_CARD, DEBIT_CARD, PAYPAL, BANK_TRANSFER, CASH_ON_DELIVERY, BLIK, APPLE_PAY, GOOGLE_PAY)
- `transactionId` (String, opcjonalne) - ID transakcji (max 255 znaków)
- `notes` (String, opcjonalne) - Notatki (max 500 znaków)

**Response:** `201 Created`
```json
{
  "id": 5,
  "orderId": 13,
  "amount": 299.98,
  "method": "CREDIT_CARD",
  "status": "PENDING",
  "transactionId": "TXN-123456",
  "transactionDate": "2024-01-01T10:00:00Z",
  "notes": "Guest payment",
  "createdAt": "2024-01-01T10:00:00Z",
  "updatedAt": "2024-01-01T10:00:00Z",
  "isActive": true
}
```

**Uwagi:**
- Endpoint dostępny bez autoryzacji - umożliwia tworzenie płatności dla zamówień gości
- Email musi się zgadzać z `guestEmail` zamówienia
- Zamówienie musi być zamówieniem gościa (user == null)
- Kwota musi się zgadzać z `totalAmount` zamówienia
- Zamówienie musi mieć status NEW lub CONFIRMED
- Wszystkie pola tekstowe są walidowane pod kątem bezpieczeństwa (brak HTML/script)

**Status codes:**
- `201 Created` - Płatność utworzona pomyślnie
- `400 Bad Request` - Błędy walidacji (nieprawidłowy email, kwota nie zgadza się, zamówienie nie jest gościa)
- `404 Not Found` - Zamówienie nie zostało znalezione
- `409 Conflict` - Zamówienie ma nieprawidłowy status (nie NEW ani CONFIRMED)

**Przykład użycia:**
```bash
curl -X POST http://localhost:8080/api/payments/guest \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": 13,
    "email": "guest@example.com",
    "amount": 299.98,
    "method": "CREDIT_CARD",
    "transactionId": "TXN-123456",
    "notes": "Guest payment"
  }'
```

### 15.2 Aktualizacja statusu płatności
**Endpoint:** `PUT /api/payments/{id}`  
**Autoryzacja:** OWNER

**Request Body:**
```json
{
  "status": "COMPLETED",
  "transactionId": "TXN-123456-UPDATED",
  "notes": "Payment completed"
}
```

**Uwagi:**
- Gdy status zmienia się na `COMPLETED`:
  - Automatyczna zmiana statusu zamówienia na `CONFIRMED`
  - Finalizacja rezerwacji magazynu

### 15.3 Pobieranie płatności
**Endpoint:** `GET /api/payments/{id}`  
**Autoryzacja:** USER (tylko własne) lub OWNER

### 15.4 Lista płatności dla zamówienia
**Endpoint:** `GET /api/payments/order/{orderId}`  
**Autoryzacja:** USER (tylko własne zamówienia) lub OWNER

**Query Parameters:**
- `page` (int, default: 0) - numer strony
- `size` (int, default: 10) - rozmiar strony
- `sortBy` (string, default: "createdAt") - pole sortowania
- `sortDir` (string, default: "desc") - kierunek sortowania (asc/desc)

**Response:** `Page<PaymentDTO>`

### 15.5 Pobieranie własnych płatności
**Endpoint:** `GET /api/payments/me`  
**Autoryzacja:** USER lub OWNER

**Query Parameters:**
- `page` (int, default: 0) - numer strony
- `size` (int, default: 10) - rozmiar strony
- `sortBy` (string, default: "createdAt") - pole sortowania
- `sortDir` (string, default: "desc") - kierunek sortowania (asc/desc)

**Response:** `Page<PaymentDTO>`

**Uwagi:**
- Zwraca płatności zalogowanego użytkownika
- Automatycznie używa ID z tokena JWT

### 15.6 Płatności według statusu
**Endpoint:** `GET /api/payments/status/{status}`  
**Autoryzacja:** OWNER

**Path Parameters:**
- `status` (PaymentStatus) - Status płatności (PENDING, PROCESSING, COMPLETED, FAILED, CANCELLED, REFUNDED)

**Query Parameters:**
- `page` (int, default: 0) - numer strony
- `size` (int, default: 10) - rozmiar strony
- `sortBy` (string, default: "createdAt") - pole sortowania
- `sortDir` (string, default: "desc") - kierunek sortowania (asc/desc)

**Response:** `Page<PaymentDTO>`

### 15.7 Płatności zamówienia według statusu
**Endpoint:** `GET /api/payments/order/{orderId}/status/{status}`  
**Autoryzacja:** USER (tylko własne zamówienia) lub OWNER

**Path Parameters:**
- `orderId` (Long) - ID zamówienia
- `status` (String) - Status płatności (PENDING, PROCESSING, COMPLETED, FAILED, CANCELLED, REFUNDED)

**Query Parameters:**
- `page` (int, default: 0) - numer strony
- `size` (int, default: 10) - rozmiar strony
- `sortBy` (string, default: "createdAt") - pole sortowania
- `sortDir` (string, default: "desc") - kierunek sortowania (asc/desc)

**Response:** `Page<PaymentDTO>`

### 15.8 Zaawansowane filtrowanie płatności
**Endpoint:** `GET /api/payments/filter`  
**Autoryzacja:** OWNER

**Query Parameters:**
- `orderId` (Long, optional) - ID zamówienia
- `status` (String, optional) - Status płatności
- `method` (String, optional) - Metoda płatności (CREDIT_CARD, DEBIT_CARD, PAYPAL, BANK_TRANSFER, CASH_ON_DELIVERY, BLIK, APPLE_PAY, GOOGLE_PAY)
- `isActive` (Boolean, optional) - Status aktywności
- `startDate` (Instant, optional) - Data początkowa (ISO 8601)
- `endDate` (Instant, optional) - Data końcowa (ISO 8601)
- `page` (int, default: 0) - numer strony
- `size` (int, default: 10) - rozmiar strony
- `sortBy` (string, default: "createdAt") - pole sortowania
- `sortDir` (string, default: "desc") - kierunek sortowania (asc/desc)

**Response:** `Page<PaymentDTO>`

**Przykład:**
```bash
GET /api/payments/filter?status=COMPLETED&method=CREDIT_CARD&startDate=2024-01-01T00:00:00Z&endDate=2024-12-31T23:59:59Z
```

### 15.9 Statystyki płatności
**Endpoint:** `GET /api/payments/stats/count`  
**Autoryzacja:** USER (tylko własne zamówienia) lub OWNER

**Query Parameters:**
- `orderId` (Long, optional) - ID zamówienia
- `status` (String, optional) - Status płatności

**Response:** `Long` - liczba płatności

**Uwagi:**
- Można podać `orderId`, `status` lub oba parametry
- Jeśli podano oba, zwraca liczbę płatności zamówienia o danym statusie
- Jeśli podano tylko `orderId`, zwraca liczbę wszystkich płatności zamówienia
- Jeśli podano tylko `status`, zwraca liczbę wszystkich płatności o danym statusie

### 15.10 Metody płatności
- `CREDIT_CARD`
- `DEBIT_CARD`
- `PAYPAL`
- `BANK_TRANSFER`
- `CASH_ON_DELIVERY`
- `BLIK`
- `APPLE_PAY`
- `GOOGLE_PAY`

### 15.6 Statusy płatności
- `PENDING` - Oczekująca
- `PROCESSING` - W trakcie przetwarzania
- `COMPLETED` - Zakończona
- `FAILED` - Nieudana
- `CANCELLED` - Anulowana
- `REFUNDED` - Zwrócona

### 15.7 Symulacja płatności (Mock Payment Gateway)
**Endpoint:** `POST /api/payments/{paymentId}/simulate`  
**Autoryzacja:** USER (tylko własne zamówienia) lub OWNER

**Query Parameters:**
- `scenario` (opcjonalny, default: `SUCCESS`) - Scenariusz symulacji:
  - `SUCCESS` - Płatność udana (status → `COMPLETED`)
  - `FAIL` - Odmowa banku (status → `FAILED`)
  - `ERROR` - Błąd połączenia (status → `FAILED`)

**Response:** `200 OK`
```json
{
  "id": 1,
  "orderId": 1,
  "amount": 199.98,
  "method": "CREDIT_CARD",
  "status": "COMPLETED",
  "transactionId": "MOCK-A1B2C3D4",
  "transactionDate": "2024-01-01T10:00:00Z",
  "notes": "Symulacja: Płatność udana (Bank OK)",
  "createdAt": "2024-01-01T10:00:00Z",
  "updatedAt": "2024-01-01T10:01:00Z"
}
```

**Przykład użycia:**
```bash
# Symulacja udanej płatności
curl -X POST "http://localhost:8080/api/payments/1/simulate?scenario=SUCCESS" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# Symulacja nieudanej płatności
curl -X POST "http://localhost:8080/api/payments/1/simulate?scenario=FAIL" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Uwagi:**
- Endpoint symuluje odpowiedź z bramki płatniczej (mock)
- Automatycznie generuje `transactionId` w formacie `MOCK-XXXXXXXX`
- Automatycznie aktualizuje status zamówienia:
  - `SUCCESS` → zamówienie zmienia status na `CONFIRMED` (jeśli było `NEW`)
  - `FAIL`/`ERROR` → zamówienie zmienia status na `CANCELLED` (jeśli było `NEW`)
- Dodaje odpowiednie notatki opisujące scenariusz
- Opóźnienie 1 sekundy symuluje czas oczekiwania na odpowiedź bramki
- Nie można symulować płatności już zakończonych (`COMPLETED`)

### 15.7.1 Symulacja płatności gościa (Mock Payment Gateway)
**Endpoint:** `POST /api/payments/guest/{paymentId}/simulate`  
**Autoryzacja:** Brak (endpoint publiczny)

**Path Parameters:**
- `paymentId` (Long) - ID płatności

**Query Parameters:**
- `email` (wymagany) - Email gościa (walidacja formatu email, musi się zgadzać z emailem zamówienia)
- `scenario` (opcjonalny, default: `SUCCESS`) - Scenariusz symulacji:
  - `SUCCESS` - Płatność udana (status → `COMPLETED`)
  - `FAIL` - Odmowa banku (status → `FAILED`)
  - `ERROR` - Błąd połączenia (status → `FAILED`)

**Response:** `200 OK`
```json
{
  "id": 5,
  "orderId": 13,
  "amount": 299.98,
  "method": "CREDIT_CARD",
  "status": "COMPLETED",
  "transactionId": "MOCK-A1B2C3D4",
  "transactionDate": "2024-01-01T10:00:00Z",
  "notes": "Symulacja: Płatność udana (Bank OK)",
  "createdAt": "2024-01-01T10:00:00Z",
  "updatedAt": "2024-01-01T10:01:00Z",
  "isActive": true
}
```

**Przykład użycia:**
```bash
# Symulacja udanej płatności gościa
curl -X POST "http://localhost:8080/api/payments/guest/5/simulate?email=guest@example.com&scenario=SUCCESS"

# Symulacja nieudanej płatności gościa
curl -X POST "http://localhost:8080/api/payments/guest/5/simulate?email=guest@example.com&scenario=FAIL"

# Symulacja błędu połączenia
curl -X POST "http://localhost:8080/api/payments/guest/5/simulate?email=guest@example.com&scenario=ERROR"
```

**Uwagi:**
- Endpoint dostępny bez autoryzacji - umożliwia symulację płatności dla zamówień gości
- Email musi się zgadzać z `guestEmail` zamówienia powiązanego z płatnością
- Płatność musi należeć do zamówienia gościa (user == null)
- Endpoint symuluje odpowiedź z bramki płatniczej (mock)
- Automatycznie generuje `transactionId` w formacie `MOCK-XXXXXXXX`
- Automatycznie aktualizuje status zamówienia:
  - `SUCCESS` → zamówienie zmienia status na `CONFIRMED` (jeśli było `NEW`)
  - `FAIL`/`ERROR` → zamówienie zmienia status na `CANCELLED` (jeśli było `NEW`)
- Dodaje odpowiednie notatki opisujące scenariusz
- Opóźnienie 1 sekundy symuluje czas oczekiwania na odpowiedź bramki
- Nie można symulować płatności już zakończonych (`COMPLETED`)

**Status codes:**
- `200 OK` - Symulacja zakończona pomyślnie
- `400 Bad Request` - Błędy walidacji (nieprawidłowy email, email nie zgadza się z zamówieniem, zamówienie nie jest gościa)
- `404 Not Found` - Płatność nie została znaleziona
- `409 Conflict` - Płatność jest już zakończona (`COMPLETED`)

---

## 17. Addresses API (`/api/addresses`)

### 16.1 Tworzenie adresu
**Endpoint:** `POST /api/addresses`  
**Autoryzacja:** USER lub OWNER

**Request Body:**
```json
{
  "line1": "ul. Przykładowa 123",
  "line2": "Mieszkanie 45",
  "city": "Warszawa",
  "region": "Mazowieckie",
  "postalCode": "00-001",
  "country": "Polska",
  "isActive": true
}
```

**Uwagi:**
- USER może tworzyć adresy tylko dla siebie
- `userId` jest automatycznie ustawiane z tokena JWT

### 16.2 Aktualizacja adresu
**Endpoint:** `PUT /api/addresses/{id}`  
**Autoryzacja:** USER (tylko własne) lub OWNER

**Request Body:**
```json
{
  "line1": "ul. Nowa 456",
  "isActive": false
}
```

### 16.3 Usuwanie adresu
**Endpoint:** `DELETE /api/addresses/{id}`  
**Autoryzacja:** USER (tylko własne) lub OWNER

**Uwagi:**
- Soft delete (ustawienie `deletedAt` i `isActive = false`)

### 16.4 Pobieranie adresu
**Endpoint:** `GET /api/addresses/{id}`  
**Autoryzacja:** USER (tylko własne) lub OWNER

### 16.5 Lista adresów użytkownika
**Endpoint:** `GET /api/addresses/user/{userId}`  
**Autoryzacja:** USER (tylko własne) lub OWNER

### 16.6 Lista aktywnych adresów
**Endpoint:** `GET /api/addresses/user/{userId}/active`  
**Autoryzacja:** USER (tylko własne) lub OWNER

### 16.7 Wszystkie adresy (tylko OWNER)
**Endpoint:** `GET /api/addresses`  
**Autoryzacja:** OWNER

**Query Parameters:**
- `page` - numer strony (default: 0)
- `size` - rozmiar strony (default: 10)
- `sortBy` - pole sortowania (default: id)
- `sortDir` - kierunek sortowania (asc/desc, default: asc)

---

## 18. Inventory API (`/api/inventory`)

### 18.1 Tworzenie stanu magazynowego
**Endpoint:** `POST /api/inventory`  
**Autoryzacja:** `ROLE_OWNER`

**Request Body:**
```json
{
  "productId": 1,
  "availableQuantity": 100,
  "reservedQuantity": 0
}
```

**Response:** `201 Created`
```json
{
  "id": 1,
  "productId": 1,
  "productName": "Produkt przykładowy",
  "sku": "PROD-001",
  "thumbnailUrl": "/uploads/products/1/thumbnail.jpg",
  "availableQuantity": 100,
  "reservedQuantity": 0,
  "minimumStockLevel": 10,
  "createdAt": "2024-01-01T10:00:00Z",
  "updatedAt": "2024-01-01T10:00:00Z",
  "deletedAt": null,
  "isActive": true
}
```

### 18.2 Aktualizacja stanu magazynowego
**Endpoint:** `PUT /api/inventory/{id}`  
**Autoryzacja:** `ROLE_OWNER`

**Request Body:**
```json
{
  "availableQuantity": 150,
  "reservedQuantity": 10
}
```

### 18.3 Usuwanie stanu magazynowego
**Endpoint:** `DELETE /api/inventory/{id}`  
**Autoryzacja:** `ROLE_OWNER`

### 18.4 Pobieranie stanu magazynowego po ID
**Endpoint:** `GET /api/inventory/{id}`  
**Autoryzacja:** Public

**Response:** `InventoryDTO`
```json
{
  "id": 1,
  "productId": 1,
  "productName": "Produkt przykładowy",
  "sku": "PROD-001",
  "thumbnailUrl": "/uploads/products/1/thumbnail.jpg",
  "availableQuantity": 100,
  "reservedQuantity": 10,
  "minimumStockLevel": 20,
  "createdAt": "2024-01-01T10:00:00Z",
  "updatedAt": "2024-01-01T10:00:00Z",
  "deletedAt": null,
  "isActive": true
}
```

### 18.5 Pobieranie stanu magazynowego po ID produktu
**Endpoint:** `GET /api/inventory/product/{productId}`  
**Autoryzacja:** Public

**Response:** `InventoryDTO`
```json
{
  "id": 1,
  "productId": 1,
  "productName": "Produkt przykładowy",
  "sku": "PROD-001",
  "thumbnailUrl": "/uploads/products/1/thumbnail.jpg",
  "availableQuantity": 100,
  "reservedQuantity": 10,
  "minimumStockLevel": 20,
  "createdAt": "2024-01-01T10:00:00Z",
  "updatedAt": "2024-01-01T10:00:00Z",
  "deletedAt": null,
  "isActive": true
}
```

### 18.6 Lista wszystkich stanów magazynowych
**Endpoint:** `GET /api/inventory`  
**Autoryzacja:** Public

**Query Parameters:**
- `page` (int, default: 0) - numer strony
- `size` (int, default: 10) - rozmiar strony
- `sortBy` (string, default: "id") - pole sortowania
- `sortDir` (string, default: "asc") - kierunek sortowania (asc/desc)

**Response:** `Page<InventoryDTO>`
```json
{
  "content": [
    {
      "id": 1,
      "productId": 1,
      "productName": "Produkt przykładowy",
      "sku": "PROD-001",
      "thumbnailUrl": "/uploads/products/1/thumbnail.jpg",
      "availableQuantity": 100,
      "reservedQuantity": 10,
      "minimumStockLevel": 20,
      "createdAt": "2024-01-01T10:00:00Z",
      "updatedAt": "2024-01-01T10:00:00Z",
      "deletedAt": null,
      "isActive": true
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "size": 10,
  "number": 0
}
```

### 18.7 Podsumowanie stanów magazynowych
**Endpoint:** `GET /api/inventory/summary`  
**Autoryzacja:** Public

**Query Parameters:**
- `page` (int, default: 0) - numer strony
- `size` (int, default: 10) - rozmiar strony
- `sortBy` (string, default: "id") - pole sortowania
- `sortDir` (string, default: "asc") - kierunek sortowania (asc/desc)

**Response:** `Page<InventorySummaryDTO>`
```json
{
  "content": [
    {
      "productId": 1,
      "productName": "Produkt przykładowy",
      "sku": "PROD-001",
      "thumbnailUrl": "/uploads/products/1/thumbnail.jpg",
      "availableQuantity": 100,
      "reservedQuantity": 10,
      "belowMinimum": false
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "size": 10,
  "number": 0
}
```

**Uwagi:**
- Zwraca uproszczone podsumowanie stanów magazynowych
- `InventorySummaryDTO` zawiera podstawowe informacje o stanie magazynowym, w tym miniaturkę produktu (`thumbnailUrl`)

### 18.8 Sprawdzanie dostępności produktu
**Endpoint:** `GET /api/inventory/product/{productId}/available`  
**Autoryzacja:** Public

**Query Parameters:**
- `quantity` (Integer, required, min: 1) - Sprawdzana ilość

**Response:** `Boolean` - `true` jeśli produkt jest dostępny w podanej ilości, `false` w przeciwnym razie

**Przykład:**
```bash
GET /api/inventory/product/1/available?quantity=5
```

**Response:**
```json
true
```

---

## 19. AI Chat API (`/api/ai/chat`)

### 19.1 Chat z asystentem AI
**Endpoint:** `POST /api/ai/chat`  
**Autoryzacja:** USER lub OWNER

**Request Body:**
```json
{
  "message": "Szukam zielonego wazonu do 200 zł",
  "conversationId": "conv-123"
}
```

**Response:**
```
Znalazłem kilka zielonych wazonów w Twoim budżecie:

- [Wazon Zielony Ceramiczny](/product/wazon-zielony-ceramiczny) - **150 zł**
- [Wazon Zielony Szklany](/product/wazon-zielony-szklany) - **180 zł**

Oba są dostępne w magazynie. Który Cię bardziej interesuje?
```

**Uwagi:**
- Asystent AI wykorzystuje Spring AI z Vertex AI Gemini
- System automatycznie indeksuje kategorie i atrybuty produktów przy starcie aplikacji
- Asystent może wyszukiwać produkty używając funkcji `searchProductsTool` i `productDetailsTool`
- Konwersacje są zapamiętywane dzięki `ChatMemory` - użyj tego samego `conversationId` dla kontynuacji rozmowy
- Asystent odpowiada po polsku i pomaga w znalezieniu idealnego produktu
- System automatycznie mapuje zapytania użytkownika na parametry wyszukiwania (kategorie, atrybuty, ceny)

**Funkcjonalności:**
- Wyszukiwanie produktów na podstawie naturalnego języka
- Filtrowanie po kategoriach, atrybutach i cenach
- Pobieranie szczegółów produktów
- Sugerowanie alternatyw, jeśli nie znaleziono idealnego dopasowania
- Zapamiętywanie kontekstu konwersacji

**Przykłady zapytań:**
- "Szukam laptopa do 3000 zł"
- "Pokaż mi czerwone buty Nike rozmiar 42"
- "Masz jakieś rzeźby drewniane?"
- "Pokaż szczegóły tego wazonu" (wymaga wcześniejszego wyszukania)

---

## 20. Contact API (`/api/contact`)

### 20.1 Wysyłanie wiadomości kontaktowej
**Endpoint:** `POST /api/contact`  
**Autoryzacja:** Public

**Request Body:**
```json
{
  "name": "Jan Kowalski",
  "email": "jan@example.com",
  "message": "Chciałbym zapytać o dostępność produktu..."
}
```

**Response:**
```
Otrzymano nową wiadomość z formularza kontaktowego: 

Od: Jan Kowalski
Adres e-mail nadawcy: jan@example.com

Treść:
Chciałbym zapytać o dostępność produktu...
```

**Status codes:**
- `200 OK` - Wiadomość została wysłana
- `400 Bad Request` - Błędne dane wejściowe
- `500 Internal Server Error` - Błąd podczas wysyłki emaila

**Uwagi:**
- Wiadomość jest wysyłana na adres email administratora skonfigurowany w `app.contact.admin.email`
- Wymaga skonfigurowanego serwera SMTP (zobacz konfigurację email w README.md)
- Wszystkie pola są wymagane i walidowane

**Konfiguracja:**
```properties
app.contact.admin.email=admin@example.com
```

---

## 21. Statistics API (`/api/statistics`)

Statistics API zapewnia szczegółowe statystyki sprzedaży i produktów dla właścicieli sklepu. Wszystkie endpointy wymagają roli `ROLE_OWNER`.

### 21.1 Top produkty według ilości sprzedanych

**Endpoint:** `GET /api/statistics/products/top-by-quantity`  
**Autoryzacja:** `ROLE_OWNER`

**Query Parameters:**
- `startDate` (Instant, optional) - Data początkowa w formacie ISO 8601 (domyślnie: 30 dni temu)
- `endDate` (Instant, optional) - Data końcowa w formacie ISO 8601 (domyślnie: teraz)
- `limit` (int, default: 10, min: 1, max: 100) - Maksymalna liczba produktów do zwrócenia

**Walidacja:**
- Daty nie mogą być w przyszłości (`@PastOrPresent`)
- `startDate` musi być przed `endDate`
- `limit` musi być między 1 a 100

**Response:** `List<TopProductDTO>`

**Przykład:**
```bash
GET /api/statistics/products/top-by-quantity?startDate=2024-01-01T00:00:00Z&endDate=2024-12-31T23:59:59Z&limit=10
Authorization: Bearer {token}
```

**Response:**
```json
[
  {
    "productId": 1,
    "productName": "Laptop Dell XPS 15",
    "productSku": "LAP-DELL-XPS15-BLK-16GB",
    "totalQuantitySold": 150,
    "totalRevenue": 225000.00,
    "orderCount": 120
  },
  {
    "productId": 2,
    "productName": "Smartphone Samsung Galaxy S24",
    "productSku": "PHN-SAMS-S24-256GB-BLK",
    "totalQuantitySold": 98,
    "totalRevenue": 294000.00,
    "orderCount": 95
  }
]
```

**Status codes:**
- `200 OK` - Lista top produktów
- `400 Bad Request` - Nieprawidłowe parametry (np. startDate po endDate)
- `403 Forbidden` - Brak uprawnień OWNER

---

### 21.2 Top produkty według przychodu

**Endpoint:** `GET /api/statistics/products/top-by-revenue`  
**Autoryzacja:** `ROLE_OWNER`

**Query Parameters:**
- `startDate` (Instant, optional) - Data początkowa w formacie ISO 8601 (domyślnie: 30 dni temu)
- `endDate` (Instant, optional) - Data końcowa w formacie ISO 8601 (domyślnie: teraz)
- `limit` (int, default: 10, min: 1, max: 100) - Maksymalna liczba produktów do zwrócenia

**Walidacja:**
- Daty nie mogą być w przyszłości (`@PastOrPresent`)
- `startDate` musi być przed `endDate`
- `limit` musi być między 1 a 100

**Response:** `List<TopProductDTO>`

**Przykład:**
```bash
GET /api/statistics/products/top-by-revenue?limit=5
Authorization: Bearer {token}
```

**Response:** (format jak w 21.1, ale posortowane według przychodu)

---

### 21.3 Top produkty według ilości - konkretny miesiąc

**Endpoint:** `GET /api/statistics/products/top-by-quantity/month`  
**Autoryzacja:** `ROLE_OWNER`

**Query Parameters:**
- `year` (int, required, min: 2000, max: 2100) - Rok
- `month` (int, required, min: 1, max: 12) - Miesiąc (1-12)
- `limit` (int, default: 10, min: 1, max: 100) - Maksymalna liczba produktów do zwrócenia

**Response:** `List<TopProductDTO>`

**Przykład:**
```bash
GET /api/statistics/products/top-by-quantity/month?year=2024&month=12&limit=10
Authorization: Bearer {token}
```

**Response:** (format jak w 21.1)

---

### 21.4 Top produkty według przychodu - konkretny miesiąc

**Endpoint:** `GET /api/statistics/products/top-by-revenue/month`  
**Autoryzacja:** `ROLE_OWNER`

**Query Parameters:**
- `year` (int, required, min: 2000, max: 2100) - Rok
- `month` (int, required, min: 1, max: 12) - Miesiąc (1-12)
- `limit` (int, default: 10, min: 1, max: 100) - Maksymalna liczba produktów do zwrócenia

**Response:** `List<TopProductDTO>`

**Przykład:**
```bash
GET /api/statistics/products/top-by-revenue/month?year=2024&month=12&limit=10
Authorization: Bearer {token}
```

**Response:** (format jak w 21.1)

---

### 21.5 Statystyki sprzedaży

**Endpoint:** `GET /api/statistics/sales`  
**Autoryzacja:** `ROLE_OWNER`

**Query Parameters:**
- `startDate` (Instant, optional) - Data początkowa w formacie ISO 8601 (domyślnie: 30 dni temu)
- `endDate` (Instant, optional) - Data końcowa w formacie ISO 8601 (domyślnie: teraz)

**Walidacja:**
- Daty nie mogą być w przyszłości (`@PastOrPresent`)
- `startDate` musi być przed `endDate`

**Response:** `SalesStatisticsDTO`

**Przykład:**
```bash
GET /api/statistics/sales?startDate=2024-01-01T00:00:00Z&endDate=2024-12-31T23:59:59Z
Authorization: Bearer {token}
```

**Response:**
```json
{
  "totalRevenue": 1250000.50,
  "totalOrders": 1250,
  "totalProductsSold": 5678,
  "averageOrderValue": 1000.00,
  "periodStart": "2024-01-01T00:00:00Z",
  "periodEnd": "2024-12-31T23:59:59Z"
}
```

**Pola:**
- `totalRevenue` - Całkowity przychód z zamówień w statusach: CONFIRMED, PROCESSING, SHIPPED, DELIVERED, COMPLETED
- `totalOrders` - Całkowita liczba zamówień
- `totalProductsSold` - Całkowita liczba sprzedanych produktów
- `averageOrderValue` - Średnia wartość zamówienia
- `periodStart` - Data początkowa okresu
- `periodEnd` - Data końcowa okresu

**Status codes:**
- `200 OK` - Statystyki sprzedaży
- `400 Bad Request` - Nieprawidłowe parametry
- `403 Forbidden` - Brak uprawnień OWNER

---

### 21.6 Statystyki sprzedaży miesięczne

**Endpoint:** `GET /api/statistics/sales/monthly`  
**Autoryzacja:** `ROLE_OWNER`

**Query Parameters:**
- `startDate` (Instant, optional) - Data początkowa w formacie ISO 8601 (domyślnie: 12 miesięcy temu)
- `endDate` (Instant, optional) - Data końcowa w formacie ISO 8601 (domyślnie: teraz)

**Walidacja:**
- Daty nie mogą być w przyszłości (`@PastOrPresent`)
- `startDate` musi być przed `endDate`

**Response:** `List<MonthlySalesDTO>`

**Przykład:**
```bash
GET /api/statistics/sales/monthly?startDate=2024-01-01T00:00:00Z&endDate=2024-12-31T23:59:59Z
Authorization: Bearer {token}
```

**Response:**
```json
[
  {
    "year": 2024,
    "month": 12,
    "monthName": "Grudzień",
    "totalRevenue": 150000.00,
    "totalOrders": 150,
    "totalProductsSold": 680
  },
  {
    "year": 2024,
    "month": 11,
    "monthName": "Listopad",
    "totalRevenue": 140000.00,
    "totalOrders": 140,
    "totalProductsSold": 620
  }
]
```

**Pola:**
- `year` - Rok
- `month` - Miesiąc (1-12)
- `monthName` - Nazwa miesiąca po polsku
- `totalRevenue` - Całkowity przychód w danym miesiącu
- `totalOrders` - Całkowita liczba zamówień w danym miesiącu
- `totalProductsSold` - Całkowita liczba sprzedanych produktów w danym miesiącu

**Status codes:**
- `200 OK` - Lista statystyk miesięcznych
- `400 Bad Request` - Nieprawidłowe parametry
- `403 Forbidden` - Brak uprawnień OWNER

---

### Modele danych Statistics API

#### TopProductDTO
```json
{
  "productId": 1,
  "productName": "Nazwa produktu",
  "productSku": "SKU-PRODUKTU",
  "totalQuantitySold": 100,
  "totalRevenue": 50000.00,
  "orderCount": 85
}
```

#### SalesStatisticsDTO
```json
{
  "totalRevenue": 1000000.00,
  "totalOrders": 1000,
  "totalProductsSold": 5000,
  "averageOrderValue": 1000.00,
  "periodStart": "2024-01-01T00:00:00Z",
  "periodEnd": "2024-12-31T23:59:59Z"
}
```

#### MonthlySalesDTO
```json
{
  "year": 2024,
  "month": 12,
  "monthName": "Grudzień",
  "totalRevenue": 150000.00,
  "totalOrders": 150,
  "totalProductsSold": 680
}
```

---

### Uwagi dotyczące Statistics API

- **Statusy zamówień:** Statystyki uwzględniają tylko zamówienia w statusach: CONFIRMED, PROCESSING, SHIPPED, DELIVERED, COMPLETED
- **Domyślne okresy:** Jeśli nie podano dat, używane są domyślne wartości (ostatnie 30 dni dla większości endpointów, ostatnie 12 miesięcy dla statystyk miesięcznych)
- **Walidacja dat:** System automatycznie sprawdza, czy `startDate` jest przed `endDate`
- **Precyzja liczbowa:** Wszystkie wartości finansowe są zaokrąglane do 2 miejsc po przecinku
- **Wydajność:** Statystyki są obliczane na podstawie zapytań do bazy danych, więc dla dużych okresów mogą wymagać więcej czasu

---

*Dokumentacja wygenerowana automatycznie 
