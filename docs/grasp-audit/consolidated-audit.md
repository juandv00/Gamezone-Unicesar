# Consolidated GRASP Audit — GameZone Unicesar

Audited commit of `develop`: **`9dc5282`** (merge of Pull Request #45, end
of Requirement 6). Every line number refers to that commit.

Source reports:
- `docs/grasp-audit/leader-audit.md` — Technical Leader
- `docs/grasp-audit/developer1-audit.md` — Developer 1
- `docs/grasp-audit/developer2-audit.md` — Developer 2

Names used in the code: `ProductPersistence`, `PersonPersistence`, and
`SalePersistence` are the specification's `ProductRepository`,
`PersonRepository`, and `SaleRepository`; `Client` is `Customer`; and
`ConsoleUI` is `ConsoleMenu`.

## Coverage

Every class of the system was audited by the member who developed it.
When another member added methods to a class in a later requirement,
those methods were audited by their author, as the rules of Requirement 7
establish; the table lists those parts explicitly so that no part was
audited twice or left out.

| Class | Package | Audited by | Parts audited by another member |
|---|---|---|---|
| `Main` | — | Technical Leader | — |
| `ConsoleUI` | ui | Technical Leader | — |
| `Sale` | model | Technical Leader | `canBeReturned` (Developer 1) |
| `SalePersistence` | persistence | Technical Leader | `PersistenceException` handling, `parseLine` (Developer 2) |
| `SaleService` | service | Technical Leader | Validator calls, typed exceptions, `getSaleById` (Developer 2) |
| `Product`, `VideoGame`, `Console` | model | Developer 1 | Constructor validation of `Product` (Technical Leader) |
| `ProductPersistence` | persistence | Developer 1 | `PersistenceException` handling, `parseLine` (Developer 2) |
| `ProductService` | service | Developer 1 | `restoreStock` (Technical Leader); validator calls, `getProductById` (Developer 2) |
| `Accessory`, `Controller`, `Cable`, `Memory` | model | Developer 1 | Constructor validation of `Accessory` (Technical Leader) |
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

Total: 42 classes, all audited. The three reports were checked for
duplicated parts; none was audited twice.

## Validation of the evidence

Every finding of the three reports cites the class, the method, and the
exact lines in `9dc5282`, and every line was checked against that commit.
Every "correct" verdict names the class and method where the pattern is
applied, and "not applicable" was not used for any whole report. No
finding had to be returned to its author.

## Team verdict per pattern

| Pattern | Leader | Developer 1 | Developer 2 | Team verdict |
|---|---|---|---|---|
| Information Expert | violation | violation | violation | **Violation** (V-01, V-02) |
| Creator | correct | correct | correct | **Correct** |
| Controller | correct | correct | correct | **Correct** |
| Low Coupling | correct | correct | correct | **Correct** |
| High Cohesion | violation | correct | correct | **Violation** (V-08) |
| Polymorphism | violation | violation | violation | **Violation** (V-03, V-04, V-05, V-06, V-07) |
| Pure Fabrication | violation | violation | correct | **Violation** (V-09) |
| Indirection | correct | correct | correct | **Correct** |
| Protected Variations | correct | violation | violation | **Violation** (covered by V-03, V-06, V-07) |

## Violations

### V-01 — Stock logic outside the classes that own the stock
| Field | Content |
|---|---|
| Pattern | Information Expert |
| Evidence | `ProductService.reduceStock` line 101, `hasEnoughStock` line 115, `restoreStock` line 134; `ProductValidator.validateStockAvailability` line 58; `AccessoryService.hasEnoughStock` line 184, `reduceStock` lines 198 and 201, `restoreStock` line 245 |
| Explanation | `Product` and `Accessory` own the stock, but services and the validator read it with `getStock()` and write it with `setStock()` to check and change it. |
| Consequence | The stock rule is repeated in seven places in three classes; a change in the rule must be repeated in all of them. |
| Agreed solution | `Product` and `Accessory` get `hasEnoughStock(int)`, `decreaseStock(int)`, and `increaseStock(int)`. The services and the validator call them. `updateStock` keeps using `setStock`, because it replaces the stock with a value typed by the user. |
| Source reports | developer1-audit V-01, developer2-audit V-01, leader-audit V-01 |
| Responsible | **Developer 1**: the methods in `Product` and `Accessory`, `ProductService.reduceStock` and `hasEnoughStock`, `ProductValidator`. **Developer 2**: `AccessoryService`. **Technical Leader**: `ProductService.restoreStock`. |

### V-02 — The return calculates the ratio paid in the sale
| Field | Content |
|---|---|
| Pattern | Information Expert |
| Evidence | `Return.paidFactor`, lines 183-189 |
| Explanation | The paid ratio depends only on the subtotal and the discount of the sale, but `Return` reads both and calculates it. |
| Consequence | A change in how the sale applies discounts would leave `Return` with the old rule, and refunds would be wrong. |
| Agreed solution | `Sale.calculatePaidRatio()` returns the ratio (1 when the subtotal is 0); `Return.paidFactor` uses it. |
| Source report | developer1-audit V-02 |
| Responsible | **Technical Leader**: `Sale.calculatePaidRatio()`. **Developer 1**: `Return`. |

### V-03 — Product categories decided by type checks and text literals
| Field | Content |
|---|---|
| Pattern | Polymorphism (also Protected Variations) |
| Evidence | `CategoryDiscount.matchesCategory`, lines 56-64, and `calculateDiscount`, lines 69-79; `PromotionService.registerCategoryDiscount`, lines 71-76 |
| Explanation | The category of an item is found with `instanceof` and compared with literals, and the list of valid categories is repeated in the service. |
| Consequence | A new product type or category requires changing `CategoryDiscount` and `PromotionService`; missing one of them breaks the discount or the registration. |
| Agreed solution | `Product` declares an abstract `getCategory()` (implemented by `VideoGame` and `Console`) and `Accessory` implements `getCategory()`. `CategoryDiscount` compares the category of every item with its target and offers `isSupportedCategory(String)`, which `PromotionService` uses. |
| Source reports | developer1-audit V-03, developer2-audit V-05 |
| Responsible | **Developer 1**: `Product`, `VideoGame`, `Console`, `Accessory`, `CategoryDiscount`. **Developer 2**: `PromotionService`. |

### V-04 — Console compatibility duplicated and checked by type
| Field | Content |
|---|---|
| Pattern | Polymorphism |
| Evidence | `Controller` lines 22, 54-78 and `Memory` lines 26, 75-99 (duplicated code); `AccessoryService.findAccessoriesCompatibleWith` lines 130-135 and `addCompatibleConsole` lines 218-224; `ConsoleUI.addCompatibleConsole` line 846 |
| Explanation | Two accessory types share a behavior that has no type in the hierarchy, so the code is duplicated and its users check `instanceof Controller` / `instanceof Memory`. |
| Consequence | A new accessory with compatibility requires copying the code and changing three classes. |
| Agreed solution | New abstract class `ConsoleCompatibleAccessory` (model), extending `Accessory`, with the list and the three methods; `Controller` and `Memory` extend it. `AccessoryService` and `ConsoleUI` ask only for `ConsoleCompatibleAccessory`. |
| Source reports | developer1-audit V-04, developer2-audit V-03, leader-audit V-03 |
| Responsible | **Developer 1**: creates `ConsoleCompatibleAccessory` and changes `Controller` and `Memory`. **Developer 2**: `AccessoryService`. **Technical Leader**: `ConsoleUI`. |

### V-05 — Warranty eligibility decided with `instanceof Console`
| Field | Content |
|---|---|
| Pattern | Polymorphism |
| Evidence | `SaleService.assignWarranties` line 479 and `containsConsole` line 494; `ConsoleUI.askExtendedWarranties` line 513; `WarrantyService.validateConsole` lines 132-136; `ReturnService.registerReturn` lines 197-199 |
| Explanation | Five places decide whether a product receives a warranty by checking its class. |
| Consequence | Giving warranties to another product type would require finding and changing all five checks. |
| Agreed solution | `Product.isWarrantyEligible()` returns false; `Console` overrides it to return true. The five checks call it. |
| Source reports | leader-audit V-02, developer2-audit V-04 |
| Responsible | **Developer 1**: `Product` and `Console`. **Technical Leader**: `SaleService` and `ConsoleUI`. **Developer 2**: `WarrantyService` and `ReturnService`. |

### V-06 — Product file format chosen with type conditionals
| Field | Content |
|---|---|
| Pattern | Polymorphism (also Protected Variations) |
| Evidence | `ProductPersistence.toLine`, lines 77-90, and `fromLine`, lines 126-131 |
| Explanation | The format of each product type is selected with `instanceof` to write and with type codes to read. |
| Consequence | A new product type requires editing both chains; one missing branch loses data silently. |
| Agreed solution | Keep the format in `ProductPersistence` but register one writer per class and one reader per type code; the save and load methods look them up instead of using conditionals. The file format does not change. |
| Source report | developer1-audit V-05 |
| Responsible | **Developer 1**. |

### V-07 — File formats chosen with type conditionals in the other repositories
| Field | Content |
|---|---|
| Pattern | Polymorphism (also Protected Variations) |
| Evidence | `PersonPersistence.toLine` lines 76-87 and `fromLine` lines 122-127; `AccessoryRepository.toLine` lines 80-96 and `fromLine` lines 119-148; `PromotionRepository.toLine` lines 83-94 and `fromLine` lines 119-132; `WarrantyRepository.toLine` line 74; `WarrantyService.resolveWarranties` lines 63-67 |
| Explanation | Same problem as V-06, in every repository of Developer 2. |
| Consequence | A new type in any module requires editing two chains; a missing branch loses data or stores a wrong type. |
| Agreed solution | The same design as V-06 in each repository, with no change in any file format. |
| Source report | developer2-audit V-02 |
| Responsible | **Developer 2**. |

### V-08 — `ConsoleUI` concentrates every interaction of the system
| Field | Content |
|---|---|
| Pattern | High Cohesion |
| Evidence | `ConsoleUI`: 1,479 lines, 62 methods, 31 imports, seven submenus, input reading, error handling, type names, and money formatting |
| Explanation | One class holds the interaction of seven modules plus input parsing and error presentation. |
| Consequence | Every change to any menu touches the same large file, which is hard to understand and causes merge conflicts. |
| Agreed solution | One class per submenu (`ProductMenu`, `PersonMenu`, `SaleMenu`, `AccessoryMenu`, `PromotionMenu`, `ReturnMenu`, `WarrantyMenu`), `ConsoleInput` for reading input, and `ErrorHandler` for the differentiated error handling. `ConsoleUI` keeps the main menu. Every message stays exactly the same. |
| Source report | leader-audit V-04 |
| Responsible | **Technical Leader**. |

### V-09 — Money formatting duplicated in four classes
| Field | Content |
|---|---|
| Pattern | Pure Fabrication |
| Evidence | `Sale.formatAmount` lines 303-305, `ConsoleUI.formatAmount` lines 1235-1237, `Return.formatAmount` lines 249-251, `Warranty.formatAmount` lines 90-92 |
| Explanation | Formatting money is not a responsibility of any domain class, and there is no class for it. |
| Consequence | A format change must be repeated four times; a missed copy shows inconsistent amounts. |
| Agreed solution | New class `CurrencyFormatter` in a new package `com.gamezone.util` (cross-cutting, like `exceptions`), with one static method `format(double)`; the four classes use it. |
| Source reports | leader-audit V-05, developer1-audit V-06 |
| Responsible | **Technical Leader**: creates `CurrencyFormatter` and changes `Sale` and `ConsoleUI`. **Developer 1**: `Return` and `Warranty`. |

