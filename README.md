# Product Service

Mikroserwis katalogu produktów sklepu e-commerce z elektroniką.

Odpowiada za:
- kategorie i produkty (komputery, laptopy, tablety, elektronika ogólna),
- konfigurację produktów (opcje z dopłatami, np. RAM, procesor),
- warianty magazynowe (gotowe wersje produktów z własnym SKU i stanem),
- parametry produktów do filtrowania,
- wyliczanie cen i dostarczanie danych do koszyka i zamówień.

## Miejsce w systemie

Frontend nie komunikuje się z serwisem bezpośrednio – wszystkie żądania przechodzą przez BFF.
Z Product Service korzystają również Cart Service i Order Service.

Diagram architektury: [`docs/diagrams/architecture.puml`](docs/diagrams/architecture.puml)

## Dokumentacja

| Dokument | Zawartość |
|---|---|
| [`docs/data-model.md`](docs/data-model.md) | model danych i uzasadnienie decyzji |
| [`docs/api-requirements.md`](docs/api-requirements.md) | czego od serwisu potrzebują inne serwisy |
| [`docs/diagrams/product-service.puml`](docs/diagrams/product-service.puml) | diagram klas modelu |

## Stack

Java 21 · Spring Boot 4.1 · PostgreSQL · Flyway · Keycloak (JWT) · logi JSON do ELK · GitHub Actions

## Sposób pracy

Repozytorium działa w oparciu o **GitHub Flow**:

1. gałąź `main` jest zawsze stabilna,
2. każda funkcja powstaje na osobnej gałęzi z `main` (`feature/...`, `fix/...`),
3. zmiany trafiają do `main` wyłącznie przez Pull Request z zielonym CI i review.