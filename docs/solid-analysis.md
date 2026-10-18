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
*Pending: Developer 2.*

## 5. Order of the corrections and coordination in the shared branch
*Pending: Technical Leader.*
