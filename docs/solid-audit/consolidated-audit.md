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

## Team verdict per principle

| Principle | Leader | Developer 1 | Developer 2 | Team verdict |
|---|---|---|---|---|
| Single Responsibility (SRP) | violation | violation | violation | **Violation** (V-01, V-02) |
| Open/Closed (OCP) | violation | correct | correct | **Violation** (V-03) |
| Liskov Substitution (LSP) | correct | correct | correct | **Correct** |
| Interface Segregation (ISP) | violation | violation | violation | **Violation** (V-07) |
| Dependency Inversion (DIP) | violation | violation | violation | **Violation** (V-04, V-05, V-06; also V-03) |

## Violations

### V-01 — Domain classes build the text of receipts and certificates
| Field | Content |
|---|---|
| Principle | Single Responsibility Principle |
| Evidence | `Sale.generateReceipt`, lines 286-317 · `Return.generateReturnReceipt`, lines 196-218, and `Return.appendItemLine`, lines 228-234 · `Warranty.generateWarrantyCertificate`, lines 76-89 · printed by `SaleMenu` (lines 135, 178, 211), `ReturnMenu` (line 126), and `WarrantyMenu` (line 102) |
| Explanation | `Sale`, `Return`, and `Warranty` keep the data and the rules of the domain, and they also build the full text shown in the console (titles, separators, labels in Spanish, money format). They have a second reason to change: the presentation. |
| Consequence | Changing the layout of a receipt forces a change in the model classes that every module uses, and every requirement that added data to a sale also edited its receipt inside the model. `Return` and `Warranty` import `CurrencyFormatter` only to build text. |
| Agreed solution | Three presentation classes in the `ui` package, each with a static `format` method that builds exactly the same text from the getters: `SaleReceiptFormatter`, `ReturnReceiptFormatter`, and `WarrantyCertificateFormatter`. `Return.refundFor` becomes public as `calculateRefundFor(double)`, so the return receipt shows the refund of each item without repeating the calculation. The menus print the text of the formatters; then the old methods are removed from the model. |
| Source reports | leader-audit V-01, developer1-audit V-01 |
| Responsible | **Technical Leader**: `SaleReceiptFormatter`, the removal of `Sale.generateReceipt`, and the use of the three formatters in `SaleMenu`, `ReturnMenu`, and `WarrantyMenu`. **Developer 1**: `ReturnReceiptFormatter`, `WarrantyCertificateFormatter`, `Return.calculateRefundFor`, and the removal of `generateReturnReceipt`, `appendItemLine`, and `generateWarrantyCertificate` after the menus use the formatters. |

### V-02 — The return service also calculates the monthly balance
| Field | Content |
|---|---|
| Principle | Single Responsibility Principle |
| Evidence | `ReturnService.calculateMonthlySales`, lines 348-357, `calculateMonthlyReturns`, lines 368-377, `generateMonthlyBalance`, lines 390-392, and `validateMonth`, lines 415-419 · called by `ReturnMenu.showMonthlyBalance`, lines 161-163 |
| Explanation | `ReturnService` registers and consults returns, and it also produces a financial report of the store that combines sales and returns. The report changes for reasons that have nothing to do with the rules of returns. |
| Consequence | A change in the balance puts the registration of returns at risk, and the balance cannot be used without creating the whole return service; `ReturnService` reads every sale only because of the report. |
| Agreed solution | A new class `BalanceService` in the `service` package with the same three methods and the same message for an invalid month. It depends on read-only roles: `SaleLookup` (see V-07) and a new interface `ReturnLookup` (`viewAllReturns`, `viewReturnsByCustomer`, `viewReturnsBySale`) implemented by `ReturnService`. `ReturnMenu` uses `BalanceService`, created in `Main`; then the balance is removed from `ReturnService`. |
| Source reports | developer2-audit V-01 |
| Responsible | **Developer 2**: `ReturnLookup`, `BalanceService`, and the removal of the balance methods and `validateMonth` from `ReturnService` after the menu uses `BalanceService`. **Technical Leader**: `ReturnMenu` and `Main`. |

### V-03 — The main menu must be modified to add a module
| Field | Content |
|---|---|
| Principle | Open/Closed Principle (also Dependency Inversion) |
| Evidence | `ConsoleUI.run`, lines 143-151 (one `case` per submenu) · `ConsoleUI.showMainMenu`, lines 163-169 (one line per module) · fields, lines 25-31 · five constructors, lines 41, 55, 72, 93, and 117 · creation of the input and the submenus, lines 121-130 (`new ConsoleInput(new Scanner(System.in))`, `new ProductMenu(...)` ... `new WarrantyMenu(...)`) |
| Explanation | The main menu knows each submenu by name, so a new module means editing `ConsoleUI` in five places. `ConsoleUI` also creates the concrete submenus and the `Scanner` instead of receiving them. |
| Consequence | Every requirement that added a module changed `ConsoleUI` and added a constructor. The number and the text of an option are kept in two places that can get out of sync. |
| Agreed solution | An interface `Menu` in the `ui` package (`getTitle()`, `show()`) implemented by the seven submenus. `ConsoleUI` receives a `ConsoleInput` and a `List<Menu>`, prints one numbered line per menu from its title, and opens the chosen one; the five constructors are replaced by one. `Main` creates the `Scanner`, the `ConsoleInput`, the `ErrorHandler`, and the submenus in the current order, so the screen is identical. |
| Source reports | leader-audit V-02 |
| Responsible | **Technical Leader**. |

### V-04 — The product service creates its file class
| Field | Content |
|---|---|
| Principle | Dependency Inversion Principle |
| Evidence | `ProductService`, line 21 (`private final ProductPersistence productPersistence;`) and lines 28-31 (`this.productPersistence = new ProductPersistence();`) · `Main`, line 57 (`new ProductService()`) |
| Explanation | The rules of the products depend on the concrete class that reads and writes `data/products.txt`, and the service creates it. |
| Consequence | The products cannot be stored in another way without modifying the service, and the service cannot be created without the real file. |
| Agreed solution | A new interface `ProductStorage` (`save`, `load`) in the `persistence` package, implemented by `ProductPersistence`. `ProductService` receives a `ProductStorage`. `Main` creates the `ProductPersistence` and passes it (the same object is also given to `SalePersistence`, see V-06). The constructor without parameters is kept until `Main` passes the storage, and then it is removed. |
| Source reports | developer1-audit V-03 |
| Responsible | **Developer 1**: `ProductStorage`, `ProductPersistence`, `ProductService`, and the removal of the old constructor. **Technical Leader**: `Main`. |

### V-05 — The services of people, accessories, promotions, returns, and warranties depend on concrete repositories
| Field | Content |
|---|---|
| Principle | Dependency Inversion Principle |
| Evidence | `PersonService`, line 24 and lines 30-33 (`new PersonPersistence()`) · `AccessoryService`, lines 21 and 30 · `PromotionService`, lines 20 and 29 · `ReturnService`, lines 25, 40, 55, and 73 · `WarrantyService`, lines 25 and 38 (`WarrantyRepository`), and lines 8, 26, 39, and 54 (`SalePersistence`) · `Main`, line 58 (`new PersonService()`) |
| Explanation | The services contain the rules of their modules and depend on concrete classes that read and write text files; `PersonService` creates its own. `WarrantyService` also depends on the file class of the sales, which belongs to another module. |
| Consequence | Changing how any of these modules is stored forces a change in its service, and a change in how sales are stored affects the warranties. |
| Agreed solution | One storage interface per module in the `persistence` package, implemented by its repository: `PersonStorage` (`save`, `load`), `AccessoryStorage`, `PromotionStorage`, and `ReturnStorage` (`saveAll`, `loadAll`), and `WarrantyStorage` (`saveAll`, `loadAll`, `toWarranty`). Each service receives its interface, and `WarrantyService` receives `SaleStorage` (see V-06) instead of `SalePersistence`. `Main` passes a `PersonPersistence` to `PersonService`; then the constructor without parameters is removed. |
| Source reports | developer2-audit V-03 |
| Responsible | **Developer 2**: the five interfaces, the repositories, the five services, and the removal of the old constructor of `PersonService`. **Technical Leader**: `SaleStorage` (created before Developer 2 starts) and `Main`. |

### V-06 — The sale persistence and the sale service depend on concrete file classes
| Field | Content |
|---|---|
| Principle | Dependency Inversion Principle |
| Evidence | `SalePersistence`, lines 44-46, line 54 (`new AccessoryRepository()`), and lines 68-69 (`new ProductPersistence()`, `new PersonPersistence()`) · `SaleService`, line 40, lines 55-57 (`new SalePersistence()`), and the parameters of lines 69, 84, and 102 |
| Explanation | The rules of the sales depend on the concrete class of the sales file, and that class creates the file classes of the products and the people. Part of the composition of the system is hidden inside these classes. |
| Consequence | The sales cannot be stored in another way without modifying `SaleService`, and `SalePersistence` stays tied to how the products, people, and accessories are stored. |
| Agreed solution | A new interface `SaleStorage` (`save`, `load`) in the `persistence` package, implemented by `SalePersistence`. `SalePersistence` receives `ProductStorage`, `PersonStorage`, and `AccessoryStorage` (see V-04 and V-05); its constructors that create them are removed. `SaleService` depends on `SaleStorage`, and its constructor that creates a `SalePersistence` is removed. `Main` creates every storage once and keeps the current loading order (products, people, accessories, promotions, warranties, sales, returns), so the messages of a file that cannot be read stay the same. |
| Source reports | leader-audit V-04 |
| Responsible | **Technical Leader**. |

### V-07 — Clients depend on complete services when they use only some operations
| Field | Content |
|---|---|
| Principle | Interface Segregation Principle |
| Evidence | `SaleService`, lines 41-42, uses three operations of `ProductService` (lines 226, 271, 298) and three of `AccessoryService` (lines 231, 275-276, 302) · `ReturnService`, lines 26-28, uses `findById` and `listAll` of `SaleService` (lines 97, 151, 351), `findById` and `restoreStock` of `ProductService` (lines 106, 214), and `restoreStock` of `AccessoryService` (line 217) · `WarrantyService`, line 27, uses only `findById` of `ProductService` (line 58) · `SaleMenu`, lines 28-29, `AccessoryMenu`, line 24, `ReturnMenu`, line 22, and `WarrantyMenu`, line 21, use only one query each (lines 116, 123, 243, 88, and 82) · `ProductService` (seven public operations), `AccessoryService` (twelve), and `SaleService` (nine) offer no smaller roles |
| Explanation | Each client depends on all the operations of a service of another module, including registrations and listings that it never calls, because the services do not offer role interfaces. |
| Consequence | A change in an operation that a client never calls still affects it; clients cannot be used with a simpler source of data, and their dependencies do not show what they really need. |
| Agreed solution | Role interfaces in the `service` package, implemented by the existing services: `ProductLookup` (`findById`, `getProductById`) and `ProductInventory extends ProductLookup` (adds `reduceStock`, `restoreStock`) by `ProductService`; `AccessoryInventory` (`findById`, `hasEnoughStock`, `reduceStock`, `restoreStock`) by `AccessoryService`; `SaleLookup` (`findById`, `getSaleById`, `listAll`) by `SaleService`. Clients: `SaleService` uses `ProductInventory` and `AccessoryInventory`; `ReturnService` uses `SaleLookup`, `ProductInventory`, and `AccessoryInventory`; `WarrantyService` uses `ProductLookup`; `SaleMenu` uses `ProductLookup` and `AccessoryInventory`; `AccessoryMenu` uses `ProductLookup`; `ReturnMenu` and `WarrantyMenu` use `SaleLookup`. `ProductMenu` and `SaleMenu` keep their own service, whose operations they use. |
| Source reports | leader-audit V-03, developer1-audit V-02, developer2-audit V-02 |
| Responsible | **Developer 1**: `ProductLookup`, `ProductInventory`, and `ProductService`. **Developer 2**: `AccessoryInventory`, `AccessoryService`, `ReturnService`, and `WarrantyService`. **Technical Leader**: `SaleLookup` (created before the developers start), `SaleService`, `SaleMenu`, `AccessoryMenu`, `ReturnMenu`, and `WarrantyMenu`. |
