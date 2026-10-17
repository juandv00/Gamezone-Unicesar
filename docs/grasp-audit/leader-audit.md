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

