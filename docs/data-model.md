# Product Service – model danych

Dokument opisuje model danych Product Service i uzasadnia decyzje projektowe.
Nie opisuje endpointów (patrz `api-requirements.md`) ani logiki serwisu.

Diagram klas: [`diagrams/product-service.puml`](diagrams/product-service.puml)

---

## 1. Zasady wspólne dla wszystkich serwisów systemu

| Zasada | Decyzja | Dlaczego |
|---|---|---|
| Kwoty | `BigDecimal` (`precision = 12, scale = 2`) | `double`/`float` dają błędy zaokrągleń, niedopuszczalne przy pieniądzach |
| Czas | `Instant` | `LocalDateTime` nie ma strefy czasowej; daty muszą być jednoznaczne między serwerami |
| Daty techniczne | `createdAt` (`@CreationTimestamp`), `updatedAt` (`@UpdateTimestamp`) | diagnostyka, sortowanie, audyt |
| Współbieżność | `@Version` na encjach edytowanych równolegle | optimistic locking – równoczesne zmiany nie nadpiszą się po cichu |
| Statusy | `enum` jako `EnumType.STRING` | `ORDINAL` psuje dane przy zmianie kolejności wartości |
| Referencje między serwisami | zwykłe pole `Long`, **bez klucza obcego** | każdy serwis ma własną bazę; spójność zapewnia API |
| Dane historyczne | kopia (snapshot) w chwili zdarzenia | zmiana ceny czy nazwy nie może zmieniać przeszłych zamówień |

Wzorcem odniesienia był model danych Shopify (GraphQL Admin API). Różnice są opisane poniżej.

---

## 2. Encje

| Encja | Rola |
|---|---|
| `Category` | drzewo kategorii (`parent` → `children`), np. Elektronika → Komputery → Laptopy |
| `Product` | jeden model dla wszystkich rodzajów towarów |
| `ProductStatus` | stan produktu w katalogu: `DRAFT`, `ACTIVE`, `ARCHIVED` |
| `ProductOption` | opcja wybierana przez klienta, np. „RAM”, „Procesor” |
| `ProductOptionValue` | konkretna wartość opcji z dopłatą, np. „32GB, +800 zł” |
| `ProductVariant` | gotowa kombinacja opcji z własną ceną, SKU i stanem magazynowym |
| `ProductAttribute` | stały parametr do filtrowania, np. „Przekątna: 15,6"” |

---

## 3. Decyzje

### 3.1 Jeden `Product` zamiast klasy na każdy typ

**Decyzja:** jedna encja `Product`; rodzaj produktu wyznacza relacja do `Category`.

**Dlaczego:**
- Shopify robi tak samo: jeden obiekt `Product`, a typ to pole (`productType`, `category`).
- Nowa kategoria (np. tablety) to rekord w bazie, a nie nowa klasa, migracja i wdrożenie.
- Odrzucona alternatywa: `enum ProductType` z logiką `if (type == COMPUTER)` – każda nowa kategoria wymagałaby zmiany kodu.

### 3.2 Dwa mechanizmy wyboru wersji produktu

| Rodzaj | Przykład | Mechanizm | Cena |
|---|---|---|---|
| składany na zamówienie | komputer, laptop | `ProductOption` + `ProductOptionValue.priceDelta` | `basePrice + Σ priceDelta` |
| gotowy towar z półki | iPad, telefon | `ProductVariant` | `variant.price` |
| bez wersji | słuchawki, kabel | brak opcji i wariantów | `basePrice` |

Flaga `Product.configurable` określa, który mechanizm obowiązuje.

**Dlaczego nie same warianty (jak w Shopify):**
w Shopify każdy wariant to kombinacja z ręcznie wpisaną pełną ceną. W konfiguratorze liczba wariantów rośnie wykładniczo
(3 CPU × 4 RAM × 3 dyski × 2 GPU = 72), a zmiana ceny jednego komponentu wymaga edycji dziesiątek rekordów.
Model z dopłatami rośnie liniowo (3 + 4 + 3 + 2 = 12 wartości).

**Dlaczego nie same dopłaty:**
gotowy towar (iPad 256GB) ma własny kod EAN i stan magazynowy, a model z dopłatami nie śledzi stanu konkretnej kombinacji.

Oba mechanizmy korzystają z tych samych tabel opcji: wariant to zestaw `ProductOptionValue`
(tabela łącząca `variant_option_values`), a produkt konfigurowalny używa ich bezpośrednio z dopłatami.

### 3.3 Opcje a parametry

**Decyzja:** osobna encja `ProductAttribute`.

**Dlaczego:** opcja to wybór klienta wpływający na cenę; parametr to stała cecha do filtrowania.
Mieszanie ich prowadzi do „opcji” z dopłatą 0 zł i jedną wartością, które zaśmiecają konfigurator.

### 3.4 Stawka VAT

**Decyzja:** pole `Product.vatRate` (`BigDecimal`, np. `23.00`).

**Dlaczego:** polska faktura wymaga stawki VAT dla każdej pozycji. Stawka należy do produktu, a nie jest stałą w kodzie.
Order Service kopiuje ją do pozycji zamówienia w chwili zakupu.

Ceny są cenami **brutto** (sprzedaż B2C); kwoty netto i VAT wylicza się na fakturze.

### 3.5 Cena i kopie danych

Product Service jest **jedynym źródłem cen** w systemie:
- Cart Service pobiera aktualne ceny przy każdym odczycie koszyka (ceny „żywe”),
- Order Service pobiera je raz, przy składaniu zamówienia, i zapisuje kopię (ceny „zamrożone”).

Dlatego Product Service nie musi przechowywać historii cen – historię przechowują zamówienia.

### 3.6 Status produktu zamiast usuwania

**Decyzja:** pole `Product.status` (`ProductStatus`: `DRAFT`, `ACTIVE`, `ARCHIVED`); produktów nie usuwamy fizycznie.

| Status | Widoczny w katalogu | Można dodać do koszyka | Pozycja już w koszyku |
|---|---|---|---|
| `DRAFT` | nie | nie | – (nie mogła trafić do koszyka) |
| `ACTIVE` | tak | tak | normalna |
| `ARCHIVED` | nie | nie | oznaczona jako niedostępna, nie przejdzie do zamówienia |

**Dlaczego:**
- koszyki i zamówienia przechowują `productId`, `variantId` i `optionValueId`; fizyczne usunięcie produktu
  zamieniłoby te referencje w „wiszące” id, a Product Service nie umiałby odróżnić „nigdy nie istniał” od „wycofany”;
- `DRAFT` pozwala przygotować produkt (opcje, warianty, parametry) przed publikacją;
- Shopify stosuje ten sam podział: `ACTIVE`, `ARCHIVED`, `DRAFT`.

Zamówienia nie zależą od statusu – przechowują kopię danych z chwili zakupu.

---

## 4. Otwarte kwestie

| Temat | Stan |
|---|---|
| Wycofanie pojedynczej opcji lub wariantu (np. RAM 8GB niedostępny, produkt nadal w sprzedaży) | status jest na poziomie produktu; może być potrzebna flaga `active` na `ProductOptionValue` i `ProductVariant` |
| Promocje i ceny promocyjne | poza zakresem; mogą wymagać osobnej encji |
| Reguły zgodności komponentów (np. CPU ↔ płyta główna) | poza zakresem |
| Stany magazynowe komponentów konfigurowalnych | poza zakresem |