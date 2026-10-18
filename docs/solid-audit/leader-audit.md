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

## Open/Closed Principle (OCP)

**Verdict: violation** (V-02).

Where it is applied correctly:
- `SaleService.applyBestPromotion` (lines 411-422) works with any
  `Promotion` through `findBestPromotionFor` and `calculateDiscount`, so a
  new type of promotion does not change the sale.
- `SaleService.assignWarranties` (line 478) and
  `containsWarrantyEligibleProduct` (line 501) ask
  `product.isWarrantyEligible()`, and `SaleMenu` does the same (line 153),
  so a new type of product with a warranty does not change them.
- `ErrorHandler.executeOperation` (lines 28-44) has one message per
  category of error; a new exception that belongs to an existing category
  (a subclass of `BusinessRuleException`, for example) is shown without
  changes, and any other error falls in the last `catch (RuntimeException e)`
  (line 41).

### V-02
| Field | Content |
|---|---|
| ID | V-02 |
| Principle | Open/Closed Principle (also Dependency Inversion) |
| Module | Interface and start (Workshop 1, extended in every requirement, divided in Req 7) |
| Evidence | `ConsoleUI.run`, lines 143-151: `case 1 -> productMenu.show(); case 2 -> personMenu.show(); ... case 7 -> warrantyMenu.show();` · `ConsoleUI.showMainMenu`, lines 163-169: one `System.out.println` per module (`"1. Gestión de productos"` ... `"7. Gestión de garantías"`) · seven fields, one per submenu (lines 25-31) · five constructors (lines 41, 55, 72, 93, and 117), one more parameter for each module added by a requirement · the last constructor creates the input and every submenu (lines 121-130: `new ConsoleInput(new Scanner(System.in))`, `new ProductMenu(...)`, ... `new WarrantyMenu(...)`) |
| Explanation | The main menu is not closed for modification: it knows each submenu by name. Adding a module means editing `ConsoleUI` in five places (a field, a constructor parameter, the creation of the submenu, a `case`, and a line of the menu). It also breaks Dependency Inversion: `ConsoleUI` creates the concrete submenus and the `Scanner` of the console, instead of receiving them. |
| Consequence | Every requirement that added a module (accessories, promotions, returns, warranties) changed `ConsoleUI` and added one more constructor, and the next one will do the same. The number of an option and its text are kept in two separate places (the `case` and the `println`), so they can get out of sync. The main menu cannot be built with other submenus or another source of input. |
| Proposed solution | Create an interface `Menu` in the `ui` package with `getTitle()` and `show()`, implemented by the seven submenus. `ConsoleUI` receives a `ConsoleInput` and a `List<Menu>`, prints one numbered line per menu from its title, and opens `menus.get(option - 1)`. `Main` creates the `Scanner`, the `ConsoleInput`, the `ErrorHandler`, and the submenus in the current order, so the menu shows exactly the same lines and options. |
| Other member's code | No. Every class involved is mine. |
