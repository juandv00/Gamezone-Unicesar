# AI Usage Log — Developer 2

I used Claude throughout the development of my module. These are the things I used it for:

I had a lot of trouble with Git at the beginning: I created my branch with a typo (person-modole instead of person-module), ended up with a branch that had no commit history, and at one point almost committed directly on develop without noticing. Claude helped me diagnose each of these step by step using git status and git branch, and explained why commits directly on main/develop are forbidden by the workshop.
I also switched from IntelliJ to NetBeans midway because I found IntelliJ confusing, and Claude helped me confirm my project was still correctly linked to the Git repository after the switch.
To understand the Person/Client/Seller hierarchy, I asked how super() works in the constructor and why Person needs to be abstract. I initially wrote Person as a normal class and Claude pointed out that it contradicted our own analysis.md, so I corrected it.
For persistence, I got stuck on how to save a list of Person objects to a file when some are actually Client and some are Seller. Claude explained the concept using an unrelated example (animals, not people) — instanceof, casting, and using a type marker at the start of each line — and I used that logic to write my own save() and load() methods for PersonPersistence.

What I DIDN'T do: I didn't ask it to design the hierarchy or decide the attributes (that came from our team's analysis.md), I didn't ask for the analysis answers themselves, and I didn't copy a finished class — I wrote Person, Client, Seller, and PersonPersistence myself, using generic examples as reference.

## Detailed entries (Requirements 1–5)

- **Tool used in every entry:** Claude (Anthropic).
- **Dates:** 2026-09-27.

### 1. Understanding persistence of a class hierarchy in a single file

* **Date:** 2026-09-27
* **Tool:** Claude
* **Phase and branch:** Requirement 1 – feature/accessory-module
* **Objective:** Understand how to persist an abstract class with three concrete
subclasses in one file (AccessoryRepository / AccessoryService).
* **Query:** How do I structure the repository and service for the accessory
module, and how do I distinguish the three concrete types when loading?
* **Response:** Explained the discriminator column idea and the general
loadAll/saveAll flow. Used a generic example with an unrelated domain to explain
how different subclasses can be identified when reading a file.
* **Decision:** Used the explanation about discriminators and the generic
example as a guide to design my own implementation.
* **Related commit:** feat: implement AccessoryRepository

### 2. JavaDoc style for the new classes

* **Date:** 2026-09-27
* **Tool:** Claude
* **Phase and branch:** Requirement 1 – feature/accessory-module
* **Objective:** Keep the same JavaDoc convention used in Workshop 1.
* **Query:** Pasted the class comment from our Person persistence class and
asked how to replicate the same style for the new classes.
* **Response:** Explained the structure of the JavaDoc, including how to
describe the purpose of a class and how to document parameters and return
values.
* **Decision:** Used the explanation as a reference and wrote the JavaDoc for my
own classes and methods.
* **Related commit:** feat: implement AccessoryRepository

### 3. Compilation errors in saveAll

* **Date:** 2026-09-27
* **Tool:** Claude
* **Phase and branch:** Requirement 1 – feature/accessory-module

* **Objective:** Understand why AccessoryRepository.saveAll did not compile
after the model classes were pushed.
* **Query:** Sent a screenshot of the method with the compilation errors.
* **Response:** Explained that Java is case-sensitive and identified the
difference between the generic type and the class name. Also explained that the
try block had been closed incorrectly and that this caused the braces to become
unbalanced. Clarified the relationship between `throws IOException` and the
existing exception handling.

* **Decision:** Applied the corrections myself based on the explanation and
verified that the file compiled.

- `**Related commit:** feat: implement AccessoryRepository` 

### 4. Errors in loadAll and missing helper methods

* **Date:** 2026-09-27

- `**Tool:** Claude` 

- `**Phase and branch:** Requirement 1 – feature/accessory-module` 

- `**Objective:** Understand the errors in loadAll and what toLine/fromLine were supposed to do.` 

* **Query:** Sent the repository code and asked about the errors that appeared.
* **Response:** Identified problems such as a variable having the same name as
its class, calling `.add` on the wrong object, an incorrect copied error
message, and missing `toLine`/`fromLine` methods. Explained the responsibility
of each helper using a generic example.
* **Decision:** Corrected the names and structure myself and implemented the
helper methods based on the explanation.

- `**Related commit:** feat: implement AccessoryRepository` 

### 5. Understanding fromLine and toLine for Controller, Cable, and Memory

* **Date:** 2026-09-27

- `**Tool:** Claude` 

- `**Phase and branch:** Requirement 1 – feature/accessory-module` 

* **Objective:** Adapt the Person.fromLine pattern to the accessory hierarchy.
* **Query:** Sent my draft, the Person.fromLine implementation used as
reference, and the Controller/Cable/Memory classes.
* **Response:** Explained how to identify the information that belongs to each
accessory type, how numeric values and enums should be interpreted, and how
compatible consoles could be handled after creating an object. The explanation
used generic examples rather than providing the finished implementation.
* **Decision:** Used the explanation and the existing Person example as a guide
and wrote my own `fromLine` and `toLine` methods.

- `**Related commit:** feat: implement AccessoryRepository` 

### 6. Review of the repository and JavaDoc

* **Date:** 2026-09-27

- `**Tool:** Claude` 

- `**Phase and branch:** Requirement 1 – feature/accessory-module * **Objective:** Review AccessoryRepository and identify problems before committing.` 

* **Query:** Uploaded AccessoryRepository.java and asked for help reviewing the
JavaDoc and implementation.

* **Response:** Identified several logical problems, including a mismatch
between the value written by `toLine` and the value expected by `fromLine`,
missing fields when saving, and the way the console list was being converted to
text. Explained why each issue could cause problems when loading the data again.
* **Decision:** Corrected the issues myself using the explanations and then
reviewed the save/load behavior.

* **Related commit:** feat: implement AccessoryRepository

### 7. Commit message conventions

* **Date:** 2026-09-27

- `**Tool:** Claude` 

- `**Phase and branch:** Requirement 1 – feature/accessory-module` 

- `**Objective:** Choose the appropriate Conventional Commit message.` 

- `**Query:** Asked what type of commit should be used because the work was a new feature rather than a correction.` 

- `**Response:** Explained the difference between `feat`, `fix`, and other common Conventional Commit types, with generic examples.` 

- `**Decision:** Used `feat` because the accessory module was new functionality.` 

- `**Related commit:** feat: implement AccessoryRepository` 

### 8. Git troubleshooting

* **Date:** 2026-09-27
* **Tool:** Claude
* **Phase and branch:** Requirements 1–4 – feature branches
* **Objective:** Understand and resolve Git errors while working with feature
branches.

* **Query:** Sent screenshots showing detached HEAD, rejected pushes, `src
refspec does not match any`, non-fast-forward errors, and a command with an
incorrect space.

* **Response:** Explained what each error meant, how local and remote branches
relate, why a remote branch can contain commits that are not present locally,
and why the changes should be pulled before pushing when appropriate. Also
explained the importance of avoiding force push when working with the team.
* **Decision:** Followed the explanations to create/check out the appropriate
branch, synchronize it with the remote repository, and push the changes.
* **Related commit:** n/a

### 9. AccessoryService

* **Date:** 2026-09-27
* **Tool:** Claude
* **Phase and branch:** Requirement 1 – feature/accessory-module
* **Objective:** Understand the responsibilities of AccessoryService and follow
the patterns already used in the project.
* **Query:** Uploaded the project and asked for help understanding my remaining
part.
* **Response:** Reviewed the existing ProductService, ProductPersistence,
SaleService, and Console classes and explained the patterns they followed. Used
generic examples to explain service responsibilities, stock updates, and
compatibility rules.

* **Decision:** Used those explanations and existing project classes as
references to implement AccessoryService myself.

* **Related commit:** AccessoryService commit

### 10. Opening the Pull Request

- `**Date:** 2026-09-27 * **Tool:** Claude` 

- `**Phase and branch:** Requirement 1 – feature/accessory-module` 

* **Objective:** Understand how to open a Pull Request and what should be
preserved while the team is still working.

* **Query:** Asked how to create the Pull Request.

* **Response:** Explained the relationship between the base branch and the
feature branch, what information should be included in the title and
description, and why the shared branch should not be deleted before the team
finishes.

- `**Decision:** Followed the GitHub process and opened the Pull Request. * **Related commit:** n/a` 

### 11. PromotionRepository and PromotionService

* **Date:** 2026-09-27

- `**Tool:** Claude` 

- `**Phase and branch:** Requirement 2 – feature/promotion-module` 

* **Objective:** Understand how the persistence and promotion-selection logic
should be organized.

* **Query:** Uploaded the Requirement 2 statement and Developer 1's model
classes.

* **Response:** Explained the role of the repository and service, polymorphism
in the promotion hierarchy, why the selection logic belongs in the service, and
the role of `isActive`. Used examples to clarify the expected behavior without
directly implementing the finished classes.

* **Decision:** Used the explanations and the existing project patterns to
develop my own implementation.

* **Related commit:** PromotionRepository / PromotionService commit

### 12. Missing sale id and the return module

* **Date:** 2026-09-27
* **Tool:** Claude

* **Phase and branch:** Requirement 3 – feature/return-module
* **Objective:** Understand the requirements for ReturnRepository and
ReturnService and the dependencies with Sale.

* **Query:** Uploaded the Requirement 3 statement and asked for guidance while
working as Developer 2.
* **Response:** Identified that `Sale` did not have an id and that `SaleService`
did not have a `findById` method, which affected the return process. Explained
why these elements were necessary and used generic examples to describe how the
return repository and service should interact.

* **Decision:** Made the required changes myself and used the explanations to
structure the return module. Later used the error messages to understand
problems such as `getSale` versus `getOriginalSale` and the number of parameters
expected by the `Return` constructor.
* **Related commit:** ReturnRepository / ReturnService commit

### 13. Leader review of the return module

* **Date:** 2026-09-27
* **Tool:** Claude

* **Phase and branch:** Requirement 3 – feature/return-module
* **Objective:** Understand the problems identified during the leader's review
and determine how to organize the corrections.

* **Query:** Sent screenshots of the leader's report concerning over-returning
units, monthly totals, `canBeReturned`, ids, malformed lines, and the corrected
files.

* **Response:** Explained the reason behind each reported problem and how the
existing logic could produce the observed behavior. Also explained the purpose
of separating changes into different commits and how `git add -p` can be used
for that.

* **Decision:** Used the explanations to understand and apply the corrections
myself, then tested the affected cases.

* **Related commit:** Corrections to return module

### 14. WarrantyRepository and WarrantyService

* **Date:** 2026-09-27

- `**Tool:** Claude` 

- `**Phase and branch:** Requirement 4 – feature/warranty-module` 

- `**Objective:** Understand the repository and service responsibilities according to the leader's specification.` 

- `**Query:** Uploaded the Requirement 4 statement and the leader's specification, including the use of SalePersistence, console-only validation,` 

and the expiring-soon rule.

* **Response:** Explained how the warranty information should be persisted, how
the validation rules should work, and why the repository should not depend
directly on SaleService. Used generic examples to explain the circular-
dependency problem.

* **Decision:** Used the explanations and project patterns to implement
WarrantyRepository and WarrantyService myself.

* **Related commit:** WarrantyRepository / WarrantyService commit

### 15. Requirement 5 plan and fix/accessory-data-file

* **Date:** 2026-09-27
* **Tool:** Claude
* **Phase and branch:** Phase 2 – fix/accessory-data-file
* **Objective:** Understand the integration plan, the order of my branches, and
the required accessory data-file adjustment.
* **Query:** Uploaded the Requirement 5 statement and the leader's plan; later
sent the `dir data` output, accessory menu screenshot, and generated file.
* **Response:** Explained the relationship between the branches, the expected
data-file format, and the steps required for the branch. Helped identify that
the expected accessory data file did not exist and explained how the application
generated the file when an accessory was registered. Also explained where the
compatible-console information appeared in the menu.

* **Decision:** Changed the required file path, generated the file through the
application instead of creating it manually, and verified the resulting format.
* **Related commit:** fix: change accessory data file from accessories.txt to
accessories.csv

### 16. Drafting this log

* **Date:** 2026-09-27
* **Tool:** Claude

* **Phase and branch:** Phase 2 – fix/accessory-data-file
* **Objective:** Organize the AI usage log according to the actual help received
during development.
* **Query:** Asked for the log to describe the assistance as explanations,
generic examples, and help understanding errors instead of presenting the AI as
having written the complete project code.

* **Response:** Helped organize the information from the development process
into individual entries, keeping the focus on the questions asked, the
explanations received, the decisions made, and the errors that were analyzed.
* **Decision:** Kept the log focused on the actual learning and debugging
assistance received and removed claims that the AI directly implemented complete
modules.

* **Related commit:** docs: add developer 2 AI usage log

### A2 — Warranty circular dependency
- **Date:** 2026-09-27
- **Tool:** Claude (Anthropic)
- **Phase and branch:** Phase 3 – `fix/warranty-circular-dependency`
- **Objective:** Remove the dependency of `WarrantyRepository` on sales, as required by adjustment A2, so the repository stores and loads only identifiers and `WarrantyService` resolves the sale and product.
- **Query:** How to make the repository return only identifiers and where to rebuild the warranties from them.
- **Response:** Use a small record (`WarrantyRecord`) with the stored fields; `loadAll()` returns records, and `WarrantyService` receives `SalePersistence` and `ProductService` by constructor to resolve the sale and product of each record, skipping the ones that cannot be found.
- **Decision:** Accepted. After pushing the record and the service, the project did not compile because `Main` still used the old constructors; the review also found an outdated class JavaDoc and unused imports in `WarrantyRepository`, and the class diagram still showed the old dependency. I fixed `Main`, the JavaDoc, the imports, and the diagram in separate commits, and tested that warranties are saved as ids and reloaded after restarting.
- **Related commit:** `fix: add WarrantyRecord to hold warranty identifiers`, `fix: resolve warranty references in WarrantyService from stored ids`, `fix: wire WarrantyService with SalePersistence and ProductService in Main`

### A6 — Monthly balance report
- **Date:** 2026-09-27
- **Tool:** Claude (Anthropic)
- **Phase and branch:** Phase 4 – `fix/monthly-balance-report`
- **Objective:** Align the monthly balance methods with the names required by Requirement 5 and make sure the sales total uses the final total of each sale.
- **Query:** Which changes A6 needs, given that `ReturnService` already had methods for the monthly sales and returns totals.
- **Response:** The methods existed as `calculateMonthlySalesTotal` and `calculateMonthlyReturnsTotal`; A6 requires `calculateMonthlySales(int month, int year)` and `calculateMonthlyReturns(int month, int year)`. The sales total already used `calculateFinalTotal()`, which includes discounts and extended warranties, and `ConsoleUI` already showed the three values.
- **Decision:** Renamed both methods and their calls in `ConsoleUI` in the same commit (so every commit compiles), updated the JavaDoc to mention extended warranties, and updated the method names in `return-analysis.md` and `return-class-diagram.md`. `generateMonthlyBalance` keeps its signature.
- **Related commit:** `fix: rename monthly balance methods in ReturnService`, `docs: update monthly balance method names in return docs`

### A4 — Returning accessories
- **Date:** 2026-10-11
- **Tool:** Claude (Anthropic)
- **Phase and branch:** Phase 4 – `fix/return-accessory-stock`
- **Objective:** Allow accessories to be returned and restore their stock, since the return module only restored products through `ProductService.restoreStock`.
- **Query:** Which classes must change so a return can include accessories, and how to restore the stock of each item through its own service.
- **Response:** Add `AccessoryService.restoreStock(accessoryId, quantity)`; let `Return` hold returned accessories (new constructor, the old one delegates); in `ReturnService.registerReturn`, resolve each id as a product of the sale first and then as an accessory, count returnable units separately for products and accessories, and restore stock by item type; store the item ids in `ReturnRepository` and resolve accessories from the sale when loading; pass `AccessoryService` from `Main`; list accessories in the console return flow.
- **Decision:** Accepted. Kept the signature `registerReturn(String saleId, List<String> productIds, String reason)`, where the ids are now item ids, and kept the previous constructors of `Return` and `ReturnService` so existing code still compiles. Tested: sold two controllers, returned one (stock restored), returning the same units again was rejected, a return with a video game and a controller refunded both, and the returns reloaded after restarting.
- **Related commit:** `fix: support returning accessories and restoring their stock`, `fix: wire AccessoryService in returns and list accessories in ConsoleUI`, `docs: add A4 entry to developer 2 AI log`

### A7 — Warranty cancellation when a console is returned
- **Date:** 2026-10-11
- **Tool:** Claude (Anthropic)
- **Phase and branch:** Phase 4 – `feature/return-warranty-cancellation`
- **Objective:** Make sure a returned console does not keep an active warranty, and refund the cost of its extended warranty.
- **Query:** How to cancel the warranty of each returned console and include its cost in the return refund, given that A5 already changed the refund calculation.
- **Response:** Add `WarrantyService.cancelWarranties(productId, saleId)`, which removes the warranty and returns its refundable cost (0 for basic, the additional cost for extended). `ReturnService.registerReturn` calls it for every returned console unit after all validations pass, and passes the total to a new `Return` constructor that adds it to the refund and shows it in the receipt. `ReturnRepository` stores that amount as an optional last field, and `Main` passes `WarrantyService` to `ReturnService`.
- **Decision:** Accepted. If a sale has several units of the same console, each unit has its own warranty, so `cancelWarranties` cancels one warranty per call (one call per returned unit); otherwise returning one console would leave the other units without warranty. The reason is validated before cancelling, so a rejected return never removes a warranty. Tested: a sale with two PS5 (extended) and a Switch (basic); returning one PS5 refunded its discounted price plus 250,000 and kept the other PS5's warranty; returning the Switch cancelled its basic warranty with no extra refund; a return with an empty reason was rejected without cancelling anything; returns reloaded after restarting.
- **Related commit:** `feat: add cancelWarranties to WarrantyService`, `feat: include cancelled warranty refund in Return`, `feat: cancel warranties of returned consoles in ReturnService`, `docs: add A7 entry to developer 2 AI log`

### Refactor — Remove the persistence-to-service dependency in returns
- **Date:** 2026-10-11
- **Tool:** Claude (Anthropic)
- **Phase and branch:** Phase 5 – `refactor/return-repository-dependencies`
- **Objective:** Respect the dependency direction required by the workshop (ui → service → persistence → model): `ReturnRepository` (persistence) depended on `SaleService` and `ProductService` (service) to resolve sales and products when loading returns.
- **Query:** How to load returns without making `ReturnRepository` depend on the service layer, following the same approach used for warranties in A2.
- **Response:** Add a `ReturnRecord` record with the stored data as ids (return id, date, sale id, item ids, reason, warranty refund). `ReturnRepository` keeps `saveAll(List<Return>)`, but `loadAll()` now returns `List<ReturnRecord>` and it has no constructor parameters. `ReturnService` resolves each record in its constructor (`resolveReturns`): the sale through `SaleService`, and each item id as a product of the sale (or `ProductService`) and then as an accessory of the sale. `Main` creates `new ReturnRepository()`. `return-class-diagram.md` is updated to the current design.
- **Decision:** Accepted. The file format does not change, so existing `returns.csv` files still load, including lines without the warranty refund field. Tested: a sale with a PS5 (extended warranty), a video game, and a controller; returned the PS5 and the controller (refund with discount plus 250,000 for the warranty, stock and warranty updated), restarted the application and the return reloaded with the same values; an old line with six fields also loaded. The results are the same as before the change.
- **Related commit:** `refactor: add ReturnRecord for stored returns`, `refactor: resolve stored returns in ReturnService instead of ReturnRepository`, `docs: update return class diagram and developer 2 AI log`

## Requirement 6 — Validations and Exceptions

### Refactor of the services and persistence classes
- **Date:** 2026-10-11
- **Tool:** Claude (Anthropic)
- **Phase and branch:** Requirement 6 – `feature/exception-handling`
- **Objective:** Make `ProductService`, `PersonService`, and `SaleService` delegate their validations to the validators and throw typed exceptions, and make `ProductPersistence`, `PersonPersistence`, and `SalePersistence` (the specification's repositories) report file errors as `PersistenceException`.
- **Query:** How to replace the inline `IllegalArgumentException` checks with validator calls without changing the behavior of the menu, and how to turn `IOException` into `PersistenceException` with the file and the operation in the message.
- **Response:** The persistence classes throw `PersistenceException("Error de lectura/escritura en el archivo <ruta> ...", e)` instead of printing the error, and a new private `parseLine` reports a corrupted line the same way. `ProductService.registerProduct` calls `validateProductData`; `reduceStock` and `restoreStock` use the new `getProductById` and the stock validation. `PersonService` calls `validatePersonData`, `validateSellerData`, and `validateUniquePerson`, and adds `getClientById` and `getSellerById`. `SaleService` calls `validateSaleData` and `validateStockAvailability`, throws `BusinessRuleException` or `ResourceNotFoundException` instead of returning null, adds `getSaleById`, and wraps the `IOException` of the accessory stock in a `PersistenceException`.
- **Decision:** Accepted. The `findById` methods still return null, because `ReturnService`, `WarrantyService`, and the sale item lookup depend on it; the lookups that must fail were added as new `get...ById` methods. Removing `throws IOException` from `SaleService` made the `catch (IOException ...)` in `ConsoleUI.registerSale` a compile error, so I removed `IOException` from that catch in the same commit (the only caller). Tested: valid sales, returns, and registrations produce the same data files as before; a sale without stock, a duplicated client, an invalid email, and an unknown sale are rejected with the new exceptions; a corrupted `products.txt` is reported as a `PersistenceException`.
- **Related commit:** `refactor: report file errors as PersistenceException in ProductPersistence`, `refactor: report file errors as PersistenceException in PersonPersistence`, `refactor: report file errors as PersistenceException in SalePersistence`, `refactor: delegate product validations to ProductValidator`, `refactor: delegate person validations to PersonValidator`, `refactor: use SaleValidator and typed exceptions in SaleService`

## Requirement 7 — GRASP Audit and Correction

### Phase 1 — Audit

#### Audit of my code against the nine GRASP patterns
- **Date:** 2026-10-16
- **Tool:** Claude (Anthropic)
- **Phase and branch:** Requirement 7, Phase 1 – `feature/grasp-audit`
- **Objective:** Audit the person module, the repositories and services of accessories, promotions, returns, and warranties, and my use of exceptions and validators in Requirement 6, against the nine GRASP patterns, with evidence from commit `9dc5282`.
- **Query:** Went through my repositories and services with Claude pattern by pattern, asking for each pattern where it applies in my code, which fragments could break it, and what concrete consequence each one would have.
- **Response:** Five findings: accessory stock read and written by `AccessoryService` (Information Expert), type conditionals in four repositories and in `WarrantyService.resolveWarranties` (Polymorphism), `instanceof Controller`/`Memory` in `AccessoryService` (Polymorphism), `instanceof Console` in `WarrantyService` and `ReturnService` (Polymorphism), and the list of categories repeated in `PromotionService` (Protected Variations). Creator, Controller, Low Coupling, High Cohesion, Pure Fabrication, and Indirection were found correctly applied, with examples such as `ReturnRecord` and `WarrantyRecord`.
- **Decision:** I opened every cited fragment in the IDE on commit `9dc5282` to confirm the line numbers, and grouped the type conditionals of my four repositories into one finding because they have the same cause and the same fix. I kept the monthly balance in `ReturnService` as correct for High Cohesion, because adjustment A6 placed it there and it only uses the returns the class manages. I reported to the Technical Leader the four findings that need classes of Developer 1.
- **Related commit:** `docs: add scope to developer2 audit`, `docs: add information expert and creator findings to developer2 audit`, `docs: add controller, coupling and cohesion verdicts to developer2 audit`, `docs: add polymorphism findings to developer2 audit`, `docs: add pure fabrication, indirection and protected variations to developer2 audit`

#### Guiding question 4
- **Date:** 2026-10-16
- **Tool:** Claude (Anthropic)
- **Phase and branch:** Requirement 7, Phase 1 – `feature/grasp-audit`
- **Objective:** Write the team's answer to guiding question 4 (how to prove that the refactor does not change the behavior).
- **Query:** Asked for ways to compare the behavior of a console program before and after a refactor, and what evidence is usually kept.
- **Response:** Run a fixed input script with the 33 menu operations and a script of error cases on `develop` before the first correction and on the branch after the corrections, from the same data folder, and compare the screen output line by line and the data files byte by byte.
- **Decision:** I chose the scripted comparison because the team already used it in Requirement 6, and wrote the answer listing the operations and the error cases we will run.
- **Related commit:** `docs: answer guiding question 4 in grasp analysis`

### Phase 2 — Correction

#### Services that use the new operations of the model (V-01, V-03, V-04, V-05)
- **Date:** 2026-10-17
- **Tool:** Claude (Anthropic)
- **Phase and branch:** Requirement 7, Phase 2 – `feature/grasp-refactor`
- **Objective:** Use in my services the operations that Developer 1 added: the stock methods of `Accessory`, `CategoryDiscount.isSupportedCategory`, `ConsoleCompatibleAccessory`, and `Product.isWarrantyEligible()`.
- **Query:** Asked Claude to review my services and show which lines should call the new operations, keeping the same return values and the same messages.
- **Response:** `AccessoryService` uses `hasEnoughStock`, `decreaseStock`, and `increaseStock`, and asks only for `ConsoleCompatibleAccessory`; `PromotionService` asks `CategoryDiscount` if the category is supported; `WarrantyService` and `ReturnService` ask `isWarrantyEligible()`.
- **Decision:** I kept `setStock` in `updateStock`, because it replaces the stock with the value typed by the user. I renamed `validateConsole` to `validateWarrantyEligible` because the method no longer checks the class, and kept its message. I compiled after each commit and ran the accessory, promotion, warranty, and return operations of the regression scenarios.
- **Related commit:** `refactor: use the stock operations of Accessory in AccessoryService (V-01)`, `refactor: ask CategoryDiscount for the supported categories in PromotionService (V-03)`, `refactor: find compatible accessories through ConsoleCompatibleAccessory (V-04)`, `refactor: ask products for warranty eligibility in WarrantyService and ReturnService (V-05)`

#### File formats of my repositories (V-07)
- **Date:** 2026-10-17
- **Tool:** Claude (Anthropic)
- **Phase and branch:** Requirement 7, Phase 2 – `feature/grasp-refactor`
- **Objective:** Remove the type conditionals of `PersonPersistence`, `AccessoryRepository`, `PromotionRepository`, and `WarrantyRepository`, with the same design that Developer 1 used in `ProductPersistence`, without changing any file.
- **Query:** Asked how to apply the registered writers and readers to each repository, and where to keep the warranty types so that `WarrantyService` does not choose the class by its code.
- **Response:** One registration per type in the constructor of each repository; in warranties, the repository registers the code and the constructor of each type and creates the warranty of a record (`toWarranty`) after the service resolves the sale and the product.
- **Decision:** I checked that every repository keeps its behavior with invalid lines (the promotion and warranty repositories still print "Se omitió una línea inválida…", and a line of an unknown type is still ignored) and when a file cannot be written (the accessory repository still prints the error, out-of-scope finding O-03). I ran the regression scenarios that reload the data and read files with unknown types and invalid lines, and the comparison reported no differences.
- **Related commit:** `refactor: register a file format per person type in PersonPersistence (V-07)`, `refactor: register a file format per accessory type in AccessoryRepository (V-07)`, `refactor: register a file format per promotion type in PromotionRepository (V-07)`, `refactor: register the warranty types in WarrantyRepository (V-07)`, `docs: log developer 2 corrections in grasp refactor log`

## Requirement 8 — SOLID Audit and Correction

### Phase 1 — Audit

#### Audit of my code against the five SOLID principles
- **Date:** 2026-10-17
- **Tool:** Claude (Anthropic)
- **Phase and branch:** Requirement 8, Phase 1 – `feature/solid-audit`
- **Objective:** Audit the people module, the repositories and services of accessories, promotions, returns (including the monthly balance), and warranties, and my use of exceptions and validators, against the five SOLID principles, with evidence from commit `75cc180`.
- **Query:** Went through my classes with Claude principle by principle, asking for each principle where it applies in my code, which fragments could break it, and what concrete consequence each one would have.
- **Response:** Three findings: the monthly balance inside `ReturnService` (SRP), `ReturnService` and `WarrantyService` depending on complete services of other modules and `AccessoryService` without a role for its inventory (ISP), and the services depending on concrete repositories, with `PersonService` creating its own (DIP). OCP and LSP were found correct, with examples.
- **Decision:** I opened every cited fragment in the IDE on commit `75cc180` to confirm the line numbers, and counted which operations of each service my classes really call. I classified the `instanceof Client`/`Seller` checks of `PersonService` as correct for LSP, because they select the role that each operation needs. I marked the findings whose fix needs code of other members, including `WarrantyService` depending on `SalePersistence`, and reported them to the Technical Leader.
- **Related commit:** `docs: add scope to developer2 SOLID audit`, `docs: add single responsibility findings to developer2 SOLID audit`, `docs: add open/closed verdict to developer2 SOLID audit`, `docs: add Liskov substitution verdict to developer2 SOLID audit`, `docs: add interface segregation findings to developer2 SOLID audit`, `docs: add dependency inversion findings to developer2 SOLID audit`

#### Guiding question 4
- **Date:** 2026-10-17
- **Tool:** Claude (Anthropic)
- **Phase and branch:** Requirement 8, Phase 1 – `feature/solid-audit`
- **Objective:** Write the team's answer to guiding question 4 (strategy to preserve the external behavior).
- **Query:** Asked which of our non-regression scenarios are most affected by moving the creation of objects to `Main` and the receipts to new classes.
- **Response:** The scenarios that start the application with files that cannot be read or are corrupted, because the order in which the files are loaded decides the message, and the main scenario, because it prints every receipt and certificate.
- **Decision:** I kept the method of Requirement 7 (the seven scenarios in `docs/regression/`, compared line by line and byte by byte) and wrote the answer explaining why each group of scenarios matters for these corrections, with the commands for the reference commit `75cc180`.
- **Related commit:** `docs: answer guiding question 4 in solid analysis`

### Phase 2 — Correction

#### Storage abstractions of my services (V-05)
- **Date:** 2026-10-18
- **Tool:** Claude (Anthropic)
- **Phase and branch:** Requirement 8, Phase 2 – `feature/solid-refactor`
- **Objective:** Make `PersonService`, `AccessoryService`, `PromotionService`, `ReturnService`, and `WarrantyService` depend on interfaces instead of the concrete file classes.
- **Query:** Asked Claude to review my services and repositories and propose one interface per module with only the operations that each service calls.
- **Response:** `PersonStorage` (`save`, `load`), `AccessoryStorage`, `PromotionStorage`, and `ReturnStorage` (`saveAll`, `loadAll`), and `WarrantyStorage`, which also has `toWarranty`; `WarrantyService` receives the `SaleStorage` of the Technical Leader instead of `SalePersistence`; `PersonService` keeps its old constructor until `Main` passes the storage.
- **Decision:** I kept the method names of each repository (`save`/`load` in `PersonPersistence`, `saveAll`/`loadAll` in the others) so the repositories only add `implements` and `@Override`. I compiled after each commit.
- **Related commit:** `refactor: make PersonService depend on the PersonStorage abstraction (V-05)`, `refactor: make AccessoryService and PromotionService depend on storage abstractions (V-05)`, `refactor: make ReturnService and WarrantyService depend on storage abstractions (V-05)`

#### Roles of the sales, products, and accessories (V-07)
- **Date:** 2026-10-18
- **Tool:** Claude (Anthropic)
- **Phase and branch:** Requirement 8, Phase 2 – `feature/solid-refactor`
- **Objective:** Make `ReturnService` and `WarrantyService` depend only on the operations they use, and offer an inventory role of the accessories.
- **Query:** Asked which role interfaces my services should receive, using `SaleLookup` and the roles of the products already in the branch, and which operations of `AccessoryService` the sales and returns need.
- **Response:** `AccessoryInventory` (`findById`, `hasEnoughStock`, `reduceStock`, `restoreStock`) implemented by `AccessoryService`; `ReturnService` receives `SaleLookup`, `ProductInventory`, and `AccessoryInventory`; `WarrantyService` receives `ProductLookup`.
- **Decision:** I counted the calls of each service again in the code before choosing the roles, kept `reduceStock` with its `throws IOException` so `SaleService` does not change its behavior, and compiled.
- **Related commit:** `refactor: make ReturnService and WarrantyService depend on lookup and inventory roles (V-07)`

#### Monthly balance in BalanceService (V-02)
- **Date:** 2026-10-18
- **Tool:** Claude (Anthropic)
- **Phase and branch:** Requirement 8, Phase 2 – `feature/solid-refactor`
- **Objective:** Move the monthly balance out of `ReturnService` with the same results and messages.
- **Query:** Asked how to move the three balance methods to a new class that does not need the whole return service.
- **Response:** `BalanceService` with the same three methods and the same check of the month, depending on `SaleLookup` and a new interface `ReturnLookup` (the three queries of the returns) implemented by `ReturnService`; the old methods stay until `ReturnMenu` uses the new class.
- **Decision:** I kept the same loops and the same message "El mes debe estar entre 1 y 12." so the balance gives the same values, and ran the regression scenarios before my Pull Request: the comparison reported no differences, including the balance of the `main` and `reload` scenarios.
- **Related commit:** `refactor: add the ReturnLookup role implemented by ReturnService (V-02)`, `refactor: move the monthly balance to BalanceService (V-02)`, `docs: log developer 2 corrections in solid refactor log`

#### Removal of the old code (V-02, V-05)
- **Date:** 2026-10-18
- **Tool:** Claude (Anthropic)
- **Phase and branch:** Requirement 8, Phase 2 – `feature/solid-refactor`
- **Objective:** Remove the constructor of `PersonService` and the balance of `ReturnService`, which nobody uses after the corrections of the Technical Leader.
- **Query:** Asked how to confirm that `PersonService()` and the balance methods of `ReturnService` have no callers before removing them.
- **Response:** Search for every use in the project and compile after each removal; update the description of `ReturnService`, which still mentioned the balance.
- **Decision:** I checked with Find Usages in NetBeans that they had no callers, removed them in two commits, and compiled after each one.
- **Related commit:** `refactor: remove the PersonService constructor that created its own persistence (V-05)`, `refactor: remove the monthly balance methods from ReturnService (V-02)`, `docs: log developer 2 cleanup in solid refactor log`
