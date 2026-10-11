# Promotion Module Class Diagram

This diagram shows the promotion module and how it integrates with the existing classes of the system, organized by layer. It includes the new promotion hierarchy with its attributes, methods, and inheritance relationships; the new persistence and service classes; and the relationships with `Sale`, `SaleService`, and `Product` (used by category promotions). Classes of the system not involved in the promotion module are omitted for readability; see `class-diagram.md` for the complete base system.

```mermaid
classDiagram
    namespace model {
        class Promotion {
            <<abstract>>
            -String id
            -String name
            -LocalDate startDate
            -LocalDate endDate
            +Promotion(String id, String name, LocalDate startDate, LocalDate endDate)
            +getId() String
            +setId(String id) void
            +getName() String
            +setName(String name) void
            +getStartDate() LocalDate
            +setStartDate(LocalDate startDate) void
            +getEndDate() LocalDate
            +setEndDate(LocalDate endDate) void
            +isActive(LocalDate date) boolean
            +calculateDiscount(Sale sale)* double
        }

        class PercentageDiscount {
            -double percentage
            +getPercentage() double
            +setPercentage(double percentage) void
            +calculateDiscount(Sale sale) double
        }

        class CategoryDiscount {
            -double percentage
            -String targetCategory
            +getPercentage() double
            +setPercentage(double percentage) void
            +getTargetCategory() String
            +setTargetCategory(String targetCategory) void
            -matchesCategory(Product product) boolean
            +calculateDiscount(Sale sale) double
        }

        class BulkPurchaseDiscount {
            -int minimumQuantity
            -double percentage
            +getMinimumQuantity() int
            +setMinimumQuantity(int minimumQuantity) void
            +getPercentage() double
            +setPercentage(double percentage) void
            +calculateDiscount(Sale sale) double
        }

        class Sale {
            -LocalDate date
            -Client client
            -Seller seller
            -List~Product~ products
            -List~Accessory~ accessories
            -String appliedPromotionName
            -double discountAmount
            +getDate() LocalDate
            +getProducts() List~Product~
            +getAccessories() List~Accessory~
            +getAppliedPromotionName() String
            +setAppliedPromotionName(String name) void
            +getDiscountAmount() double
            +setDiscountAmount(double discountAmount) void
            +calculateTotal() double
            +calculateFinalTotal() double
            +hasDiscount() boolean
            +generateReceipt() String
        }

        class Product {
            <<abstract>>
            -String id
            -String title
            -double price
            -int stock
            +getPrice() double
            +getDescription()* String
        }

        class VideoGame
        class Console

        class Accessory {
            <<abstract>>
            -String id
            -String title
            -double price
            -int stock
            +getPrice() double
        }

        class Client
        class Seller
    }

    namespace persistence {
        class PromotionRepository {
            -String FILE_PATH = "data/promotions.csv"
            -String DELIMITER = ";"
            +saveAll(List~Promotion~ promotions) void
            +loadAll() List~Promotion~
            -toLine(Promotion promotion) String
            -fromLine(String line) Promotion
        }

        class SalePersistence {
            +SalePersistence(AccessoryRepository accessoryRepository)
            +save(List~Sale~ sales) void
            +load() List~Sale~
        }
    }

    namespace service {
        class PromotionService {
            -PromotionRepository promotionRepository
            -List~Promotion~ promotions
            +PromotionService(PromotionRepository promotionRepository)
            +registerPercentageDiscount(String id, String name, LocalDate startDate, LocalDate endDate, double percentage) PercentageDiscount
            +registerCategoryDiscount(String id, String name, LocalDate startDate, LocalDate endDate, double percentage, String targetCategory) CategoryDiscount
            +registerBulkPurchaseDiscount(String id, String name, LocalDate startDate, LocalDate endDate, int minimumQuantity, double percentage) BulkPurchaseDiscount
            +listAllPromotions() List~Promotion~
            +listActivePromotions() List~Promotion~
            +findBestPromotionFor(Sale sale) Promotion
            +findById(String id) Promotion
            -ensureIdIsAvailable(String id) void
        }

        class SaleService {
            -SalePersistence salePersistence
            -ProductService productService
            -AccessoryService accessoryService
            -PromotionService promotionService
            -List~Sale~ sales
            +SaleService(ProductService productService, AccessoryService accessoryService, SalePersistence salePersistence, PromotionService promotionService)
            +registerSale(Client client, Seller seller, List~Product~ products) Sale
            +registerSale(Client client, Seller seller, List~Product~ products, List~Accessory~ accessories) Sale
            +listAll() List~Sale~
            -applyBestPromotion(Sale sale) void
        }
    }

    namespace ui {
        class ConsoleUI {
            -PromotionService promotionService
            -SaleService saleService
            -promotionMenu() void
            -registerPercentagePromotion() void
            -registerCategoryPromotion() void
            -registerBulkPurchasePromotion() void
            -printPromotions(List~Promotion~ promotions, String emptyMessage) void
            -registerSale() void
            -viewSaleReceipt() void
        }

        class Main {
            +main(String[] args)$ void
        }
    }

    Promotion <|-- PercentageDiscount
    Promotion <|-- CategoryDiscount
    Promotion <|-- BulkPurchaseDiscount
    Product <|-- VideoGame
    Product <|-- Console

    Sale "1" --> "1" Client
    Sale "1" --> "1" Seller
    Sale o-- Product : 1..*
    Sale o-- Accessory : 0..*

    Promotion ..> Sale : calculates discount for
    CategoryDiscount ..> VideoGame : filters VIDEOGAME
    CategoryDiscount ..> Console : filters CONSOLE

    PromotionRepository ..> Promotion : saves and loads
    SalePersistence ..> Sale : saves and loads
    PromotionService ..> PromotionRepository
    PromotionService ..> Promotion : selects best
    SaleService ..> PromotionService : findBestPromotionFor
    SaleService ..> SalePersistence
    SaleService ..> Sale : applies discount

    ConsoleUI ..> PromotionService
    ConsoleUI ..> SaleService
    Main ..> PromotionRepository
    Main ..> PromotionService
    Main ..> SaleService
    Main ..> ConsoleUI
```