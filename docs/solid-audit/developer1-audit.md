# SOLID Audit — Developer 1 (Reinaldo Padilla Pedroza)

Audited commit of `develop`: `75cc180` (merge of Pull Request #51, end of
Requirement 7). Every line number in this report refers to that commit.

## Scope

According to the responsibility table of Requirement 8, `docs/grasp-refactor-log.md`,
and the commit history, I audit the code I developed in the workshop, in
each requirement, and in the GRASP correction. Methods added to these
classes by other members are audited by them and are listed as excluded.
Names used in the code: `ProductPersistence` is the specification's
`ProductRepository`.

| Module (requirement) | Class | Audited | Excluded (audited by) |
|---|---|---|---|
| Products (Workshop 1, Req 7) | `Product`, `VideoGame`, `Console` | Whole classes, including `hasEnoughStock`, `decreaseStock`, `increaseStock`, `getCategory`, and `isWarrantyEligible` (Req 7) | Validation in the `Product` constructor (Req 6, Technical Leader) |
| Products (Workshop 1, Req 7) | `ProductPersistence` | Constructor with the format of each type (Req 7), `save`, `load`, `registerFormat` | `PersistenceException` handling and `parseLine` (Req 6, Developer 2) |
| Products (Workshop 1) | `ProductService` | Constructor, `registerProduct`, `listAll`, `findById`, `reduceStock`, `hasEnoughStock` | `restoreStock` (Req 3, Technical Leader); validator calls and `getProductById` (Req 6, Developer 2) |
| Accessories (Req 1, Req 7) | `Accessory`, `ConsoleCompatibleAccessory` (created in Req 7), `Controller`, `Cable`, `Memory` | Whole classes | Validation in the `Accessory` constructor (Req 6, Technical Leader) |
| Promotions (Req 2, Req 7) | `Promotion`, `PercentageDiscount`, `CategoryDiscount`, `BulkPurchaseDiscount` | Whole classes, including `CategoryDiscount.isSupportedCategory` (Req 7) | — |
| Returns (Req 3, Req 7) | `Return` | Constructors, refund calculation (`calculateRefundAmount`, `refundFor`), receipt (`generateReturnReceipt`, `appendItemLine`) | Returned accessories (A4) and `warrantyRefund` (A7), Developer 2 |
| Returns (Req 3) | `Sale` | `canBeReturned` only | Rest of `Sale` (Technical Leader) |
| Warranties (Req 4) | `Warranty`, `BasicWarranty`, `ExtendedWarranty` | Whole classes, including the certificate | — |
| Exceptions and validations (Req 6) | `GameZoneException`, `ResourceNotFoundException`, `BusinessRuleException`, `InvalidDataException`, `PersistenceException` | Whole classes | — |
| Exceptions and validations (Req 6) | `ProductValidator`, `PersonValidator`, `SaleValidator` | Whole classes | Their use in services and repositories (Developer 2) |

Total: 26 classes, plus the method `Sale.canBeReturned`.
