# Return Module Class Diagram

This diagram shows the return module and how it integrates with the existing classes of the system, organized by layer. It includes the new `Return` class with its attributes, methods, and its association with the existing `Sale` class; the new persistence and service classes; and the integration with `Sale` (sale id and `canBeReturned()`), `ProductService` (`restoreStock()`), and the extension of the console menu. Classes of the system not involved in the return module are omitted for readability; see `class-diagram.md` for the complete base system and `promotion-class-diagram.md` for the promotion module. After the integration adjustments of Requirement 5, a return can include accessories (A4), refunds each item in proportion to the sale discount (A5), and cancels the warranties of returned consoles (A7). `ReturnRepository` stores and loads only identifiers (`ReturnRecord`), and `ReturnService` resolves the sale and the returned items, so the persistence layer does not depend on the service layer. See `integrated-class-diagram.md` for the complete integrated system.

```mermaid
classDiagram
    namespace model {
        class Return {
            -String id
            -LocalDate date
            -Sale originalSale
            -List~Product~ returnedProducts
            -List~Accessory~ returnedAccessories
            -String reason
            -double warrantyRefund
            -double refundAmount
            +Return(String id, LocalDate date, Sale originalSale, List~Product~ returnedProducts, String reason)
            +Return(String id, LocalDate date, Sale originalSale, List~Product~ returnedProducts, List~Accessory~ returnedAccessories, String reason)
            +Return(String id, LocalDate date, Sale originalSale, List~Product~ returnedProducts, List~Accessory~ returnedAccessories, String reason, double warrantyRefund)
            +getId() String
            +getDate() LocalDate
            +getOriginalSale() Sale
            +getReturnedProducts() List~Product~
            +getReturnedAccessories() List~Accessory~
            +getReason() String
            +getWarrantyRefund() double
            +getRefundAmount() double
            +calculateRefundAmount() double
            -paidFactor() double
            -refundFor(double listPrice) double
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
            -double warrantyCost
            +getId() String
            +setId(String id) void
            +getDate() LocalDate
            +getClient() Client
            +getProducts() List~Product~
            +getAccessories() List~Accessory~
            +getDiscountAmount() double
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
        class Accessory {
            <<abstract>>
            -String id
            -String title
            -double price
            -int stock
        }
        class Warranty {
            <<abstract>>
            +getAdditionalCost()* double
        }
        class Client
        class Seller
    }

    namespace persistence {
        class ReturnRepository {
            -String FILE_PATH = "data/returns.csv"
            -String DELIMITER = ";"
            -String ID_SEPARATOR = ","
            +saveAll(List~Return~ returns) void
            +loadAll() List~ReturnRecord~
            -toLine(Return r) String
            -fromLine(String line) ReturnRecord
            -parseLine(String line) ReturnRecord
        }

        class ReturnRecord {
            <<record>>
            String id
            LocalDate date
            String saleId
            List~String~ itemIds
            String reason
            double warrantyRefund
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
            -AccessoryService accessoryService
            -WarrantyService warrantyService
            -List~Return~ returns
            +ReturnService(ReturnRepository returnRepository, SaleService saleService, ProductService productService, AccessoryService accessoryService, WarrantyService warrantyService)
            -resolveReturns(List~ReturnRecord~ records) List~Return~
            +registerReturn(String saleId, List~String~ productIds, String reason) Return
            +viewAllReturns() List~Return~
            +viewReturnsByCustomer(String customerId) List~Return~
            +viewReturnsBySale(String saleId) List~Return~
            +calculateMonthlySales(int month, int year) double
            +calculateMonthlyReturns(int month, int year) double
            +generateMonthlyBalance(int month, int year) double
            -countReturnableUnits(Sale sale) Map~String, Integer~
            -findProductById(List~Product~ products, String productId) Product
            -findAccessoryById(List~Accessory~ accessories, String accessoryId) Accessory
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

        class AccessoryService {
            +findById(String id) Accessory
            +reduceStock(String accessoryId, int amount) boolean
            +restoreStock(String accessoryId, int quantity) boolean
        }

        class WarrantyService {
            +cancelWarranties(String productId, String saleId) double
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
    Return --> "0..*" Product : returnedProducts
    Return --> "0..*" Accessory : returnedAccessories
    Warranty --> "1" Sale : sale
    Sale "1" --> "1" Client
    Sale "1" --> "1" Seller
    Sale o-- Product : 1..*
    Sale o-- Accessory : 0..*

    ReturnRepository ..> Return : saves
    ReturnRepository ..> ReturnRecord : loads
    SalePersistence ..> Sale : saves and loads (with id)
    ProductPersistence ..> Product : saves and loads

    ReturnService ..> ReturnRepository
    ReturnService ..> ReturnRecord : resolves
    ReturnService ..> SaleService : findById, listAll
    ReturnService ..> ProductService : restoreStock
    ReturnService ..> AccessoryService : restoreStock
    ReturnService ..> WarrantyService : cancelWarranties
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