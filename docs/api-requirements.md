# Product Service – wymagania innych serwisów

Dokument opisuje, czego od Product Service potrzebują pozostałe serwisy.
To wymagania, a nie ostateczny kontrakt API – szczegóły (ścieżki, kształt JSON) mogą się zmienić w implementacji.

---

## 1. Kto korzysta z serwisu

| Serwis | Po co | Kiedy |
|---|---|---|
| BFF | katalog, szczegóły produktu, konfigurator | przeglądanie sklepu przez klienta |
| Cart Service | walidacja konfiguracji, aktualne ceny i nazwy | dodanie do koszyka, każdy odczyt koszyka |
| Order Service | walidacja, ceny, nazwy, SKU, `vatRate` do zamrożenia | tylko przy składaniu zamówienia |

---

## 2. Wymagane możliwości

### 2.1 Odczyt produktu (BFF)

- pobranie produktu po `id` z opcjami, wartościami, wariantami i parametrami,
- lista produktów z paginacją i filtrem po kategorii,
- katalog dla klienta zwraca wyłącznie produkty `ACTIVE`; produkty `DRAFT` i `ARCHIVED` widzi tylko administrator.

### 2.2 Odczyt wielu produktów naraz (Cart Service)

Koszyk z 15 pozycjami nie może wykonywać 15 osobnych zapytań.
Serwis musi przyjąć listę `id` i zwrócić wszystkie produkty w jednej odpowiedzi.

### 2.3 Wycena konfiguracji (Cart Service, Order Service)

Najważniejsza operacja. Wejście – lista pozycji:

| Pole | Opis |
|---|---|
| `productId` | wymagane |
| `variantId` | dla produktów z wariantami, inaczej `null` |
| `optionValueIds` | dla produktów konfigurowalnych, inaczej puste |

Serwis dla każdej pozycji:
1. sprawdza, czy produkt istnieje i ma status `ACTIVE`,
2. sprawdza, czy wariant należy do produktu,
3. sprawdza, czy wartości opcji należą do produktu,
4. sprawdza, czy wybrano wszystkie opcje z `required = true` i dokładnie jedną wartość z każdej opcji,
5. wylicza cenę jednostkową: `basePrice + Σ priceDelta` albo `variant.price`.

Wyjście – dla każdej pozycji dane potrzebne do wyświetlenia i zamrożenia:

| Pole | Kto używa |
|---|---|
| `productName` | Cart, Order |
| `sku` (dla wariantów) | Order |
| `unitPrice` | Cart, Order |
| `vatRate` | Order |
| wybrane opcje: nazwa opcji, nazwa wartości, `priceDelta` | Cart, Order |
| status walidacji / błąd | Cart, Order |

Jedna operacja wsadowa obsługuje oba serwisy: koszyk używa jej przy odczycie, a zamówienie – przy składaniu.

**Dlaczego wycena jest tutaj, a nie w Cart/Order:** reguły ceny (dopłaty, warianty, w przyszłości promocje)
muszą być w jednym miejscu. Gdyby każdy serwis liczył cenę sam, przy pierwszej zmianie reguł ceny w koszyku
i w zamówieniu zaczęłyby się różnić.

---

## 3. Błędy

| Sytuacja | Odpowiedź |
|---|---|
| produkt nie istnieje | `404` |
| produkt ma status `DRAFT` lub `ARCHIVED` | `422` – produkt niedostępny; w operacji wsadowej pozycja oznaczona jako niedostępna (koszyk pokazuje ją klientowi, zamówienie odrzuca) |
| niepoprawna konfiguracja (obca opcja, brak wymaganej, wariant innego produktu) | `422` z opisem, która pozycja i dlaczego |
| w operacji wsadowej część pozycji błędna | wynik per pozycja – jedna zła pozycja nie blokuje odczytu całego koszyka |

---

## 4. Bezpieczeństwo

- każde wywołanie zawiera JWT z Keycloak (przekazany przez BFF lub token serwisowy),
- serwis waliduje token lokalnie (klucz publiczny z JWKS),
- odczyt katalogu jest dostępny dla każdego zalogowanego użytkownika; zmiany w katalogu wymagają roli administratora.