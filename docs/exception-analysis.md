# Exception and Validation Analysis — GameZone Unicesar

This document contains the team's answers to the five guiding questions
of Requirement 6, which adds a cross-cutting layer of custom exceptions
and a layer of validators, so the system separates validations from
business logic and tells the user clearly what kind of error happened.

## Summary of the design

| Package | Classes | Role |
|---|---|---|
| `com.gamezone.exceptions` | `GameZoneException` (abstract root), `ResourceNotFoundException`, `BusinessRuleException`, `InvalidDataException`, `PersistenceException` | Typed errors that any layer can throw. |
| `com.gamezone.validation` | `ProductValidator`, `PersonValidator`, `SaleValidator` | Static checks that the services call before an operation; they throw the typed errors. |
| `com.gamezone.service` | `ProductService`, `PersonService`, `SaleService` | Delegate their checks to the validators and throw typed errors. |
| `com.gamezone.persistence` | `ProductPersistence`, `PersonPersistence`, `SalePersistence` | Turn every `IOException` (and every corrupted line) into a `PersistenceException` that names the file and the operation. |
| `com.gamezone.ui` | `ConsoleUI` | Catches each type of error and shows a different message in Spanish. |

In our code base the specification's `ProductRepository`,
`PersonRepository`, and `SaleRepository` are the workshop classes
`ProductPersistence`, `PersonPersistence`, and `SalePersistence`, and
`ConsoleMenu` is `ConsoleUI`. The specification's `Customer` is our
`Client` class.

## About the exception hierarchy

### 1. The root class GameZoneException can be declared as Exception (checked) or as RuntimeException (unchecked). Which option is more suitable for this project and why? What consequence does it have on the signature of the methods of the services and repositories?

We declared `GameZoneException` as **unchecked** (it extends
`RuntimeException`), as the specification requires, and we agree with
that choice:

- **The errors are not recoverable where they happen.** A service that
  finds insufficient stock, or a repository that cannot write a file,
  cannot fix the problem; it can only report it. The only place that
  does something useful with the error is the user interface, which
  shows a message. With unchecked exceptions, the error travels from the
  validator, through the service, to `ConsoleUI` without every
  intermediate method having to declare or re-throw it.
- **Cleaner signatures.** If the root class were checked, every method
  that calls a validator, and every method that calls those methods,
  would need `throws GameZoneException` (or a `try/catch`). The change
  would spread through the whole system, including services and modules
  that do not use the new layer, and it would break existing callers
  that do not declare the exception.
- **It matches the existing code.** The system already reported errors
  with `IllegalArgumentException`, which is also unchecked, so changing
  the exception type does not change the shape of any method.
- **It works with lambdas.** `ConsoleUI` runs each operation through a
  `Runnable` (see question 5), which cannot throw checked exceptions.

Consequences on the signatures:

- The services and repositories do **not** declare the new exceptions
  with `throws`. They are documented with `@throws` in the JavaDoc, so
  the caller still knows which errors to expect.
- `SaleService` no longer declares `throws IOException`. It used to
  propagate the checked `IOException` of `AccessoryService.reduceStock`;
  now it catches it and throws a `PersistenceException` with the
  original exception as its cause. The only caller of those methods,
  `ConsoleUI`, removed its `catch (IOException e)` in the same commit.
- The persistence classes keep the same signatures (`save` and `load`);
  only their `catch` blocks change.

The cost of unchecked exceptions is that the compiler does not force
anyone to handle them. We compensate with the JavaDoc `@throws` tags and
with one central handler in `ConsoleUI` that catches every category, plus
a final `catch (RuntimeException e)` so the user never sees a stack
trace.

### 2. Why is it preferable to have a hierarchy of custom exceptions with a common root class (GameZoneException) instead of throwing a generic IllegalArgumentException in every case? Which design principles are applied?

With a single generic exception, every error looks the same: the caller
can only read a text message, and the user cannot tell whether they
typed something wrong, whether the product is out of stock, or whether
the system failed. The hierarchy solves this:

- **Each type means one category of problem**, so the caller decides
  what to do by the type, not by parsing the message:

  | Exception | Category | Example |
    |---|---|---|
  | `ResourceNotFoundException` | Something searched by id does not exist | Sale `SALE-99` |
  | `BusinessRuleException` | A business rule is violated | Insufficient stock, duplicated client, sale without items |
  | `InvalidDataException` | A value has the wrong format or range | Price ≤ 0, empty name, email without `@` |
  | `PersistenceException` | A file cannot be read or written | Corrupted line, file locked |

- **The common root** lets a caller handle all of them at once when the
  category does not matter (for example, `Main` catches
  `GameZoneException` at startup), and it guarantees that every error of
  the system has an error code (`getErrorCode()`).
- **Each subclass carries useful data**: `ResourceNotFoundException`
  keeps the resource type and id, `InvalidDataException` keeps the field
  name, and `PersistenceException` keeps the original `IOException` as
  its cause, so the technical details are not lost even though the user
  does not see them.
- **The messages are built in one place.** The constructors build the
  Spanish messages ("Venta con identificador SALE-99 no existe en el
  sistema."), so they are consistent everywhere.

Design principles applied:

- **Single Responsibility:** validators check, services execute business
  rules, repositories handle files, and the UI decides how to present
  each error.
- **Open/Closed:** a new category of error is a new subclass of
  `GameZoneException`; the existing catch blocks keep working.
- **Liskov Substitution and polymorphism:** any subclass can be used
  where a `GameZoneException` is expected, and the catch blocks select
  the behavior by the actual type.
- **Information Expert and encapsulation:** each exception builds its own
  message from its own data (private fields with getters).
- **Separation of concerns between layers:** the lower layers report
  *what* happened; only the UI decides *how* to tell the user.

### 3. The validators form a new layer (com.gamezone.validation). At which point of the flow are they invoked and why is this location consistent with the layered architecture of the system? Can the validators access the persistence package? Justify.

The validators are invoked **by the services, at the beginning of each
operation, before any change is made**. Examples:

- `PersonService.registerClient` calls `validatePersonData` and
  `validateUniquePerson` before creating the `Client` and saving the
  file.
- `ProductService.registerProduct` calls `validateProductData` before
  adding the product.
- `SaleService` calls `validateSaleData` (step 1 of the unified sale
  flow) and `validateStockAvailability` for every product (step 2)
  before creating the sale, so a rejected sale never reduces the stock.
- `ProductService.getProductById`, `PersonService.getClientById` /
  `getSellerById`, and `SaleService.getSaleById` call the
  `validate...Exists` methods, so a missing resource is reported as a
  `ResourceNotFoundException`.

The flow is: `ConsoleUI` → service → **validator** → (if valid)
repository and model.

This location is consistent with the layered architecture because:

- The **UI** only reads input and shows results; it does not decide
  whether data is valid.
- The **services** own the business operations, so they decide *when*
  each check is needed; the validators only know *how* to check.
- The validators sit logically between `service` and `model`: they
  receive model objects (`Product`, `Person`, `Sale`) or plain values,
  and they depend only on `model` and `exceptions`.

**The validators cannot access the persistence package**, and they do
not call services either:

- Their dependencies must point downwards, like every other layer. If a
  validator read a file or called a repository, a service would depend on
  a validator that depends on persistence, creating a second path to the
  files and hiding I/O inside what should be a pure check.
- Everything a validator needs is passed as a parameter. For example,
  `validateUniquePerson(List<Person> existing, String newId)` receives
  the list that `PersonService` already loaded, and
  `validateProductExists(Product product, String productId)` receives
  the result of the service's search. The service obtains the data; the
  validator only judges it.
- This makes the validators static, stateless, and easy to test without
  files.

### 4. The refactor must preserve the external behavior of the system. Which strategy does the team follow to guarantee that the menu operations keep working the same way? What evidence is shown at the end to demonstrate that nothing was broken?

The Requirement mentions the 10 operations of the workshop menu; our
integrated system has 33 operations in seven submenus, and all of them
must keep working.

**Strategy:**

1. **Change the exception type, not the conditions.** Every condition
   that caused an error before still causes an error, and every valid
   operation produces the same result. The validators contain the same
   checks that the services had inline (for example, the empty fields of
   `registerClient`).
2. **Keep the public signatures.** `findById` in the three services still
   returns `null` when nothing is found, because other modules
   (`ReturnService`, `WarrantyService`, the sale item lookup) rely on
   that. The failing lookups were added as new methods (`getProductById`,
   `getClientById`, `getSellerById`, `getSaleById`).
3. **Small, compiling commits.** Each commit changes one class (or one
   class and its only caller) and compiles on its own, so any regression
   can be traced to one commit.
4. **The same data before and after.** The file formats did not change.
5. **Regression test with the real application** before merging, run on
   `develop` before the change and on the branch after it.

**Evidence:**

- **The same scenario produces the same data files.** We ran the full
  presentation scenario (register a console and a video game, register a
  client, a sale with a console, a video game, and an accessory with an
  extended warranty, a partial return, the warranty list, and the
  monthly balance) on `develop` and on the refactored branch, starting
  from the same `data/` folder. All seven data files (`products.txt`,
  `persons.txt`, `sales.txt`, `accessories.csv`, `promotions.csv`,
  `returns.csv`, and `warranties.csv`) were identical byte by byte.
- **The same results on screen.** Final total 2,950,000, refund
  2,770,000, monthly net balance 180,000, stock restored, and warranty
  cancelled, as in Requirement 5.
- **All 33 menu operations were executed** with the same input on both
  versions (registering products, clients, accessories, compatible
  consoles, stock, sales, returns, and the three kinds of promotion, and
  every listing, receipt, warranty query, and the monthly balance). Both
  runs printed the same number of lines with the same values (only the
  language changed), and the seven data files were again identical.
- **Every error now has its category:**

  | Input | Message shown |
    |---|---|
  | Video game with price 0 | Los datos ingresados no son válidos: El campo 'precio' no es válido: debe ser mayor que cero. |
  | Client with email `correo-malo` | Los datos ingresados no son válidos: El campo 'correo electrónico' no es válido: … |
  | Client with an id already registered | No se puede completar la operación: Ya existe una persona registrada con la identificación C1. |
  | Sale for client `C9` (not registered) | No se encontró el recurso solicitado: Persona con identificador C9 no existe en el sistema. |
  | Sale of 4 units with 3 in stock | No se puede completar la operación: Stock insuficiente para Zelda: disponibles 3, solicitadas 4. |
  | Return of sale `SALE-99` | No se encontró el recurso solicitado: Venta con identificador SALE-99 no existe en el sistema. |
  | `products.txt` cannot be written | Ocurrió un problema al acceder a los datos del sistema. Contacte al administrador. |
  | Corrupted line in `products.txt` at startup | No fue posible cargar los datos del sistema. Contacte al administrador. |

**Visible changes that were intended:** the error messages are now
specific; the console text, receipts, and certificates are in Spanish
(as required); a duplicated person id and an email without a valid
format are now rejected (both listed by the specification); a product
price must be greater than zero; and a corrupted data file stops the
application with a clear message instead of a stack trace.

## About the user interface

### 5. The ConsoleMenu class previously caught a single generic exception. Now it must catch four different types. Is this done with a single try block with multiple catch clauses, with cascading catch, or with a general catch (GameZoneException)? Justify the choice considering code clarity and the differentiation of messages to the user.

We use **one `try` block with multiple `catch` clauses, written once**
in a private method of `ConsoleUI`:

```java
private void executeOperation(Runnable operation) {
    try {
        operation.run();
    } catch (ResourceNotFoundException e) {
        System.out.println("No se encontró el recurso solicitado: " + e.getMessage());
    } catch (BusinessRuleException e) {
        System.out.println("No se puede completar la operación: " + e.getMessage());
    } catch (InvalidDataException e) {
        System.out.println("Los datos ingresados no son válidos: " + e.getMessage());
    } catch (PersistenceException e) {
        System.out.println("Ocurrió un problema al acceder a los datos del sistema. Contacte al administrador.");
    } catch (IllegalArgumentException | IllegalStateException e) {
        System.out.println("No se puede completar la operación: " + e.getMessage());
    } catch (RuntimeException e) {
        System.out.println("Ocurrió un error inesperado. Contacte al administrador.");
    }
}
```

Every option of every submenu runs through it, for example
`case 1 -> executeOperation(this::registerSale);`.

Why this option:

- **Differentiated messages.** A single `catch (GameZoneException e)`
  would show the same prefix for every error, which is exactly the
  problem the business reported. Separate clauses give each category its
  own message, and the persistence message deliberately hides the
  technical details.
- **Clarity.** The four clauses are siblings of the root class, so their
  order does not change the result, and each one is easy to read. Nested
  ("cascading") `try/catch` blocks would add indentation without adding
  meaning.
- **One place instead of many.** Before, each operation had its own
  `try/catch` with its own message. Writing the handler once removes
  that duplication, and a new operation gets the same error handling just
  by being called through `executeOperation`.
- **Safety net.** After the four custom types, the modules that are
  outside the scope of this requirement (accessories, promotions,
  returns, and warranties still use `IllegalArgumentException`) are
  shown as business rule errors, and a final `catch (RuntimeException e)`
  guarantees that the user never sees a stack trace. The general
  catch clauses go last, because Java requires the more specific types
  before the more general ones.

`Main` also handles the errors of the startup, when the services load the
data files: a `PersistenceException` shows "No fue posible cargar los
datos del sistema. Contacte al administrador." and the application
stops without a stack trace.

## Design decisions not defined in the specification

- **Lookups:** `findById` keeps returning `null` (other modules depend on
  it), and new `get...ById` methods throw `ResourceNotFoundException`.
- **Sales with only accessories:** `SaleValidator` has the three-parameter
  `validateSaleData` of the specification (products required) and an
  overload with the accessories list, used by `SaleService`, because
  integration adjustment A3 allows a sale with only accessories.
- **Sellers have no email:** `validatePersonData` validates the email
  only when it is given; sellers pass `null`. The extra fields of a
  seller are checked by `validateSellerData`.
- **Model constructors:** `Product`, `Accessory`, `Cable`, and `Memory`
  throw `InvalidDataException` for a negative price or stock (and a
  non-positive length or capacity), so this error is shown as invalid
  data even before the service validates the product.
- **Corrupted files:** a stored line that cannot be parsed is reported as
  a `PersistenceException`, the same as a file that cannot be read.
- **Scope:** the three services and repositories of the specification
  were refactored. The accessory, promotion, return, and warranty modules
  keep their exceptions; their messages were translated to Spanish and
  `ConsoleUI` handles them as business rule errors.
- **Language:** the exception messages and every message shown by the
  console are in Spanish, as required; identifiers, comments, JavaDoc,
  and commit messages are in English.