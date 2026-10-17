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
