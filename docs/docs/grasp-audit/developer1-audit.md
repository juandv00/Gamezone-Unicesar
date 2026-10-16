# GRASP Audit — Developer 1 (Reinaldo Padilla Pedroza)

Audited commit of `develop`: `9dc5282` (merge of Pull Request #45, end of
Requirement 6). Every line number in this report refers to that commit.

## Scope

According to the responsibility table of Requirement 7 and the commit
history, I audit the code I developed in the workshop and in each
requirement. Methods added to these classes by other members are audited
by them and are listed as excluded.

| Module (requirement) | Class | Audited | Excluded (audited by) |
|---|---|---|---|
| Products (Workshop 1) | `Product`, `VideoGame`, `Console` | Whole classes | Validation in the `Product` constructor (Req 6, Technical Leader) |
| Products (Workshop 1) | `ProductPersistence` (the specification's `ProductRepository`) | `save`, `load`, `toLine`, `fromLine` | `PersistenceException` handling and `parseLine` (Req 6, Developer 2) |
| Products (Workshop 1) | `ProductService` | `registerProduct`, `listAll`, `findById`, `reduceStock`, `hasEnoughStock` | `restoreStock` (Req 3, Technical Leader); validator calls and `getProductById` (Req 6, Developer 2) |
| Accessories (Req 1) | `Accessory`, `Controller`, `Cable`, `Memory` | Whole classes | Validation in the `Accessory` constructor (Req 6, Technical Leader) |
| Promotions (Req 2, A1) | `Promotion`, `PercentageDiscount`, `CategoryDiscount`, `BulkPurchaseDiscount` | Whole classes | — |
| Returns (Req 3, A5) | `Return` | Constructors, refund calculation (`calculateRefundAmount`, `paidFactor`, `refundFor`), receipt | Returned accessories (A4) and `warrantyRefund` (A7), Developer 2 |
| Returns (Req 3) | `Sale` | `canBeReturned` only | Rest of `Sale` (Technical Leader) |
| Warranties (Req 4) | `Warranty`, `BasicWarranty`, `ExtendedWarranty` | Whole classes | — |
| Exceptions and validations (Req 6) | `GameZoneException`, `ResourceNotFoundException`, `BusinessRuleException`, `InvalidDataException`, `PersistenceException` | Whole classes | — |
| Exceptions and validations (Req 6) | `ProductValidator`, `PersonValidator`, `SaleValidator` | Whole classes | Their use in services and repositories (Developer 2) |

## Information Expert

**Verdict: violation** (V-01, V-02).

Where it is applied correctly:
- `Sale.canBeReturned()` (lines 257-260) decides the 30-day window with the
  date that `Sale` owns.
- `Promotion.isActive(LocalDate)` (lines 72-74) and
  `Warranty.isActive(LocalDate)` (lines 64-66) use their own start and end
  dates.
- `Return.calculateRefundAmount()` (lines 163-174) adds up the refunds of
  the items that `Return` stores.

### V-01
| Field | Content |
|---|---|
| ID | V-01 |
| Pattern | Information Expert |
| Module | Products (Workshop 1) and Exceptions and validations (Req 6) |
| Evidence | `ProductService.reduceStock`, line 101: `product.setStock(product.getStock() - amount);` · `ProductService.hasEnoughStock`, line 115: `return product != null && product.getStock() >= amount;` · `ProductValidator.validateStockAvailability`, line 58: `if (product.getStock() < requestedQuantity)` |
| Explanation | The stock is an attribute of `Product`, but the operations on it (checking whether there are enough units and subtracting units) are written outside the class, reading the value with `getStock()` and writing it back with `setStock()`. The class that has the information is not the one doing the work. |
| Consequence | The stock rule is repeated in three places (and the same happens with `Accessory`, see below). A new rule, for example that stock can never go below a minimum, would have to be found and changed in every copy, and forgetting one would give inconsistent results. |
| Proposed solution | Apply Information Expert: add to `Product` the operations `hasEnoughStock(int)`, `decreaseStock(int)`, and `increaseStock(int)`, and make the service and the validator call them. Do the same in `Accessory`, so both inventories follow the same design. |
| Other member's code | Yes. `ProductService.restoreStock` (line 134, Technical Leader) and the stock methods of `AccessoryService` (Developer 2) do the same with `setStock`. Reported to the Technical Leader. |

### V-02
| Field | Content |
|---|---|
| ID | V-02 |
| Pattern | Information Expert |
| Module | Returns (Req 3, adjustment A5) |
| Evidence | `Return.paidFactor`, lines 183-189: `double subtotal = originalSale.calculateTotal(); ... return 1.0 - originalSale.getDiscountAmount() / subtotal;` |
| Explanation | The fraction of the price that the customer actually paid depends only on data of the sale (its subtotal and its discount). `Return` asks the sale for both values and does the calculation itself, instead of asking the sale for the result. |
| Consequence | `Return` depends on how the sale stores its discount. If the way the sale applies discounts changes (for example, a discount that does not apply to every item), `Return` would keep calculating with the old rule, and refunds would be wrong. |
| Proposed solution | Apply Information Expert: add to `Sale` a method that returns the paid ratio (`calculatePaidRatio()`), and make `Return.paidFactor` use it. |
| Other member's code | Yes. `Sale` is the Technical Leader's class. Reported to the Technical Leader. |

## Creator

**Verdict: correct.**

- `ProductPersistence.fromLine` (lines 115-132) creates `VideoGame` and
  `Console` objects because it has the data needed to initialize them
  (the stored line).
- `ProductService` creates its `ProductPersistence` in its constructor
  (line 29) because it is the only class that uses it closely; no other
  class shares that instance.
- `Return` is not created by my code; `ReturnService` (Developer 2) creates
  it with the data of the sale, which is correct according to Creator.
- `Controller` and `Memory` create their own list of compatible consoles
  (`new ArrayList<>()`), because they contain it.

## Controller

**Verdict: correct.**

`ProductService` is the first object after the user interface that
receives the product operations (`registerProduct`, `reduceStock`,
`findById`). It coordinates the validator and the persistence class, and it
does not contain any console code. My model classes, validators, and
exceptions do not receive system events, so for them the pattern does not
apply.

## Low Coupling

**Verdict: correct.**

- The model classes only depend on other model classes and on the
  `exceptions` package.
- The validators depend only on the model and the exceptions; they never
  call services or persistence classes (`ProductValidator`, lines 3-6).
- `ProductService` depends only on `Product`, `ProductPersistence`, and
  `ProductValidator`.
- The promotion subclasses only know `Sale`, `Product`, and `Accessory`,
  which they need to calculate the discount.

## High Cohesion

**Verdict: correct.**

Each class has one responsibility: the product, accessory, promotion, and
warranty classes hold their data and their own calculations; each
validator checks the rules of one module; each exception represents one
category of error; `ProductPersistence` only reads and writes the products
file. The largest class I audited, `Return` (257 lines), only stores a
return, calculates its refund, and builds its receipt.

## Polymorphism

**Verdict: violation** (V-03, V-04, V-05).

Where it is applied correctly:
- `Promotion.calculateDiscount(Sale)` is abstract (line 84) and each
  subclass implements its own rule, so `PromotionService` evaluates every
  promotion without knowing its type.
- `Warranty` declares `getDurationInMonths()`, `getWarrantyType()`, and
  `getAdditionalCost()` (lines 51-55); the end date and the certificate
  work for both subclasses.
- `Product.getDescription()` and `Accessory.getDescription()` /
  `getType()` are implemented by each subclass.

### V-03
| Field | Content |
|---|---|
| ID | V-03 |
| Pattern | Polymorphism (also Protected Variations) |
| Module | Promotions (Req 2, adjustment A1) |
| Evidence | `CategoryDiscount.matchesCategory`, lines 56-64: `if ("VIDEOGAME".equalsIgnoreCase(targetCategory)) { return product instanceof VideoGame; } if ("CONSOLE"...) { return product instanceof Console; }` · `CategoryDiscount.calculateDiscount`, lines 69-79: a separate branch for `"ACCESSORY"` |
| Explanation | The category of an item is decided by asking for its concrete class with `instanceof` and comparing it with text literals. The category is something that varies by type, so each type should answer it through a polymorphic operation. |
| Consequence | Adding a new product type (for example, a collectible) requires changing `CategoryDiscount` and adding another branch; if someone forgets it, the new items never receive category discounts. The list of valid categories is also repeated in `PromotionService` (Developer 2). |
| Proposed solution | Apply Polymorphism: add an abstract `getCategory()` to `Product` and a `getCategory()` to `Accessory`, implemented by each subclass ("VIDEOGAME", "CONSOLE", "ACCESSORY"). `CategoryDiscount` compares `getCategory()` with its target for products and accessories alike, and exposes the list of supported categories so that it is defined in one place. |
| Other member's code | Yes. `PromotionService.registerCategoryDiscount` (Developer 2) would use the list of supported categories. Reported to the Technical Leader. |

### V-04
| Field | Content |
|---|---|
| ID | V-04 |
| Pattern | Polymorphism |
| Module | Accessories (Req 1) |
| Evidence | `Controller`, lines 22, 54-78 and `Memory`, lines 26, 75-99: the same attribute `compatibleConsoleIds` and the same methods `getCompatibleConsoleIds`, `addCompatibleConsole`, and `isCompatibleWith` are written twice. `Accessory` has no operation for compatibility. |
| Explanation | "Compatible with consoles" is a behavior shared by two accessory types, but the hierarchy has no type that represents it. The code that uses accessories can only find out whether an accessory supports compatibility by asking `instanceof Controller` or `instanceof Memory`. |
| Consequence | The duplicated code must be changed in two places. A new accessory type with compatibility (for example, a headset) would require copying the code again and changing every class that checks the type. |
| Proposed solution | Apply Polymorphism: create an abstract class `ConsoleCompatibleAccessory` (extends `Accessory`) with the list and the three methods; `Controller` and `Memory` extend it. The rest of the system asks for that type only. |
| Other member's code | Yes. The type checks are in `AccessoryService` (Developer 2) and `ConsoleUI` (Technical Leader). Reported to the Technical Leader. |

### V-05
| Field | Content |
|---|---|
| ID | V-05 |
| Pattern | Polymorphism (also Protected Variations) |
| Module | Products (Workshop 1) |
| Evidence | `ProductPersistence.toLine`, lines 77-90: `if (product instanceof VideoGame videoGame) {...} else if (product instanceof Console console) {...}` · `ProductPersistence.fromLine`, lines 126-131: `if (type.equals("VIDEOGAME")) {...} else if (type.equals("CONSOLE")) {...}` |
| Explanation | The format of each product type is selected with a chain of conditionals on the type, in both directions. |
| Consequence | Every new product type requires editing both chains; a type that is added to one chain but not to the other is silently skipped (`toLine` returns an empty line). |
| Proposed solution | Apply Polymorphism through Pure Fabrication: keep the file format in the persistence layer (it does not belong to the model), but register one format object per product type (keyed by class to write and by type code to read), so adding a type means registering a format instead of editing conditionals. |
| Other member's code | No. |

## Pure Fabrication

**Verdict: violation** (V-06).

Where it is applied correctly: `ProductPersistence` and the three
validators are fabricated classes that keep file access and validation
rules out of the domain classes.

### V-06
| Field | Content |
|---|---|
| ID | V-06 |
| Pattern | Pure Fabrication |
| Module | Returns (Req 3) and Warranties (Req 4) |
| Evidence | `Return.formatAmount`, lines 249-251 and `Warranty.formatAmount`, lines 90-92: the same method `String.format(Locale.US, "$%,.2f", amount)` |
| Explanation | Formatting money for display is not a responsibility of the domain: it does not belong to a return or to a warranty. Each class wrote its own copy because there is no class responsible for it. |
| Consequence | Changing the currency format (for example, to Colombian pesos without decimals) requires finding every copy; if one is missed, receipts and certificates show different formats. |
| Proposed solution | Apply Pure Fabrication: one class responsible for formatting amounts, used by every class that prints money. |
| Other member's code | Yes. `Sale` and `ConsoleUI` (Technical Leader) have the same method. Reported to the Technical Leader. |

## Indirection

**Verdict: correct.**

- `ProductService` is the intermediary between the user interface and
  `ProductPersistence`; the interface never reads the products file.
- The validators sit between the services and the business rules: a
  service does not need to know how a rule is checked.
- The exception hierarchy lets the lower layers report an error without
  knowing how the user interface will show it.

## Protected Variations

**Verdict: violation** — covered by V-03 and V-05, registered under
Polymorphism because that is the pattern that guides their correction.

Where it is applied correctly: `GameZoneException` is the stable
interface for every error; a new category of error is a new subclass, and
the code that catches `GameZoneException` does not change.

## Problems reported to the Technical Leader

1. V-01 also affects `ProductService.restoreStock` (Technical Leader) and
   the stock methods of `AccessoryService` (Developer 2).
2. V-02 requires a new method in `Sale` (Technical Leader).
3. V-03: `PromotionService.registerCategoryDiscount` (Developer 2)
   repeats the list of categories.
4. V-04: `AccessoryService` (Developer 2) and `ConsoleUI` (Technical
   Leader) check `instanceof Controller` / `instanceof Memory`.
5. V-06: `Sale` and `ConsoleUI` (Technical Leader) repeat the money
   format.




