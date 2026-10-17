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

## Polymorphism

**Verdict: violation** (V-02, V-03, V-04).

Where it is applied correctly: `PromotionService.findBestPromotionFor`
evaluates every promotion through `calculateDiscount`, without asking for
its type.

### V-02
| Field | Content |
|---|---|
| ID | V-02 |
| Pattern | Polymorphism (also Protected Variations) |
| Module | People (Workshop 1), Accessories (Req 1), Promotions (Req 2), Warranties (Req 4, A2) |
| Evidence | `PersonPersistence.toLine`, lines 76-87 (`instanceof Client` / `instanceof Seller`) and `fromLine`, lines 122-127 (`type.equals("CLIENT")` / `"SELLER"`) · `AccessoryRepository.toLine`, lines 80-96 and `fromLine`, lines 119-148 · `PromotionRepository.toLine`, lines 83-94 and `fromLine`, lines 119-132 · `WarrantyRepository.toLine`, line 74: `w instanceof BasicWarranty ? "BASIC" : "EXTENDED"` and `WarrantyService.resolveWarranties`, lines 63-67: `if (record.type().equals("BASIC"))` |
| Explanation | In every repository the format of each type is chosen with a chain of conditionals on the class (to write) and on a type code (to read). The behavior varies by type, but it is not assigned to an object per type. |
| Consequence | Adding a type (a new person role, accessory, promotion, or warranty) requires editing two chains in the repository; if one is forgotten, the object is written as an empty line or ignored when loading, and the data is lost without any error. `WarrantyRepository` would even store a third warranty type as `EXTENDED`. |
| Proposed solution | Apply Polymorphism through Pure Fabrication: keep the format in the persistence layer, but register one format per type in each repository (keyed by class to write and by type code to read), so a new type is a new registration instead of a new branch. |
| Other member's code | No. |

### V-03
| Field | Content |
|---|---|
| ID | V-03 |
| Pattern | Polymorphism |
| Module | Accessories (Req 1) |
| Evidence | `AccessoryService.findAccessoriesCompatibleWith`, lines 130-135: `if (accessory instanceof Controller controller && controller.isCompatibleWith(consoleId)) {...} else if (accessory instanceof Memory memory && memory.isCompatibleWith(consoleId))` · `AccessoryService.addCompatibleConsole`, lines 218-224: the same two branches |
| Explanation | The service asks for the concrete class of each accessory to know whether it supports console compatibility, and repeats the same call in each branch. |
| Consequence | A new accessory type with compatibility requires a new branch in both methods; if one is forgotten, the accessory can be registered as compatible but is never found (or the other way around). |
| Proposed solution | Apply Polymorphism: use one type for every accessory that supports compatibility, and ask only for that type. |
| Other member's code | Yes. The accessory hierarchy (`Controller`, `Memory`) is Developer 1's. Reported to the Technical Leader. |

### V-04
| Field | Content |
|---|---|
| ID | V-04 |
| Pattern | Polymorphism |
| Module | Warranties (Req 4) and Returns (A7) |
| Evidence | `WarrantyService.validateConsole`, lines 132-136: `if (!(product instanceof Console))` · `ReturnService.registerReturn`, lines 197-199: `if (product instanceof Console) { warrantyRefund += warrantyService.cancelWarranties(...); }` |
| Explanation | Whether a product receives a warranty is decided by asking whether it is a `Console`. That is a property that varies by product type, so the product should answer it. |
| Consequence | If the store starts giving warranties to another product type, every `instanceof Console` in the system must be found and changed; the same check also exists in `SaleService` and `ConsoleUI`. |
| Proposed solution | Apply Polymorphism: `Product` answers whether it receives a warranty, and the services ask that instead of checking the class. |
| Other member's code | Yes. `Product` and `Console` are Developer 1's classes; `SaleService` and `ConsoleUI` are the Technical Leader's. Reported to the Technical Leader. |


