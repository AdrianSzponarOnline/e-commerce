# E-commerce Platform - Instrukcja Uruchomienia i Użytkowania

## Opis Projektu

Platforma e-commerce zbudowana w Spring Boot 3.5.6 z wykorzystaniem PostgreSQL, Spring Security, JWT i OpenAPI. Projekt zawiera system autoryzacji, zarządzanie użytkownikami, produktami, kategoriami, zamówieniami i płatnościami.

## Wymagania Systemowe

- **Java 17** lub nowsza
- **Maven 3.6+**
- **PostgreSQL 12+**
- **IDE** (IntelliJ IDEA, Eclipse, VS Code)

## Instalacja i Konfiguracja

### 1. Instalacja PostgreSQL

1. Pobierz i zainstaluj PostgreSQL z [oficjalnej strony](https://www.postgresql.org/download/)
2. Podczas instalacji zapamiętaj hasło dla użytkownika `postgres`
3. Uruchom PostgreSQL i upewnij się, że działa na porcie 5432

### 2. Konfiguracja Bazy Danych

1. Otwórz pgAdmin lub psql
2. Utwórz nową bazę danych:
```sql
CREATE DATABASE ecommerce_db;
```

### 3. Konfiguracja Aplikacji

1. Skopiuj plik `application-example.properties` do `application.properties`:
```bash
cp src/main/resources/application-example.properties src/main/resources/application.properties
```

2. Edytuj plik `src/main/resources/application.properties` i ustaw:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/ecommerce_db
spring.datasource.username=postgres
spring.datasource.password=TWOJE_HASLO_POSTGRES

# Konfiguracja JWT
security.jwt.secret-key=3cfa76ef14937c1c0ea519f8fc057a80fcd04a7420f8e8bcd0a7567c272e007b
security.jwt.expiration-time=3600000
```

Zastąp `TWOJE_HASLO_POSTGRES` rzeczywistym hasłem do bazy danych.

**Uwaga**: W środowisku produkcyjnym należy wygenerować nowy, bezpieczny klucz JWT zamiast używać domyślnego.

## Uruchomienie Aplikacji

### Metoda 1: Przez IDE

1. Otwórz projekt w swoim IDE
2. Znajdź klasę `ECommerceApplication.java`
3. Kliknij prawym przyciskiem i wybierz "Run ECommerceApplication"

### Metoda 2: Przez Maven

```bash
# W katalogu głównym projektu
mvn spring-boot:run
```

### Metoda 3: JAR

```bash
# Kompilacja
mvn clean package

# Uruchomienie
java -jar target/E-commerce-0.0.1-SNAPSHOT.jar
```

## Dostęp do Aplikacji

- **Aplikacja**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **API Documentation**: http://localhost:8080/v3/api-docs

## Struktura API

### Endpointy Autoryzacji (`/api/auth`)

#### 1. Rejestracja Użytkownika
```http
POST /api/auth/register
Content-Type: application/json

{
    "email": "jan.kowalski@example.com",
    "password": "haslo123"
}
```

#### 2. Logowanie
```http
POST /api/auth/login
Content-Type: application/json

{
    "email": "jan.kowalski@example.com",
    "password": "haslo123"
}
```

**Odpowiedź:**
```json
{
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "type": "Bearer"
}
```

#### 3. Informacje o Zalogowanym Użytkowniku
```http
GET /api/auth/me
Authorization: Bearer YOUR_JWT_TOKEN
```

## Przykłady Użycia

### 1. Rejestracja Nowego Użytkownika

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "anna.nowak@example.com",
    "password": "bezpiecznehaslo123"
  }'
```

### 2. Logowanie

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "anna.nowak@example.com",
    "password": "bezpiecznehaslo123"
  }'
```

### 3. Sprawdzenie Informacji o Użytkowniku

```bash
curl -X GET http://localhost:8080/api/auth/me \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

## Struktura Bazy Danych

Aplikacja automatycznie tworzy następujące tabele:

- **users** - użytkownicy
- **roles** - role użytkowników
- **user_roles** - przypisania ról
- **addresses** - adresy użytkowników
- **categories** - kategorie produktów
- **products** - produkty
- **orders** - zamówienia
- **order_items** - pozycje zamówień
- **payments** - płatności
- **product_images** - zdjęcia produktów
- **newsletter_subscriptions** - subskrypcje newslettera

## Domyślne Dane

Po uruchomieniu aplikacji zostaną utworzone:

- **Role**: `ROLE_USER`, `ROLE_OWNER`
- **Użytkownik testowy**: `user1@example.com` (hasło: `password`)

## Bezpieczeństwo

- Aplikacja używa JWT do autoryzacji
- Hasła są hashowane za pomocą BCrypt
- Token JWT wygasa po 1 godzinie (3600000 ms)
- Klucz JWT jest skonfigurowany w `application.properties`
- Wszystkie endpointy (oprócz rejestracji i logowania) wymagają autoryzacji

## Rozwiązywanie Problemów

### Problem: Błąd połączenia z bazą danych
- Sprawdź czy PostgreSQL jest uruchomiony
- Zweryfikuj dane połączenia w `application.properties`
- Upewnij się, że baza danych `ecommerce_db` istnieje

### Problem: Port 8080 zajęty
- Zmień port w `application.properties`:
```properties
server.port=8081
```

### Problem: Błędy kompilacji
- Upewnij się, że masz Java 17
- Wykonaj `mvn clean install`

## Rozwój

### Dodawanie Nowych Endpointów

1. Utwórz nowy kontroler w pakiecie `controller`
2. Dodaj odpowiednie serwisy w pakiecie `service`
3. Zdefiniuj DTO w pakiecie `dto`
4. Dodaj testy w pakiecie `test`

### Struktura Pakietów

```
src/main/java/com/ecommerce/E_commerce/
├── config/          # Konfiguracja (Security, JWT)
├── controller/      # Kontrolery REST
├── dto/            # Data Transfer Objects
├── exception/      # Obsługa wyjątków
├── model/          # Encje JPA
├── repository/     # Repozytoria danych
└── service/        # Logika biznesowa
```

## Wsparcie

W przypadku problemów:
1. Sprawdź logi aplikacji
2. Zweryfikuj konfigurację bazy danych
3. Upewnij się, że wszystkie zależności są zainstalowane

---

