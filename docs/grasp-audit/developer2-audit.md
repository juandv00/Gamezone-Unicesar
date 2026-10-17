# GRASP Analysis — GameZone Unicesar

Answers to the five guiding questions of Requirement 7. The team discussed
every question together; each answer names the member who wrote it.

## 1. Criteria for a verdict and minimum evidence
*Pending: Technical Leader.*

## 2. Boundary findings between members
*Pending: Technical Leader.*

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
*Pending: Technical Leader.*
