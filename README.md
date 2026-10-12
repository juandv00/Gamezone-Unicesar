# GameZone Unicesar

Inventory and sales management system for GameZone Unicesar — a
console-based Java application developed for the Programming III
workshop (Universidad Popular del Cesar).

The system manages products (video games and consoles), accessories
(controllers, cables, and memories), people (clients and sellers),
sales, promotional discounts, returns, and console warranties, with a
monthly balance report and data persisted to local files between
executions.

The four modules (accessories, promotions, returns, and warranties) work
together in a single integrated system (Requirement 5): one sale can
include video games, consoles, and accessories, receives the best active
promotion automatically, generates basic and extended warranties, and
can later be partially returned, which updates the inventory, the
warranties, and the monthly balance.

## Requirements

- **Java JDK 17** or higher
- **Apache Maven** 3.8+

## Project structure

```
com.gamezone
├── model        # Domain classes (Person, Client, Seller, Product, VideoGame,
│                #   Console, Accessory, Controller, Cable, Memory, Sale,
│                #   Promotion, PercentageDiscount, CategoryDiscount,
│                #   BulkPurchaseDiscount, Return, Warranty,
│                #   BasicWarranty, ExtendedWarranty)
├── exceptions   # Custom exception hierarchy (GameZoneException and its
│                #   four subclasses), usable from any layer
├── validation   # Validators called by the services (ProductValidator,
│                #   PersonValidator, SaleValidator)
├── persistence  # File-based read/write for each module (stores ids,
│                #   not objects, for warranties and returns)
├── service      # Business rules
├── ui           # Console-based user interface (ConsoleUI)
└── Main.java    # Application entry point
```

Additional folders:
- `data/` — files where the application's data is persisted:
  - `persons.txt` — clients and sellers (includes 3 pre-loaded sellers
    required for the first run).
  - `accessories.csv` — accessories (includes 3 pre-loaded accessories,
    one of each type: a controller, a cable, and a memory).
  - `promotions.csv` — promotions (includes 4 pre-loaded promotions: one
    of each type, plus a category promotion for accessories).
  - `products.txt`, `sales.txt`, `returns.csv`, `warranties.csv` —
    created automatically when the first item of each kind is
    registered.
- `docs/` — analysis documents, UML diagrams (Mermaid), and each team
  member's AI usage log.

## How to build

From the repository root (where `pom.xml` is located):

```bash
mvn clean compile
```

## How to run

**Option 1 — From an IDE (recommended):**
Open the project as a Maven project in IntelliJ IDEA (or any IDE with
Maven support) and run `com.gamezone.Main`.

**Option 2 — From the command line:**

```bash
mvn clean compile
java -cp target/classes com.gamezone.Main
```

The application starts a text menu in Spanish. Data loaded from `data/`
on startup is automatically saved back to disk after each operation.
If the data files cannot be read, the application shows a message and
does not start.

## Functional operations

The main menu is organized into seven modules:

**1. Product management**
1. Register a new video game
2. Register a new console
3. List all products

**2. Person management**
1. Register a new client
2. List all clients
3. List all sellers *(three sellers come pre-loaded on first run)*

**3. Sale management**
1. Register a new sale (client + seller + one or more items). Products
   and accessories are entered in the same list by their id. The sale
   receives an id (`SALE-1`, `SALE-2`, ...), and for each console the
   seller is asked whether to add an extended warranty. The best active
   promotion is applied automatically, every console receives its
   warranty, and the receipt and warranty certificates are printed.
2. View full sales history
3. View purchase history for a specific client
4. View sales history for a specific seller
5. View the receipt of a specific sale, including the discount applied

**4. Accessory management**
1. Register a new controller
2. Register a new cable
3. Register a new memory
4. List all accessories
5. List accessories by type (Controller, Cable, or Memory)
6. Find accessories compatible with a console
7. Add a compatible console to a controller or memory
8. Update accessory stock

**5. Promotion management**
1. Register a new percentage promotion
2. Register a new category promotion
3. Register a new bulk purchase promotion
4. List all promotions
5. List the promotions active today

**6. Return management**
1. Register a new return (sale id, products and accessories to return,
   and reason). The stock is restored, the warranties of returned
   consoles are cancelled, the refund is calculated automatically, and
   the return receipt is printed.
2. List all returns
3. List returns by client
4. List returns by sale
5. View the monthly balance (total sales, total returns, and net
   balance for a given month and year)

**7. Warranty management**
1. View the warranty of a product in a specific sale (certificate and
   whether it is active today)
2. List all warranties
3. List the warranties active today
4. List the warranties expiring soon (within a number of days chosen by
   the user)

## Validation and error handling

The system distinguishes four kinds of errors, each with its own
exception (package `com.gamezone.exceptions`) and its own message in the
console:

| Exception | When it is thrown | Message shown to the user |
|---|---|---|
| `ResourceNotFoundException` | A product, client, seller, or sale searched by id does not exist. | *No se encontró el recurso solicitado: …* |
| `BusinessRuleException` | A business rule is violated: insufficient stock, a duplicated person id, a sale without items, among others. | *No se puede completar la operación: …* |
| `InvalidDataException` | A value has the wrong format or range: an empty field, a price that is not positive, a negative stock, an invalid email. | *Los datos ingresados no son válidos: …* |
| `PersistenceException` | A data file cannot be read or written, or contains a corrupted line. | *Ocurrió un problema al acceder a los datos del sistema. Contacte al administrador.* |

All of them extend the abstract class `GameZoneException`, which extends
`RuntimeException` and stores an error code.

- **Validators** (package `com.gamezone.validation`): `ProductValidator`,
  `PersonValidator`, and `SaleValidator` group the checks of each module
  in static methods. The services call them before executing an
  operation, so a rejected operation never changes any data. Validators
  do not access services or persistence classes.
- **Services:** `ProductService`, `PersonService`, and `SaleService`
  delegate their checks to the validators and throw the typed
  exceptions. Their `findById` methods still return `null` when nothing
  is found; the new `getProductById`, `getClientById`, `getSellerById`,
  and `getSaleById` methods throw `ResourceNotFoundException` instead.
- **Persistence:** `ProductPersistence`, `PersonPersistence`, and
  `SalePersistence` turn every `IOException` into a
  `PersistenceException` that names the file and the operation, keeping
  the original exception as its cause.
- **User interface:** every menu operation runs through one handler in
  `ConsoleUI` that catches each exception type separately. The user never
  sees a technical message or a stack trace.

## Business rules

**Products and people**
- A product needs an id and a title, a price greater than zero, and a
  stock that is not negative.
- A client needs an id, a name, a phone, and a valid email; a seller
  needs an id, a name, a phone, an employee code, and a shift.
- Two people (clients or sellers) cannot have the same id.

**Sales and stock**
- A sale must include a client, a seller, and at least one item (a
  product or an accessory).
- A sale is registered in this order: validate the items and their
  stock, create the sale and calculate its subtotal, apply the best
  promotion on the subtotal, assign the warranties of the consoles,
  calculate the final total, reduce the stock of each item through its
  own service, and save the sale.
- Stock is validated for every product and accessory before anything
  is reduced, so a rejected sale leaves the inventory untouched.
- Final total = subtotal − promotion discount + extended warranty cost.

**Accessories**
- Controllers and memories can register the consoles they are
  compatible with; cables do not have console compatibility.

**Promotions**
- There are three kinds of promotions:
  - *Percentage*: a percentage discount over the sale subtotal.
  - *Category*: a percentage discount applied only to the items of a
    target category (`VIDEOGAME`, `CONSOLE`, or `ACCESSORY`).
  - *Bulk purchase*: a percentage discount over the subtotal when the
    sale includes a minimum number of products (accessories do not
    count toward the minimum).
- Each promotion has a start date and an end date, and it is only
  applied to sales whose date falls within that range (both dates
  included). Expired or future promotions stay registered but are not
  applied.
- When a sale is registered, every promotion active on the sale date is
  evaluated and only the one granting the highest discount is applied
  (promotions are not cumulative). If no promotion applies, the sale
  has no discount.
- The discount can never exceed the sale subtotal.
- The receipt shows each item, the subtotal, the discount applied (with
  the promotion name), the extended warranty cost, and the final total.

**Returns**
- A return refers to an existing sale by its id and can include only
  some of its products and accessories (partial returns are allowed).
- A return can only be registered within the 30 calendar days that
  follow the sale date (the 30th day included).
- Every returned item must belong to the original sale, and each unit
  sold can only be returned once, even across several returns of the
  same sale.
- The stock of every returned item is restored through its own service
  (products or accessories).
- Each item is refunded in proportion to the discount of the original
  sale: list price × (1 − discount / subtotal). The receipt shows the
  list price, the proportional discount, and the refund of each item.
- When a console is returned, its warranty is cancelled. The cost of a
  cancelled extended warranty is added to the refund; a basic warranty
  has no cost to refund. If a sale has several units of the same
  console, one warranty is cancelled per returned unit.

**Warranties**
- Only consoles receive warranties; video games and accessories do not.
- Every console sold receives exactly one warranty, starting on the sale
  date:
  - *Basic warranty* (default): covers factory defects for 6 months, at
    no additional cost.
  - *Extended warranty* (optional, chosen by the seller): covers factory
    defects and accidental damage for 12 months, and costs 10% of the
    console price per unit. It replaces the basic warranty, since it
    covers the same and more.
- The cost of the extended warranties is added to the sale total and is
  not affected by promotion discounts.
- A warranty is active from its start date to its end date, both
  included.
- Returning a console cancels its warranty (see *Returns*).

**Monthly balance**
- For a given month and year, the report shows the total sales (final
  totals, after promotion discounts and including extended warranty
  costs), the total refunded by the returns registered in that month,
  and the net balance (sales minus returns).

## Team

See [TEAM.md](TEAM.md) for roles, module distribution, and committed
activities per team member.

## Design documentation

See the [docs/](docs) folder for:
- `analysis.md` — design analysis of the base system and the accessory
  module.
- `promotion-analysis.md` — design analysis of the promotion module.
- `return-analysis.md` — design analysis of the return module.
- `warranty-analysis.md` — design analysis of the warranty module.
- `accessory-analysis.md` — design analysis of the accessory module.
- `integration-analysis.md` — the integration of the four modules
  (Requirement 5): each adjustment, its cause, and the solution applied.
- `exception-analysis.md` — design analysis of the exception and
  validation layers (Requirement 6).
- `hierarchy-diagram.md`, `class-diagram.md` — the base system's UML
  diagrams, written in Mermaid.
- `layers-diagram.md` — the four layers of the integrated system and
  the dependencies between them, written in Mermaid.
- `promotion-class-diagram.md` — the updated class diagram with the
  promotion module, written in Mermaid.
- `return-class-diagram.md` — the updated class diagram with the return
  module, written in Mermaid.
- `warranty-class-diagram.md` — the updated class diagram with the
  warranty module, written in Mermaid.
- `accessory-class-diagram.md` — the class diagram of the accessory
  module, written in Mermaid.
- `integrated-class-diagram.md` — a single class diagram of the
  integrated system, with the four layers and every module.
- `exception-class-diagram.md` — the class diagram of the exception
  hierarchy, the validators, and how the services and the console use
  them, written in Mermaid.
- `ai-usage/` — each team member's AI usage log.