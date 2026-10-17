# GRASP Audit — Developer 2 (Ednilson Contreras Buelvas)

Audited commit of `develop`: `9dc5282` (merge of Pull Request #45, end of
Requirement 6). Every line number in this report refers to that commit.

## Scope

According to the responsibility table of Requirement 7 and the commit
history, I audit the repositories and services of each module, the person
module, and the use of exceptions and validators that I added in
Requirement 6.

| Module (requirement) | Class | Audited |
|---|---|---|
| People (Workshop 1) | `Person`, `Client` (the specification's `Customer`), `Seller` | Whole classes |
| People (Workshop 1) | `PersonPersistence` (the specification's `PersonRepository`), `PersonService` | Whole classes |
| Accessories (Req 1, A4) | `AccessoryRepository`, `AccessoryService` | Whole classes, including `restoreStock` (A4) |
| Promotions (Req 2) | `PromotionRepository`, `PromotionService` | Whole classes, including the selection of the best promotion |
| Returns (Req 3, A4, A6, A7, refactor) | `ReturnRepository`, `ReturnRecord`, `ReturnService` | Whole classes, including the monthly balance and the warranty cancellation |
| Returns (A4, A7) | `Return` | The returned accessories and the warranty refund |
| Warranties (Req 4, A2, A7) | `WarrantyRepository`, `WarrantyRecord`, `WarrantyService` | Whole classes, including `cancelWarranties` |
| Exceptions and validations (Req 6) | `ProductService`, `PersonService`, `SaleService` | The calls to the validators, the typed exceptions, and the methods `getProductById`, `getClientById`, `getSellerById`, `getSaleById` |
| Exceptions and validations (Req 6) | `ProductPersistence`, `PersonPersistence`, `SalePersistence` | The `PersistenceException` handling and `parseLine` |

## Information Expert

**Verdict: violation** (V-01).

Where it is applied correctly:
- `ReturnService.calculateMonthlyReturns` (lines 369-378) asks each
  `Return` for its refund (`getRefundAmount()`) instead of calculating it.
- `PromotionService.findBestPromotionFor` (lines 141-156) asks each
  promotion for its own discount (`calculateDiscount(sale)`).
- `WarrantyService.listActiveWarranties` (lines 195-204) asks each
  warranty whether it is active (`isActive(today)`).

### V-01
| Field | Content |
|---|---|
| ID | V-01 |
| Pattern | Information Expert |
| Module | Accessories (Req 1, adjustment A4) |
| Evidence | `AccessoryService.hasEnoughStock`, line 184: `return accessory != null && accessory.getStock() >= amount;` · `AccessoryService.reduceStock`, lines 198 and 201: `accessory.getStock() < amount` and `accessory.setStock(accessory.getStock() - amount);` · `AccessoryService.restoreStock`, line 245: `accessory.setStock(accessory.getStock() + quantity);` |
| Explanation | The stock belongs to `Accessory`, but the service reads it and writes it back to check and change it. The class that has the information does not do the work. |
| Consequence | The stock rule is repeated in three methods of this class and, in the same way, in `ProductService`. A change in the rule must be repeated everywhere. |
| Proposed solution | Apply Information Expert: `Accessory` offers `hasEnoughStock`, `decreaseStock`, and `increaseStock`, and the service calls them. |
| Other member's code | Yes. `Accessory` is Developer 1's class; the methods must be added there first. Reported to the Technical Leader. |

## Creator

**Verdict: correct.**

- `PersonService.registerClient` creates the `Client` (line 50) and
  `registerSeller` creates the `Seller` (line 71): the service has the
  data to initialize them and keeps the list where they are stored.
- `WarrantyService` creates `BasicWarranty` and `ExtendedWarranty`
  (lines 101 and 119) and records them in its list.
- `ReturnService.registerReturn` creates the `Return` (lines 204-212)
  with the sale and the items it has just validated.
- `PromotionService` and `AccessoryService` create each promotion and
  accessory they register.

## Controller

**Verdict: correct.**

Each service is the first object after the user interface that receives
the operations of its module. `ReturnService.registerReturn` is the
clearest case: it coordinates the validation of the sale, the warranty
cancellation (`WarrantyService`), the stock restoration (`ProductService`
and `AccessoryService`), and the persistence, without any console code.

## Low Coupling

**Verdict: correct.**

- No persistence class depends on a service: `ReturnRepository` and
  `WarrantyRepository` store identifiers and return records
  (`ReturnRecord`, `WarrantyRecord`), and the services resolve them.
- `WarrantyService` uses `SalePersistence` instead of `SaleService`, which
  avoids the circular dependency `SaleService → WarrantyService →
  SaleService` (adjustment A2).
- The services that use the validators depend on them only through static
  calls, and the validators do not depend on the services.

## High Cohesion

**Verdict: correct.**

Each repository reads and writes one file, and each service manages one
module. `ReturnService` also calculates the monthly balance (lines
349-393), which combines sales and returns; this location was required by
adjustment A6 and only adds three short methods that use the returns the
class already manages, so the class stays focused on returns.

