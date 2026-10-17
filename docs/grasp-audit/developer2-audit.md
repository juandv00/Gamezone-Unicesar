# GRASP Audit — Developer 2 (Ednilson Contreras Buelvas)

Audited commit of `develop`: `9dc5282` (merge of Pull Request #45, end of
Requirement 6). Every line number in this report refers to that commit.

## Scope

According to the responsibility table of Requirement 7 and the commit
history, I audit the repositories and services of each module, the person
module, and the use of exceptions and validators that I added in
Requirement 6.

| Module (requirement) | Class | Audited |
|---|---|---|
| People (Workshop 1) | `Person`, `Client` (the specification's `Customer`), `Seller` | Whole classes |
| People (Workshop 1) | `PersonPersistence` (the specification's `PersonRepository`), `PersonService` | Whole classes |
| Accessories (Req 1, A4) | `AccessoryRepository`, `AccessoryService` | Whole classes, including `restoreStock` (A4) |
| Promotions (Req 2) | `PromotionRepository`, `PromotionService` | Whole classes, including the selection of the best promotion |
| Returns (Req 3, A4, A6, A7, refactor) | `ReturnRepository`, `ReturnRecord`, `ReturnService` | Whole classes, including the monthly balance and the warranty cancellation |
| Returns (A4, A7) | `Return` | The returned accessories and the warranty refund |
| Warranties (Req 4, A2, A7) | `WarrantyRepository`, `WarrantyRecord`, `WarrantyService` | Whole classes, including `cancelWarranties` |
| Exceptions and validations (Req 6) | `ProductService`, `PersonService`, `SaleService` | The calls to the validators, the typed exceptions, and the methods `getProductById`, `getClientById`, `getSellerById`, `getSaleById` |
| Exceptions and validations (Req 6) | `ProductPersistence`, `PersonPersistence`, `SalePersistence` | The `PersistenceException` handling and `parseLine` |

