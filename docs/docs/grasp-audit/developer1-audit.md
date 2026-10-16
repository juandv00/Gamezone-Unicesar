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

