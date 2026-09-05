# TEAM.md

## Team Members

| Name | Student Code | Role | Module |
|------|--------------|------|--------|
| Juan David Molina Jimenez | 1065584147 | Technical Leader | Sales Module & Integration |
| Reinaldo Junior Padilla Pedroza | 1066872777 | Developer 1 | Product Module |
| Ednilson De Jesus Contreras Buelvas | 1043670130 | Developer 2 | Person Module |

## Roles and Responsibilities

### Technical Leader — Juan David Molina Jimenez
Responsible for repository coordination, system integration, and implementation of the complete Sales module. Also implements the console user interface and the application's main class.

**Feature branch:** `feature/sale-module`

**Classes implemented:**
1. `Sale` (model)
2. `SalePersistence` (persistence)
3. `SaleService` (service)
4. `ConsoleUI` (ui)
5. `Main` (application entry point)

**Committed activities:**
1. Create the GitHub repository with initial configuration (README, .gitignore, license).
2. Configure project branches (main and develop) and enable branch protection.
3. Set up the Maven project with the initial pom.xml and the four-layer package structure.
4. Write the TEAM.md file with team information, assigned roles, and class distribution.
5. Implement the Sale domain class with its attributes, constructor, and basic methods.
6. Implement the total calculation method for a sale.
7. Implement the persistence class for the sales module.
8. Implement the sales service class with validation rules (minimum one product, stock verification, inventory update).
9. Implement the basic structure of the user interface class (main menu).
10. Implement the UI submenus for each of the three modules.
11. Implement the Main class with initial data loading and dependency injection.
12. Review and merge developers' Pull Requests into the integration branch.
13. Write the final README.md with build and run instructions.

---

### Developer 1 — Reinaldo Junior Padilla Pedroza
Responsible for implementing the complete Product module, covering the model, persistence, and service layers.

**Feature branch:** `feature/product-module`

**Classes implemented:**
1. `Product` (abstract base class, model)
2. `VideoGame` (derived class, model)
3. `Console` (derived class, model)
4. `ProductPersistence` (persistence)
5. `ProductService` (service)

**Committed activities:**
1. Create the feature branch for the product module.
2. Implement the abstract base class of the product hierarchy with common attributes, constructor, and common methods.
3. Declare the abstract description method that derived classes must implement.
4. Implement the first derived class (VideoGame) with its particular attributes and description method implementation.
5. Implement the second derived class (Console) with its particular attributes and description method implementation.
6. Implement the persistence class for the product module with save and load methods from files.
7. Implement the service class for the product module with registration, listing, and stock update methods.
8. Document all module classes with JavaDoc in English.
9. Request Pull Requests to the Technical Leader for module integration.

---

### Developer 2 — Ednilson De Jesus Contreras Buelvas
Responsible for implementing the complete Person module, covering the model, persistence, and service layers.

**Feature branch:** `feature/person-module`

**Classes implemented:**
1. `Person` (abstract base class, model)
2. `Client` (derived class, model)
3. `Seller` (derived class, model)
4. `PersonPersistence` (persistence)
5. `PersonService` (service)

**Committed activities:**
1. Create the feature branch for the person module.
2. Implement the abstract base class of the person hierarchy with common attributes, constructor, and common methods.
3. Declare the abstract or business method that derived classes must implement according to the analysis performed.
4. Implement the first derived class (Client) with its particular attributes.
5. Implement the second derived class (Seller) with its particular attributes.
6. Implement the persistence class for the person module with save and load methods from files.
7. Implement the service class for the person module with registration and listing methods.
8. Document all module classes with JavaDoc in English.
9. Request Pull Requests to the Technical Leader for module integration.

## Git Workflow Summary

- **main**: stable, protected branch. Only updated via merge from `develop`.
- **develop**: integration branch, protected. Receives merges from all approved feature branches.
- **feature/sale-module**: Technical Leader's working branch.
- **feature/product-module**: Developer 1's working branch.
- **feature/person-module**: Developer 2's working branch.

All commits follow the Conventional Commits convention (`feat:`, `fix:`, `docs:`, `refactor:`, `chore:`), written in English. Every feature branch must be merged into `develop` through a reviewed and approved Pull Request, following cross-review (no one approves their own Pull Request).