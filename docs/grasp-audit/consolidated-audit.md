# Consolidated GRASP Audit — GameZone Unicesar

Audited commit of `develop`: **`9dc5282`** (merge of Pull Request #45, end
of Requirement 6). Every line number refers to that commit.

Source reports:
- `docs/grasp-audit/leader-audit.md` — Technical Leader
- `docs/grasp-audit/developer1-audit.md` — Developer 1
- `docs/grasp-audit/developer2-audit.md` — Developer 2

Names used in the code: `ProductPersistence`, `PersonPersistence`, and
`SalePersistence` are the specification's `ProductRepository`,
`PersonRepository`, and `SaleRepository`; `Client` is `Customer`; and
`ConsoleUI` is `ConsoleMenu`.

## Coverage

Every class of the system was audited by the member who developed it.
When another member added methods to a class in a later requirement,
those methods were audited by their author, as the rules of Requirement 7
establish; the table lists those parts explicitly so that no part was
audited twice or left out.

| Class | Package | Audited by | Parts audited by another member |
|---|---|---|---|
| `Main` | — | Technical Leader | — |
| `ConsoleUI` | ui | Technical Leader | — |
| `Sale` | model | Technical Leader | `canBeReturned` (Developer 1) |
| `SalePersistence` | persistence | Technical Leader | `PersistenceException` handling, `parseLine` (Developer 2) |
| `SaleService` | service | Technical Leader | Validator calls, typed exceptions, `getSaleById` (Developer 2) |
| `Product`, `VideoGame`, `Console` | model | Developer 1 | Constructor validation of `Product` (Technical Leader) |
| `ProductPersistence` | persistence | Developer 1 | `PersistenceException` handling, `parseLine` (Developer 2) |
| `ProductService` | service | Developer 1 | `restoreStock` (Technical Leader); validator calls, `getProductById` (Developer 2) |
| `Accessory`, `Controller`, `Cable`, `Memory` | model | Developer 1 | Constructor validation of `Accessory` (Technical Leader) |
| `Promotion`, `PercentageDiscount`, `CategoryDiscount`, `BulkPurchaseDiscount` | model | Developer 1 | — |
| `Return` | model | Developer 1 | Returned accessories (A4) and warranty refund (A7) (Developer 2) |
| `Warranty`, `BasicWarranty`, `ExtendedWarranty` | model | Developer 1 | — |
| `GameZoneException`, `ResourceNotFoundException`, `BusinessRuleException`, `InvalidDataException`, `PersistenceException` | exceptions | Developer 1 | — |
| `ProductValidator`, `PersonValidator`, `SaleValidator` | validation | Developer 1 | — |
| `Person`, `Client`, `Seller` | model | Developer 2 | — |
| `PersonPersistence`, `PersonService` | persistence, service | Developer 2 | — |
| `AccessoryRepository`, `AccessoryService` | persistence, service | Developer 2 | — |
| `PromotionRepository`, `PromotionService` | persistence, service | Developer 2 | — |
| `ReturnRepository`, `ReturnRecord`, `ReturnService` | persistence, service | Developer 2 | — |
| `WarrantyRepository`, `WarrantyRecord`, `WarrantyService` | persistence, service | Developer 2 | — |

Total: 42 classes, all audited. The three reports were checked for
duplicated parts; none was audited twice.

## Validation of the evidence

Every finding of the three reports cites the class, the method, and the
exact lines in `9dc5282`, and every line was checked against that commit.
Every "correct" verdict names the class and method where the pattern is
applied, and "not applicable" was not used for any whole report. No
finding had to be returned to its author.

## Team verdict per pattern

| Pattern | Leader | Developer 1 | Developer 2 | Team verdict |
|---|---|---|---|---|
| Information Expert | violation | violation | violation | **Violation** (V-01, V-02) |
| Creator | correct | correct | correct | **Correct** |
| Controller | correct | correct | correct | **Correct** |
| Low Coupling | correct | correct | correct | **Correct** |
| High Cohesion | violation | correct | correct | **Violation** (V-08) |
| Polymorphism | violation | violation | violation | **Violation** (V-03, V-04, V-05, V-06, V-07) |
| Pure Fabrication | violation | violation | correct | **Violation** (V-09) |
| Indirection | correct | correct | correct | **Correct** |
| Protected Variations | correct | violation | violation | **Violation** (covered by V-03, V-06, V-07) |

