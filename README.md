# GameZone Unicesar

Inventory and sales management system for GameZone Unicesar — a
console-based Java application developed for the Programming III
workshop (Universidad Popular del Cesar).

The system manages products (video games and consoles), accessories
(controllers, cables, and memories), people (clients and sellers),
sales, and promotional discounts, with data persisted to local files
between executions.

## Requirements

- **Java JDK 17** or higher
- **Apache Maven** 3.8+

## Project structure

```
com.gamezone
├── model        # Domain classes (Person, Client, Seller, Product, VideoGame,
│                #   Console, Accessory, Controller, Cable, Memory, Sale,
│                #   Promotion, PercentageDiscount, CategoryDiscount,
│                #   BulkPurchaseDiscount)
├── persistence  # File-based read/write for each module
├── service      # Business rules and validations
├── ui           # Console-based user interface (ConsoleUI)
└── Main.java    # Application entry point
```

Additional folders:
- `data/` — files where the application's data is persisted:
  - `persons.txt` — clients and sellers (includes 3 pre-loaded sellers
    required for the first run).
  - `products.txt`, `accessories.txt`, `sales.txt` — created
    automatically when the first item of each kind is registered.
  - `promotions.csv` — promotions (includes 3 pre-loaded promotions, one
    of each type).
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

The application starts a text menu. Data loaded from `data/` on
startup is automatically saved back to disk after each operation.

## Functional operations

The main menu is organized into five modules:

**1. Product management**
1. Register a new video game
2. Register a new console
3. List all products

**2. Person management**
1. Register a new client
2. List all clients
3. List all sellers *(three sellers come pre-loaded on first run)*

**3. Sale management**
1. Register a new sale (client + seller + one or more products, plus
   optional accessories). The best active promotion is applied
   automatically and the receipt is printed.
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

## Business rules

**Sales and stock**
- A sale must include at least one product; accessories are optional
  and can only be sold together with products.
- Stock is validated for every product and accessory before anything
  is reduced, so a rejected sale leaves the inventory untouched.

**Accessories**
- Controllers and memories can register the consoles they are
  compatible with; cables do not have console compatibility.

**Promotions**
- There are three kinds of promotions:
  - *Percentage*: a percentage discount over the sale subtotal.
  - *Category*: a percentage discount applied only to the products of a
    target category (`VIDEOGAME` or `CONSOLE`).
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
  the promotion name), and the final total.

## Team

See [TEAM.md](TEAM.md) for roles, module distribution, and committed
activities per team member.

## Design documentation

See the [docs/](docs) folder for:
- `analysis.md` — design analysis of the base system and the accessory
  module.
- `promotion-analysis.md` — design analysis of the promotion module.
- `hierarchy-diagram.md`, `class-diagram.md`, `layers-diagram.md` — the
  base system's UML diagrams, written in Mermaid.
- `promotion-class-diagram.md` — the updated class diagram with the
  promotion module, written in Mermaid.
- `ai-usage/` — each team member's AI usage log.