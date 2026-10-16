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


