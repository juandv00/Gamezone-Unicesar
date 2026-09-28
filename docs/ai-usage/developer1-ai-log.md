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




















