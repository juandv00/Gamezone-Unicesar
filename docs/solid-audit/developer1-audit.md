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

## Single Responsibility Principle (SRP)

**Verdict: violation** (V-01).

Where it is applied correctly:
- `Product` (lines 10-148) keeps the data of a product and the operations
  on its own stock (`hasEnoughStock`, `decreaseStock`, `increaseStock`,
  lines 78-100); it changes only if the product changes.
- `ProductPersistence` only translates products to and from the lines of
  `data/products.txt` (`save` line 97, `load` line 117).
- Each validator checks the rules of one module (`ProductValidator`,
  `PersonValidator`, `SaleValidator`), and each exception represents one
  category of error.
- Each promotion subclass only contains its discount rule
  (`PercentageDiscount.calculateDiscount` line 36,
  `CategoryDiscount.calculateDiscount` line 82,
  `BulkPurchaseDiscount.calculateDiscount` line 49).
- `Sale.canBeReturned()` (line 273) decides the 30-day window with the date
  of the sale.

### V-01
| Field | Content |
|---|---|
| ID | V-01 |
| Principle | Single Responsibility Principle |
| Module | Returns (Req 3) and Warranties (Req 4) |
| Evidence | `Return.generateReturnReceipt`, lines 196-218 (`sb.append("===== GameZone Unicesar - Recibo de devolución =====\n"); ...`) and `Return.appendItemLine`, lines 228-234 · `Warranty.generateWarrantyCertificate`, lines 76-89 (`sb.append("===== GameZone Unicesar - Certificado de garantía =====\n"); ...`) · `import com.gamezone.util.CurrencyFormatter;` in `Return` line 3 and `Warranty` line 3, used only by those methods |
| Explanation | `Return` and `Warranty` are domain classes: they keep the data of a return or a warranty and their rules (the refund, the coverage dates). They also build, line by line, the text that the console shows to the user, with its titles, separators, labels in Spanish, and money format. These are two reasons to change: a change in the rules of returns or warranties, and a change in how the receipt or the certificate looks. |
| Consequence | Changing only the layout of a receipt (a new line, a different separator, another label) forces a change in a model class and its recompilation, and puts the domain at risk of an unintended change. If the store wanted another output (a printed or a file receipt), the model would collect one more format for each output. The model also depends on a presentation helper (`CurrencyFormatter`) only to format text. |
| Proposed solution | Move each text to a presentation class: `ReturnReceiptFormatter` and `WarrantyCertificateFormatter` in the `ui` package, with a static `format` method that builds exactly the same text from the getters of the object. `Return` exposes the refund of one item (`refundFor`, today private) as a public method, so the receipt can show it without repeating the calculation. Then the menus print the text of the formatters and the old methods are removed from the model. |
| Other member's code | Yes. `SaleMenu` (lines 135, 178, 211), `ReturnMenu` (line 126), and `WarrantyMenu` (line 102), which print these texts, belong to the Technical Leader, and `Sale.generateReceipt` (Technical Leader) has the same problem. Reported to the Technical Leader. |
