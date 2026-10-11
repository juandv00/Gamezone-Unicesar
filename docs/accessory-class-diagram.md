# Accessory Module Class Diagram

This diagram shows the accessory module and how it integrates with the existing classes of the system, organized by layer: the `Accessory` hierarchy with its attributes, methods, and inheritance relationships; its persistence and service classes; and the integration with `Sale`, `SaleService`, `SalePersistence`, and the console menu. Classes not involved in the accessory module are omitted for readability; see `class-diagram.md` for the base system and `integrated-class-diagram.md` for the complete integrated system.

```mermaid
classDiagram
    namespace model {
        class Accessory {
            <<abstract>>
            -String id
            -String title
            -double price
            -int stock
            +Accessory(String id, String title, double price, int stock)
            +getId() String
            +setId(String id) void
            +getTitle() String
            +setTitle(String title) void
            +getPrice() double
            +setPrice(double price) void
            +getStock() int
            +setStock(int stock) void
            +getType()* String
            +getDescription()* String
            +equals(Object o) boolean
            +hashCode() int
        }

        class Controller {
            -ConnectionType connectionType
            -List~String~ compatibleConsoleIds
            +Controller(String id, String title, double price, int stock, ConnectionType connectionType)
            +getConnectionType() ConnectionType
            +setConnectionType(ConnectionType connectionType) void
            +getCompatibleConsoleIds() List~String~
            +addCompatibleConsole(String consoleId) void
            +isCompatibleWith(String consoleId) boolean
            +getType() String
            +getDescription() String
        }

        class ConnectionType {
            <<enumeration>>
            WIRELESS
            WIRED
        }

        class Cable {
            -double lengthInMeters
            -String connectorType
            +Cable(String id, String title, double price, int stock, double lengthInMeters, String connectorType)
            +getLengthInMeters() double
            +setLengthInMeters(double lengthInMeters) void
            +getConnectorType() String
            +setConnectorType(String connectorType) void
            +getType() String
            +getDescription() String
        }

        class Memory {
            -int capacityInGB
            -MemoryType memoryType
            -List~String~ compatibleConsoleIds
            +Memory(String id, String title, double price, int stock, int capacityInGB, MemoryType memoryType)
            +getCapacityInGB() int
            +setCapacityInGB(int capacityInGB) void
            +getMemoryType() MemoryType
            +setMemoryType(MemoryType memoryType) void
            +getCompatibleConsoleIds() List~String~
            +addCompatibleConsole(String consoleId) void
            +isCompatibleWith(String consoleId) boolean
            +getType() String
            +getDescription() String
        }

        class MemoryType {
            <<enumeration>>
            SD
            MICRO_SD
            INTERNAL_CARD
        }

        class Sale {
            -List~Product~ products
            -List~Accessory~ accessories
            +getProducts() List~Product~
            +getAccessories() List~Accessory~
            +calculateTotal() double
        }

        class Product {
            <<abstract>>
        }
        class Console
    }

    namespace persistence {
        class AccessoryRepository {
            -String FILE_PATH = "data/accessories.csv"
            -String DELIMITER = "tab"
            +saveAll(List~Accessory~ accessories) void
            +loadAll() List~Accessory~
            -toLine(Accessory accessory) String
            -fromLine(String line) Accessory
        }

        class SalePersistence {
            +SalePersistence(AccessoryRepository accessoryRepository)
            +save(List~Sale~ sales) void
            +load() List~Sale~
        }
    }

    namespace service {
        class AccessoryService {
            -AccessoryRepository accessoryRepository
            -List~Accessory~ accessories
            +AccessoryService(AccessoryRepository accessoryRepository)
            +registerController(String id, String title, double price, int stock, ConnectionType connectionType) Controller
            +registerCable(String id, String title, double price, int stock, double lengthInMeters, String connectorType) Cable
            +registerMemory(String id, String title, double price, int stock, int capacityInGB, MemoryType memoryType) Memory
            +listAllAccessories() List~Accessory~
            +listAccessoriesByType(String type) List~Accessory~
            +findAccessoriesCompatibleWith(String consoleId) List~Accessory~
            +findById(String id) Accessory
            +updateStock(String accessoryId, int quantity) boolean
            +hasEnoughStock(String accessoryId, int amount) boolean
            +reduceStock(String accessoryId, int amount) boolean
            +addCompatibleConsole(String accessoryId, String consoleId) boolean
        }

        class SaleService {
            -AccessoryService accessoryService
            +registerSale(Client client, Seller seller, List~Product~ products, List~Accessory~ accessories) Sale
        }
    }

    namespace ui {
        class ConsoleUI {
            -AccessoryService accessoryService
            -accessoryMenu() void
            -registerController() void
            -registerCable() void
            -registerMemory() void
            -listAccessories() void
            -listAccessoriesByType() void
            -listAccessoriesCompatibleWithConsole() void
            -addCompatibleConsole() void
            -updateAccessoryStock() void
            -readSaleAccessories() List~Accessory~
        }

        class Main {
            +main(String[] args)$ void
        }
    }

    Accessory <|-- Controller
    Accessory <|-- Cable
    Accessory <|-- Memory
    Product <|-- Console
    Controller --> ConnectionType
    Memory --> MemoryType
    Controller ..> Console : compatible console ids
    Memory ..> Console : compatible console ids

    Sale o-- Product : 1..*
    Sale o-- Accessory : 0..*

    AccessoryRepository ..> Accessory : saves and loads (type discriminator)
    SalePersistence ..> AccessoryRepository : resolves accessories by id
    AccessoryService ..> AccessoryRepository
    SaleService ..> AccessoryService : validates and reduces stock
    ConsoleUI ..> AccessoryService
    ConsoleUI ..> SaleService
    Main ..> AccessoryRepository
    Main ..> AccessoryService
```