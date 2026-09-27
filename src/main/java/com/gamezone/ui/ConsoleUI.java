package com.gamezone.ui;

import com.gamezone.model.Accessory;
import com.gamezone.model.Cable;
import com.gamezone.model.Client;
import com.gamezone.model.Console;
import com.gamezone.model.Controller;
import com.gamezone.model.Memory;
import com.gamezone.model.Person;
import com.gamezone.model.Product;
import com.gamezone.model.Sale;
import com.gamezone.model.Seller;
import com.gamezone.model.VideoGame;
import com.gamezone.service.AccessoryService;
import com.gamezone.service.PersonService;
import com.gamezone.service.ProductService;
import com.gamezone.service.SaleService;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Provides the console-based user interface for GameZone Unicesar,
 * allowing the user to manage products, people, accessories, and sales
 * through a text menu that delegates all operations to the corresponding
 * services.
 */
public class ConsoleUI {

    private final ProductService productService;
    private final PersonService personService;
    private final SaleService saleService;
    private final AccessoryService accessoryService;
    private final Scanner scanner;

    /**
     * Creates a new ConsoleUI using the given services to perform all
     * business operations.
     *
     * @param productService the service used for product operations
     * @param personService  the service used for people operations
     * @param saleService    the service used for sale operations
     */
    public ConsoleUI(ProductService productService, PersonService personService, SaleService saleService) {
        this(productService, personService, saleService, null);
    }

    /**
     * Creates a new ConsoleUI that also supports managing accessories and
     * selling them together with products.
     *
     * @param productService   the service used for product operations
     * @param personService    the service used for people operations
     * @param saleService      the service used for sale operations
     * @param accessoryService the service used for accessory operations, or
     *                         null to disable the accessory module
     */
    public ConsoleUI(ProductService productService, PersonService personService,
                     SaleService saleService, AccessoryService accessoryService) {
        this.productService = productService;
        this.personService = personService;
        this.saleService = saleService;
        this.accessoryService = accessoryService;
        this.scanner = new Scanner(System.in);
    }

    /**
     * Starts the application's main loop, showing the main menu until
     * the user chooses to exit.
     */
    public void run() {
        boolean exit = false;
        while (!exit) {
            showMainMenu();
            int option = readOption();
            switch (option) {
                case 1 -> productMenu();
                case 2 -> personMenu();
                case 3 -> saleMenu();
                case 4 -> accessoryMenu();
                case 0 -> exit = true;
                default -> System.out.println("Invalid option. Please try again.");
            }
        }
        System.out.println("Thank you for using GameZone Unicesar!");
        scanner.close();
    }

    /**
     * Displays the main menu options.
     */
    private void showMainMenu() {
        System.out.println("\n===== GameZone Unicesar =====");
        System.out.println("1. Product management");
        System.out.println("2. Person management");
        System.out.println("3. Sale management");
        System.out.println("4. Accessory management");
        System.out.println("0. Exit");
        System.out.print("Select an option: ");
    }

    /**
     * Reads an integer option from the user, returning -1 if the input
     * is not a valid number.
     *
     * @return the option selected by the user
     */
    private int readOption() {
        try {
            return Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    // ===================== PRODUCT MENU =====================

    /**
     * Displays the product management submenu, allowing the user to register
     * video games, register consoles, and list all available products.
     */
    private void productMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- Product management ---");
            System.out.println("1. Register a new video game");
            System.out.println("2. Register a new console");
            System.out.println("3. List all products");
            System.out.println("0. Back to main menu");
            System.out.print("Select an option: ");
            int option = readOption();
            switch (option) {
                case 1 -> registerVideoGame();
                case 2 -> registerConsole();
                case 3 -> listProducts();
                case 0 -> back = true;
                default -> System.out.println("Invalid option. Please try again.");
            }
        }
    }

    /**
     * Prompts the user for the data of a new video game and registers it.
     */
    private void registerVideoGame() {
        CommonProductData data = readCommonProductData();
        if (data == null) {
            return;
        }
        System.out.print("Platform: ");
        String platform = scanner.nextLine().trim();
        System.out.print("Genre: ");
        String genre = scanner.nextLine().trim();
        System.out.print("Age rating: ");
        String ageRating = scanner.nextLine().trim();

        try {
            VideoGame videoGame = new VideoGame(data.id(), data.title(), data.price(), data.stock(), platform, genre, ageRating);
            productService.registerProduct(videoGame);
            System.out.println("Video game registered successfully.");
        } catch (IllegalArgumentException e) {
            System.out.println("Could not register video game: " + e.getMessage());
        }
    }

    /**
     * Prompts the user for the data of a new console and registers it.
     */
    private void registerConsole() {
        CommonProductData data = readCommonProductData();
        if (data == null) {
            return;
        }
        System.out.print("Brand: ");
        String brand = scanner.nextLine().trim();
        System.out.print("Model: ");
        String model = scanner.nextLine().trim();
        System.out.print("Generation: ");
        String generation = scanner.nextLine().trim();

        try {
            Console console = new Console(data.id(), data.title(), data.price(), data.stock(), brand, model, generation);
            productService.registerProduct(console);
            System.out.println("Console registered successfully.");
        } catch (IllegalArgumentException e) {
            System.out.println("Could not register console: " + e.getMessage());
        }
    }

    /**
     * Reads the attributes shared by every product type (id, title, price,
     * and stock), used when registering either a video game or a console.
     *
     * @return the common product data, or null if price or stock was invalid
     */
    private CommonProductData readCommonProductData() {
        System.out.print("Product id: ");
        String id = scanner.nextLine().trim();
        System.out.print("Title: ");
        String title = scanner.nextLine().trim();
        Double price = readPrice();
        Integer stock = readStock();
        if (price == null || stock == null) {
            return null;
        }
        return new CommonProductData(id, title, price, stock);
    }

    /**
     * Simple holder for the attributes shared by every product type,
     * used only while collecting input in the console UI.
     */
    private record CommonProductData(String id, String title, double price, int stock) {
    }

    /**
     * Lists every product currently available in the inventory.
     */
    private void listProducts() {
        List<Product> products = productService.listAll();
        if (products.isEmpty()) {
            System.out.println("No products registered yet.");
            return;
        }
        System.out.println("\n--- Product inventory ---");
        for (Product product : products) {
            System.out.println(product.getDescription());
        }
    }

    // ===================== PERSON MENU =====================

    /**
     * Displays the person management submenu, allowing the user to register
     * clients and list registered clients and sellers.
     */
    private void personMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- Person management ---");
            System.out.println("1. Register a new client");
            System.out.println("2. List all clients");
            System.out.println("3. List all sellers");
            System.out.println("0. Back to main menu");
            System.out.print("Select an option: ");
            int option = readOption();
            switch (option) {
                case 1 -> registerClient();
                case 2 -> listClients();
                case 3 -> listSellers();
                case 0 -> back = true;
                default -> System.out.println("Invalid option. Please try again.");
            }
        }
    }

    /**
     * Prompts the user for the data of a new client and registers it.
     */
    private void registerClient() {
        System.out.print("Client id: ");
        String id = scanner.nextLine().trim();
        System.out.print("Name: ");
        String name = scanner.nextLine().trim();
        System.out.print("Phone: ");
        String phone = scanner.nextLine().trim();
        System.out.print("Email: ");
        String email = scanner.nextLine().trim();

        try {
            personService.registerClient(id, name, phone, email);
            System.out.println("Client registered successfully.");
        } catch (IllegalArgumentException e) {
            System.out.println("Could not register client: " + e.getMessage());
        }
    }

    /**
     * Lists every client currently registered.
     */
    private void listClients() {
        List<Client> clients = personService.listClient();
        if (clients.isEmpty()) {
            System.out.println("No clients registered yet.");
            return;
        }
        System.out.println("\n--- Registered clients ---");
        for (Client client : clients) {
            System.out.println(client);
        }
    }

    /**
     * Lists every seller currently registered.
     */
    private void listSellers() {
        List<Seller> sellers = personService.listSeller();
        if (sellers.isEmpty()) {
            System.out.println("No sellers registered yet.");
            return;
        }
        System.out.println("\n--- Registered sellers ---");
        for (Seller seller : sellers) {
            System.out.println(seller);
        }
    }

    // ===================== SALE MENU =====================

    /**
     * Displays the sale management submenu, allowing the user to register a
     * new sale and consult sales history.
     */
    private void saleMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- Sale management ---");
            System.out.println("1. Register a new sale");
            System.out.println("2. View full sales history");
            System.out.println("3. View purchase history for a client");
            System.out.println("4. View sales history for a seller");
            System.out.println("0. Back to main menu");
            System.out.print("Select an option: ");
            int option = readOption();
            switch (option) {
                case 1 -> registerSale();
                case 2 -> listAllSales();
                case 3 -> listSalesByClient();
                case 4 -> listSalesBySeller();
                case 0 -> back = true;
                default -> System.out.println("Invalid option. Please try again.");
            }
        }
    }

    /**
     * Guides the user through registering a new sale: selecting the client,
     * the seller, one or more products and, when the accessory module is
     * available, one or more accessories. A sale must include at least one
     * product or accessory.
     */
    private void registerSale() {
        System.out.print("Client id: ");
        String clientId = scanner.nextLine().trim();
        Person clientPerson = personService.findById(clientId);
        if (!(clientPerson instanceof Client client)) {
            System.out.println("No client found with that id.");
            return;
        }

        System.out.print("Seller id: ");
        String sellerId = scanner.nextLine().trim();
        Person sellerPerson = personService.findById(sellerId);
        if (!(sellerPerson instanceof Seller seller)) {
            System.out.println("No seller found with that id.");
            return;
        }

        List<Product> products = new ArrayList<>();
        boolean addingProducts = true;
        while (addingProducts) {
            System.out.print("Product id to add (or 0 to finish): ");
            String productId = scanner.nextLine().trim();
            if (productId.equals("0")) {
                addingProducts = false;
                continue;
            }
            Product product = productService.findById(productId);
            if (product == null) {
                System.out.println("No product found with that id.");
                continue;
            }
            products.add(product);
            System.out.println(product.getTitle() + " added.");
        }

        if (accessoryService == null) {
            Sale sale = saleService.registerSale(client, seller, products);
            if (sale == null) {
                System.out.println("Sale could not be registered (no products, or insufficient stock).");
            } else {
                System.out.println("Sale registered successfully. Total: $" + sale.calculateTotal());
            }
            return;
        }

        List<Accessory> accessories = readSaleAccessories();
        try {
            Sale sale = saleService.registerSale(client, seller, products, accessories);
            if (sale == null) {
                System.out.println("Sale could not be registered (no products or accessories, or insufficient stock).");
            } else {
                System.out.println("Sale registered successfully. Total: $" + sale.calculateTotal());
            }
        } catch (IOException | IllegalStateException e) {
            System.out.println("Could not register sale: " + e.getMessage());
        }
    }

    /**
     * Asks the user for the accessories to include in a sale, one id at a
     * time, until the user enters 0.
     *
     * @return the list of accessories selected (may be empty)
     */
    private List<Accessory> readSaleAccessories() {
        List<Accessory> accessories = new ArrayList<>();
        boolean addingAccessories = true;
        while (addingAccessories) {
            System.out.print("Accessory id to add (or 0 to finish): ");
            String accessoryId = scanner.nextLine().trim();
            if (accessoryId.equals("0")) {
                addingAccessories = false;
                continue;
            }
            Accessory accessory = accessoryService.findById(accessoryId);
            if (accessory == null) {
                System.out.println("No accessory found with that id.");
                continue;
            }
            accessories.add(accessory);
            System.out.println(accessory.getTitle() + " added.");
        }
        return accessories;
    }

    /**
     * Displays the full history of sales registered in the system.
     */
    private void listAllSales() {
        List<Sale> sales = saleService.listAll();
        printSales(sales);
    }

    /**
     * Displays the purchase history of a specific client.
     */
    private void listSalesByClient() {
        System.out.print("Client id: ");
        String clientId = scanner.nextLine().trim();
        List<Sale> sales = saleService.findByClient(clientId);
        printSales(sales);
    }

    /**
     * Displays the sales history attended by a specific seller.
     */
    private void listSalesBySeller() {
        System.out.print("Seller id: ");
        String sellerId = scanner.nextLine().trim();
        List<Sale> sales = saleService.findBySeller(sellerId);
        printSales(sales);
    }

    /**
     * Prints the given list of sales, or a message if it is empty.
     *
     * @param sales the list of sales to print
     */
    private void printSales(List<Sale> sales) {
        if (sales.isEmpty()) {
            System.out.println("No sales found.");
            return;
        }
        System.out.println("\n--- Sales ---");
        for (Sale sale : sales) {
            System.out.println(sale);
            List<Accessory> accessories = sale.getAccessories();
            if (!accessories.isEmpty()) {
                StringBuilder titles = new StringBuilder();
                for (Accessory accessory : accessories) {
                    if (titles.length() > 0) {
                        titles.append(", ");
                    }
                    titles.append(accessory.getTitle());
                }
                System.out.println("  Accessories: " + titles);
            }
        }
    }

    // ===================== ACCESSORY MENU =====================

    /**
     * Displays the accessory management submenu, allowing the user to
     * register controllers, cables and memories, list and filter them,
     * query and register console compatibility, and update their stock.
     */
    private void accessoryMenu() {
        if (accessoryService == null) {
            System.out.println("The accessory module is not available.");
            return;
        }
        boolean back = false;
        while (!back) {
            System.out.println("\n--- Accessory management ---");
            System.out.println("1. Register a new controller");
            System.out.println("2. Register a new cable");
            System.out.println("3. Register a new memory");
            System.out.println("4. List all accessories");
            System.out.println("5. List accessories by type");
            System.out.println("6. Find accessories compatible with a console");
            System.out.println("7. Add a compatible console to a controller or memory");
            System.out.println("8. Update accessory stock");
            System.out.println("0. Back to main menu");
            System.out.print("Select an option: ");
            int option = readOption();
            switch (option) {
                case 1 -> registerController();
                case 2 -> registerCable();
                case 3 -> registerMemory();
                case 4 -> listAccessories();
                case 5 -> listAccessoriesByType();
                case 6 -> listAccessoriesCompatibleWithConsole();
                case 7 -> addCompatibleConsole();
                case 8 -> updateAccessoryStock();
                case 0 -> back = true;
                default -> System.out.println("Invalid option. Please try again.");
            }
        }
    }

    /**
     * Prompts the user for the data of a new controller, registers it and
     * optionally registers its compatible consoles.
     */
    private void registerController() {
        CommonAccessoryData data = readCommonAccessoryData();
        if (data == null) {
            return;
        }
        System.out.print("Connection type (1 = WIRELESS, 2 = WIRED): ");
        Controller.ConnectionType connectionType = switch (readOption()) {
            case 1 -> Controller.ConnectionType.WIRELESS;
            case 2 -> Controller.ConnectionType.WIRED;
            default -> null;
        };
        if (connectionType == null) {
            System.out.println("Invalid connection type.");
            return;
        }

        try {
            Controller controller = accessoryService.registerController(
                    data.id(), data.title(), data.price(), data.stock(), connectionType);
            System.out.println("Controller registered successfully.");
            readCompatibleConsoles(controller.getId());
        } catch (IllegalArgumentException | IOException e) {
            System.out.println("Could not register controller: " + e.getMessage());
        }
    }

    /**
     * Prompts the user for the data of a new cable and registers it.
     */
    private void registerCable() {
        CommonAccessoryData data = readCommonAccessoryData();
        if (data == null) {
            return;
        }
        System.out.print("Length in meters: ");
        double length;
        try {
            length = Double.parseDouble(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid length.");
            return;
        }
        System.out.print("Connector type (e.g. HDMI, USB): ");
        String connectorType = scanner.nextLine().trim();

        try {
            accessoryService.registerCable(data.id(), data.title(), data.price(), data.stock(),
                    length, connectorType);
            System.out.println("Cable registered successfully.");
        } catch (IllegalArgumentException | IOException e) {
            System.out.println("Could not register cable: " + e.getMessage());
        }
    }

    /**
     * Prompts the user for the data of a new memory, registers it and
     * optionally registers its compatible consoles.
     */
    private void registerMemory() {
        CommonAccessoryData data = readCommonAccessoryData();
        if (data == null) {
            return;
        }
        System.out.print("Capacity in GB: ");
        int capacity;
        try {
            capacity = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid capacity.");
            return;
        }
        System.out.print("Memory type (1 = SD, 2 = MICRO_SD, 3 = INTERNAL_CARD): ");
        Memory.MemoryType memoryType = switch (readOption()) {
            case 1 -> Memory.MemoryType.SD;
            case 2 -> Memory.MemoryType.MICRO_SD;
            case 3 -> Memory.MemoryType.INTERNAL_CARD;
            default -> null;
        };
        if (memoryType == null) {
            System.out.println("Invalid memory type.");
            return;
        }

        try {
            Memory memory = accessoryService.registerMemory(
                    data.id(), data.title(), data.price(), data.stock(), capacity, memoryType);
            System.out.println("Memory registered successfully.");
            readCompatibleConsoles(memory.getId());
        } catch (IllegalArgumentException | IOException e) {
            System.out.println("Could not register memory: " + e.getMessage());
        }
    }

    /**
     * Reads the attributes shared by every accessory type (id, title, price,
     * and stock), rejecting empty ids and ids already in use.
     *
     * @return the common accessory data, or null if any value was invalid
     */
    private CommonAccessoryData readCommonAccessoryData() {
        System.out.print("Accessory id: ");
        String id = scanner.nextLine().trim();
        if (id.isEmpty()) {
            System.out.println("The accessory id cannot be empty.");
            return null;
        }
        if (accessoryService.findById(id) != null) {
            System.out.println("An accessory with that id already exists.");
            return null;
        }
        System.out.print("Title: ");
        String title = scanner.nextLine().trim();
        Double price = readPrice();
        Integer stock = readStock();
        if (price == null || stock == null) {
            return null;
        }
        return new CommonAccessoryData(id, title, price, stock);
    }

    /**
     * Simple holder for the attributes shared by every accessory type,
     * used only while collecting input in the console UI.
     */
    private record CommonAccessoryData(String id, String title, double price, int stock) {
    }

    /**
     * Asks the user for console ids to register as compatible with the given
     * accessory, one at a time, until the user enters 0.
     *
     * @param accessoryId the id of the controller or memory being updated
     */
    private void readCompatibleConsoles(String accessoryId) {
        boolean adding = true;
        while (adding) {
            System.out.print("Compatible console id (or 0 to finish): ");
            String consoleId = scanner.nextLine().trim();
            if (consoleId.equals("0")) {
                adding = false;
                continue;
            }
            registerCompatibility(accessoryId, consoleId);
        }
    }

    /**
     * Validates that the given id belongs to a registered console and, if so,
     * registers it as compatible with the given accessory.
     *
     * @param accessoryId the id of the controller or memory
     * @param consoleId   the id of the console to register
     */
    private void registerCompatibility(String accessoryId, String consoleId) {
        if (!(productService.findById(consoleId) instanceof Console console)) {
            System.out.println("No console found with that id.");
            return;
        }
        try {
            if (accessoryService.addCompatibleConsole(accessoryId, consoleId)) {
                System.out.println(console.getTitle() + " registered as compatible.");
            } else {
                System.out.println("Only controllers and memories support console compatibility.");
            }
        } catch (IOException e) {
            System.out.println("Could not save compatibility: " + e.getMessage());
        }
    }

    /**
     * Lets the user add a compatible console to an existing controller or
     * memory.
     */
    private void addCompatibleConsole() {
        System.out.print("Accessory id: ");
        String accessoryId = scanner.nextLine().trim();
        Accessory accessory = accessoryService.findById(accessoryId);
        if (accessory == null) {
            System.out.println("No accessory found with that id.");
            return;
        }
        if (!(accessory instanceof Controller) && !(accessory instanceof Memory)) {
            System.out.println("Only controllers and memories support console compatibility.");
            return;
        }
        System.out.print("Console id: ");
        String consoleId = scanner.nextLine().trim();
        registerCompatibility(accessoryId, consoleId);
    }

    /**
     * Lists every accessory currently available in the inventory.
     */
    private void listAccessories() {
        printAccessories(accessoryService.listAllAccessories(), "No accessories registered yet.");
    }

    /**
     * Lists the accessories of the type chosen by the user.
     */
    private void listAccessoriesByType() {
        System.out.print("Type (Controller, Cable or Memory): ");
        String type = scanner.nextLine().trim();
        printAccessories(accessoryService.listAccessoriesByType(type),
                "No accessories found for that type.");
    }

    /**
     * Lists the accessories registered as compatible with the console chosen
     * by the user.
     */
    private void listAccessoriesCompatibleWithConsole() {
        System.out.print("Console id: ");
        String consoleId = scanner.nextLine().trim();
        printAccessories(accessoryService.findAccessoriesCompatibleWith(consoleId),
                "No accessories registered as compatible with that console.");
    }

    /**
     * Updates the stock of an accessory to a new non-negative quantity.
     */
    private void updateAccessoryStock() {
        System.out.print("Accessory id: ");
        String accessoryId = scanner.nextLine().trim();
        if (accessoryService.findById(accessoryId) == null) {
            System.out.println("No accessory found with that id.");
            return;
        }
        Integer stock = readStock();
        if (stock == null) {
            return;
        }
        if (stock < 0) {
            System.out.println("Stock cannot be negative.");
            return;
        }
        try {
            accessoryService.updateStock(accessoryId, stock);
            System.out.println("Stock updated successfully.");
        } catch (IOException e) {
            System.out.println("Could not update stock: " + e.getMessage());
        }
    }

    /**
     * Prints the given list of accessories with their type and id, or the
     * given message if the list is empty.
     *
     * @param accessories  the accessories to print
     * @param emptyMessage the message shown when there are no accessories
     */
    private void printAccessories(List<Accessory> accessories, String emptyMessage) {
        if (accessories.isEmpty()) {
            System.out.println(emptyMessage);
            return;
        }
        System.out.println("\n--- Accessories ---");
        for (Accessory accessory : accessories) {
            System.out.println("[" + accessory.getType() + "] " + accessory.getId() + " - "
                    + accessory.getDescription());
        }
    }

    // ===================== INPUT HELPERS =====================

    /**
     * Reads a price from the user, returning null if the input is not a
     * valid non-negative number.
     *
     * @return the price entered, or null if invalid
     */
    private Double readPrice() {
        System.out.print("Price: ");
        try {
            return Double.parseDouble(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid price.");
            return null;
        }
    }

    /**
     * Reads a stock quantity from the user, returning null if the input is
     * not a valid integer.
     *
     * @return the stock entered, or null if invalid
     */
    private Integer readStock() {
        System.out.print("Stock: ");
        try {
            return Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid stock.");
            return null;
        }
    }
}