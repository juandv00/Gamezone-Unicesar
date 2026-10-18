# AI Usage Log — Developer 1 (Product Module)

I used Claude for support throughout the development of my module. These are the things I used him for:

- I had problems configuring IntelliJ (it didn't recognize my Maven project, and it wouldn't let me create packages). I showed him screenshots, and he guided me step by step until it was resolved.

- To fully understand the object-oriented design I was working with (inheritance, abstract classes, polymorphism), I asked him to explain why Product had to be abstract and why getDescription() had to be an abstract method there. I used the example he gave me as a reference to write Product, Video Game, and Console, understanding each part (the super() in the constructor, the @Override, why attributes are private with getters/setters).

- For persistence, I asked which file formats were allowed and why, and I decided to use plain text with semicolons because it's the easiest to show and explain in the presentation.

- For the service, with their help, I understood why this layer is the only one that can communicate with persistence, and how I should validate the stock before deducting it (so that the sales module can then use that method).

What I DIDN'T do: I didn't ask them to generate the class diagram, I didn't ask them for the answers to the analysis, and I didn't copy code without understanding what it did—in each class, I can explain why it's written that way.

## Session 2

**Legitimate Uses:**

- Code examples and explanations were requested for: overriding `toString()`

in the product hierarchy, adding validation to the constructor for negative prices/stocks,

and implementing `equals()`/`hashCode()` based on the product ID.

- Help was requested to understand and correct a bug when committing
  a code change (`equals`/`hashCode`) using a leftover commit message from
  a previous commit, and how to safely correct a commit message
  before submitting it (without using `force-push`, which is prohibited).

**My Own Decisions:**
- Decided which additional improvements to add (`toString`, validation,
  `equals`/`hashCode`).

## Requirement 5 — Integration (entries in the required format)

This section documents the use of AI (Claude) during the integration of my
part of the system: the model layer for accessories, promotions, returns,
and warranties, plus adjustment A1.

### 1. Accessory hierarchy
- **Date:** 2026-09-26
- **Tool:** Claude (Anthropic)
- **Phase and branch:** Phase 1 – `feature/accessory-module`
- **Objective:** Create the subclasses `Controller`, `Cable`, and `Memory` from the abstract class `Accessory`.
- **Query:** How to create the three accessory subclasses from the abstract class `Accessory`.
- **Response:** A subclass uses `extends`, and `super(...)` must be the first line of its constructor to initialize the private attributes of the parent class; the attributes that belong only to each type go in the subclass.
- **Decision:** Accepted. I wrote the three classes with the attributes from the specification.
- **Related commit:** `feat: add Accessory abstract class and Controller, Cable, Memory subclasses`

### 2. JavaDoc fixes after PR #15 was rejected
- **Date:** 2026-09-26
- **Tool:** Claude (Anthropic)
- **Phase and branch:** Phase 1 – `feature/accessory-module`
- **Objective:** Understand why the merge was blocked and how to submit the corrections.
- **Query:** Why the merge was blocked and how to send the corrections requested in the review.
- **Response:** The branch requires an approving review from another member, and new commits pushed to the same branch are added to the same open PR.
- **Decision:** Accepted. I corrected the JavaDoc of `Accessory` and `Controller` and pushed to the same branch.
- **Related commit:** `fix: correct JavaDoc comments in Accessory and Controller classes`

### 3. Promotion hierarchy
- **Date:** 2026-09-26
- **Tool:** Claude (Anthropic)
- **Phase and branch:** Phase 2 – `feature/promotion-module`
- **Objective:** Structure `Promotion` and its subclasses `PercentageDiscount`, `CategoryDiscount`, and `BulkPurchaseDiscount`.
- **Query:** How to structure the promotion hierarchy so each type calculates its own discount.
- **Response:** `Promotion` holds `id`, `name`, `startDate`, and `endDate`, implements `isActive(date)` with both ends included, and declares `calculateDiscount(Sale)` as abstract; each subclass implements its own rule.
- **Decision:** Accepted, and split the work into commits by step: abstract class, `isActive`, one commit per subclass, and JavaDoc.
- **Related commit:** `feat: add Promotion abstract class with common fields` and the following subclass commits

### 4. Return class and `Sale.canBeReturned()`
- **Date:** 2026-09-27
- **Tool:** Claude (Anthropic)
- **Phase and branch:** Phase 4 – `feature/return-module`
- **Objective:** Structure `Return` and validate that a sale can still be returned.
- **Query:** How to structure `Return` and how to validate the 30-day return window.
- **Response:** `Return` stores the id, date, original `Sale`, returned products, and reason; `calculateRefundAmount()` adds the prices and `generateReturnReceipt()` builds the receipt. `Sale.canBeReturned()` allows returns only within 30 days of the sale date.
- **Decision:** Accepted. The leader had already changed `Sale`, so I ran `git pull` before creating my branch and added my method on top of his version to avoid conflicts. After the review, I added the validations and the English receipt requested in the PR.
- **Related commit:** `feat: add canBeReturned method to Sale class`

### 5. Warranty hierarchy
- **Date:** 2026-09-27
- **Tool:** Claude (Anthropic)
- **Phase and branch:** Phase 3 – `feature/warranty-module`
- **Objective:** Build `Warranty`, `BasicWarranty`, and `ExtendedWarranty` following the repository style and the leader's specification.
- **Query:** How to calculate the end date of each warranty type without duplicating code.
- **Response:** The `Warranty` constructor validates its arguments and calculates `endDate` by calling the abstract `getDurationInMonths()`, so the subclasses must return constants (6 and 12). `BasicWarranty` has no cost and `ExtendedWarranty` costs 10% of the product price.
- **Decision:** Accepted. Comparing my code with the specification, I found that `generateWarrantyCertificate()` did not include the product id or the sale id, and I fixed it. I committed step by step (skeleton, getters, `isActive`, certificate, each subclass).
- **Related commit:** `docs: add JavaDoc comments to ExtendedWarranty constructor`

### 6. A1 — Category discount for accessories
- **Date:** 2026-09-27
- **Tool:** Claude (Anthropic)
- **Phase and branch:** Phase 2 – `feature/accessory-category-discount`
- **Objective:** Allow `ACCESSORY` as a target category in category promotions.
- **Query:** How to allow `ACCESSORY` as a target category and where to validate the allowed categories.
- **Response:** `CategoryDiscount.calculateDiscount` sums `sale.getAccessories()` when the target is `ACCESSORY`; the validation of the three allowed categories belongs in the service layer (`PromotionService.registerCategoryDiscount`).
- **Decision:** Accepted, with one commit per change (model, service, menu option, data file). I first pasted the validation outside the method and the project did not compile; I moved it inside `registerCategoryDiscount` and compiled before committing. After the review, I moved a `@throws` line that was placed in the JavaDoc of the wrong method.
- **Related commit:** `feat: support ACCESSORY target category in CategoryDiscount`, `feat: validate allowed target categories in PromotionService`, `feat: add accessory option to category promotion menu`, `feat: add accessory category promotion to promotions data`

### 7. Atomic commit planning
- **Date:** 2026-09-27
- **Tool:** Claude (Anthropic)
- **Phase and branch:** Phase 2 – `feature/accessory-category-discount`
- **Objective:** Split each module into atomic commits to meet the minimum of six per module.
- **Query:** How to divide my work into atomic commits.
- **Response:** An atomic commit is one complete logical step (a class, a method group, or the JavaDoc), not necessarily one file, and each commit must compile on its own.
- **Decision:** Accepted and applied to the commits of A1.
- **Related commit:** —

**What I did not ask the AI for:** the analysis or the class diagrams. The team decisions, such as English messages and the order of the phases, come from the leader's plan.

### 8. A5 — Proportional refund for discounted sales
- **Date:** 2026-10-11
- **Tool:** Claude (Anthropic)
- **Phase and branch:** Phase 4 – `fix/return-discounted-refund`
- **Objective:** Stop refunding the list price of returned items when the original sale had a promotion, since the customer would get back more than he paid.
- **Query:** How to calculate the refund of each returned item proportionally to the discount of the original sale, and how to show it in the return receipt.
- **Response:** The refund of each item is `price × (1 − discount / subtotal)`, where the discount and subtotal come from the original sale (`getDiscountAmount()` and `calculateTotal()`); if the subtotal is 0 the factor is 1. The receipt shows, for each item, the list price, the proportional discount, and the refunded amount.
- **Decision:** Accepted. The calculation applies to products and accessories (A4 was already merged). The extended warranty cost is not part of the subtotal, so it does not change the factor. Tested: a sale with subtotal 1,000,000 and discount 100,000; returning items of 200,000 and 300,000 refunds 180,000 and 270,000; a sale without discount refunds the list price.
- **Related commit:** `fix: refund returned items proportionally to the sale discount`



## Requirement 6 — Validations and Exceptions

### Exception hierarchy and validators
- **Date:** 2026-10-11
- **Tool:** Claude (Anthropic)
- **Phase and branch:** Requirement 6 – `feature/exception-handling`
- **Objective:** Create the custom exception hierarchy (`com.gamezone.exceptions`) and the three validators (`com.gamezone.validation`) defined by the specification.
- **Query:** How to implement `GameZoneException` as an abstract unchecked root with an error code, its four subclasses with Spanish messages, and static validators for products, people, and sales that only depend on the model and the exceptions.
- **Response:** `GameZoneException` extends `RuntimeException`, stores a private error code, and has two protected constructors (message, and message with error code). Each subclass passes its own code: `ResourceNotFoundException(resourceType, resourceId)` builds "<Tipo> con identificador <id> no existe en el sistema."; `InvalidDataException(fieldName, problem)` builds "El campo '<campo>' no es válido: <problema>"; `BusinessRuleException(message)`; and `PersistenceException(message, cause)` keeps the original exception with `initCause`. The validators are final classes with a private constructor and static methods that throw these exceptions.
- **Decision:** Accepted, with three additions to the specification: `PersonValidator.validateSellerData` (sellers have an employee code and a shift but no email, so `validatePersonData` only validates the email when it is not null); an overload of `SaleValidator.validateSaleData` that also receives the accessories, because adjustment A3 allows sales with only accessories; and `validateStockAvailability` also rejects a quantity that is not positive. The error code constants are private so the classes have no public attributes. Every class compiled on its own before committing.
- **Related commit:** `feat: add GameZoneException root exception`, `feat: add ResourceNotFoundException`, `feat: add BusinessRuleException`, `feat: add InvalidDataException`, `feat: add PersistenceException`, `feat: add ProductValidator`, `feat: add PersonValidator`, `feat: add SaleValidator`



## Requirement 7 — GRASP Audit and Correction

### Phase 1 — Audit

#### Audit of my code against the nine GRASP patterns
- **Date:** 2026-10-16
- **Tool:** Claude (Anthropic)
- **Phase and branch:** Requirement 7, Phase 1 – `feature/grasp-audit`
- **Objective:** Audit the product module, the accessory, promotion, and warranty hierarchies, `Return` with `Sale.canBeReturned`, and the `exceptions` and `validation` packages against the nine GRASP patterns, with evidence from commit `9dc5282`.
- **Query:** Went through my classes with Claude pattern by pattern, asking for each pattern where it applies in my code, which fragments could break it, and what concrete consequence each one would have.
- **Response:** Six findings: stock read and written outside `Product` (Information Expert), the paid ratio calculated by `Return` with `Sale` data (Information Expert), categories decided with `instanceof` in `CategoryDiscount` (Polymorphism), compatibility duplicated in `Controller` and `Memory` (Polymorphism), type conditionals in `ProductPersistence` (Polymorphism), and the money format copied in `Return` and `Warranty` (Pure Fabrication). Creator, Controller, Low Coupling, High Cohesion, and Indirection were found correctly applied, with examples.
- **Decision:** I opened every cited fragment in the IDE on commit `9dc5282` to confirm the line numbers and that the problem is real, and kept only the findings I can explain with their consequence. I classified `ProductService` creating its own `ProductPersistence` as correct (Creator: it is the only class that uses it closely). I marked the findings whose fix needs code of other members and reported them to the Technical Leader.
- **Related commit:** `docs: add scope to developer1 audit`, `docs: add information expert and creator findings to developer1 audit`, `docs: add controller, coupling and cohesion verdicts to developer1 audit`, `docs: add polymorphism findings to developer1 audit`, `docs: add pure fabrication, indirection and protected variations to developer1 audit`

#### Guiding question 3
- **Date:** 2026-10-16
- **Tool:** Claude (Anthropic)
- **Phase and branch:** Requirement 7, Phase 1 – `feature/grasp-audit`
- **Objective:** Write the team's answer to guiding question 3 (code that breaks more than one pattern).
- **Query:** Asked how other teams usually register a fragment that breaks Polymorphism and Protected Variations at the same time, and how to choose the pattern that guides the correction.
- **Response:** Register it once under the pattern that describes the fix (replacing a type conditional with an operation per type is Polymorphism) and mention the other pattern next to it; the other section points to the same ID.
- **Decision:** I applied the rule to my own findings (V-03 and V-05) and wrote the answer with those examples; it matches how the consolidation registered them.
- **Related commit:** `docs: answer guiding question 3 in grasp analysis`


### Phase 2 — Correction

#### Stock operations and paid ratio (V-01, V-02)
- **Date:** 2026-10-17
- **Tool:** Claude (Anthropic)
- **Phase and branch:** Requirement 7, Phase 2 – `feature/grasp-refactor`
- **Objective:** Move the stock logic into `Product` and `Accessory` (V-01) and make `Return` use the paid ratio calculated by `Sale` (V-02), without changing any result.
- **Query:** Asked Claude to review my stock code and propose the methods that `Product` and `Accessory` should offer, and how `Return` should use `Sale.calculatePaidRatio()` keeping the same refund for every item.
- **Response:** `hasEnoughStock(int)`, `decreaseStock(int)`, and `increaseStock(int)` in both classes, used by `ProductService` and `ProductValidator`; in `Return`, `paidFactor` is removed and `refundFor` asks the sale for the ratio, which is the same calculation (1 when the subtotal is 0).
- **Decision:** I kept `setStock` for `updateStock`, as the consolidated audit says, because it replaces the stock with a typed value. I compared `calculatePaidRatio()` with the old `paidFactor` line by line before removing it, compiled after each commit, and ran the regression scenarios to check that the refunds of the return receipts were the same.
- **Related commit:** `refactor: move stock operations into Product and Accessory (V-01)`, `refactor: use the paid ratio of Sale in Return refunds (V-02)`

#### Categories, console compatibility, and warranty eligibility (V-03, V-04, V-05)
- **Date:** 2026-10-17
- **Tool:** Claude (Anthropic)
- **Phase and branch:** Requirement 7, Phase 2 – `feature/grasp-refactor`
- **Objective:** Replace the type checks of my hierarchies with polymorphic operations that Developer 2 and the Technical Leader will use.
- **Query:** Asked how to implement `getCategory()`, the abstract class `ConsoleCompatibleAccessory`, and `isWarrantyEligible()` so that the discount of `CategoryDiscount` keeps exactly the same value.
- **Response:** `getCategory()` abstract in `Product` and implemented in `VideoGame`, `Console`, and `Accessory`; `calculateDiscount` sums the products and then the accessories whose category matches, in the same order as before; `ConsoleCompatibleAccessory` holds the list and the three methods that were duplicated in `Controller` and `Memory`; `isWarrantyEligible()` is false in `Product` and true in `Console`.
- **Decision:** I checked that the order of the sums in `calculateDiscount` is the same as before, so the double values do not change, and that the scenario with the 25 % accessories promotion gives the same discount. I left the list of supported categories in one place (`CategoryDiscount.isSupportedCategory`) for Developer 2 to use.
- **Related commit:** `refactor: let each product and accessory report its category (V-03)`, `refactor: add ConsoleCompatibleAccessory for controllers and memories (V-04)`, `refactor: let products report whether they receive a warranty (V-05)`

#### Product file format and money format (V-06, V-09)
- **Date:** 2026-10-17
- **Tool:** Claude (Anthropic)
- **Phase and branch:** Requirement 7, Phase 2 – `feature/grasp-refactor`
- **Objective:** Remove the type conditionals of `ProductPersistence` without changing the file, and use `CurrencyFormatter` in `Return` and `Warranty`.
- **Query:** Asked how to register a writer per class and a reader per type code in `ProductPersistence` keeping the model free of file logic, and how to check that the file stays byte-identical.
- **Response:** A map from class to writer and a map from type code to reader, filled in the constructor; `toLine` and `fromLine` look them up. The regression scenarios compare the data files byte by byte.
- **Decision:** I kept the validations and the order of `fromLine` (the price and the stock are read before the type), so a corrupted line still produces the same `PersistenceException`. I ran the regression scripts on my branch and the comparison reported no differences in the output or in `products.txt`.
- **Related commit:** `refactor: register a file format per product type in ProductPersistence (V-06)`, `refactor: format amounts with CurrencyFormatter in Return and Warranty (V-09)`, `docs: log developer 1 corrections in grasp refactor log`

## Requirement 8 — SOLID Audit and Correction

### Phase 1 — Audit

#### Audit of my code against the five SOLID principles
- **Date:** 2026-10-17
- **Tool:** Claude (Anthropic)
- **Phase and branch:** Requirement 8, Phase 1 – `feature/solid-audit`
- **Objective:** Audit the product module, the accessory, promotion, and warranty hierarchies, `Return` with `Sale.canBeReturned`, the `exceptions` and `validation` packages, and `ConsoleCompatibleAccessory` against the five SOLID principles, with evidence from commit `75cc180`.
- **Query:** Went through my classes with Claude principle by principle, asking for each principle where it applies in my code, which fragments could break it, and what concrete consequence each one would have.
- **Response:** Three findings: the receipt and certificate texts built inside `Return` and `Warranty` (SRP), `ProductService` used as one type with seven operations by clients that need one to three (ISP), and `ProductService` creating `ProductPersistence` (DIP). OCP and LSP were found correct in my hierarchies, with examples.
- **Decision:** I opened every cited fragment in the IDE on commit `75cc180` to confirm the line numbers, and checked each subclass of my hierarchies against its base class for LSP. I kept the DIP finding even though the same creation was correct under Creator in Requirement 7, because the two principles look at different things. I did not count `CategoryDiscount.isSupportedCategory` as an OCP violation, because it is the single list of categories and not a conditional. I marked the findings whose fix needs code of other members and reported them to the Technical Leader.
- **Related commit:** `docs: add scope to developer1 SOLID audit`, `docs: add single responsibility findings to developer1 SOLID audit`, `docs: add open/closed verdict to developer1 SOLID audit`, `docs: add Liskov substitution verdict to developer1 SOLID audit`, `docs: add interface segregation findings to developer1 SOLID audit`, `docs: add dependency inversion findings to developer1 SOLID audit`

#### Guiding question 3
- **Date:** 2026-10-17
- **Tool:** Claude (Anthropic)
- **Phase and branch:** Requirement 8, Phase 1 – `feature/solid-audit`
- **Objective:** Write the team's answer to guiding question 3 (relation between this audit and the GRASP audit of Requirement 7).
- **Query:** Asked how a correction of a GRASP pattern can leave a SOLID problem in the same class, and how to tell the difference between Creator and Dependency Inversion.
- **Response:** GRASP assigns responsibilities inside the classes, while several SOLID principles look at the dependencies between them; a creation can be correct for Creator and still make a rule depend on a detail.
- **Decision:** I compared the GRASP refactor log with my findings and with the findings of the team and wrote the answer with three cases of each situation from our own code (`ConsoleUI`, `ProductService`, `Return`/`Warranty`, and the GRASP corrections that closed the hierarchies).
- **Related commit:** `docs: answer guiding question 3 in solid analysis`
