# AI Usage Log — Technical Leader (Juan David)

This log documents the use of AI (Claude) during the development of the
GameZone Unicesar system, as required by the workshop's AI usage policy.
Per an in-class clarification from the professor, the team was authorized
to use AI to generate complete content (code, diagrams, documentation),
provided every member understands the reasoning behind it well enough to
defend it during the oral presentation.

## 1. Code review of existing model, persistence, and service classes

**What I asked:** Reviewed the already-implemented classes (`Person`,
`Client`, `Seller`, `Product`, `VideoGame`, `Console`, `ProductService`,
`ProductPersistence`, `PersonService`, `PersonPersistence`) against the
workshop's restrictions (private attributes, abstract base classes,
layered dependencies).

**What I learned:** Confirmed that encapsulation, inheritance, and layer
separation were correctly applied. Understood why exposing internal
lists directly (e.g. `getProducts()`) breaks encapsulation, and why a
defensive copy (`new ArrayList<>(...)`) is needed instead.

## 2. Sale.java and SaleService.java review and fixes

**What I asked:** Reviewed `Sale.java` for correctness against our
`analysis.md` decision that `Sale` should calculate its own total
(Information Expert principle).

**What I learned and applied:**
- Added a defensive copy in `getProducts()` so external code cannot
  mutate the sale's internal product list.
- Added `toString()` for consistency with the rest of the domain model
  and to support console output.
- Discussed a potential bug in `SaleService.registerSale()` around
  validating stock when the same product appears more than once in the
  list (representing quantity > 1). As a team, we decided to keep the
  simpler per-item validation for this workshop's scope, understanding
  and accepting the trade-off (each occurrence is validated and reduced
  independently, without aggregating quantities).

## 3. ConsoleUI implementation

**What I asked:** Requested help implementing the three submenus
(products, people, sales) inside `ConsoleUI.java`, following the ten
functional operations required by the workshop.

**What I learned:** Understood why each submenu method only calls
`service` layer methods (never `persistence` directly), how
`instanceof Client client` pattern matching is needed because
`PersonService.findById()` returns the abstract `Person` type
polymorphically, and how input validation helpers (`readPrice`,
`readStock`) prevent `NumberFormatException` from crashing the console
loop.

## 4. Main.java wiring

**What I asked:** Requested the final `Main.java`, wiring the three
services and starting `ConsoleUI`.

**What I learned:** Understood the role of `Main` as the composition
root — the only class responsible for creating and injecting all
dependencies, keeping every other layer decoupled from object creation.

## 5. Diagnosing and fixing a Git Flow violation

**What I asked:** Asked for help understanding why `PersonService.java`
was missing when trying to compile the sale/UI module locally.

**What I learned:** Discovered that a teammate's Pull Request had been
merged directly into `main` instead of `develop`, violating our own
Git Flow convention (`main` should only receive merges from `develop`).
Understood the fix: create a dedicated branch (`chore/sync-main-into-develop`)
from `develop`, merge `origin/main` into it, and open a properly
reviewed Pull Request into `develop` — instead of rewriting history or
force-pushing, which the workshop explicitly forbids.

## 6. Git troubleshooting

**What I asked:** Requested help resolving several Git issues during
this process:
- Recovering from an aborted merge stuck in the Vim editor.
- Understanding why `git merge origin/main` reported "Already up to
  date" incorrectly (stale local remote-tracking references — needed
  `git fetch origin` first).
- Losing an uncommitted merge after a `git merge --abort` and having to
  redo it cleanly with `git merge origin/main --no-edit`.

**What I learned:** The importance of `git fetch` before merging from a
remote branch, and how `git merge --abort` fully discards an in-progress
merge, including any work not yet committed.

## 7. Runtime bug: seller data not loading correctly

**What I asked:** Investigated why only 2 of 3 pre-loaded sellers were
being listed by the application.

**What I learned:** The `data/persons.txt` file, generated via
PowerShell with `-Encoding UTF8`, included a Byte Order Mark (BOM) at
the start of the file. This invisible character was prepended to the
first line's `SELLER` type identifier, causing the type comparison in
`PersonPersistence.fromLine()` to fail silently for that one record.
Fixed by regenerating the file with `-Encoding ascii`, which does not
add a BOM.

## 8. Repository structure review

**What I asked:** Compared our actual repository layout against the
one required by the workshop (page 15 of the assignment PDF).

**What I learned:** Our `pom.xml` and `src/` currently live inside a
nested `gamezone-unicesar/` folder instead of the repository root,
while `data/`, `docs/`, `README.md`, and `TEAM.md` are correctly at the
root. This mismatch needs to be corrected before final submission.