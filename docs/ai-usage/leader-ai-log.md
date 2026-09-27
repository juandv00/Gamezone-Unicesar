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