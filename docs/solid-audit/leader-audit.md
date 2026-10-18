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
