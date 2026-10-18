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
