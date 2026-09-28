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

## Requirement 5 — Integration (September 26–27)

This section documents the use of AI (Claude) during the integration of my part of the system: the model layer for accessories, promotions, returns and warranties, plus adjustment A1.

### 1. Accessory hierarchy (Requirement 1, `feature/accessory-module`)

**What I asked:** How to create the subclasses `Controller`, `Cable` and `Memory` from the abstract class `Accessory`.

**What I learned and applied:**
- A subclass uses `extends`, and `super(...)` must be the first line of its constructor to initialize the private attributes of the parent class.
- The attributes that belong only to each type go in the subclass. I wrote the three classes with the attributes from the specification.
- Commit: `feat: add Accessory abstract class and Controller, Cable, Memory subclasses`.

### 2. JavaDoc fixes after PR #15 was rejected

**What I asked:** Why the merge was blocked and how to submit the corrections.

**What I learned and applied:**
- The branch requires an approving review from another member, and new commits pushed to the same branch are added to the same open PR.
- I corrected the JavaDoc of `Accessory` and `Controller` and pushed to the same branch.
- Commit: `fix: correct JavaDoc comments in Accessory and Controller classes`.

### 3. Promotion hierarchy (Requirement 2, `feature/promotion-module`)

**What I asked:** How to structure `Promotion` and its subclasses `PercentageDiscount`, `CategoryDiscount` and `BulkPurchaseDiscount`.

**What I learned and applied:**
- `Promotion` holds `id`, `name`, `startDate` and `endDate`, implements `isActive(date)` with both ends of the range included, and declares `calculateDiscount(Sale)` as abstract so each subclass defines its own rule.
- `PercentageDiscount` applies the percentage over the sale total. `CategoryDiscount` sums only the items of the target category and applies the percentage to that sum. `BulkPurchaseDiscount` applies the percentage only when the sale has at least `minimumQuantity` products.
- I split the work into commits by step: abstract class, `isActive`, one commit per subclass, and JavaDoc.

### 4. Return class and `Sale.canBeReturned()` (Requirement 3, `feature/return-module`)

**What I asked:** How to structure `Return` and how to validate that a sale can still be returned.

**What I learned and applied:**
- `Return` stores the id, date, original `Sale`, returned products and reason. `calculateRefundAmount()` adds up the prices of the returned products and stores the result, and `generateReturnReceipt()` builds the receipt text.
- `Sale.canBeReturned()` allows a return only within 30 days of the sale date.
- The leader had already changed `Sale` (sale id and other fields), so I ran `git pull` before creating my branch and added my method on top of his version to avoid conflicts.
- Commit: `feat: add canBeReturned method to Sale class`.

### 5. Warranty hierarchy (Requirement 4, `feature/warranty-module`)

**What I asked:** How to build `Warranty`, `BasicWarranty` and `ExtendedWarranty` following the code style of the repository and the leader's specification.

**What I learned and applied:**
- The `Warranty` constructor validates its arguments with `IllegalArgumentException` and calculates `endDate` by calling the abstract method `getDurationInMonths()`, so the subclasses must return constants (6 and 12 months) and not their own fields.
- `BasicWarranty` has no additional cost, and `ExtendedWarranty` costs 10% of the product price.
- Comparing my code with the specification, I found that `generateWarrantyCertificate()` did not include the product id or the sale id, and I fixed it.
- I committed step by step (skeleton, getters, `isActive`, certificate, each subclass) and added the JavaDoc that was missing in the `ExtendedWarranty` constructor.
- Commit: `docs: add JavaDoc comments to ExtendedWarranty constructor`.

### 6. A1 — Category discount for accessories (`feature/accessory-category-discount`)

**What I asked:** How to allow `ACCESSORY` as a target category in category promotions.

**What I learned and applied:**
- `CategoryDiscount.calculateDiscount` sums `sale.getAccessories()` when the target category is `ACCESSORY`; for the other categories it still filters `sale.getProducts()`.
- The validation of the three allowed categories belongs in the service layer, so it lives in `PromotionService.registerCategoryDiscount` and throws `IllegalArgumentException`.
- I added option 3 to `ConsoleUI` and the new promotion to `promotions.csv`, with one commit per change.
- I pasted the validation outside the method and the project did not compile. I moved it inside `registerCategoryDiscount` and compiled before committing.

### 7. Atomic commit planning

**What I asked:** How to split each module into atomic commits to meet the minimum of six per module.

**What I learned and applied:** An atomic commit is one complete logical step (a class, a method group or the JavaDoc), not necessarily one file, and each commit must compile on its own.

**What I DIDN'T do:** I didn't ask it for the analysis or the class diagrams. The team decisions, such as English messages and the order of the phases, come from the leader's plan.







































