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

## 9. Exam Requirement 1: integrating the accessory module

**What I asked:** After the exam week, with the professor's authorization
to use AI for integration, I asked for help integrating the accessory
module (`Accessory`, `Controller`, `Cable`, `Memory`, `AccessoryRepository`,
`AccessoryService`), already merged by Developer 1 and Developer 2, into
the sales flow. The work covered `Sale`, `SaleService`, `SalePersistence`,
`Main`, and `ConsoleUI`, with each class verified against the real
repository and compiled before committing.

**What I learned and applied:**
- Additive changes: `Sale` gained an `accessories` list and two new
  constructors, while the original constructors now delegate to the full
  one with an empty list. Existing callers kept compiling without changes.
- Dependency injection: `SaleService` received a new constructor that
  takes `AccessoryService` and `SalePersistence` as parameters, so `Main`
  acts as the composition root and decides how the objects are wired.
- Stock validation with repeated items: the new `registerSale(...)`
  overload counts the units of each item before validating stock, and it
  validates every item before reducing anything, so a rejected sale leaves
  the inventory untouched.
- Backward-compatible persistence: `SalePersistence` writes accessory ids
  as an optional fifth field. Sales stored before the change still load
  correctly.
- Missing service operation: `AccessoryService` had no way to register
  and persist console compatibility, so the query for compatible
  accessories would always be empty. I added one additive method,
  `addCompatibleConsole(accessoryId, consoleId)`, and notified the
  reviewers in the Pull Request because it is a teammate's class.
- Consistency between documentation and code: our first answer to
  question 12 of `analysis.md` said accessories should extend `Product`,
  which contradicted the implemented independent hierarchy. I rewrote it
  to justify the real design. I also noticed that allowing sales with
  only accessories broke the business rule stated in question 7 (a sale
  needs at least one product), so I fixed the code to respect that rule
  instead of changing the documentation.

## 10. Git and IDE issues during Requirement 1

**What I asked:** Requested help with several problems that came up
while integrating Requirement 1.

**What I learned:**
- `git checkout feature/accessory-module` failed with "pathspec did not
  match" because `git pull origin develop` only downloads `develop`.
  Running `git fetch origin` first made the remote branch known locally,
  and the checkout then created a tracking branch.
- Uncommitted local changes follow you when switching branches. Before
  starting, I reviewed them with `git diff`, discarded a whitespace-only
  change with `git restore`, and kept my documentation change for a
  separate `docs:` commit.
- IntelliJ showed "file is read only" because I had opened the compiled
  `Sale.class` inside `target/classes` instead of the source file under
  `src/main/java`. The `target/` folder is generated by Maven and ignored
  by Git.
- IntelliJ re-indented lines when pasting code, which added
  whitespace-only changes to a teammate's lines. I learned to check the
  expected line counts with `git diff --stat` before committing, and that
  an unpushed commit can be replaced with `git commit --amend --no-edit`.
  Copying the file directly with `Copy-Item` avoids the reformatting.
- I staged files one by one instead of using `git add .`, keeping each
  commit atomic (one concern per commit) and each commit compiling on
  its own.

## 11. Exam Requirement 2: integrating the promotion module

**What I asked:** Requested help integrating the promotion module into
the system. Developer 1 implemented the model (`Promotion`,
`PercentageDiscount`, `CategoryDiscount`, `BulkPurchaseDiscount`) and
Developer 2 the persistence and service (`PromotionRepository`,
`PromotionService`). My part covered `Sale`, `SalePersistence`,
`SaleService`, `ConsoleUI`, `Main`, `README.md`, and the documentation
(`promotion-analysis.md` and `promotion-class-diagram.md`). Every class
was checked against the real repository and compiled before committing.

**What I learned and applied:**
- Checking the specification against the real code: the assignment
  mentioned a `generateReceipt` method and a `ConsoleMenu` class, but our
  `Sale` only had `toString()` and our menu class is `ConsoleUI`. I
  created `generateReceipt()` and worked on `ConsoleUI` instead of
  assuming the names in the document.
- Additive changes without breaking behavior: `calculateTotal()` kept its
  meaning (the subtotal) and the final total went into a new method,
  `calculateFinalTotal()`. This also avoids a circular calculation,
  because the promotions calculate their discount on the subtotal.
- Separating the work that did not depend on my teammates: `Sale` and
  `SalePersistence` were finished while the other developers worked, and
  I proposed the exact method signatures of `PromotionService` to
  Developer 2 before he wrote them, so the integration fit without
  changes.
- Backward-compatible persistence: the promotion name and discount are
  stored as optional fields at the end of each sale line, so sales saved
  before the module existed still load.
- Defense in depth: since the model does not validate the percentage
  range, `SaleService` caps the discount at the sale subtotal, and
  `ConsoleUI` validates percentages (0 to 100), dates, and ids when a
  promotion is registered.
- Polymorphism in practice: `ConsoleUI` lists promotions through their
  `toString()` and `PromotionService` compares them through
  `calculateDiscount()`, without checking their concrete types.
- Writing documentation after the code: the analysis answers and the
  Mermaid diagram describe the implemented classes, avoiding the mismatch
  we had with question 12 in Requirement 1. The diagram's syntax was
  validated with the Mermaid parser before committing.

## 12. Code review and Git Flow issues during Requirement 2

**What I asked:** Requested help reviewing my teammates' code and solving
several Git and GitHub problems during Requirement 2.

**What I learned:**
- A Pull Request includes every commit of its branch, not a single
  commit. I approved a PR after reviewing only the first commits, so I
  reviewed the rest of the code afterwards. Since then, reviews are done
  in the "Files changed" tab, which shows all the changes together.
- GitHub's "Revert" button only creates a new branch with a revert commit
  and proposes a Pull Request; nothing changes in `develop` until that PR
  is merged. I deleted the unused revert branch.
- Reviewing code by testing it: Developer 1's discount calculations were
  correct, but the classes do not validate their data (a 150% promotion
  produced a discount larger than the sale). Developer 2's code accepted
  duplicate ids, and one malformed line in `promotions.csv` crashed the
  application on startup with a `DateTimeParseException`. These were
  fixed with separate `fix:` commits before approving the PR.
- With a shared feature branch, any commit pushed while a Pull Request is
  open is added to that PR. I waited for my teammates' PR to be merged
  before pushing my integration, and Developer 1 approved the PR that
  included my commits, so nobody approved their own code.
- A PR opened against the wrong base branch (`main` instead of `develop`)
  can be fixed with the "Edit" button, without closing it.
- When local and remote histories diverge, `git pull` creates a merge
  commit and opens an editor for its message. The editor (vim) failed in
  the IDE terminal, leaving the merge unfinished; `git commit --no-edit`
  completed it, `git commit --amend -m "..."` fixed a merge message that
  included the editor's help text, and `git pull --no-edit` avoids the
  problem.
- The shared branch must not be deleted when merging intermediate Pull
  Requests; it is deleted only after the last one, as the assignment
  requires.

## 13. Exam Requirement 3: integrating the return module

**What I asked:** Requested help integrating the return module into the
system. Developer 1 implemented the model (`Return` and
`Sale.canBeReturned()`) and Developer 2 the persistence and service
(`ReturnRepository` and `ReturnService`, including the monthly balance).
My part covered `ProductService.restoreStock`, the return submenu and the
monthly balance report in `ConsoleUI`, `Main`, `README.md`, and the
documentation (`return-analysis.md` and `return-class-diagram.md`). Every
class was checked against the real repository and compiled before
committing.

**What I learned and applied:**
- Finding a missing prerequisite before starting: the specification
  refers to sales by id (`registerReturn(String saleId, ...)`), but our
  `Sale` class had no id. Before my teammates started, I added a unique
  id to `Sale`, stored it in `SalePersistence` (keeping old lines
  readable), and added `SaleService.findById`. Sales stored before the
  change receive an id when they are loaded, and it is saved immediately
  so it stays the same.
- Unblocking the team first: `restoreStock` and the sale id were
  committed before my teammates began, because their code depended on
  them. Then I sent them the exact method signatures and the business
  decisions the specification left open.
- Documenting business decisions that the specification does not define:
  the refund uses list prices even if the sale had a discount, the
  monthly balance uses the final total of each sale, a unit cannot be
  returned twice, and accessories are not returnable.
- Asking for what the interface needs: the report must show three
  values, but `generateMonthlyBalance` returns only one, so
  `ReturnService` also provides `calculateMonthlySalesTotal` and
  `calculateMonthlyReturnsTotal`.
- Model and service share the validation: `Sale.canBeReturned()` defines
  the 30-day rule, `ReturnService` enforces it, and `ConsoleUI` only uses
  it to warn the user early.
- Reusing the stock logic: `restoreStock` follows the same approach as
  the existing `reduceStock` in `ProductService`, so all stock changes go
  through one class that also saves the file.
- Being honest in the documentation: the analysis explains that
  `restoreStock` was added in this requirement, and what was reused from
  Workshop 1, instead of claiming it already existed.

## 14. Code review during Requirement 3

**What I asked:** Requested help reviewing my teammates' Pull Requests
before approving them, and handling the review of a shared branch.

**What I learned:**
- Checking that the PR contains what it claims: the first version of
  Developer 1's PR had no `Return` class at all, and one of his commits
  removed all the JavaDoc comments of `Sale` (90 comment lines) while
  adding a single method. Reading the diff stats ("104 deletions" for a
  small change) revealed the problem.
- Testing code instead of only reading it: the first version of `Return`
  accepted null or empty data and started with a refund of 0, and the
  first version of `ReturnService` allowed returning the same unit
  several times. In a test, one game sold for 200,000 produced
  1,000,000 in refunds and raised the stock above its initial value.
  Other issues: the balance used subtotals instead of final totals, the
  30-day rule was duplicated instead of using `canBeReturned()`, and a
  malformed line in `returns.csv` crashed the application on startup.
- Using "Request changes" and fix commits: the PRs stayed open until the
  problems were fixed, and the fixes were added as new commits to the
  same PR, which I reviewed again before approving.
- Verifying the fix, not just the commit: one "fix" commit only changed
  the repository class and left the service untouched, so the errors
  were still there. Comparing the pushed files with the expected version
  showed it.
- Who approves: a PR on a shared branch includes every commit not yet in
  `develop`. Developer 1's PR contained my sale-id commits, so Developer 2
  approved it; Developer 2's PR contained only his commits, so I
  approved it.
- A repository inside OneDrive can make Git fail to delete folders
  ("Deletion of directory ... failed"), because the synchronization locks
  them. The branch was deleted anyway; only an empty log folder
  remained.

## 15. Exam Requirement 4: integrating the warranty module

**What I asked:** Requested help integrating the warranty module into the
system. Developer 1 implemented the model (`Warranty`, `BasicWarranty`,
`ExtendedWarranty`) and Developer 2 the persistence and service
(`WarrantyRepository` and `WarrantyService`). My part covered the
warranty cost in `Sale` and `SalePersistence`, the warranty assignment
in `SaleService.registerSale`, the extended warranty question and the
warranty submenu in `ConsoleUI`, `Main`, `README.md`, and the
documentation (`warranty-analysis.md` and `warranty-class-diagram.md`).
Every class was checked against the real repository and compiled before
committing.

**What I learned and applied:**
- Detecting a circular dependency in the specification before writing
  code: `SaleService` would depend on `WarrantyService`, which depends on
  `WarrantyRepository`, and the specification suggested resolving sales
  in the repository through `SaleService`. None of the three objects
  could be created first in `Main`. The repository resolves sales through
  `SalePersistence` instead, which also respects the direction of the
  layers (persistence does not depend on services).
- Resolving an ambiguity with a team decision: following the
  specification literally, a console with an extended warranty would
  also get a basic one, so `findWarrantyByProduct` could not return a
  single warranty. We decided that the extended warranty replaces the
  basic one, and documented it.
- Preparing the model before the team: the warranty cost needed a place
  in the sale, so I added `warrantyCost` to `Sale` (included in
  `calculateFinalTotal()` and in the receipt) and to `SalePersistence`
  before my teammates started, without depending on their classes.
- Keeping the business rules consistent across modules: the promotion is
  calculated on the subtotal, so extended warranties are not discounted,
  and the monthly balance includes their income automatically because it
  already used `calculateFinalTotal()`.
- Extending a method without duplicating it: the existing
  `registerSale` with accessories now calls the new overload that
  receives `productIdsWithExtendedWarranty`, and the version with only
  products also assigns basic warranties.
- Validating before changing state: the requested extended warranties
  are checked against the consoles of the sale before any stock is
  reduced, so an invalid request leaves the inventory untouched.
- Handling case-insensitive input in the interface: the warranty lookup
  compares ids exactly, so `ConsoleUI` first finds the sale with
  `SaleService.findById` (case-insensitive) and passes its exact id.

## 16. Code review during Requirement 4

**What I asked:** Requested help reviewing my teammates' Pull Requests
for Requirement 4 before approving them.

**What I learned:**
- Clear agreements reduce corrections: this time I sent the exact method
  signatures, the business decisions, and the reason to use
  `SalePersistence` before the team started. Both Pull Requests matched
  the agreed design and were approved without requested changes, unlike
  in Requirement 3.
- Testing edge cases of dates: a basic warranty that starts on 2026-08-31
  ends on 2027-02-28, because `plusMonths` adjusts to the last day of the
  month. `isActive` was checked on the end date (true) and the day after
  (false).
- Testing the whole flow after integrating: a sale with two consoles
  with extended warranty, one console with basic warranty, and a video
  game produced three warranties, a warranty cost of 2 × 10% of the
  console price, a promotion calculated only on the subtotal, and a
  monthly balance that included the warranty income. The warranties were
  reloaded correctly after restarting the application.
- Reviewing design risks, not only results: the constructor of
  `Warranty` calls an overridable method (`getDurationInMonths()`); it is
  safe because both subclasses return constants, and this is documented
  in the analysis.
- Commit history matters for the evaluation: Developer 1 split his work
  into seven atomic commits, while Developer 2 used two. Commits that are
  already pushed cannot be split without `push --force`, which is not
  allowed, so the team should plan small commits from the start.

## Requirement 5 — Integration (entries in the required format)

### Planning the integration and the team's work
- **Date:** 2026-09-27
- **Tool:** Claude (Anthropic)
- **Phase and branch:** Phase 2 – planning (before `docs/accessory-documentation`)
- **Objective:** Compare the Requirement 5 specification with the real code in `develop` to know which integration adjustments were already covered, which were missing, and in which order the team could work.
- **Query:** Asked for help to decide who starts first, which branch depends on which, and to prepare a message for the team with the exact signatures so their own AI tools could help them.
- **Response:** A status table of A1–A9 (A2, A3, and A6 partially covered; A1, A4, A5, A7, A8, A9 pending), three missing deliverables (`accessory-analysis.md`, `accessory-class-diagram.md`, and `data/accessories.csv`), and a plan by phases with a separate branch per adjustment. It also noted that A4 and A5 both modify `Return`, so A5 should start after A4 is merged.
- **Decision:** Accepted the plan and shared it with the team. Accepted that A5 (proportional refund), A7 (warranty cancellation), and A2 (warranty references resolved in the service) replace decisions we had documented in Requirements 3 and 4, because Requirement 5 now defines them; these changes will be explained in `integration-analysis.md`.
- **Related commit:** — (planning, no commit)

### Accessory module documentation
- **Date:** 2026-09-27
- **Tool:** Claude (Anthropic)
- **Phase and branch:** Phase 2 – `docs/accessory-documentation`
- **Objective:** Create `docs/accessory-analysis.md` and `docs/accessory-class-diagram.md`, required by Requirement 5 and missing from the repository.
- **Query:** Asked to write both files from the real accessory classes and the answer to question 12 of `analysis.md`.
- **Response:** An analysis covering the independent hierarchy decision, the design of each subclass and its enums, console compatibility, persistence with a type discriminator, and the integration with sales; and a Mermaid diagram by layers, checked with the Mermaid parser.
- **Decision:** Accepted both files after checking the class members against the code. The file name `data/accessories.csv` is used because Developer 2 changes it in `fix/accessory-data-file` during the same phase.
- **Related commit:** `docs: add accessory module analysis`, `docs: add accessory module class diagram`

### A3 — Unified sale registration flow
- **Date:** 2026-09-27
- **Tool:** Claude (Anthropic)
- **Phase and branch:** Phase 3 – `refactor/unified-sale-registration`
- **Objective:** Reorganize `SaleService.registerSale` into the eight steps required by Requirement 5, and let the console sale flow select products and accessories in one list.
- **Query:** Asked to compare the current `registerSale` methods with the eight steps of A3 and reorganize them without breaking the existing callers.
- **Response:** The three existing `registerSale` overloads now delegate to one private method (`processSale`) that follows the required order: validate items, validate stock, create the sale, apply the promotion on the subtotal, assign warranties, final total, update the inventory by item type, and persist. A new `registerSaleByItemIds` resolves each id as a product or an accessory. In `ConsoleUI`, products and accessories are entered in the same list.
- **Decision:** Accepted. Two differences from the previous behavior were kept on purpose and will be documented in `integration-analysis.md`: the stock is now reduced after the promotion and the warranties (step 7), and a sale with only accessories is allowed because A3 requires "at least one item". Warranties are still persisted by `WarrantyService` when they are assigned (step 5), because its API saves them on each assignment. Tested an end-to-end sale (console + video game + accessory), an accessory-only sale, rejection without stock changes, and the old product-only method.
- **Related commit:** `refactor: unify sale registration flow in SaleService`, `refactor: select products and accessories in one list in ConsoleUI`

### Reviewing the Pull Requests of phases 3 and 4
- **Date:** 2026-10-11
- **Tool:** Claude (Anthropic)
- **Phase and branch:** Phases 3 and 4 – reviews of #34, #36, #37, #38, and #39
- **Objective:** Check every Pull Request of my teammates before approving it, and solve the process problems that appeared during phase 4.
- **Query:** Asked to compare each branch with `develop` (commits, changed lines, and files), compile every commit, test each adjustment end to end, and explain how to recover from a Pull Request merged into the wrong branch and from a merge conflict.
- **Response:** Every Pull Request was compiled and tested before approving it. Two incidents were solved: PR #35 (A6) was merged into `main` instead of `develop`, so the branch was restored and merged again through #36 with base `develop`; and the A5 branch was created from an outdated `develop`, so Developer 1 merged `origin/develop` into it and resolved the conflict in `Return.java` and his AI log.
- **Decision:** Accepted. From then on I check that every Pull Request says "into develop" before approving, and every branch starts with `git checkout develop` and `git pull`. `main` was not reverted after #35, because its content was the same as `develop`; it is updated in A9. A small fix (conflict markers left in the AI log) was added as a new commit to the same Pull Request instead of requesting changes.
- **Related commit:** — (reviews and approvals, no commit of mine)

### Dependency direction in the return module
- **Date:** 2026-10-11
- **Tool:** Claude (Anthropic)
- **Phase and branch:** Phase 5 – review before `docs/integration-documentation`
- **Objective:** Check the integrated code against the restrictions of the workshop before documenting it.
- **Query:** Asked to review the dependencies between layers of the integrated system before writing the integrated class diagram.
- **Response:** `ReturnRepository` (persistence) received `SaleService` and `ProductService` (service) to resolve sales and products when loading returns, which breaks the dependency direction ui → service → persistence → model. The fix follows the approach of A2: a `ReturnRecord` with ids, and `ReturnService` resolves it.
- **Decision:** Accepted. Developer 2 applied it in `refactor/return-repository-dependencies` before A8, so the documentation describes the corrected design. The file format did not change, and the returns stored before the change loaded with the same values.
- **Related commit:** Developer 2's commits in `refactor/return-repository-dependencies`

### A8 — Integration documentation
- **Date:** 2026-10-11
- **Tool:** Claude (Anthropic)
- **Phase and branch:** Phase 5 – `docs/integration-documentation`
- **Objective:** Create `docs/integration-analysis.md` and `docs/integrated-class-diagram.md`, and update `docs/layers-diagram.md` and `README.md` with every feature of the integrated system.
- **Query:** Asked to write the documentation from the real code in `develop` and the history of the Pull Requests: each adjustment A1–A7 with its cause and solution, the decisions of earlier requirements that changed, and the incidents of the process.
- **Response:** An analysis with every adjustment, the class names that differ from the specification (`SalePersistence` for `SaleRepository`, `ConsoleUI` for `ConsoleMenu`), the incidents, and a table of replaced decisions; a single Mermaid class diagram with the four layers and every module; a layers diagram with all the classes; and a README with the integrated operations and business rules.
- **Decision:** Accepted after checking each class member against the code and validating both diagrams with the Mermaid parser. Corrected one point of the first draft: our Requirement 4 version had already avoided the circular dependency of A2 by using `SalePersistence`, so the analysis explains what A2 actually changed. Before writing the verification section, I ran the full scenario of the presentation with the real application (sale with a console, a video game, and an accessory; partial return of the console and the accessory; monthly balance) and documented the actual values.
- **Related commit:** `docs: add integration analysis`, `docs: add integrated class diagram`, `docs: update layers diagram and README for the integrated system`, `docs: add Requirement 5 phase 4 and A8 entries to leader AI log`


## Requirement 6 — Validations and Exceptions (entries in the required format)

### Planning and design of the exception and validation layers
- **Date:** 2026-10-11
- **Tool:** Claude (Anthropic)
- **Phase and branch:** Requirement 6 – `feature/exception-handling` (analysis before coding)
- **Objective:** Answer the five guiding questions, draw the class diagram, and decide the order of work, since the three members share one branch.
- **Query:** Asked to compare the specification with the real code (which uses `ProductPersistence`, `PersonPersistence`, `SalePersistence`, and `ConsoleUI` instead of the names of the specification) and to define the exact signatures and the decisions the specification leaves open.
- **Response:** A design where validators are static classes that depend only on the model and the exceptions; services keep `findById` returning null and add `get...ById` methods that throw `ResourceNotFoundException`; and `ConsoleUI` handles every operation in one method with a `try` and one `catch` per exception type. Order: my documentation, then Developer 1 (exceptions and validators), then Developer 2 (services and persistence), then my changes to `ConsoleUI`, `Main`, and `README.md`.
- **Decision:** Accepted. With the team we decided to refactor only the three modules of the specification and to translate every message shown by the console to Spanish, as the restrictions require. The documentation was written before the code and checked against the final code; the diagram was validated with the Mermaid parser.
- **Related commit:** `docs: add exception and validation analysis`, `docs: add exception class diagram`

### Differentiated error handling in ConsoleUI and Main
- **Date:** 2026-10-12
- **Tool:** Claude (Anthropic)
- **Phase and branch:** Requirement 6 – `feature/exception-handling`
- **Objective:** Replace the generic `catch (IllegalArgumentException e)` blocks of `ConsoleUI` with differentiated handling, show a clear message when the data cannot be loaded, and show every message in Spanish.
- **Query:** Whether to use one `try` with multiple `catch` clauses, cascading `catch`, or a single `catch (GameZoneException e)`, and how to avoid repeating it in every operation.
- **Response:** A private `executeOperation(Runnable)` with one `catch` per custom exception, a clause for the `IllegalArgumentException`/`IllegalStateException` of the modules outside the scope, and a final `catch (RuntimeException e)`; every submenu option runs through it. `Main` creates the objects in `createConsoleUI()` and catches `PersistenceException` at startup. The checked `IOException` of `AccessoryService` is converted into a `PersistenceException`. The model constructors throw `InvalidDataException` for negative prices or stock.
- **Decision:** Accepted. The catch blocks repeated in each operation were reduced to one handler. The lookups of `registerSale`, `registerReturn`, and the warranty query now use the new `get...ById` methods. All the console text, receipts, certificates, and messages of the other modules were translated to Spanish; the accessory type filter accepts Spanish names and the extended warranty question accepts "s". Verified: the same input run on `develop` and on the branch produced identical data files and the same values for the 33 operations of the menu, and each type of error showed its own message.
- **Related commit:** `refactor: handle custom exceptions by type in ConsoleUI`, `refactor: show a clear message when stored data cannot be loaded in Main`, `refactor: throw InvalidDataException for invalid product and accessory data`, `feat: show console menu messages in Spanish`, `feat: show receipts, certificates and descriptions in Spanish`, `feat: show messages of the other modules in Spanish`, `docs: describe the exception and validation layers in README`
