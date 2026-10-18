# Consolidated SOLID Audit — GameZone Unicesar

Audited commit of `develop`: **`75cc180`** (merge of Pull Request #51, end
of Requirement 7). Every line number refers to that commit.

Source reports:
- `docs/solid-audit/leader-audit.md` — Technical Leader
- `docs/solid-audit/developer1-audit.md` — Developer 1
- `docs/solid-audit/developer2-audit.md` — Developer 2

Names used in the code: `ProductPersistence`, `PersonPersistence`, and
`SalePersistence` are the specification's `ProductRepository`,
`PersonRepository`, and `SaleRepository`; `Client` is `Customer`; and
`ConsoleUI` is `ConsoleMenu`, divided in Requirement 7 into `ConsoleUI`
(main menu), seven submenu classes, `ConsoleInput`, and `ErrorHandler`.

## Coverage

Every class of the system was audited by the member who developed it,
following the responsibility table of Requirement 8. The classes created
in the GRASP correction were assigned with `docs/grasp-refactor-log.md`,
which records who made each change. When another member added methods to
a class in a later requirement, those methods were audited by their
author; the table lists those parts so that no part was audited twice or
left out.

| Class | Package | Audited by | Parts audited by another member |
|---|---|---|---|
| `Main` | — | Technical Leader | — |
| `ConsoleUI` | ui | Technical Leader | — |
| `ConsoleInput`, `ErrorHandler` (created in Req 7) | ui | Technical Leader | — |
| `ProductMenu`, `PersonMenu`, `SaleMenu`, `AccessoryMenu`, `PromotionMenu`, `ReturnMenu`, `WarrantyMenu` (created in Req 7) | ui | Technical Leader | — |
| `CurrencyFormatter` (created in Req 7) | util | Technical Leader | — |
| `Sale` | model | Technical Leader | `canBeReturned` (Developer 1) |
| `SalePersistence` | persistence | Technical Leader | `PersistenceException` handling, `parseLine` (Developer 2) |
| `SaleService` | service | Technical Leader | Validator calls, typed exceptions, `getSaleById` (Developer 2) |
| `Product`, `VideoGame`, `Console` | model | Developer 1 | Constructor validation of `Product` (Technical Leader) |
| `ProductPersistence` | persistence | Developer 1 | `PersistenceException` handling, `parseLine` (Developer 2) |
| `ProductService` | service | Developer 1 | `restoreStock` (Technical Leader); validator calls, `getProductById` (Developer 2) |
| `Accessory`, `Controller`, `Cable`, `Memory` | model | Developer 1 | Constructor validation of `Accessory` (Technical Leader) |
| `ConsoleCompatibleAccessory` (created in Req 7) | model | Developer 1 | — |
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

Total: 56 classes (15 audited by the Technical Leader, 26 by Developer 1,
and 15 by Developer 2), all audited. The scopes of the three reports were
compared class by class and method by method: no part was audited twice.

## Validation of the evidence

Every finding of the three reports cites the class, the method, and the
exact lines in `75cc180`, with the fragment of code, and every line was
checked against that commit. Every "correct" verdict names the class and
method where the principle is applied and how. "Not applicable" was not
needed for any principle: even the members whose classes have no
subclasses use the hierarchies of the others, so the Liskov Substitution
Principle was checked from the side of the client.

Two decisions of the consolidation:
- The creation of the submenus and the `Scanner` inside `ConsoleUI` breaks
  Dependency Inversion, but it is part of the same fragment of code as
  the Open/Closed finding of the main menu and it is fixed by the same
  change. It is registered once, as V-03, with both principles.
- Developer 1's Dependency Inversion finding about `ProductService`
  creating `ProductPersistence` was checked against the consolidated GRASP
  audit, where the same creation was "correct" under Creator. The two
  verdicts do not contradict each other: Creator decides which class
  creates an object, and Dependency Inversion asks whether a rule depends
  on a detail. The finding was accepted.

No finding had to be returned to its author.
