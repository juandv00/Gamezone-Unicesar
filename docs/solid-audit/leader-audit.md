# SOLID Audit — Technical Leader (Juan David Molina Jimenez)

Audited commit of `develop`: `75cc180` (merge of Pull Request #51, end of
Requirement 7). Every line number in this report refers to that commit.

## Scope

According to the responsibility table of Requirement 8, `docs/grasp-refactor-log.md`,
and the commit history, I audit the sales module, the user interface, the
start of the application, my integrations of every module, and the
classes I created or changed in the GRASP correction. Names used in the
code: `SalePersistence` is the specification's `SaleRepository`, and
`ConsoleUI` is `ConsoleMenu`; since Requirement 7 the submenus of
`ConsoleUI` are separate classes.

| Module (requirement) | Class | Audited | Excluded (audited by) |
|---|---|---|---|
| Sales (Workshop 1, Req 2, Req 4, Req 7) | `Sale` | Whole class: items, subtotal, discount applied (Req 2), warranty cost (Req 4), `calculatePaidRatio` (Req 7), and the receipt `generateReceipt` | `canBeReturned` (Developer 1) |
| Sales (Workshop 1, Req 1) | `SalePersistence` | Constructors, `save`, `load`, the line format with accessories, promotion, and warranty cost | `PersistenceException` handling and `parseLine` (Req 6, Developer 2) |
| Sales (Workshop 1, Req 1, Req 2, Req 4) | `SaleService` | Constructors, `registerSale`, `registerSaleByItemIds`, `processSale`, the accessory integration, `applyBestPromotion` (Req 2), `assignWarranties` (Req 4), the queries, and the ids | Validator calls, typed exceptions, and `getSaleById` (Req 6, Developer 2) |
| Interface (Workshop 1, every requirement, Req 6, Req 7) | `ConsoleUI` | Whole class (main menu) | — |
| Interface (Req 7) | `ConsoleInput`, `ErrorHandler`, `ProductMenu`, `PersonMenu`, `SaleMenu`, `AccessoryMenu`, `PromotionMenu`, `ReturnMenu`, `WarrantyMenu` | Whole classes, created when `ConsoleUI` was divided; they keep the menu extensions of every module and the differentiated handling of exceptions (Req 6) | — |
| Start (Workshop 1) | `Main` | Whole class | — |
| Utilities (Req 7) | `CurrencyFormatter` | Whole class | — |
| Returns (Req 3) | `ProductService` | `restoreStock` only | Rest of `ProductService` (Developer 1 and Developer 2) |
| Exceptions and validations (Req 6) | `Product`, `Accessory` | Validation in the constructors | Rest of both classes (Developer 1) |

Total: 15 classes, plus the parts listed in the last two rows.

## Single Responsibility Principle (SRP)

**Verdict: violation** (V-01).

Where it is applied correctly:
- Since Requirement 7, every class of the interface has one
  responsibility: `ConsoleInput` reads and converts what the user types
  (lines 30-123), `ErrorHandler.executeOperation` (lines 28-44) turns each
  category of error into a message, and each submenu class handles the
  operations of one module.
- `CurrencyFormatter.format` (line 27) only formats an amount of money.
- `SalePersistence` only translates sales to and from `data/sales.txt`.
- `SaleService` coordinates the sale use case: it validates the items,
  creates the sale, and asks the promotion, warranty, product, and
  accessory services for their part (`processSale`, lines 245-313). The
  discount and the warranties are calculated by their own services, so the
  rules of those modules change without changing `SaleService`.
- `Main` only creates the objects and starts the interface.
- `Sale.toString` (lines 320-329) gives the one-line summary of the
  object, as every model class of the system does; it is the standard
  contract of `Object` and it was kept out of this finding.

### V-01
| Field | Content |
|---|---|
| ID | V-01 |
| Principle | Single Responsibility Principle |
| Module | Sales (Workshop 1, with the breakdown added in Req 2 and Req 4) |
| Evidence | `Sale.generateReceipt`, lines 286-317: `receipt.append("===== GameZone Unicesar - Recibo =====\n"); ... receipt.append("Descuento (").append(appliedPromotionName).append("): -") ... receipt.append("=======================================");` · printed by `SaleMenu.registerSale` (line 135) and `SaleMenu.viewSaleReceipt` (line 211) |
| Explanation | `Sale` is a domain class: it keeps the items of a sale and calculates its subtotal, discount, warranty cost, and total. It also builds the full text of the receipt shown in the console, with its titles, separators, labels in Spanish, and money format. That is a second reason to change: the presentation of the receipt. |
| Consequence | Changing only how the receipt looks (a new line, a label, the order of the totals) forces a change in the class that calculates the totals, which every other module uses; a mistake there affects returns, balances, and promotions. Every requirement that added data to the sale (promotions, warranties) also had to edit the receipt inside the model. |
| Proposed solution | Move the text to a presentation class `SaleReceiptFormatter` in the `ui` package, with a static `format(Sale)` method that builds exactly the same text from the getters of the sale. `SaleMenu` prints its result, and `generateReceipt` is removed from `Sale`. |
| Other member's code | Yes. Developer 1 reported the same problem in `Return.generateReturnReceipt` and `Warranty.generateWarrantyCertificate`, which are printed by my menus (`ReturnMenu` line 126, `SaleMenu` line 178, `WarrantyMenu` line 102). It is a boundary finding. |
