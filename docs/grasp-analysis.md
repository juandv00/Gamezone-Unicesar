# GRASP Analysis — GameZone Unicesar

Answers to the five guiding questions of Requirement 7. The team discussed
every question together; each answer names the member who wrote it.

## 1. Criteria for a verdict and minimum evidence
*Written by: Technical Leader*

**Criteria.** For each pattern, every member first identified the points
of their code where the pattern applies: where a responsibility is
assigned (Information Expert, Creator, Pure Fabrication), where a system
operation enters (Controller), where classes depend on each other (Low
Coupling, Indirection), what each class does (High Cohesion), and where
behavior varies by type or is expected to change (Polymorphism, Protected
Variations). Then:

- **Violation:** at least one of those points contradicts the pattern in a
  way that has a concrete consequence today or under a foreseeable change.
  Examples of the signals we looked for: data read with a getter and
  written back with a setter by another class (Information Expert),
  `instanceof` or text comparisons to choose behavior by type
  (Polymorphism), the same code copied in several classes (Pure
  Fabrication), and a class with responsibilities of several modules
  (High Cohesion).
- **Correct:** the pattern applies and every point we checked follows it.
  A style preference without a consequence is not a violation.
- **Not applicable:** the code has no point where the pattern applies (for
  example, Controller in a class that never receives a system event). It
  was not needed for any complete report, because every member's code has
  at least one service or one hierarchy.

**Minimum evidence accepted in the consolidation:**

- For a violation: class, method, and exact lines in commit `9dc5282`
  (with the fragment of code), the explanation of why it breaks the
  pattern, the concrete consequence, the proposed solution with the
  pattern that guides it, and whether it needs another member's code.
- For a correct verdict: at least one class and method where the pattern
  is applied, and how.
- For not applicable: the reason why the pattern has no point of
  application.

A finding without exact lines, or whose consequence was only "it is not
elegant", would have been returned to its author; none was.

## 2. Boundary findings between members
*Written by: Technical Leader*

Six of the nine violations of the consolidated audit are boundary
findings (V-01, V-02, V-03, V-04, V-05, and V-09). They appeared because
each requirement added code to classes that another member had written.

**A concrete case, V-05 (warranty eligibility).** The Technical Leader
found `instanceof Console` in `SaleService` (lines 479 and 494) and
`ConsoleUI` (line 513); Developer 2 found the same check in
`WarrantyService` (lines 132-136) and `ReturnService` (lines 197-199).
The origin of the problem is in Developer 1's hierarchy: `Product` has no
operation that answers whether a product receives a warranty, so the
other classes ask for the concrete class.

**How they were registered.** Each member reported the problem in their
own report with the evidence from their own code and marked "other
member's code: yes". In the consolidation they were merged into a single
violation (V-05) whose evidence lists the five places, and whose
"Responsible" field splits the correction by ownership: Developer 1 adds
`Product.isWarrantyEligible()` (overridden by `Console`), the Technical
Leader changes `SaleService` and `ConsoleUI`, and Developer 2 changes
`WarrantyService` and `ReturnService`.

**How the correction is assigned.** The member who owns the class that
must offer the new operation or the new class corrects first; then each
member who uses it changes their own code. The table "Boundary findings"
of the consolidated audit lists the six cases with their split.

## 3. Code that violates more than one pattern
*Written by: Developer 1*

Yes. Polymorphism and Protected Variations appeared together several
times, because a conditional on the type is both a missing polymorphic
operation and a point that is not protected against a new type:

- `CategoryDiscount.matchesCategory` (V-03) decides the category with
  `instanceof`, and the list of categories is repeated in
  `PromotionService`, so adding a category or a product type breaks two
  classes.
- `ProductPersistence.toLine` and `fromLine` (V-06), and the same chains
  in Developer 2's repositories (V-07), choose the file format by type.

**How we avoided duplicated findings.** A fragment of code is registered
once, under the pattern that guides its correction, and the other pattern
is written next to it ("Polymorphism (also Protected Variations)"). In
each report, the Protected Variations section points to those IDs instead
of creating new ones, and the team verdict for Protected Variations says
"covered by V-03, V-06, V-07".

**How we chose the guiding pattern.** We asked which pattern describes the
change that fixes the problem. In V-03, V-06, and V-07 the fix is to
replace a conditional on the type with an operation per type, which is
Polymorphism; protecting the variation is the result of that change. In
V-09 (money formatting duplicated in `Return`, `Warranty`, `Sale`, and
`ConsoleUI`) the fix is to create a class that is not part of the domain,
which is Pure Fabrication, even though the duplication also leaves the
format unprotected. When the same fragment was found by two members (for
example, the stock logic of V-01, reported by the three of us), it was
also registered once, as a boundary finding.

## 4. Strategy to preserve the external behavior
*Written by: Developer 2*

**Strategy.**

1. **Refactor only.** Every correction moves a responsibility or replaces
   a conditional, but keeps the same inputs, the same results, the same
   messages, and the same file formats. Defects that would change the
   behavior were moved to the out-of-scope section instead of being fixed.
2. **A fixed set of inputs for every menu operation.** Before the first
   correction we write, in `docs/grasp-refactor-log.md`, an input script
   that runs the 33 operations of the seven submenus (registrations,
   listings, a sale with a console, a video game, and an accessory with an
   extended warranty, a partial return, warranty queries, promotions, and
   the monthly balance), plus a script of error cases (invalid data,
   unknown ids, insufficient stock, an unwritable file).
3. **A reference result.** We run both scripts on `develop` before any
   correction, starting from the same `data/` folder, and keep the screen
   output and the resulting data files.
4. **The same check after each Pull Request and at the end.** Each member
   runs the scripts on the branch with their corrections. The output must
   be identical to the reference line by line, and the data files must be
   identical byte by byte. The only allowed difference is the date of the
   day, when the runs are on different days.
5. **Small commits that compile.** Each correction is one `refactor:`
   commit that mentions its violation ID, so a difference can be traced to
   one commit, and the project must compile with `mvn clean compile`
   before every Pull Request.

**Evidence shown at the end.** `docs/grasp-refactor-log.md` records the
reference result (commit, date, number of output lines, and the data
files) and the final result on the merged branch, with the comparison
(no differences in the output or in the data files). In the presentation
we run the same scripts on the final version and show the comparison.

## 5. Order of the corrections and coordination in the shared branch
*Written by: Technical Leader*

**Order.** The corrections follow the dependencies between the boundary
findings: the member who owns a class that must offer something new goes
before the members who use it.

1. **Technical Leader (preparation):** the "before" diagram, the
   non-regression strategy with the reference result, `Sale.calculatePaidRatio()`
   (needed by Developer 1 for V-02), and `CurrencyFormatter` (needed by
   Developer 1 for V-09).
2. **Developer 1:** all his corrections. He adds the operations and
   classes that the other members need: the stock operations (V-01),
   `getCategory()` and `isSupportedCategory` (V-03),
   `ConsoleCompatibleAccessory` (V-04), and `isWarrantyEligible()` (V-05).
3. **Developer 2:** his parts of V-01, V-03, V-04, and V-05, which use what
   Developer 1 added, and V-07.
4. **Technical Leader:** his parts of V-01, V-04, and V-05, and then V-08,
   the split of `ConsoleUI`. It goes last because it is the largest change
   and touches the file that every other correction of the leader also
   touches; doing it at the end avoids moving code that is about to change.

Every new operation is added without removing the old way of doing the
same thing until its users have changed, so every commit compiles even
when the parts of a boundary finding are made by different members.

**Coordination in the shared branch.** We work one member at a time in
`feature/grasp-refactor`, in the order above. Before each commit, each
member runs `git pull --no-edit`; files are added by path, never with
`git add .`, and nobody edits another member's classes. When a member
finishes, they open their Pull Request; the next member starts after it
is merged, from the updated branch. The Technical Leader's Pull Request
is the last one, and the branch is deleted after it.
