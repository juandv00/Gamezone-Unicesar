# GRASP Refactor Log — GameZone Unicesar

Change log of Phase 2 of Requirement 7. Every change corrects a violation
of the consolidated audit (`docs/grasp-audit/consolidated-audit.md`) and
keeps the external behavior of the system: the same inputs produce the
same results, the same messages, and the same content in the data files.

## Non-regression strategy

The strategy follows the answer to guiding question 4
(`docs/grasp-analysis.md`): a fixed set of inputs that runs every
operation of the menu, executed on `develop` before the first correction
(reference result), after the corrections of each member (on the
operations affected by each Pull Request), and at the end of the phase
(on every operation).

The inputs and the scripts are in `docs/regression/`:

| File | Content |
|---|---|
| `initial-data/` | The data files of the repository (`persons.txt`, `accessories.csv`, `promotions.csv`); every scenario starts from a copy of them, never from the `data/` folder of the project |
| `main-input.txt`, `errors-input.txt`, `nowrite-input.txt`, `reload-input.txt`, `corrupt-input.txt`, `exit-input.txt` | What the user types in each scenario, one line per answer |
| `corrupt-data/`, `corruptproducts-data/` | Data files with unknown types and invalid lines |
| `run-regression.ps1` | Compiles the project (or, with `-Source`, the code of a given commit, taken from a temporary copy), runs the seven scenarios, and saves the screen output and the data files written by the application in `target/regression/<label>/` |
| `compare-regression.ps1` | Compares two results file by file (screen output and every data file) |

How to run it, from the root of the project:

```powershell
powershell -ExecutionPolicy Bypass -File docs/regression/run-regression.ps1 -Label reference -Source 44d8f9e
powershell -ExecutionPolicy Bypass -File docs/regression/run-regression.ps1 -Label final
powershell -ExecutionPolicy Bypass -File docs/regression/compare-regression.ps1 -Reference reference -Candidate final
```

The first command compiles the code of `44d8f9e` (`develop` before the
corrections) without changing the current files; the second one compiles
the current code of the branch.

Expected result: `RESULT: no differences`. The dates of the day (sales,
returns, and warranties use the current date) are the only allowed
difference, and only when the two runs are on different days; in that
case the comparison is run with `-IgnoreDates`.

### Operations verified

Scenario 1 (`main`) runs the 33 operations of the seven submenus with
valid data. Scenario 2 (`errors`) runs the error cases of every module.
Scenarios 3 to 7 cover the data files.

| # | Menu operation | Input data | Expected result |
|---|---|---|---|
| 1 | Products › Register a video game | `VG-1` Elden Ring, 250000, stock 10, PS5, RPG, M; `VG-2` FIFA 26, 200000, stock 3 | "Videojuego registrado correctamente." and a `VIDEOGAME` line in `products.txt` |
| 2 | Products › Register a console | `C-1` PlayStation 5, 2500000, stock 5, Sony, PS5, 9 | "Consola registrada correctamente." and a `CONSOLE` line |
| 3 | Products › List all products | — | The three products with their stock (also listed at the end: 8, 4, and 3 units after the sales and returns) |
| 4 | People › Register a client | `CL-1` Maria Lopez, `CL-2` Pedro Diaz | "Cliente registrado correctamente." and a `CLIENT` line in `persons.txt` |
| 5 | People › List all clients | — | `CL-1` and `CL-2` |
| 6 | People › List all sellers | — | The three preloaded sellers |
| 7 | Accessories › Register a controller | `CTRL-2` Xbox Controller, wired, compatible with `C-1` | "Control registrado correctamente." and "PlayStation 5 registrada como compatible." |
| 8 | Accessories › Register a cable | `CBL-2` USB-C Cable, 1.5 m, USB | "Cable registrado correctamente." |
| 9 | Accessories › Register a memory | `MEM-2` SSD 1TB, 1000 GB, internal card, compatible with `C-1` | "Memoria registrada correctamente." |
| 10 | Accessories › List all accessories | — | The six accessories, labeled Control, Cable, or Memoria |
| 11 | Accessories › List by type | `control`, `memoria`, `cable` | Two accessories of each type |
| 12 | Accessories › Compatible with a console | `C-1` | `CTRL-2` and `MEM-2` |
| 13 | Accessories › Add a compatible console | `CTRL-1` + `C-1`; `CBL-1` | Registered for the controller; "Solo los controles y las memorias admiten compatibilidad con consolas." for the cable |
| 14 | Accessories › Update the stock | `CBL-1`, 12 | "Stock actualizado correctamente." |
| 15 | Promotions › Register a percentage promotion | `P-5`, 5 %, 2026-01-01 to 2030-12-31 | "Promoción por porcentaje registrada correctamente." |
| 16 | Promotions › Register a category promotion | `P-CON`, 3 %, consoles | "Promoción por categoría registrada correctamente." |
| 17 | Promotions › Register a bulk purchase promotion | `P-BULK`, minimum 5, 30 % | "Promoción por compra en volumen registrada correctamente." |
| 18 | Promotions › List all promotions | — | The seven promotions with their status |
| 19 | Promotions › List active promotions | — | The active promotions of the day |
| 20 | Sales › Register a sale | `SALE-1`: `C-1`, `VG-1`, `CTRL-1` with extended warranty; `SALE-2`: `VG-1`, `VG-2`, `CBL-2`, `MEM-1`; `SALE-3`: two units of `C-1` without extended warranty; `SALE-4`: `CTRL-2`, `MEM-2` | Receipts with the best promotion (10 % general, 20 % video games, 10 % general, 25 % accessories), the warranty certificates (one extended, two basic), and the stock reduced |
| 21 | Sales › Full history | — | The four sales with their accessories |
| 22 | Sales › History of a client | `CL-1` | `SALE-1` and `SALE-3` |
| 23 | Sales › History of a seller | `S002` | `SALE-2` |
| 24 | Sales › Receipt of a sale | Number 1 | The receipt of `SALE-1` |
| 25 | Warranties › Warranty of a product in a sale | `SALE-1` + `C-1`; `SALE-2` + `VG-1` | The certificate of `WAR-1` (active); "Elden Ring no tiene garantía en la venta SALE-2 (solo las consolas reciben garantía)." |
| 26 | Warranties › List all warranties | — | `WAR-1`, `WAR-2`, `WAR-3` |
| 27 | Warranties › List active warranties | — | The same three |
| 28 | Warranties › Expiring soon | 400 days; 10 days | The three warranties; "Ninguna garantía vence en los próximos 10 días." |
| 29 | Returns › Register a return | `SALE-1`: `C-1`, `CTRL-1`; `SALE-2`: `VG-2`, `MEM-1`; `SALE-3`: one `C-1` | Return receipts with the proportional discount, the extended warranty refunded, and the stock restored |
| 30 | Returns › List all returns | — | `RET-1`, `RET-2`, `RET-3` |
| 31 | Returns › Returns of a client | `CL-1` | `RET-1` and `RET-3` |
| 32 | Returns › Returns of a sale | `SALE-2` | `RET-2` |
| 33 | Returns › Monthly balance | 10/2026; 1/2020 | Sales, returns, and net balance of October 2026; all zero for 1/2020 |
| 34 | Error cases (scenario `errors`) | Invalid options; invalid price, stock, length, capacity, connection type, memory type, percentage, dates, and category; negative price; empty id and name; invalid email; duplicated client, accessory, and promotion; unknown client, seller, sale, accessory, console, and item; insufficient stock; sale without items; returns without items, without reason, and of units already returned; invalid month and year | The same error message of each case, shown by the differentiated error handling, and no change in the data files |
| 35 | Data files (scenarios `nowrite`, `noread`, `reload`, `corrupt`, `corruptproducts`) | Accessories file that cannot be read or written; products file that cannot be read; reload of the data written by scenario 1; unknown types and invalid lines in every file; invalid number in `products.txt` | The same messages ("Error al cargar los accesorios…", "No fue posible cargar los datos del sistema…", "Se omitió una línea inválida…"), the same data loaded again, and lines of unknown types ignored |

### Reference result (develop before the first correction)

Commit: `44d8f9e` (merge of Pull Request #48, `develop` with the
consolidated audit; the source code is the same as in the audited commit
`9dc5282`) — 2026-10-17 — result: the seven scenarios ran completely and
their output was kept as the reference (`target/regression/reference`).

| Scenario | Lines of screen output | Data files written by the application |
|---|---|---|
| `main` | 883 | `accessories.csv`, `persons.txt`, `products.txt`, `promotions.csv`, `returns.csv`, `sales.txt`, `warranties.csv` |
| `errors` | 742 | `accessories.csv`, `persons.txt`, `products.txt`, `promotions.csv`, `sales.txt`, `warranties.csv` |
| `nowrite` | 61 | `persons.txt`, `promotions.csv` (the accessories file is a folder) |
| `noread` | 2 | The application does not start: "No fue posible cargar los datos del sistema. Contacte al administrador." |
| `reload` | 307 | The same files as `main`, loaded and listed again |
| `corrupt` | 169 | Three "Se omitió una línea inválida…" messages; lines of unknown types ignored |
| `corruptproducts` | 2 | The application does not start (same message as `noread`) |

The out-of-scope defects of the audit keep their current behavior and are
part of the reference: a duplicated product id is accepted (O-01, scenario
`errors`), and the accessories repository prints its error and continues
(O-03, scenario `nowrite`).

## Changes

One row per change. The Commit / PR column names the commit and the Pull
Request that contains it: each member opened one Pull Request from
`feature/grasp-refactor` to `develop` with their corrections, and the
Pull Request of Developer 1 also contains the preparation of the
Technical Leader.

| Class | Change | Violation ID | Pattern applied | Commit / PR | Responsible |
|---|---|---|---|---|---|
| `Sale` | New method `calculatePaidRatio()`: the fraction of the subtotal paid after the discount (1 when the subtotal is 0) | V-02 | Information Expert | `refactor: add paid ratio calculation to Sale (V-02)` / PR of Developer 1 | Technical Leader |
| `CurrencyFormatter` (new, `com.gamezone.util`) | New class with the static method `format(double)` (`$%,.2f`, US locale) | V-09 | Pure Fabrication | `refactor: format amounts with CurrencyFormatter in Sale and ConsoleUI (V-09)` / PR of Developer 1 | Technical Leader |
| `Sale`, `ConsoleUI` | Private `formatAmount` removed; the receipts and the menu use `CurrencyFormatter.format` | V-09 | Pure Fabrication | `refactor: format amounts with CurrencyFormatter in Sale and ConsoleUI (V-09)` / PR of Developer 1 | Technical Leader |
| `Product`, `Accessory` | New methods `hasEnoughStock(int)`, `decreaseStock(int)`, and `increaseStock(int)` | V-01 | Information Expert | `refactor: move stock operations into Product and Accessory (V-01)` / PR of Developer 1 | Developer 1 |
| `ProductService` | `reduceStock` calls `decreaseStock` and `hasEnoughStock` calls `Product.hasEnoughStock` | V-01 | Information Expert | `refactor: move stock operations into Product and Accessory (V-01)` / PR of Developer 1 | Developer 1 |
| `ProductValidator` | `validateStockAvailability` asks `product.hasEnoughStock(...)` instead of comparing `getStock()` | V-01 | Information Expert | `refactor: move stock operations into Product and Accessory (V-01)` / PR of Developer 1 | Developer 1 |
| `Return` | `paidFactor` removed; `refundFor` asks the original sale for `calculatePaidRatio()` | V-02 | Information Expert | `refactor: use the paid ratio of Sale in Return refunds (V-02)` / PR of Developer 1 | Developer 1 |
| `Product`, `VideoGame`, `Console`, `Accessory` | New method `getCategory()`: abstract in `Product`, `"VIDEOGAME"` in `VideoGame`, `"CONSOLE"` in `Console`, and `"ACCESSORY"` in `Accessory` | V-03 | Polymorphism | `refactor: let each product and accessory report its category (V-03)` / PR of Developer 1 | Developer 1 |
| `CategoryDiscount` | `matchesCategory` and its `instanceof` checks removed; `calculateDiscount` compares the category of every item; new static method `isSupportedCategory(String)` with the only list of categories | V-03 | Polymorphism, Protected Variations | `refactor: let each product and accessory report its category (V-03)` / PR of Developer 1 | Developer 1 |
| `ConsoleCompatibleAccessory` (new, `model`) | New abstract class between `Accessory` and the accessories with console compatibility, with the list of consoles and `getCompatibleConsoleIds`, `addCompatibleConsole`, and `isCompatibleWith` | V-04 | Polymorphism | `refactor: add ConsoleCompatibleAccessory for controllers and memories (V-04)` / PR of Developer 1 | Developer 1 |
| `Controller`, `Memory` | Extend `ConsoleCompatibleAccessory`; their duplicated list and methods removed | V-04 | Polymorphism | `refactor: add ConsoleCompatibleAccessory for controllers and memories (V-04)` / PR of Developer 1 | Developer 1 |
| `Product`, `Console` | New method `isWarrantyEligible()`: false in `Product`, overridden to return true in `Console` | V-05 | Polymorphism | `refactor: let products report whether they receive a warranty (V-05)` / PR of Developer 1 | Developer 1 |
| `ProductPersistence` | The `instanceof` and type code chains of `toLine` and `fromLine` replaced by a writer per class and a reader per type code, registered in the constructor; the file format does not change | V-06 | Polymorphism, Protected Variations | `refactor: register a file format per product type in ProductPersistence (V-06)` / PR of Developer 1 | Developer 1 |
| `Return`, `Warranty` | Private `formatAmount` removed; the receipt and the certificate use `CurrencyFormatter.format` | V-09 | Pure Fabrication | `refactor: format amounts with CurrencyFormatter in Return and Warranty (V-09)` / PR of Developer 1 | Developer 1 |
| `AccessoryService` | `hasEnoughStock`, `reduceStock`, and `restoreStock` call the stock methods of `Accessory`; `updateStock` keeps `setStock` because it replaces the stock with a typed value | V-01 | Information Expert | `refactor: use the stock operations of Accessory in AccessoryService (V-01)` / PR of Developer 2 | Developer 2 |
| `PromotionService` | `registerCategoryDiscount` asks `CategoryDiscount.isSupportedCategory(...)` instead of repeating the list of categories | V-03 | Protected Variations | `refactor: ask CategoryDiscount for the supported categories in PromotionService (V-03)` / PR of Developer 2 | Developer 2 |
| `AccessoryService` | `findAccessoriesCompatibleWith` and `addCompatibleConsole` ask only for `ConsoleCompatibleAccessory` instead of `Controller` and `Memory` | V-04 | Polymorphism | `refactor: find compatible accessories through ConsoleCompatibleAccessory (V-04)` / PR of Developer 2 | Developer 2 |
| `WarrantyService`, `ReturnService` | `validateConsole` renamed `validateWarrantyEligible`; both services ask `product.isWarrantyEligible()` instead of `instanceof Console` | V-05 | Polymorphism | `refactor: ask products for warranty eligibility in WarrantyService and ReturnService (V-05)` / PR of Developer 2 | Developer 2 |
| `PersonPersistence` | Writer per class and reader per type code registered in the constructor (clients and sellers); the file format does not change | V-07 | Polymorphism, Protected Variations | `refactor: register a file format per person type in PersonPersistence (V-07)` / PR of Developer 2 | Developer 2 |
| `AccessoryRepository` | Writer per class and reader per type code (memories, controllers, and cables); the compatible consoles are written and read once for every `ConsoleCompatibleAccessory`; the file format does not change | V-07 | Polymorphism, Protected Variations | `refactor: register a file format per accessory type in AccessoryRepository (V-07)` / PR of Developer 2 | Developer 2 |
| `PromotionRepository` | Writer per class and reader per type code (percentage, category, and bulk purchase); the file format does not change | V-07 | Polymorphism, Protected Variations | `refactor: register a file format per promotion type in PromotionRepository (V-07)` / PR of Developer 2 | Developer 2 |
| `WarrantyRepository`, `WarrantyRecord`, `WarrantyService` | Each warranty type is registered once in the repository with its code and its constructor; the repository writes the code of the class and creates the warranty of a record (`toWarranty`), so `WarrantyService.resolveWarranties` no longer chooses the class by its code | V-07 | Polymorphism, Protected Variations | `refactor: register the warranty types in WarrantyRepository (V-07)` / PR of Developer 2 | Developer 2 |
| `ProductService` | `restoreStock` calls `product.increaseStock(quantity)` | V-01 | Information Expert | `refactor: restore product stock through Product (V-01)` / PR of Technical Leader | Technical Leader |
| `SaleService`, `ConsoleUI` | `assignWarranties`, `containsConsole` (renamed `containsWarrantyEligibleProduct`), and `askExtendedWarranties` ask `product.isWarrantyEligible()` instead of `instanceof Console` | V-05 | Polymorphism | `refactor: ask products for warranty eligibility in SaleService and ConsoleUI (V-05)` / PR of Technical Leader | Technical Leader |
| `ConsoleUI` | `addCompatibleConsole` asks only for `ConsoleCompatibleAccessory` instead of `Controller` and `Memory` | V-04 | Polymorphism | `refactor: check console compatibility through ConsoleCompatibleAccessory in ConsoleUI (V-04)` / PR of Technical Leader | Technical Leader |
| `ConsoleInput`, `ErrorHandler` (new, `ui`) | `ConsoleInput` reads and converts the user input (`readLine`, `readOption`, `readPrice`, `readStock`, `readPercentage`, `readDate`); `ErrorHandler.executeOperation` shows the message of each category of error. Both moved from `ConsoleUI` without changes in their messages | V-08 | High Cohesion, Pure Fabrication | `refactor: extract ConsoleInput and ErrorHandler from ConsoleUI (V-08)` / PR of Technical Leader | Technical Leader |
| `ProductMenu`, `PersonMenu`, `SaleMenu`, `AccessoryMenu`, `PromotionMenu`, `ReturnMenu`, `WarrantyMenu` (new, `ui`) | Each submenu and its operations moved from `ConsoleUI` to its own class with a `show()` method; every message stays the same | V-08 | High Cohesion | `refactor: move each submenu of ConsoleUI to its own class (V-08)` / PR of Technical Leader | Technical Leader |
| `ConsoleUI` | Keeps only the main menu and delegates each option to its submenu (from 1,479 to 173 lines) | V-08 | High Cohesion | `refactor: keep only the main menu in ConsoleUI (V-08)` / PR of Technical Leader | Technical Leader |

## Pattern trade-offs

- **V-04, `instanceof ConsoleCompatibleAccessory`.** `AccessoryService`
  and `ConsoleUI` still check a type, but the type is now the abstraction
  of every accessory with console compatibility, not two concrete
  classes: a new accessory with compatibility only has to extend it. The
  alternative, declaring the compatibility methods in `Accessory`, would
  give cables operations that they cannot support, which goes against
  High Cohesion. We kept the check on the abstraction.
- **V-06 and V-07, formats registered in the repositories.** The file
  format of each type could be a polymorphic method of the model classes,
  but the model must not know about files (layer rule of the project), so
  the polymorphism is applied inside the persistence layer (Pure
  Fabrication): one registered writer and reader per type. Adding a type
  means one new registration in the repository, without editing any
  conditional.
- **V-09, new dependency on `com.gamezone.util`.** `Sale`, `Return`, and
  `Warranty` now depend on `CurrencyFormatter`. It is a dependency on a
  stable helper without state, in a cross-cutting package (like
  `exceptions`), so Low Coupling is not affected in practice, and it
  removes four copies of the same method.
- No correction moved a violation to the out-of-scope findings: the nine
  violations were corrected without changing the external behavior.

## Final non-regression result

Commit: last correction of `feature/grasp-refactor`
(`refactor: keep only the main menu in ConsoleUI (V-08)`), with
the corrections of the three members — 2026-10-17 — result: **no
differences**. The seven scenarios were run on the final version and
compared with a new reference run of `develop` before the corrections
(`44d8f9e`) made on the same day: `compare-regression.ps1 -Reference
reference -Candidate final` reported the same screen output and the same
content in every data file of every scenario (the 33 menu operations, the
error cases, and the five data file scenarios). The project compiles
with `mvn clean compile`.

Verifications made in each Pull Request, on the operations affected by
its corrections:

| Pull Request | Violations corrected | Scenarios compared | Result |
|---|---|---|---|
| Developer 1 (with the preparation of the Technical Leader) | V-01, V-02, V-03, V-04, V-05, V-06, V-09 | All seven (products, accessories, promotions, sales, returns, warranties, and every data file) | No differences |
| Developer 2 | V-01, V-03, V-04, V-05, V-07 | All seven (accessories, promotions, returns, warranties, and the reload and corrupted data scenarios) | No differences |
| Technical Leader | V-01, V-04, V-05, V-08 | All seven (every operation, since the menu was divided) | No differences |
