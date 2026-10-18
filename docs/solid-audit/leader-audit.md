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

## Liskov Substitution Principle (LSP)

**Verdict: correct.**

My classes have no subclasses, but my code is the main client of the
hierarchies of the other members, so I checked that it uses every
subclass through the contract of its base class:
- `Sale.calculateTotal` (lines 194-203) adds `getPrice()` of any
  `Product` and any `Accessory`, and `SaleService.assignWarranties` asks
  every product `isWarrantyEligible()`; neither asks for the concrete
  class.
- `SaleService.applyBestPromotion` uses any `Promotion` only through
  `calculateDiscount` and `getName`, and limits the discount to the
  subtotal (line 419); every subclass returns a valid amount.
- `ErrorHandler` handles the subclasses of `GameZoneException` by their
  category, and `Main` (lines 39-45) catches `PersistenceException` and
  then any `GameZoneException`.
- The type checks that remain in my code do not compensate for a
  subclass that breaks its base class: `AccessoryMenu.addCompatibleConsole`
  asks whether the typed id is a `Console` (line 243) and whether the
  accessory is a `ConsoleCompatibleAccessory` (line 270), and
  `SalePersistence` (lines 239 and 242) asks whether a person is a
  `Client` or a `Seller`. In each case the operation needs a specific role
  that the base class never promised.

## Interface Segregation Principle (ISP)

**Verdict: violation** (V-03).

Where it is applied correctly:
- `ProductMenu`, `PersonMenu`, and `PromotionMenu` use most of the
  operations of their service, so they depend on what they use.
- `ConsoleInput` and `ErrorHandler` are small classes; each submenu uses
  the operations it needs from them.

### V-03
| Field | Content |
|---|---|
| ID | V-03 |
| Principle | Interface Segregation Principle |
| Module | Sales (Req 1, accessory integration) and Interface (Req 3, Req 4, Req 7) |
| Evidence | `SaleService`, lines 41-42: `private final ProductService productService; private final AccessoryService accessoryService;` — it only uses `findById`, `getProductById`, and `reduceStock` of the products (lines 226, 271, 298) and `findById`, `hasEnoughStock`, and `reduceStock` of the accessories (lines 231, 275-276, 302) · `ReturnMenu` line 22 and `WarrantyMenu` line 21 depend on `SaleService` and only use `getSaleById` (lines 88 and 82) · `SaleMenu` lines 28-29 depend on `ProductService` and `AccessoryService` and only use `findById` (lines 116 and 123) · `AccessoryMenu` line 24 depends on `ProductService` and only uses `findById` (line 243) · `SaleService` (nine public operations, lines 128-376) offers no read-only role for the classes that only find or list sales |
| Explanation | My classes depend on complete services of other modules when they use only the queries or only the stock operations. The menus that consult a sale also depend on the operations that register sales, and the sale service depends on the registration and listing of products and accessories. |
| Consequence | A change in an operation that these classes never call still affects them (for example, a new parameter in `registerProduct` or in `registerSale`). They cannot be used with a simpler source of sales or products, and their dependencies do not show what each one really needs. |
| Proposed solution | Make each class depend only on the role it uses. `SaleService` implements a new interface `SaleLookup` (`findById`, `getSaleById`, `listAll`) in the `service` package. `SaleService` uses the inventory roles of the products and the accessories; `ReturnMenu` and `WarrantyMenu` use `SaleLookup`; `SaleMenu` and `AccessoryMenu` use the read-only role of the products. The same services are passed as those roles, so the behavior does not change. |
| Other member's code | Yes. The roles of the products and the accessories must be offered by `ProductService` (Developer 1) and `AccessoryService` (Developer 2). Developer 1 and Developer 2 reported the same problem from their side. It is a boundary finding. |

## Dependency Inversion Principle (DIP)

**Verdict: violation** (V-04; the creation of the submenus and the
`Scanner` in `ConsoleUI` is registered with V-02).

Where it is applied correctly:
- `Main` is the composition root of the application: it is the only place
  where creating the concrete repositories and services is expected
  (lines 56-78).
- `SaleService` receives every collaborator through its constructor
  (lines 101-111), so it does not create other services.
- `ProductService.restoreStock` (lines 129-136) depends only on `Product`
  and on the persistence that its class already has; it does not create
  any object.

### V-04
| Field | Content |
|---|---|
| ID | V-04 |
| Principle | Dependency Inversion Principle |
| Module | Sales (Workshop 1, Req 1) |
| Evidence | `SalePersistence`, lines 44-46: `private final ProductPersistence productPersistence; private final PersonPersistence personPersistence; private final AccessoryRepository accessoryRepository;` · line 54: `this(new AccessoryRepository());` · lines 68-69: `this.productPersistence = new ProductPersistence(); this.personPersistence = new PersonPersistence();` · `SaleService`, line 40: `private final SalePersistence salePersistence;`, the constructor of lines 55-57: `this(productService, null, new SalePersistence());`, and the parameter of type `SalePersistence` in the other constructors (lines 69, 84, and 102) |
| Explanation | `SaleService` contains the rules of the sales and depends on the concrete class that writes `data/sales.txt`; one of its constructors even creates it. `SalePersistence`, in turn, creates the file classes of the products and the people and depends on the concrete accessory repository. The rules and the reading of the sales depend on details instead of abstractions, and part of the composition of the system is hidden inside these classes. |
| Consequence | The sales cannot be stored in another way without modifying `SaleService`. The file of the sales is tied to how products, people, and accessories are stored: if Developer 1 or Developer 2 changed their storage, `SalePersistence` would still read the old files with its own objects. `Main` cannot decide which implementations are used. |
| Proposed solution | Create the abstraction `SaleStorage` (`save`, `load`) in the `persistence` package, implemented by `SalePersistence`. `SalePersistence` receives the storage abstractions of the products, the people, and the accessories in its constructor instead of creating them, and its constructors without them are removed. `SaleService` depends on `SaleStorage`, and its constructor that creates a `SalePersistence` is removed. `Main` creates every storage once, in the same loading order as today, and passes it to the classes that use it. |
| Other member's code | Yes, indirectly. The storage abstractions of the products, the people, and the accessories must exist first; they are created by Developer 1 and Developer 2 in their own corrections. No class of another member has to change for this finding. |
