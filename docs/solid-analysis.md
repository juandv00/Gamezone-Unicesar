# SOLID Analysis — GameZone Unicesar

Answers to the five guiding questions of Requirement 8. The team discussed
every question together; each answer names the member who wrote it.

## 1. Criteria for a verdict and minimum evidence
*Pending: Technical Leader.*

## 2. Boundary findings between members
*Pending: Technical Leader.*

## 3. Relation with the GRASP audit of Requirement 7
*Written by: Developer 1*

We found both situations.

**SOLID violations in code that GRASP had already corrected.**
- `ConsoleUI`: the GRASP correction (High Cohesion) divided it into seven
  submenu classes, `ConsoleInput`, and `ErrorHandler`, so each class now
  has one responsibility. But the main menu still knows each submenu by
  name: it has a `case` and a line of text for each module and creates
  every submenu and the `Scanner`. That breaks Open/Closed and Dependency
  Inversion. GRASP decided who does each part of the work; it did not
  decide how a new module is added.
- `ProductService` and `ProductPersistence`: the GRASP correction
  (Polymorphism) gave `ProductPersistence` a file format per product type.
  In the same audit, `ProductService` creating its `ProductPersistence`
  was correct under Creator, because it is the only class that uses it
  closely. Dependency Inversion looks at the direction of the dependency:
  a class with business rules depends on a class that reads a file, so it
  is a violation in this audit (developer1-audit V-03).
- `Return` and `Warranty`: the GRASP correction (Pure Fabrication) moved
  the money format to `CurrencyFormatter`, which removed the duplicated
  code. The text of the receipt and of the certificate stayed in the model
  classes, which is still a second responsibility (developer1-audit V-01).

**GRASP corrections that solved a SOLID problem along the way.**
- `Product.getCategory()` and `Product.isWarrantyEligible()` replaced the
  `instanceof` checks of `CategoryDiscount`, `WarrantyService`,
  `ReturnService`, and `SaleService` (Polymorphism). Those classes are now
  closed for new product types, so Open/Closed is correct in them.
- `ConsoleCompatibleAccessory` (Polymorphism) moved the compatibility
  operations out of `Controller` and `Memory` into a common class.
  `Cable` does not inherit operations it could not honor, so the accessory
  hierarchy is correct for Liskov Substitution and for Interface
  Segregation.
- The file format registered per type in the five repositories
  (Polymorphism and Protected Variations) made the reading and writing of
  every file closed for new types: Open/Closed is correct in the
  persistence layer.

**What we learned from the comparison.** Most GRASP findings were inside a
class (who has the information, who does the work). Most SOLID findings
of this audit are about the dependencies between classes: the types that
a class creates or receives (Dependency Inversion) and how many
operations of another class it depends on (Interface Segregation). In
Requirement 7 those dependencies were accepted under Low Coupling because
they were few; SOLID looks at their kind. The corrections of Requirement 8
must not reintroduce a GRASP violation: the formatters are new Pure
Fabrication classes, and the role interfaces lower the coupling.

## 4. Strategy to preserve the external behavior
*Written by: Developer 2*

**Strategy.** We reuse the non-regression method of Requirement 7, which
is already in `docs/regression/`:

1. **Refactor only.** Every correction moves a responsibility, adds an
   abstraction, or changes the type of a field or a parameter, but keeps
   the same inputs, results, messages, and file formats. The defects
   registered as out of scope (O-01 to O-03 of Requirement 7) are kept as
   they are.
2. **The same inputs for every operation of the menu.** The seven
   scenarios of `docs/regression/` run the 33 operations of the seven
   submenus with valid data (`main`), the error cases of every module
   (`errors`), a data file that cannot be written (`nowrite`) or read
   (`noread`), a second start that loads the saved data (`reload`), and
   data files with corrupted lines (`corrupt`, `corruptproducts`).
3. **A reference result.** Before the first correction we run the
   scenarios on `75cc180` and keep the screen output and the data files
   written by the application.
4. **The same check after each Pull Request and at the end.** The output
   must be identical to the reference line by line and the data files byte
   by byte; only the date of the day may change when the runs are on
   different days (then the comparison uses `-IgnoreDates`).
5. **Small commits that compile.** Each correction is a `refactor:` commit
   that mentions its violation, and the project must compile with
   `mvn clean compile` before every Pull Request.

**Why these scenarios matter for SOLID.** Many corrections of this
requirement move the creation of objects from the services to `Main`. If
`Main` created the repositories in another order, the application would
load the files in another order, and the message of a file that cannot be
read could change: the `noread`, `corrupt`, `corruptproducts`, and
`reload` scenarios show it. The receipts and certificates move to new
classes, and the `main` scenario prints every receipt, return receipt,
and certificate, so a single different character would appear in the
comparison. The monthly balance moves to a new class, and it is checked in
`main` and in `reload`.

How to run it, from the root of the project:

```powershell
powershell -ExecutionPolicy Bypass -File docs/regression/run-regression.ps1 -Label reference -Source 75cc180
powershell -ExecutionPolicy Bypass -File docs/regression/run-regression.ps1 -Label final
powershell -ExecutionPolicy Bypass -File docs/regression/compare-regression.ps1 -Reference reference -Candidate final
```

**Evidence shown at the end.** `docs/solid-refactor-log.md` records the
reference result (commit, date, and files compared), the comparison of
each Pull Request, and the final comparison on the merged branch, which
must say `RESULT: no differences`. In the presentation we run the same
scripts on the final version and show the comparison, and we run the
application to show the main menu and a sale.

## 5. Order of the corrections and coordination in the shared branch
*Pending: Technical Leader.*
