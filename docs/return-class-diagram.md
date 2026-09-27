# Return Module Class Diagram

This diagram shows the return module and how it integrates with the existing classes of the system, organized by layer. It includes the new `Return` class with its attributes, methods, and its association with the existing `Sale` class; the new persistence and service classes; and the integration with `Sale` (sale id and `canBeReturned()`), `ProductService` (`restoreStock()`), and the extension of the console menu. Classes of the system not involved in the return module are omitted for readability; see `class-diagram.md` for the complete base system and `promotion-class-diagram.md` for the promotion module.

```mermaid
classDiagram
    namespace model {
        class Return {
            -String id
            -LocalDate date
            -Sale originalSale
            -List~Product~ returnedProducts
            -String reason
            -double refundAmount
            +Return(String id, LocalDate date, Sale originalSale, List~Product~ returnedProducts, String reason)
            +getId() String
            +getDate() LocalDate
            +getOriginalSale() Sale
            +getReturnedProducts() List~Product~
            +getReason() String
            +getRefundAmount() double
            +calculateRefundAmount() double
            +generateReturnReceipt() String
        }

        class Sale {
            -String id
            -LocalDate date
            -Client client
            -Seller seller
            -List~Product~ products
            -List~Accessory~ accessories
            -String appliedPromotionName
            -double discountAmount
            +getId() String
            +setId(String id) void
            +getDate() LocalDate
            +getClient() Client
            +getProducts() List~Product~
            +calculateTotal() double
            +calculateFinalTotal() double
            +canBeReturned() boolean
            +generateReceipt() String
        }

        class Product {
            <<abstract>>
            -String id
            -String title
            -double price
            -int stock
            +getId() String
            +getPrice() double
            +getStock() int
            +setStock(int stock) void
            +getDescription()* String
        }

        class VideoGame
        class Console
        class Accessory
        class Client
        class Seller
    }

    namespace persistence {
        class ReturnRepository {
            -String FILE_PATH = "data/returns.csv"
            -String DELIMITER = ";"
            -SaleService saleService
            -ProductService productService
            +ReturnRepository(SaleService saleService, ProductService productService)
            +saveAll(List~Return~ returns) void
            +loadAll() List~Return~
            -toLine(Return r) String
            -fromLine(String line) Return
            -parseLine(String line) Return
        }

        class SalePersistence {
            +save(List~Sale~ sales) void
            +load() List~Sale~
        }

        class ProductPersistence {
            +save(List~Product~ products) void
            +load() List~Product~
        }
    }

    namespace service {
        class ReturnService {
            -String RETURN_ID_PREFIX = "RET-"
            -ReturnRepository returnRepository
            -SaleService saleService
            -ProductService productService
            -List~Return~ returns
            +ReturnService(ReturnRepository returnRepository, SaleService saleService, ProductService productService)
            +registerReturn(String saleId, List~String~ productIds, String reason) Return
            +viewAllReturns() List~Return~
            +viewReturnsByCustomer(String customerId) List~Return~
            +viewReturnsBySale(String saleId) List~Return~
            +calculateMonthlySalesTotal(int month, int year) double
            +calculateMonthlyReturnsTotal(int month, int year) double
            +generateMonthlyBalance(int month, int year) double
            -countReturnableUnits(Sale sale) Map~String, Integer~
            -findProductById(List~Product~ products, String productId) Product
            -nextReturnId() String
            -validateMonth(int month) void
        }

        class SaleService {
            -String SALE_ID_PREFIX = "SALE-"
            -List~Sale~ sales
            +findById(String saleId) Sale
            +listAll() List~Sale~
            -nextSaleId() String
            -assignMissingIds() void
        }

        class ProductService {
            -List~Product~ products
            +findById(String productId) Product
            +reduceStock(String productId, int amount) boolean
            +restoreStock(String productId, int quantity) boolean
        }
    }

    namespace ui {
        class ConsoleUI {
            -ReturnService returnService
            -SaleService saleService
            -returnMenu() void
            -registerReturn() void
            -listReturnsByClient() void
            -listReturnsBySale() void
            -showMonthlyBalance() void
            -printReturns(List~Return~ returns, String emptyMessage) void
        }

        class Main {
            +main(String[] args)$ void
        }
    }

    Product <|-- VideoGame
    Product <|-- Console

    Return "0..*" --> "1" Sale : originalSale
    Return --> "1..*" Product : returnedProducts
    Sale "1" --> "1" Client
    Sale "1" --> "1" Seller
    Sale o-- Product : 1..*
    Sale o-- Accessory : 0..*

    ReturnRepository ..> Return : saves and loads
    ReturnRepository ..> SaleService : resolves sale by id
    ReturnRepository ..> ProductService : resolves products by id
    SalePersistence ..> Sale : saves and loads (with id)
    ProductPersistence ..> Product : saves and loads

    ReturnService ..> ReturnRepository
    ReturnService ..> SaleService : findById, listAll
    ReturnService ..> ProductService : restoreStock
    ReturnService ..> Return : creates
    ReturnService ..> Sale : canBeReturned
    SaleService ..> SalePersistence
    ProductService ..> ProductPersistence

    ConsoleUI ..> ReturnService
    ConsoleUI ..> SaleService
    Main ..> ReturnRepository
    Main ..> ReturnService
    Main ..> ConsoleUI
```