# SOLID Analysis — GameZone Unicesar

Answers to the five guiding questions of Requirement 8. The team discussed
every question together; each answer names the member who wrote it.

## 1. Criteria for a verdict and minimum evidence
*Written by: Technical Leader*

**Criteria.** For each principle, every member first identified the points
of their code where the principle applies, and then decided the verdict
with the same questions:

- **Single Responsibility:** for each class we listed who could ask for a
  change in it (the rules of the store, the way the screen looks, the way
  the data is stored, a report of another area). It is a violation when a
  class has two of those reasons and that has a concrete consequence, for
  example, the text of a receipt built inside a model class, or a
  financial report inside the service of returns.
- **Open/Closed:** we looked at the variations that the requirements
  already showed (new product, accessory, promotion, and warranty types,
  and new modules in the menu). It is a violation when adding one of them
  forces editing a method that already works (a `switch` with one `case`
  per type or module, a chain of `if`, one field and one constructor per
  module). An extension point where a new type only adds one registration
  line, without changing the algorithm, was classified as correct.
- **Liskov Substitution:** for each hierarchy we checked that every
  subclass accepts the same inputs as its base class, returns values with
  the same meaning, and does not throw exceptions that the base class does
  not announce; and, from the side of the clients, that no client checks
  the concrete class to compensate for a subclass that behaves
  differently. A type check that selects a role that the operation needs
  (only a `Client` can buy) is not a violation, because the base class
  never promised that role.
- **Interface Segregation:** for each dependency between classes of
  different modules we compared the operations that the client calls with
  the operations of the type it depends on. It is a violation when the
  client depends on a type with many operations that it never calls and
  there is no smaller role it could depend on. A menu that uses most of
  the operations of its own service is correct.
- **Dependency Inversion:** it is a violation when a high-level class (a
  service with business rules, or the main menu) creates or depends on the
  concrete type of a class that does input or output (a file, the
  console). Dependencies between services of the business layer were not
  counted as Dependency Inversion violations, because both sides are
  policy; whether they are too wide is decided by Interface Segregation.
  `Main` is the composition root and is the expected place to create
  concrete classes.

The verdicts mean:
- **Violation:** at least one point contradicts the principle with a
  concrete consequence today or under a foreseeable change. A style
  preference without a consequence is not a violation.
- **Correct:** the principle applies and every point we checked follows
  it.
- **Not applicable:** the code has no point where the principle applies.
  It was not needed: even the members whose classes have no subclasses use
  the hierarchies of the others, so Liskov Substitution was checked from
  the side of the client.

**Minimum evidence accepted in the consolidation:**
- For a violation: class, method, and exact lines in commit `75cc180`
  (with the fragment of code), why it breaks the principle, the concrete
  consequence, the proposed solution, and whether it needs another
  member's code.
- For a correct verdict: at least one class and method where the principle
  is applied, and how.
- For not applicable: the reason why the principle has no point of
  application.

A finding without exact lines, or whose consequence was only "it is not
elegant", would have been returned to its author; none was.

## 2. Boundary findings between members
*Written by: Technical Leader*

Five of the seven violations are boundary findings: V-01, V-02, V-04,
V-05, and V-07. They appear because the services and the menus of one
member use the classes of the others.

**A concrete case, V-07 (Interface Segregation).** The three members
found the same problem from their side. I found that `SaleService`
depends on the whole `ProductService` and `AccessoryService` while it only
uses three operations of each, and that `ReturnMenu` and `WarrantyMenu`
depend on the whole `SaleService` only to call `getSaleById`. Developer 1
found that `ProductService` offers no role interfaces, so its clients in
the code of the three of us depend on its seven operations. Developer 2
found that `ReturnService` and `WarrantyService` depend on complete
services of other modules, and that `AccessoryService` has no role for the
classes that only need its inventory.

**How it was registered.** Each member reported the problem with the
evidence of their own code and marked "other member's code: yes". In the
consolidation the three findings (leader-audit V-03, developer1-audit
V-02, developer2-audit V-02) became one violation, V-07, whose evidence
lists every client and whose solution defines four role interfaces:
`ProductLookup`, `ProductInventory`, `AccessoryInventory`, and
`SaleLookup`.

**How the correction was assigned.** Each member creates the role of his
own service (Developer 1 the roles of the products, Developer 2 the role
of the accessories, and I the role of the sales), and then each member
changes the fields of his own clients. The Technical Leader creates
`SaleLookup` first, because Developer 2 needs it for `ReturnService`.

The other cases follow the same rule. In V-01, `Sale` (mine) and `Return`
and `Warranty` (Developer 1) build their texts; each member creates the
formatters of his classes, I change the menus that print them, and then
Developer 1 removes his old methods. In V-02, Developer 2 moves the
balance to `BalanceService`, I change `ReturnMenu` and `Main`, and then
Developer 2 removes the balance from `ReturnService`. The table "Boundary
findings" of the consolidated audit lists the five cases.

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
*Written by: Technical Leader*

**Order.** The corrections follow the dependencies between the boundary
findings: the member who owns the class that must offer a new abstraction
goes before the members who use it.

1. **Technical Leader (preparation):** the "before" diagram, the
   non-regression strategy with the reference result, `SaleStorage`, and
   `SaleLookup`. They go first because Developer 2 needs them for
   `WarrantyService` and `ReturnService`.
2. **Developer 1:** `ProductStorage` (V-04), `ProductLookup` and
   `ProductInventory` (V-07), and the formatters of `Return` and
   `Warranty` (V-01).
3. **Developer 2:** the five storage interfaces (V-05), `AccessoryInventory`
   and his clients (V-07), and `BalanceService` (V-02). He uses
   `SaleStorage`, `SaleLookup`, and the roles of the products.
4. **Technical Leader:** `Main` passes the storages (V-04, V-05), then
   V-06, his clients of V-07, the receipts (V-01), the main menu (V-03),
   and the balance in `ReturnMenu` (V-02). V-03 goes after the others
   because it rewrites `ConsoleUI` and the end of `Main`, which the other
   corrections also touch.
5. **Developer 1 and Developer 2 (cleanup):** they remove the old
   constructors, the texts of `Return` and `Warranty`, and the balance of
   `ReturnService`, which nobody uses at that moment.
6. **Technical Leader:** the "after" diagram, the final verification, and
   the README.

Every new abstraction is added without removing the old way of doing the
same thing until its users have changed, so every commit compiles and
passes the non-regression scenarios even when the parts of a boundary
finding are made by different members.

**Coordination in the shared branch.** We work one member at a time in
`feature/solid-refactor`, in the order above. Before each commit each
member runs `git pull --no-edit`, adds his files by path (never
`git add .`), and pushes immediately; nobody edits a class of another
member, except the `@Override` annotations of the methods of an interface
that his class implements. When a member finishes his part, he opens his
Pull Request; the next member starts after it is merged, from the updated
branch. The cleanup commits of the developers go in the Technical
Leader's last Pull Request, which closes the phase, and the branch is
deleted after it.
