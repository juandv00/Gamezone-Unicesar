# SOLID Refactor Log — GameZone Unicesar

Change log of Phase 2 of Requirement 8. Every change corrects a violation
of the consolidated audit (`docs/solid-audit/consolidated-audit.md`) and
keeps the external behavior of the system: the same inputs produce the
same results, the same messages, and the same content in the data files.

## Non-regression strategy

The strategy follows the answer to guiding question 4
(`docs/solid-analysis.md`) and reuses the scenarios and scripts of
Requirement 7, which are in `docs/regression/` (see
`docs/grasp-refactor-log.md` for the description of each file). The same
inputs, which run every operation of the menu, are executed on `develop`
before the first correction (reference result), on the branch before each
Pull Request (on the operations affected by its corrections; the scripts
always run the seven scenarios), and at the end of the phase (on every
operation).

How to run it, from the root of the project:

```powershell
powershell -ExecutionPolicy Bypass -File docs/regression/run-regression.ps1 -Label reference -Source 6fcea3a
powershell -ExecutionPolicy Bypass -File docs/regression/run-regression.ps1 -Label final
powershell -ExecutionPolicy Bypass -File docs/regression/compare-regression.ps1 -Reference reference -Candidate final
```

The first command compiles the code of `6fcea3a` (`develop` with the
consolidated audit, before the corrections) without changing the current
files; the second one compiles the current code of the branch.

Expected result: `RESULT: no differences`. The dates of the day (sales,
returns, and warranties use the current date) are the only allowed
difference, and only when the two runs are on different days; in that
case the comparison is run with `-IgnoreDates`.

What each group of scenarios protects in these corrections:
- `main` prints every sale receipt, return receipt, and warranty
  certificate, and the monthly balance, so it checks that the new
  formatters (V-01) and `BalanceService` (V-02) produce exactly the same
  text, and that the main menu built from the list of submenus (V-03)
  shows the same lines.
- `noread`, `corrupt`, `corruptproducts`, and `reload` start the
  application with files that cannot be read, corrupted files, or the
  saved data. They check that `Main`, which now creates every storage
  (V-04, V-05, V-06), loads the files in the same order and shows the same
  messages.
- `errors` and `nowrite` check that the error messages and the files
  written after an error do not change when the services depend on roles
  and storages instead of concrete classes (V-05, V-07).

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

Commit: `6fcea3a` (merge of Pull Request #55, `develop` with the
consolidated audit; the source code is the same as in the audited commit
`75cc180`) — 2026-10-18 — result: the seven scenarios ran completely and their
output was kept as the reference (`target/regression/reference`).

| Scenario | Lines of screen output | Data files written by the application |
|---|---|---|
| `main` | 883 | `accessories.csv`, `persons.txt`, `products.txt`, `promotions.csv`, `returns.csv`, `sales.txt`, `warranties.csv` |
| `errors` | 742 | `accessories.csv`, `persons.txt`, `products.txt`, `promotions.csv`, `sales.txt`, `warranties.csv` |
| `nowrite` | 61 | `persons.txt`, `promotions.csv` (the accessories file is a folder) |
| `noread` | 2 | The application does not start: "No fue posible cargar los datos del sistema. Contacte al administrador." |
| `reload` | 307 | The same files as `main`, loaded and listed again |
| `corrupt` | 169 | Three "Se omitió una línea inválida…" messages; lines of unknown types ignored |
| `corruptproducts` | 2 | The application does not start (same message as `noread`) |

The out-of-scope defects O-01 to O-03 of Requirement 7 keep their current
behavior and are part of the reference.

## Changes

One row per change. The Commit / PR column names the commit and the Pull
Request that contains it. Each member opened a Pull Request from
`feature/solid-refactor` to `develop`: the Pull Request of Developer 1
also contains the preparation of the Technical Leader, and the last Pull
Request of the Technical Leader also contains the cleanup commits of the
two developers, which remove the old code once nobody uses it.

| Class | Change | Violation ID | Principle applied | Commit / PR | Responsible |
|---|---|---|---|---|---|
| `SaleStorage` (new, `persistence`) | New interface with `save(List<Sale>)` and `load()` | V-06 | Dependency Inversion | `refactor: add the SaleStorage abstraction implemented by SalePersistence (V-06)` / PR of Developer 1 | Technical Leader |
| `SalePersistence` | Implements `SaleStorage` (`@Override` on `save` and `load`); created before Developer 2 starts, because `WarrantyService` needs it | V-06 | Dependency Inversion | `refactor: add the SaleStorage abstraction implemented by SalePersistence (V-06)` / PR of Developer 1 | Technical Leader |
| `SaleLookup` (new, `service`) | New role interface with the read-only queries of the sales: `findById`, `getSaleById`, and `listAll` | V-07 | Interface Segregation | `refactor: add the SaleLookup role implemented by SaleService (V-07)` / PR of Developer 1 | Technical Leader |
| `SaleService` | Implements `SaleLookup` (`@Override` on the three queries, including `getSaleById` of Developer 2, without changing it) | V-07 | Interface Segregation | `refactor: add the SaleLookup role implemented by SaleService (V-07)` / PR of Developer 1 | Technical Leader |
| `ProductStorage` (new, `persistence`) | New interface with `save(List<Product>)` and `load()` | V-04 | Dependency Inversion | `refactor: make ProductService depend on the ProductStorage abstraction (V-04)` / PR of Developer 1 | Developer 1 |
| `ProductPersistence` | Implements `ProductStorage` | V-04 | Dependency Inversion | `refactor: make ProductService depend on the ProductStorage abstraction (V-04)` / PR of Developer 1 | Developer 1 |
| `ProductService` | The field is a `ProductStorage`; new constructor `ProductService(ProductStorage)`. The constructor without parameters is kept for now and passes a new `ProductPersistence` to the new one, until `Main` passes the storage. The renamed field also appears in the line of `restoreStock` that saves the products | V-04 | Dependency Inversion | `refactor: make ProductService depend on the ProductStorage abstraction (V-04)` / PR of Developer 1 | Developer 1 |
| `ProductLookup`, `ProductInventory` (new, `service`) | `ProductLookup` with `findById` and `getProductById`; `ProductInventory` extends it with `reduceStock` and `restoreStock` | V-07 | Interface Segregation | `refactor: split the lookup and inventory roles of ProductService into interfaces (V-07)` / PR of Developer 1 | Developer 1 |
| `ProductService` | Implements `ProductInventory` (`@Override` on the four methods, including `restoreStock` of the Technical Leader and `getProductById` of Developer 2, without changing them) | V-07 | Interface Segregation | `refactor: split the lookup and inventory roles of ProductService into interfaces (V-07)` / PR of Developer 1 | Developer 1 |
| `ReturnReceiptFormatter` (new, `ui`) | Static method `format(Return)` that builds exactly the same text as `Return.generateReturnReceipt` | V-01 | Single Responsibility | `refactor: move the return receipt text to ReturnReceiptFormatter (V-01)` / PR of Developer 1 | Developer 1 |
| `Return` | The private `refundFor` becomes the public `calculateRefundFor(double)`, used by the formatter; the old receipt is kept until the menu uses the formatter | V-01 | Single Responsibility | `refactor: move the return receipt text to ReturnReceiptFormatter (V-01)` / PR of Developer 1 | Developer 1 |
| `WarrantyCertificateFormatter` (new, `ui`) | Static method `format(Warranty)` that builds exactly the same text as `Warranty.generateWarrantyCertificate` | V-01 | Single Responsibility | `refactor: move the warranty certificate text to WarrantyCertificateFormatter (V-01)` / PR of Developer 1 | Developer 1 |
| `PersonStorage` (new, `persistence`) | New interface with `save(List<Person>)` and `load()`, implemented by `PersonPersistence` | V-05 | Dependency Inversion | `refactor: make PersonService depend on the PersonStorage abstraction (V-05)` / PR of Developer 2 | Developer 2 |
| `PersonService` | The field is a `PersonStorage`; new constructor `PersonService(PersonStorage)`; the constructor without parameters is kept for now and passes a new `PersonPersistence` | V-05 | Dependency Inversion | `refactor: make PersonService depend on the PersonStorage abstraction (V-05)` / PR of Developer 2 | Developer 2 |
| `AccessoryStorage`, `PromotionStorage` (new, `persistence`) | New interfaces with `saveAll` and `loadAll`, implemented by `AccessoryRepository` and `PromotionRepository` | V-05 | Dependency Inversion | `refactor: make AccessoryService and PromotionService depend on storage abstractions (V-05)` / PR of Developer 2 | Developer 2 |
| `AccessoryService`, `PromotionService` | Receive and keep the storage interface instead of the concrete repository | V-05 | Dependency Inversion | `refactor: make AccessoryService and PromotionService depend on storage abstractions (V-05)` / PR of Developer 2 | Developer 2 |
| `ReturnStorage`, `WarrantyStorage` (new, `persistence`) | `ReturnStorage` with `saveAll` and `loadAll`; `WarrantyStorage` also with `toWarranty`; implemented by `ReturnRepository` and `WarrantyRepository` | V-05 | Dependency Inversion | `refactor: make ReturnService and WarrantyService depend on storage abstractions (V-05)` / PR of Developer 2 | Developer 2 |
| `ReturnService`, `WarrantyService` | Receive `ReturnStorage` and `WarrantyStorage`; `WarrantyService` receives `SaleStorage` instead of `SalePersistence` | V-05 | Dependency Inversion | `refactor: make ReturnService and WarrantyService depend on storage abstractions (V-05)` / PR of Developer 2 | Developer 2 |
| `AccessoryInventory` (new, `service`) | New role interface with `findById`, `hasEnoughStock`, `reduceStock`, and `restoreStock`, implemented by `AccessoryService` | V-07 | Interface Segregation | `refactor: make ReturnService and WarrantyService depend on lookup and inventory roles (V-07)` / PR of Developer 2 | Developer 2 |
| `ReturnService`, `WarrantyService` | `ReturnService` depends on `SaleLookup`, `ProductInventory`, and `AccessoryInventory`; `WarrantyService` depends on `ProductLookup` | V-07 | Interface Segregation | `refactor: make ReturnService and WarrantyService depend on lookup and inventory roles (V-07)` / PR of Developer 2 | Developer 2 |
| `ReturnLookup` (new, `service`) | New role interface with `viewAllReturns`, `viewReturnsByCustomer`, and `viewReturnsBySale`, implemented by `ReturnService` | V-02 | Single Responsibility (with Interface Segregation) | `refactor: add the ReturnLookup role implemented by ReturnService (V-02)` / PR of Developer 2 | Developer 2 |
| `BalanceService` (new, `service`) | The monthly balance (`calculateMonthlySales`, `calculateMonthlyReturns`, `generateMonthlyBalance`, and the check of the month) with the same calculations and message; it depends on `SaleLookup` and `ReturnLookup`. The methods of `ReturnService` are kept until the menu uses the new class | V-02 | Single Responsibility | `refactor: move the monthly balance to BalanceService (V-02)` / PR of Developer 2 | Developer 2 |
