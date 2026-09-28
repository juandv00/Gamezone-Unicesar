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
