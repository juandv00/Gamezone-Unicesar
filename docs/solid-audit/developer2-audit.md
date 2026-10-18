# SOLID Audit — Developer 2 (ednilsondcontreras)

Audited commit of `develop`: `75cc180` (merge of Pull Request #51, end of
Requirement 7). Every line number in this report refers to that commit.

## Scope

According to the responsibility table of Requirement 8, `docs/grasp-refactor-log.md`,
and the commit history, I audit the code I developed in the workshop, in
each requirement, and in the GRASP correction. Names used in the code:
`PersonPersistence` is the specification's `PersonRepository`, and
`Client` is `Customer`.

| Module (requirement) | Class | Audited | Excluded (audited by) |
|---|---|---|---|
| People (Workshop 1) | `Person`, `Client`, `Seller` | Whole classes | — |
| People (Workshop 1, Req 7) | `PersonPersistence` | Whole class, including the format of each type registered in the constructor (Req 7) | — |
| People (Workshop 1) | `PersonService` | Whole class | — |
| Accessories (Req 1, Req 7) | `AccessoryRepository`, `AccessoryService` | Whole classes, including the formats registered in the constructor and the use of `ConsoleCompatibleAccessory` (Req 7) | — |
| Promotions (Req 2, Req 7) | `PromotionRepository`, `PromotionService` | Whole classes | — |
| Returns (Req 3) | `ReturnRepository`, `ReturnRecord`, `ReturnService` | Whole classes, including the monthly balance | — |
| Returns (Req 3, adjustments A4 and A7) | `Return` | Returned accessories and `warrantyRefund` | Rest of `Return` (Developer 1) |
| Warranties (Req 4, Req 7) | `WarrantyRepository`, `WarrantyRecord`, `WarrantyService` | Whole classes, including the registered warranty types and `toWarranty` (Req 7) | — |
| Exceptions and validations (Req 6) | `ProductService`, `SaleService` | Validator calls, typed exceptions, `getProductById`, `getSaleById` | Rest of both classes (Developer 1 and Technical Leader) |
| Exceptions and validations (Req 6) | `ProductPersistence`, `SalePersistence` | `PersistenceException` handling and `parseLine` | Rest of both classes (Developer 1 and Technical Leader) |

Total: 15 classes, plus the parts listed in the last three rows.
