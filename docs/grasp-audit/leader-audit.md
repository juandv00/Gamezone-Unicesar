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

## Controller

**Verdict: correct.**

`ConsoleUI` only reads input and shows results; every system operation is
received by a service: for example, "register a sale" is received by
`SaleService.registerSaleByItemIds`, which runs the unified flow
(validation, promotion, warranties, stock, persistence). `ConsoleUI` does
not decide any business rule.

## Low Coupling

**Verdict: correct.**

- `SaleService` depends on the services of the modules it coordinates and
  on `SalePersistence`; it never touches files.
- `SalePersistence` depends on `ProductPersistence`, `PersonPersistence`,
  and `AccessoryRepository` to resolve the references of each stored sale.
  These are classes of its own layer; the dependencies point downwards and
  there are no cycles.
- `ConsoleUI` depends only on services, model classes, and exceptions,
  never on persistence classes.

## High Cohesion

**Verdict: violation** (V-04).

Where it is applied correctly: `Sale`, `SalePersistence`, `SaleService`,
and `Main` each have one clear responsibility.

### V-04
| Field | Content |
|---|---|
| ID | V-04 |
| Pattern | High Cohesion |
| Module | Interface and start (Workshop 1 and every requirement) |
| Evidence | `ConsoleUI` has 1,479 lines, 62 methods, 31 imports, and depends on seven services. It contains the main menu (`run` and `showMainMenu`, lines 158-193), seven submenus with every operation of each module (`productMenu` line 245, `personMenu` 354, `saleMenu` 427, `accessoryMenu` 627, `warrantyMenu` 978, `returnMenu` 1084, `promotionMenu` 1246), the input readers (`readOption` line 201, `readPrice` 1417, `readDate` 1471), the error handling (`executeOperation`, line 221), the accessory type names (`toAccessoryType` 929, `accessoryTypeLabel` 944), and money formatting (`formatAmount` 1235). |
| Explanation | The class grew with every requirement and now holds unrelated responsibilities: the interaction of seven different modules, the reading and validation of input, and the presentation of errors. |
| Consequence | Any change to one module's menu means editing a 1,479-line file that every member touches, which makes merge conflicts likely and the class hard to understand and test. A new module would make it even larger. |
| Proposed solution | Apply High Cohesion (with Pure Fabrication): one class per submenu (`ProductMenu`, `PersonMenu`, `SaleMenu`, `AccessoryMenu`, `PromotionMenu`, `ReturnMenu`, `WarrantyMenu`), one class to read the input (`ConsoleInput`), and one class for the error handling (`ErrorHandler`). `ConsoleUI` keeps only the main menu and delegates to the submenus. |
| Other member's code | No. |

## Polymorphism

**Verdict: violation** (V-02, V-03).

Where it is applied correctly: `SaleService.applyBestPromotion` works with
`Promotion` through `calculateDiscount`, and `executeOperation` handles
every error through the `GameZoneException` hierarchy.

### V-02
| Field | Content |
|---|---|
| ID | V-02 |
| Pattern | Polymorphism |
| Module | Warranties (Req 4) |
| Evidence | `SaleService.assignWarranties`, line 479: `if (!(product instanceof Console)) { continue; }` · `SaleService.containsConsole`, line 494: `if (product instanceof Console && ...)` · `ConsoleUI.askExtendedWarranties`, line 513: `if (!(product instanceof Console) || ...)` |
| Explanation | Whether a product receives a warranty is decided by asking its class. The rule varies by product type, so each type should answer it. |
| Consequence | If another product type receives warranties, every `instanceof Console` must be found and changed (the same check also exists in `WarrantyService` and `ReturnService`). |
| Proposed solution | Apply Polymorphism: `Product` answers whether it receives a warranty (`Console` answers yes), and the sale flow and the menu ask that. |
| Other member's code | Yes. `Product` and `Console` are Developer 1's classes. Developer 2 reported the same check in his services. |

### V-03
| Field | Content |
|---|---|
| ID | V-03 |
| Pattern | Polymorphism |
| Module | Accessories (Req 1) |
| Evidence | `ConsoleUI.addCompatibleConsole`, line 846: `if (!(accessory instanceof Controller) && !(accessory instanceof Memory))` |
| Explanation | The menu decides whether an accessory supports console compatibility by checking two concrete classes. |
| Consequence | A new accessory type with compatibility would be rejected by the menu until this condition is changed. |
| Proposed solution | Apply Polymorphism: ask for the common type of the accessories with compatibility. |
| Other member's code | Yes. The accessory hierarchy is Developer 1's, and the same check exists in `AccessoryService` (Developer 2). |


