# SOLID Audit — Developer 2 (ednilsondcontreras)

Audited commit of `develop`: `75cc180` (merge of Pull Request #51, end of
Requirement 7). Every line number in this report refers to that commit.

## Scope

According to the responsibility table of Requirement 8, `docs/grasp-refactor-log.md`,
and the commit history, I audit the code I developed in the workshop, in
each requirement, and in the GRASP correction. Names used in the code:
`PersonPersistence` is the specification's `PersonRepository`, and
`Client` is `Customer`.

| Module (requirement) | Class | Audited | Excluded (audited by) |
|---|---|---|---|
| People (Workshop 1) | `Person`, `Client`, `Seller` | Whole classes | — |
| People (Workshop 1, Req 7) | `PersonPersistence` | Whole class, including the format of each type registered in the constructor (Req 7) | — |
| People (Workshop 1) | `PersonService` | Whole class | — |
| Accessories (Req 1, Req 7) | `AccessoryRepository`, `AccessoryService` | Whole classes, including the formats registered in the constructor and the use of `ConsoleCompatibleAccessory` (Req 7) | — |
| Promotions (Req 2, Req 7) | `PromotionRepository`, `PromotionService` | Whole classes | — |
| Returns (Req 3) | `ReturnRepository`, `ReturnRecord`, `ReturnService` | Whole classes, including the monthly balance | — |
| Returns (Req 3, adjustments A4 and A7) | `Return` | Returned accessories and `warrantyRefund` | Rest of `Return` (Developer 1) |
| Warranties (Req 4, Req 7) | `WarrantyRepository`, `WarrantyRecord`, `WarrantyService` | Whole classes, including the registered warranty types and `toWarranty` (Req 7) | — |
| Exceptions and validations (Req 6) | `ProductService`, `SaleService` | Validator calls, typed exceptions, `getProductById`, `getSaleById` | Rest of both classes (Developer 1 and Technical Leader) |
| Exceptions and validations (Req 6) | `ProductPersistence`, `SalePersistence` | `PersistenceException` handling and `parseLine` | Rest of both classes (Developer 1 and Technical Leader) |

Total: 15 classes, plus the parts listed in the last three rows.

## Single Responsibility Principle (SRP)

**Verdict: violation** (V-01).

Where it is applied correctly:
- Each repository only translates its objects to and from one data file
  (`PersonPersistence.save` line 91 and `load` line 111,
  `AccessoryRepository.saveAll` line 140 and `loadAll` line 158, and the
  same in `PromotionRepository`, `ReturnRepository`, and
  `WarrantyRepository`).
- `PersonService`, `AccessoryService`, `PromotionService`, and
  `WarrantyService` contain only the rules of their module and delegate
  the data checks to the validators (for example,
  `PersonService.registerClient`, lines 47-48:
  `PersonValidator.validatePersonData(...)`, `validateUniquePerson(...)`).
- `ReturnRecord` and `WarrantyRecord` only carry the stored ids until the
  objects are rebuilt.
- My parts of other classes keep one responsibility each: the Req 6
  `parseLine` methods only convert one line and report a corrupted line as
  `PersistenceException` (`ProductPersistence.parseLine` line 160), and
  the `getProductById`/`getSaleById` methods only find an object or throw
  `ResourceNotFoundException` through the validators
  (`ProductService` line 82, `SaleService` line 350).

### V-01
| Field | Content |
|---|---|
| ID | V-01 |
| Principle | Single Responsibility Principle |
| Module | Returns (Req 3, monthly balance) |
| Evidence | `ReturnService.calculateMonthlySales`, lines 348-357 (`for (Sale sale : saleService.listAll()) { ... totalSales += sale.calculateFinalTotal(); }`), `calculateMonthlyReturns`, lines 368-377, `generateMonthlyBalance`, lines 390-392, and `validateMonth`, lines 415-419 · the class description, lines 16-19: "managing returns ... and generating the monthly balance report" |
| Explanation | `ReturnService` has two reasons to change. Its main responsibility is to register and consult returns (deadline, ownership, stock, and warranties). The monthly balance is a financial report of the store that combines sales and returns: it changes when the store changes how it measures its income (for example, a new column, another period, or excluding the cost of warranties), not when the rules of returns change. |
| Consequence | Any change in the balance forces a change in the class that registers returns and puts that flow at risk; the balance cannot be reused or tested without creating the whole return service with its repository, sales, products, and accessories. The class also depends on all the sales only because of the report (`saleService.listAll()`, line 351). |
| Proposed solution | Move the balance to a new class `BalanceService` in the `service` package, which receives the read-only queries of the sales and of the returns. To give it the returns without the registration operation, `ReturnService` implements a small interface `ReturnLookup` (`viewAllReturns`, `viewReturnsByCustomer`, `viewReturnsBySale`). The texts and results of the balance do not change, including the message "El mes debe estar entre 1 y 12.". After the menu uses `BalanceService`, the balance methods and `validateMonth` are removed from `ReturnService`. |
| Other member's code | Yes. `ReturnMenu.showMonthlyBalance` (lines 161-163) calls the balance in `ReturnService`, and `Main` creates the services; both belong to the Technical Leader. The sales are read through the queries of `SaleService` (Technical Leader). Reported to the Technical Leader. |

## Open/Closed Principle (OCP)

**Verdict: correct.**

- **File formats:** since Requirement 7, each repository registers the
  format of each type once in its constructor and saving and loading look
  it up: `PersonPersistence` (lines 55-62), `AccessoryRepository` (lines
  61-84), `PromotionRepository` (lines 61-74), and `WarrantyRepository`
  (lines 63-66). A new type adds one registration; the existing reading
  and writing code is not modified.
- **Best promotion:** `PromotionService.findBestPromotionFor` (lines
  138-153) calls `promotion.calculateDiscount(sale)` for any promotion, so
  a new type of promotion does not change the selection.
- **Warranty eligibility and compatibility:** `WarrantyService.validateWarrantyEligible`
  (lines 130-134) asks `product.isWarrantyEligible()`, and
  `AccessoryService.findAccessoriesCompatibleWith` and
  `addCompatibleConsole` (lines 131 and 217) ask only for
  `ConsoleCompatibleAccessory`, so a new product or accessory type does not
  change these services.
- A new type of promotion or warranty is offered to the menu with a new
  method (`registerPercentageDiscount`, `assignExtendedWarranty`), which
  adds code without modifying the methods that already work.

## Liskov Substitution Principle (LSP)

**Verdict: correct.**

- `Client` and `Seller` extend `Person` (lines 7 and 8) only by adding
  their own attributes; their `toString` starts with `super.toString()`
  (`Client` line 34, `Seller` line 46). Every operation that receives a
  `Person` works with any subclass: `PersonService.findById` (lines
  112-120), `PersonValidator.validatePersonExists`, and
  `PersonPersistence.save`, which looks up the format of the class.
- The `instanceof` checks in `PersonService.listClient` (line 84),
  `listSeller` (line 99), `getClientById` (line 131), and `getSellerById`
  (line 147) do not compensate for a subclass that breaks the base class:
  they select the role that the operation needs (only a client can buy,
  only a seller can sell). The base class never promised those roles.
- `AccessoryService` (lines 131 and 217) treats every
  `ConsoleCompatibleAccessory` in the same way, whether it is a
  `Controller` or a `Memory`.
- `PromotionService` and `WarrantyService` use `Promotion` and `Warranty`
  only through the methods of the base class (`isActive`,
  `calculateDiscount`, `getAdditionalCost`), and the result is valid for
  every subclass.

## Interface Segregation Principle (ISP)

**Verdict: violation** (V-02).

Where it is applied correctly:
- The menus of my modules use most of the operations of their service
  (`AccessoryMenu` uses nine of the twelve public operations of
  `AccessoryService`), so a smaller role would not help them.
- The record classes (`ReturnRecord`, `WarrantyRecord`) expose only the
  data that the services read.

### V-02
| Field | Content |
|---|---|
| ID | V-02 |
| Principle | Interface Segregation Principle |
| Module | Returns (Req 3), Warranties (Req 4), and Accessories (Req 1) |
| Evidence | `ReturnService`, lines 26-28: `private final SaleService saleService; private final ProductService productService; private final AccessoryService accessoryService;` — it only uses `saleService.findById` and `listAll` (lines 97, 151, 351), `productService.findById` and `restoreStock` (lines 106, 214), and `accessoryService.restoreStock` (line 217) · `WarrantyService`, line 27: `private final ProductService productService;` — it only uses `findById` (line 58) · `AccessoryService` (lines 19-244) offers twelve public operations, but `SaleService` (Technical Leader, field line 42) only uses `findById`, `hasEnoughStock`, and `reduceStock` (lines 231, 275-276, 302) |
| Explanation | My services depend on complete services of other modules while they use one or two operations of each: they also depend on the registration of sales and products, the sale history, and the listing operations. In the other direction, `AccessoryService` does not offer a smaller role for the classes that only need its inventory. |
| Consequence | A change in an operation that `ReturnService` or `WarrantyService` never call (for example, a new parameter in `registerSale` or `registerProduct`) still affects them, and they cannot be used or tested with a simpler source of sales or products. The dependencies also hide what the returns and warranties really need from the other modules. |
| Proposed solution | Make every client depend only on the role it uses. `AccessoryService` implements a new interface `AccessoryInventory` (`findById`, `hasEnoughStock`, `reduceStock`, `restoreStock`). `ReturnService` receives a read-only role of the sales, the inventory role of the products, and `AccessoryInventory`; `WarrantyService` receives the read-only role of the products. The behavior does not change, because the same services are passed as those roles. |
| Other member's code | Yes. The roles of the sales and of the products must be offered by `SaleService` (Technical Leader) and `ProductService` (Developer 1), and `SaleService` must use `AccessoryInventory` (Technical Leader). Reported to the Technical Leader. |

## Dependency Inversion Principle (DIP)

**Verdict: violation** (V-03).

Where it is applied correctly:
- My services already receive their repository from outside (for
  example, `AccessoryService(AccessoryRepository)`, line 30), except
  `PersonService`; they do not create the repositories of other modules.
- The repositories depend only on the model, the records, and
  `PersistenceException`; they never call a service.

### V-03
| Field | Content |
|---|---|
| ID | V-03 |
| Principle | Dependency Inversion Principle |
| Module | People (Workshop 1), Accessories (Req 1), Promotions (Req 2), Returns (Req 3), and Warranties (Req 4) |
| Evidence | `PersonService`, line 24: `private PersonPersistence personPersistence;` and lines 30-33: `this.personPersistence = new PersonPersistence();` · the services depend on the concrete file classes: `AccessoryService` lines 21 and 30 (`AccessoryRepository`), `PromotionService` lines 20 and 29 (`PromotionRepository`), `ReturnService` lines 25, 40, 55, and 73 (`ReturnRepository`), and `WarrantyService` lines 25 and 38 (`WarrantyRepository`) · `WarrantyService` lines 8, 26, and 39 depend on `SalePersistence`, and line 54 reads the sales file with `salePersistence.load()` |
| Explanation | The services are the high-level modules of my code: they contain the rules of people, accessories, promotions, returns, and warranties. They depend on concrete classes that read and write text files. `PersonService` even creates its persistence class. Receiving the repository by the constructor is not enough: the type of the parameter is still the concrete class, so the rule depends on the detail. |
| Consequence | Changing how any module is stored (another file format, a database, or a list in memory for tests) forces a change in the services. `PersonService` cannot be created without reading `data/persons.txt`, and `WarrantyService` depends on the file class of another member's module, so a change in how sales are stored affects the warranties. |
| Proposed solution | Create one storage abstraction per module in the `persistence` package, implemented by its repository: `PersonStorage` (`save`, `load`), `AccessoryStorage`, `PromotionStorage`, and `ReturnStorage` (`saveAll`, `loadAll`), and `WarrantyStorage` (`saveAll`, `loadAll`, `toWarranty`). Each service receives its interface. `WarrantyService` receives the storage abstraction of the sales instead of `SalePersistence`. `PersonService` receives a `PersonStorage`; its current constructor is kept until `Main` passes the storage, and then it is removed. |
| Other member's code | Yes. `Main` (Technical Leader) must create `PersonPersistence` and pass it, and the storage abstraction of the sales must be created by the Technical Leader, who owns `SalePersistence`. Reported to the Technical Leader. |
