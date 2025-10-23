# Product API Documentation

## Przegląd
API produktów zapewnia pełną funkcjonalność CRUD oraz zaawansowane wyszukiwanie i filtrowanie produktów z obsługą paginacji i sortowania. System obsługuje dynamiczne atrybuty produktów, automatyczne generowanie SKU oraz integrację z kategoriami i atrybutami kategorii.

## Base URL
```
/api/products
```

## Autoryzacja
- **Publiczne endpointy** - dostępne dla wszystkich użytkowników
- **Owner endpointy** - wymagają roli `ROLE_OWNER`
- **Zabezpieczone endpointy**:
  - `POST /api/products` - tworzenie produktów
  - `PUT /api/products/{id}` - aktualizacja produktów  
  - `DELETE /api/products/{id}` - usuwanie produktów
  - `GET /api/products/active?isActive=false` - pobieranie nieaktywnych produktów

## Funkcjonalności
- ✅ Pełne CRUD operacje na produktach
- ✅ Dynamiczne atrybuty produktów (rozmiar, kolor, materiał, etc.)
- ✅ Automatyczne generowanie SKU na podstawie atrybutów
- ✅ Zaawansowane wyszukiwanie i filtrowanie
- ✅ Paginacja i sortowanie
- ✅ Integracja z kategoriami i atrybutami kategorii
- ✅ Operacje bulk dla atrybutów produktów
- ✅ Statystyki i analityka
- ✅ Bezpieczeństwo na poziomie metody

## Dostępne Metody

### 1. CRUD Operations (Owner only)

#### 1.1 Tworzenie produktu
**Endpoint:** `POST /api/products`
**Autoryzacja:** `ROLE_OWNER` required

**Parametry:**
```json
{
  "name": "string (required, max 255)",
  "description": "string (required)",
  "shortDescription": "string (max 255)",
  "price": "BigDecimal (required, > 0)",
  "vatRate": "BigDecimal (required, >= 0)",
  "shippingCost": "BigDecimal (>= 0)",
  "estimatedDeliveryTime": "string (max 100)",
  "thumbnailUrl": "string",
  "seoSlug": "string (required, max 255)",
  "categoryId": "Long (required)",
  "isFeatured": "Boolean (optional, default false)",
  "attributeValues": [
    {
      "categoryAttributeId": "Long (required)",
      "value": "String (required, max 255)"
    }
  ]
}
```

**Zwracany wynik:**
```json
{
  "id": 1,
  "name": "Laptop Gaming",
  "description": "Wysokiej klasy laptop do gier",
  "shortDescription": "Laptop gaming",
  "price": 2999.99,
  "sku": "ELE-LAP-15IN-1",
  "vatRate": 23.00,
  "isFeatured": false,
  "shippingCost": 0.00,
  "estimatedDeliveryTime": "2-3 dni",
  "thumbnailUrl": "https://example.com/image.jpg",
  "seoSlug": "laptop-gaming",
  "category": { /* Category object */ },
  "attributeValues": [ /* List of ProductAttributeValue objects */ ],
  "createdAt": "2024-01-01T10:00:00Z",
  "updatedAt": "2024-01-01T10:00:00Z",
  "deletedAt": null,
  "isActive": true
}
```

**Status codes:**
- `201 Created` - Produkt został utworzony
- `400 Bad Request` - Błędne dane wejściowe
- `401 Unauthorized` - Brak autoryzacji
- `403 Forbidden` - Brak uprawnień (wymagana rola OWNER)
- `404 Not Found` - Kategoria nie istnieje

---

#### 1.2 Aktualizacja produktu
**Endpoint:** `PUT /api/products/{id}`
**Autoryzacja:** `ROLE_OWNER` required

**Parametry:**
```json
{
  "name": "string (max 255)",
  "description": "string",
  "shortDescription": "string (max 255)",
  "price": "BigDecimal (> 0)",
  "vatRate": "BigDecimal (>= 0)",
  "shippingCost": "BigDecimal (>= 0)",
  "estimatedDeliveryTime": "string (max 100)",
  "thumbnailUrl": "string",
  "seoSlug": "string (max 255)",
  "categoryId": "Long",
  "isFeatured": "Boolean",
  "isActive": "Boolean"
}
```

**Zwracany wynik:**
```json
{
  "id": 1,
  "name": "Zaktualizowany Laptop Gaming",
  // ... reszta pól jak w create
}
```

**Status codes:**
- `200 OK` - Produkt został zaktualizowany
- `400 Bad Request` - Błędne dane wejściowe
- `401 Unauthorized` - Brak autoryzacji
- `403 Forbidden` - Brak uprawnień (wymagana rola OWNER)
- `404 Not Found` - Produkt lub kategoria nie istnieje

---

#### 1.3 Usuwanie produktu (soft delete)
**Endpoint:** `DELETE /api/products/{id}`
**Autoryzacja:** `ROLE_OWNER` required

**Parametry:**
- `id` (path parameter) - ID produktu

**Zwracany wynik:**
- `204 No Content` - Produkt został usunięty

**Status codes:**
- `204 No Content` - Produkt został usunięty
- `401 Unauthorized` - Brak autoryzacji
- `403 Forbidden` - Brak uprawnień (wymagana rola OWNER)
- `404 Not Found` - Produkt nie istnieje

---

### 2. Pobieranie pojedynczych produktów

#### 2.1 Pobieranie po ID
**Endpoint:** `GET /api/products/{id}`

**Parametry:**
- `id` (path parameter) - ID produktu

**Zwracany wynik:**
```json
{
  "id": 1,
  "name": "Laptop Gaming",
  // ... pełny obiekt ProductDTO
}
```

**Status codes:**
- `200 OK` - Produkt znaleziony
- `404 Not Found` - Produkt nie istnieje

---

#### 2.2 Pobieranie po SEO slug
**Endpoint:** `GET /api/products/slug/{seoSlug}`

**Parametry:**
- `seoSlug` (path parameter) - SEO slug produktu

**Zwracany wynik:**
```json
{
  "id": 1,
  "name": "Laptop Gaming",
  "seoSlug": "laptop-gaming",
  // ... pełny obiekt ProductDTO
}
```

**Status codes:**
- `200 OK` - Produkt znaleziony
- `404 Not Found` - Produkt nie istnieje

---

#### 2.3 Pobieranie po SKU
**Endpoint:** `GET /api/products/sku/{sku}`

**Parametry:**
- `sku` (path parameter) - SKU produktu

**Zwracany wynik:**
```json
{
  "id": 1,
  "name": "Laptop Gaming",
  "sku": "ELE-LAP-15IN-1",
  // ... pełny obiekt ProductDTO
}
```

**Status codes:**
- `200 OK` - Produkt znaleziony
- `404 Not Found` - Produkt nie istnieje

---

### 3. Listy produktów z paginacją i sortowaniem

#### 3.1 Wszystkie produkty
**Endpoint:** `GET /api/products`

**Parametry query:**
- `page` (int, default: 0) - Numer strony
- `size` (int, default: 10) - Rozmiar strony
- `sortBy` (string, default: "id") - Pole do sortowania
- `sortDir` (string, default: "asc") - Kierunek sortowania (asc/desc)

**Zwracany wynik:**
```json
{
  "content": [
    {
      "id": 1,
      "name": "Laptop Gaming",
      // ... pełny obiekt ProductDTO
    }
  ],
  "pageable": {
    "sort": {
      "sorted": true,
      "unsorted": false
    },
    "pageNumber": 0,
    "pageSize": 10
  },
  "totalElements": 100,
  "totalPages": 10,
  "first": true,
  "last": false,
  "numberOfElements": 10
}
```

**Status codes:**
- `200 OK` - Lista produktów

---

#### 3.2 Produkty według kategorii (ID)
**Endpoint:** `GET /api/products/category/{categoryId}`

**Parametry:**
- `categoryId` (path parameter) - ID kategorii
- `page` (int, default: 0) - Numer strony
- `size` (int, default: 10) - Rozmiar strony
- `sortBy` (string, default: "name") - Pole do sortowania
- `sortDir` (string, default: "asc") - Kierunek sortowania

**Zwracany wynik:**
```json
{
  "content": [ /* Lista produktów z kategorii */ ],
  "pageable": { /* Informacje o paginacji */ },
  "totalElements": 25,
  "totalPages": 3
}
```

**Status codes:**
- `200 OK` - Lista produktów z kategorii

---

#### 3.3 Produkty według kategorii (slug)
**Endpoint:** `GET /api/products/category-slug/{categorySlug}`

**Parametry:**
- `categorySlug` (path parameter) - SEO slug kategorii
- `page` (int, default: 0) - Numer strony
- `size` (int, default: 10) - Rozmiar strony
- `sortBy` (string, default: "name") - Pole do sortowania
- `sortDir` (string, default: "asc") - Kierunek sortowania

**Zwracany wynik:**
```json
{
  "content": [ /* Lista produktów z kategorii */ ],
  "pageable": { /* Informacje o paginacji */ },
  "totalElements": 25,
  "totalPages": 3
}
```

**Status codes:**
- `200 OK` - Lista produktów z kategorii

---

#### 3.4 Produkty według zakresu cen
**Endpoint:** `GET /api/products/price-range`

**Parametry:**
- `minPrice` (BigDecimal, required) - Minimalna cena
- `maxPrice` (BigDecimal, required) - Maksymalna cena
- `page` (int, default: 0) - Numer strony
- `size` (int, default: 10) - Rozmiar strony
- `sortBy` (string, default: "price") - Pole do sortowania
- `sortDir` (string, default: "asc") - Kierunek sortowania

**Zwracany wynik:**
```json
{
  "content": [ /* Lista produktów w zakresie cen */ ],
  "pageable": { /* Informacje o paginacji */ },
  "totalElements": 50,
  "totalPages": 5
}
```

**Status codes:**
- `200 OK` - Lista produktów w zakresie cen

---

#### 3.5 Produkty wyróżnione
**Endpoint:** `GET /api/products/featured`

**Parametry:**
- `isFeatured` (Boolean, default: true) - Czy produkt jest wyróżniony
- `page` (int, default: 0) - Numer strony
- `size` (int, default: 10) - Rozmiar strony
- `sortBy` (string, default: "name") - Pole do sortowania
- `sortDir` (string, default: "asc") - Kierunek sortowania

**Zwracany wynik:**
```json
{
  "content": [ /* Lista produktów wyróżnionych */ ],
  "pageable": { /* Informacje o paginacji */ },
  "totalElements": 15,
  "totalPages": 2
}
```

**Status codes:**
- `200 OK` - Lista produktów wyróżnionych

---

#### 3.6 Produkty aktywne/nieaktywne
**Endpoint:** `GET /api/products/active`
**Autoryzacja:** 
- `isActive=true` - publiczne
- `isActive=false` - wymagana rola `ROLE_OWNER`

**Parametry:**
- `isActive` (Boolean, default: true) - Czy produkt jest aktywny
- `page` (int, default: 0) - Numer strony
- `size` (int, default: 10) - Rozmiar strony
- `sortBy` (string, default: "name") - Pole do sortowania
- `sortDir` (string, default: "asc") - Kierunek sortowania

**Zwracany wynik:**
```json
{
  "content": [ /* Lista produktów aktywnych/nieaktywnych */ ],
  "pageable": { /* Informacje o paginacji */ },
  "totalElements": 80,
  "totalPages": 8
}
```

**Status codes:**
- `200 OK` - Lista produktów aktywnych/nieaktywnych
- `401 Unauthorized` - Brak autoryzacji (dla nieaktywnych produktów)
- `403 Forbidden` - Brak uprawnień (dla nieaktywnych produktów)

---

### 4. Wyszukiwanie i filtrowanie

#### 4.1 Wyszukiwanie po nazwie
**Endpoint:** `GET /api/products/search/name`

**Parametry:**
- `name` (string, required) - Nazwa do wyszukania
- `page` (int, default: 0) - Numer strony
- `size` (int, default: 10) - Rozmiar strony
- `sortBy` (string, default: "name") - Pole do sortowania
- `sortDir` (string, default: "asc") - Kierunek sortowania

**Zwracany wynik:**
```json
{
  "content": [ /* Lista produktów zawierających nazwę */ ],
  "pageable": { /* Informacje o paginacji */ },
  "totalElements": 5,
  "totalPages": 1
}
```

**Status codes:**
- `200 OK` - Lista znalezionych produktów

---

#### 4.2 Wyszukiwanie po opisie
**Endpoint:** `GET /api/products/search/description`

**Parametry:**
- `description` (string, required) - Opis do wyszukania
- `page` (int, default: 0) - Numer strony
- `size` (int, default: 10) - Rozmiar strony
- `sortBy` (string, default: "name") - Pole do sortowania
- `sortDir` (string, default: "asc") - Kierunek sortowania

**Zwracany wynik:**
```json
{
  "content": [ /* Lista produktów zawierających opis */ ],
  "pageable": { /* Informacje o paginacji */ },
  "totalElements": 8,
  "totalPages": 1
}
```

**Status codes:**
- `200 OK` - Lista znalezionych produktów

---

#### 4.3 Produkty według kategorii i zakresu cen
**Endpoint:** `GET /api/products/category/{categoryId}/price-range`

**Parametry:**
- `categoryId` (path parameter) - ID kategorii
- `minPrice` (BigDecimal, required) - Minimalna cena
- `maxPrice` (BigDecimal, required) - Maksymalna cena
- `page` (int, default: 0) - Numer strony
- `size` (int, default: 10) - Rozmiar strony
- `sortBy` (string, default: "price") - Pole do sortowania
- `sortDir` (string, default: "asc") - Kierunek sortowania

**Zwracany wynik:**
```json
{
  "content": [ /* Lista produktów z kategorii w zakresie cen */ ],
  "pageable": { /* Informacje o paginacji */ },
  "totalElements": 12,
  "totalPages": 2
}
```

**Status codes:**
- `200 OK` - Lista produktów z kategorii w zakresie cen

---

### 5. Zaawansowane filtrowanie

#### 5.1 Produkty według kategorii i statusu wyróżnienia
**Endpoint:** `GET /api/products/category/{categoryId}/featured`

**Parametry:**
- `categoryId` (path parameter) - ID kategorii
- `isFeatured` (Boolean, default: true) - Czy produkt jest wyróżniony
- `page` (int, default: 0) - Numer strony
- `size` (int, default: 10) - Rozmiar strony
- `sortBy` (string, default: "name") - Pole do sortowania
- `sortDir` (string, default: "asc") - Kierunek sortowania

**Zwracany wynik:**
```json
{
  "content": [ /* Lista produktów wyróżnionych z kategorii */ ],
  "pageable": { /* Informacje o paginacji */ },
  "totalElements": 3,
  "totalPages": 1
}
```

**Status codes:**
- `200 OK` - Lista produktów wyróżnionych z kategorii

---

#### 5.2 Produkty według zakresu cen i statusu wyróżnienia
**Endpoint:** `GET /api/products/price-range/featured`

**Parametry:**
- `minPrice` (BigDecimal, required) - Minimalna cena
- `maxPrice` (BigDecimal, required) - Maksymalna cena
- `isFeatured` (Boolean, default: true) - Czy produkt jest wyróżniony
- `page` (int, default: 0) - Numer strony
- `size` (int, default: 10) - Rozmiar strony
- `sortBy` (string, default: "price") - Pole do sortowania
- `sortDir` (string, default: "asc") - Kierunek sortowania

**Zwracany wynik:**
```json
{
  "content": [ /* Lista produktów wyróżnionych w zakresie cen */ ],
  "pageable": { /* Informacje o paginacji */ },
  "totalElements": 7,
  "totalPages": 1
}
```

**Status codes:**
- `200 OK` - Lista produktów wyróżnionych w zakresie cen

---

### 6. Statystyki

#### 6.1 Liczba produktów w kategorii
**Endpoint:** `GET /api/products/stats/category/{categoryId}/count`

**Parametry:**
- `categoryId` (path parameter) - ID kategorii

**Zwracany wynik:**
```json
25
```

**Status codes:**
- `200 OK` - Liczba produktów w kategorii

---

#### 6.2 Liczba produktów wyróżnionych
**Endpoint:** `GET /api/products/stats/featured/count`

**Parametry:**
- `isFeatured` (Boolean, default: true) - Czy produkt jest wyróżniony

**Zwracany wynik:**
```json
15
```

**Status codes:**
- `200 OK` - Liczba produktów wyróżnionych

---

#### 6.3 Liczba produktów aktywnych
**Endpoint:** `GET /api/products/stats/active/count`

**Parametry:**
- `isActive` (Boolean, default: true) - Czy produkt jest aktywny

**Zwracany wynik:**
```json
80
```

**Status codes:**
- `200 OK` - Liczba produktów aktywnych

---

## Przykłady użycia

### Pobieranie pierwszej strony produktów posortowanych według ceny malejąco
```bash
GET /api/products?page=0&size=10&sortBy=price&sortDir=desc
```

### Wyszukiwanie laptopów w kategorii Elektronika
```bash
GET /api/products/category/1?page=0&size=5&sortBy=name&sortDir=asc
```

### Produkty w zakresie cen 100-500 zł posortowane według ceny
```bash
GET /api/products/price-range?minPrice=100&maxPrice=500&sortBy=price&sortDir=asc
```

### Wyszukiwanie produktów zawierających "gaming" w nazwie
```bash
GET /api/products/search/name?name=gaming&sortBy=price&sortDir=asc
```

### Produkty wyróżnione z kategorii Elektronika
```bash
GET /api/products/category/1/featured?isFeatured=true&sortBy=name&sortDir=asc
```

## Obsługiwane pola sortowania

- `id` - ID produktu
- `name` - Nazwa produktu
- `price` - Cena
- `createdAt` - Data utworzenia
- `updatedAt` - Data aktualizacji
- `isFeatured` - Status wyróżnienia
- `isActive` - Status aktywności

## Obsługiwane kierunki sortowania

- `asc` - Rosnąco (domyślny)
- `desc` - Malejąco

## Obsługiwane statusy HTTP

- `200 OK` - Sukces
- `201 Created` - Zasób został utworzony
- `204 No Content` - Zasób został usunięty
- `400 Bad Request` - Błędne żądanie
- `404 Not Found` - Zasób nie został znaleziony
- `500 Internal Server Error` - Błąd serwera

---

## Product Attribute Values API

### Przegląd
System atrybutów produktów pozwala na dynamiczne definiowanie cech produktów (rozmiar, kolor, materiał, etc.) i ich wartości. Atrybuty są powiązane z kategoriami i mogą być używane do generowania SKU.

### Base URL
```
/api/product-attribute-values
```

### Główne endpointy
- `POST /api/product-attribute-values` - Tworzenie wartości atrybutu
- `POST /api/product-attribute-values/bulk` - Tworzenie wielu wartości (bulk)
- `PUT /api/product-attribute-values/{id}` - Aktualizacja wartości atrybutu
- `PUT /api/product-attribute-values/product/{productId}` - Aktualizacja wszystkich atrybutów produktu
- `DELETE /api/product-attribute-values/{id}` - Usuwanie wartości atrybutu
- `DELETE /api/product-attribute-values/product/{productId}` - Usuwanie wszystkich atrybutów produktu
- `GET /api/product-attribute-values/{id}` - Pobieranie wartości atrybutu po ID
- `GET /api/product-attribute-values/product/{productId}` - Pobieranie wszystkich atrybutów produktu
- `GET /api/product-attribute-values/category-attribute/{categoryAttributeId}` - Pobieranie wartości według atrybutu kategorii
- `GET /api/product-attribute-values/search/value?value=15.6` - Wyszukiwanie wartości
- `GET /api/product-attribute-values/attribute-type/{type}` - Pobieranie według typu atrybutu
- `GET /api/product-attribute-values/product/{productId}/key-attributes` - Kluczowe atrybuty produktu

### Przykład tworzenia wartości atrybutu
```json
{
  "productId": 1,
  "categoryAttributeId": 1,
  "value": "15.6 inch"
}
```

### Przykład odpowiedzi
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

### Przykład bulk operacji
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

### Walidacja atrybutów produktu
- `productId` - ID produktu (wymagane)
- `categoryAttributeId` - ID atrybutu kategorii (wymagane)
- `value` - Wartość atrybutu (wymagane, max 255 znaków)

### Uwagi techniczne
- Atrybuty produktów są automatycznie usuwane przy usuwaniu produktu
- Bulk operacje są bardziej wydajne dla dużej liczby atrybutów
- Wszystkie operacje CUD wymagają roli `ROLE_OWNER`
- Endpointy odczytu są publiczne
- Paginacja jest domyślnie ustawiona na 10 elementów na stronę
