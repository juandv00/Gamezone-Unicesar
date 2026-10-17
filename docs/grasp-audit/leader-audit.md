# GRASP Audit — Technical Leader (Juan David Molina Jimenez)

Audited commit of `develop`: `9dc5282` (merge of Pull Request #45, end of
Requirement 6). Every line number in this report refers to that commit.

## Scope

| Module (requirement) | Class | Audited | Excluded (audited by) |
|---|---|---|---|
| Sales (Workshop 1, Req 1, 2, 4) | `Sale` | Whole class: items, totals, applied promotion and discount, warranty cost, receipt | `canBeReturned` (Developer 1) |
| Sales (Workshop 1, Req 1, 2, 4) | `SalePersistence` (the specification's `SaleRepository`) | `save`, `load`, `toLine`, `fromLine`, constructors | `PersistenceException` handling and `parseLine` (Req 6, Developer 2) |
| Sales (Workshop 1, Req 1, 2, 4, A3) | `SaleService` | Unified registration flow (`processSale`), accessories, best promotion, warranty assignment, queries | Validator calls, typed exceptions, and `getSaleById` (Req 6, Developer 2) |
| Interface and start (all requirements) | `ConsoleUI` (the specification's `ConsoleMenu`) | Whole class, including every submenu and the differentiated exception handling (Req 6) | — |
| Interface and start (all requirements) | `Main` | Whole class | — |
| Returns (Req 3) | `ProductService` | `restoreStock` | Rest of the class (Developers 1 and 2) |
| Exceptions and validations (Req 6) | `Product`, `Accessory` | The validation in the constructors | Rest of the classes (Developer 1) |

## Information Expert

**Verdict: violation** (V-01).

Where it is applied correctly:
- `Sale.calculateTotal()` (lines 194-203), `calculateFinalTotal()` (lines
  237-239), and `hasDiscount()` (lines 246-248) use the items, discount,
  and warranty cost that `Sale` stores.
- `Sale.generateReceipt()` (lines 270-301) builds the receipt with the
  sale's own data.
- `SaleService.applyBestPromotion` asks `PromotionService` for the best
  promotion and each promotion for its own discount.

### V-01
| Field | Content |
|---|---|
| ID | V-01 |
| Pattern | Information Expert |
| Module | Returns (Req 3) |
| Evidence | `ProductService.restoreStock`, line 134: `product.setStock(product.getStock() + quantity);` |
| Explanation | Restoring units is an operation on the stock, which belongs to `Product`. The method reads the stock and writes it back from outside. |
| Consequence | It repeats the stock logic that also exists in `reduceStock` and in `AccessoryService`; a change in the stock rule would have to be repeated here. |
| Proposed solution | Apply Information Expert: call a `Product` operation that increases the stock. |
| Other member's code | Yes. The operation must exist in `Product` (Developer 1); Developer 1 reported the same problem in `reduceStock`. |

## Creator

**Verdict: correct.**

- `SaleService.processSale` creates the `Sale` (line 285, in step 3 of the
  unified flow) because it has the client, seller, and items that
  initialize it, and it keeps the list of sales.
- `Main.createConsoleUI` (lines 56-79) is the composition root: it creates
  every repository and service in dependency order and passes them through
  the constructors.
- `SalePersistence` creates `ProductPersistence` and `PersonPersistence`
  (lines 68-69) because it uses them closely to resolve the stored sales,
  and no other class shares those instances.
- `ConsoleUI` creates `VideoGame` and `Console` objects from the data the
  user types (lines 280 and 301) and gives them to `ProductService`.


