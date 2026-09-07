# GameZone Unicesar

Inventory and sales management system for GameZone Unicesar — a
console-based Java application developed for the Programming III
workshop (Universidad Popular del Cesar).

The system manages products (video games and consoles), people
(clients and sellers), and sales, with data persisted to local files
between executions.

## Requirements

- **Java JDK 17** or higher
- **Apache Maven** 3.8+

## Project structure

com.gamezone
├── model # Domain classes (Person, Client, Seller, Product,
│ VideoGame, Console, Sale)
├── persistence # File-based read/write for each module
├── service # Business rules and validations
├── ui # Console-based user interface (ConsoleUI)
└── Main.java # Application entry point


Additional folders:
- `data/` — text files where the application's data is persisted
  (includes 3 pre-loaded sellers required for the first run).
- `docs/` — analysis document, UML diagrams (Mermaid), and each team
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

The console menu is organized into three modules:

**Product management**
1. Register a new video game
2. Register a new console
3. List all products

**Person management**
4. Register a new client
5. List all clients
6. List all sellers *(three sellers come pre-loaded on first run)*

**Sale management**
7. Register a new sale (client + seller + one or more products)
8. View full sales history
9. View purchase history for a specific client
10. View sales history for a specific seller

## Team

See [TEAM.md](TEAM.md) for roles, module distribution, and committed
activities per team member.

## Design documentation

See the [docs/](docs) folder for the design analysis
(`analysis.md`) and the three UML diagrams (`hierarchy-diagram.md`,
`class-diagram.md`, `layers-diagram.md`), written in Mermaid.