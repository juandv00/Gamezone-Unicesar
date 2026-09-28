package com.gamezone.ui;

import com.gamezone.model.Accessory;
import com.gamezone.model.Cable;
import com.gamezone.model.Client;
import com.gamezone.model.Console;
import com.gamezone.model.Controller;
import com.gamezone.model.Memory;
import com.gamezone.model.Person;
import com.gamezone.model.Product;
import com.gamezone.model.Promotion;
import com.gamezone.model.Return;
import com.gamezone.model.Warranty;
import com.gamezone.model.Sale;
import com.gamezone.model.Seller;
import com.gamezone.model.VideoGame;
import com.gamezone.service.AccessoryService;
import com.gamezone.service.PersonService;
import com.gamezone.service.ProductService;
import com.gamezone.service.PromotionService;
import com.gamezone.service.ReturnService;
import com.gamezone.service.WarrantyService;
import com.gamezone.service.SaleService;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import java.util.Scanner;

/**
 * Provides the console-based user interface for GameZone Unicesar, allowing the
 * user to manage products, people, accessories, sales, promotions, returns, and
 * warranties through a text menu that delegates all operations to the
 * corresponding services.
 */
public class ConsoleUI {

    private final ProductService productService;
    private final PersonService personService;
    private final SaleService saleService;
    private final AccessoryService accessoryService;
    private final PromotionService promotionService;
    private final ReturnService returnService;
    private final WarrantyService warrantyService;
    private final Scanner scanner;

    /**
     * Creates a new ConsoleUI using the given services to perform all business
     * operations.
     *
     * @param productService the service used for product operations
     * @param personService the service used for people operations
     * @param saleService the service used for sale operations
     */
    public ConsoleUI(ProductService productService, PersonService personService, SaleService saleService) {
        this(productService, personService, saleService, null);
    }

    /**
     * Creates a new ConsoleUI that also supports managing accessories and
     * selling them together with products.
     *
     * @param productService the service used for product operations
     * @param personService the service used for people operations
     * @param saleService the service used for sale operations
     * @param accessoryService the service used for accessory operations, or
     * null to disable the accessory module
     */
    public ConsoleUI(ProductService productService, PersonService personService,
            SaleService saleService, AccessoryService accessoryService) {
        this(productService, personService, saleService, accessoryService, null);
    }

    /**
     * Creates a new ConsoleUI that also supports managing promotions, in
     * addition to accessories and sales.
     *
     * @param productService the service used for product operations
     * @param personService the service used for people operations
     * @param saleService the service used for sale operations
     * @param accessoryService the service used for accessory operations, or
     * null to disable the accessory module
     * @param promotionService the service used for promotion operations, or
     * null to disable the promotion module
     */
    public ConsoleUI(ProductService productService, PersonService personService,
            SaleService saleService, AccessoryService accessoryService,
            PromotionService promotionService) {
        this(productService, personService, saleService, accessoryService, promotionService, null);
    }

    /**
     * Creates a new ConsoleUI that also supports registering and consulting
     * product returns and the monthly balance report, in addition to
     * accessories, promotions, and sales.
     *
     * @param productService the service used for product operations
     * @param personService the service used for people operations
     * @param saleService the service used for sale operations
     * @param accessoryService the service used for accessory operations, or
     * null to disable the accessory module
     * @param promotionService the service used for promotion operations, or
     * null to disable the promotion module
     * @param returnService the service used for return operations, or null to
     * disable the return module
     */
    public ConsoleUI(ProductService productService, PersonService personService,
            SaleService saleService, AccessoryService accessoryService,
            PromotionService promotionService, ReturnService returnService) {
        this(productService, personService, saleService, accessoryService, promotionService,
                returnService, null);
    }

    /**
     * Creates a new ConsoleUI that also supports extended warranties when
     * registering a sale and consulting the warranties of sold consoles, in
     * addition to accessories, promotions, returns, and sales.
     *
     * @param productService the service used for product operations
     * @param personService the service used for people operations
     * @param saleService the service used for sale operations
     * @param accessoryService the service used for accessory operations, or
     * null to disable the accessory module
     * @param promotionService the service used for promotion operations, or
     * null to disable the promotion module
     * @param returnService the service used for return operations, or null to
     * disable the return module
     * @param warrantyService the service used for warranty operations, or null
     * to disable the warranty module
     */
    public ConsoleUI(ProductService productService, PersonService personService,
            SaleService saleService, AccessoryService accessoryService,
            PromotionService promotionService, ReturnService returnService,
            WarrantyService warrantyService) {
        this.productService = productService;
        this.personService = personService;
        this.saleService = saleService;
        this.accessoryService = accessoryService;
        this.promotionService = promotionService;
        this.returnService = returnService;
        this.warrantyService = warrantyService;
        this.scanner = new Scanner(System.in);
    }

    /**
     * Starts the application's main loop, showing the main menu until the user
     * chooses to exit.
     */
    public void run() {
        boolean exit = false;
        while (!exit) {
            showMainMenu();
            int option = readOption();
            switch (option) {
                case 1 ->
                    productMenu();
                case 2 ->
                    personMenu();
                case 3 ->
                    saleMenu();
                case 4 ->
                    accessoryMenu();
                case 5 ->
                    promotionMenu();
                case 6 ->
                    returnMenu();
                case 7 ->
                    warrantyMenu();
                case 0 ->
                    exit = true;
                default ->
                    System.out.println("Invalid option. Please try again.");
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
        System.out.println("5. Promotion management");
        System.out.println("6. Return management");
        System.out.println("7. Warranty management");
        System.out.println("0. Exit");
        System.out.print("Select an option: ");
    }

    /**
     * Reads an integer option from the user, returning -1 if the input is not a
     * valid number.
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
                case 1 ->
                    registerVideoGame();
                case 2 ->
                    registerConsole();
                case 3 ->
                    listProducts();
                case 0 ->
                    back = true;
                default ->
                    System.out.println("Invalid option. Please try again.");
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
     * Reads the attributes shared by every product type (id, title, price, and
     * stock), used when registering either a video game or a console.
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
     * Simple holder for the attributes shared by every product type, used only
     * while collecting input in the console UI.
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
                case 1 ->
                    registerClient();
                case 2 ->
                    listClients();
                case 3 ->
                    listSellers();
                case 0 ->
                    back = true;
                default ->
                    System.out.println("Invalid option. Please try again.");
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
     * Displays the sale management submenu, allowing the user to register a new
     * sale and consult sales history.
     */
    private void saleMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- Sale management ---");
            System.out.println("1. Register a new sale");
            System.out.println("2. View full sales history");
            System.out.println("3. View purchase history for a client");
            System.out.println("4. View sales history for a seller");
            System.out.println("5. View the receipt of a sale");
            System.out.println("0. Back to main menu");
            System.out.print("Select an option: ");
            int option = readOption();
            switch (option) {
                case 1 ->
                    registerSale();
                case 2 ->
                    listAllSales();
                case 3 ->
                    listSalesByClient();
                case 4 ->
                    listSalesBySeller();
                case 5 ->
                    viewSaleReceipt();
                case 0 ->
                    back = true;
                default ->
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }

    /**
     * Guides the user through registering a new sale: selecting the client, the
     * seller, and the items sold (products and accessories are entered in the
     * same list, by id). When the warranty module is available, the user is
     * asked whether each console of the sale should receive an extended
     * warranty. The sale follows the unified registration flow of SaleService,
     * and the receipt and warranty certificates are printed.
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

        List<String> itemIds = new ArrayList<>();
        List<Product> products = new ArrayList<>();
        boolean addingItems = true;
        while (addingItems) {
            System.out.print("Item id to add, product or accessory (or 0 to finish): ");
            String itemId = scanner.nextLine().trim();
            if (itemId.equals("0")) {
                addingItems = false;
                continue;
            }
            Product product = productService.findById(itemId);
            if (product != null) {
                itemIds.add(product.getId());
                products.add(product);
                System.out.println(product.getTitle() + " added.");
                continue;
            }
            Accessory accessory = accessoryService != null ? accessoryService.findById(itemId) : null;
            if (accessory != null) {
                itemIds.add(accessory.getId());
                System.out.println(accessory.getTitle() + " (accessory) added.");
                continue;
            }
            System.out.println("No product or accessory found with that id.");
        }

        List<String> extendedIds = warrantyService != null ? askExtendedWarranties(products) : new ArrayList<>();
        try {
            Sale sale = saleService.registerSaleByItemIds(client, seller, itemIds, extendedIds);
            if (sale == null) {
                System.out.println("Sale could not be registered (no items, or insufficient stock).");
            } else {
                System.out.println("Sale registered successfully.");
                System.out.println(sale.generateReceipt());
                if (warrantyService != null) {
                    printSaleWarranties(sale);
                }
            }
        } catch (IOException | IllegalArgumentException | IllegalStateException e) {
            System.out.println("Could not register sale: " + e.getMessage());
        }
    }

    /**
     * Asks the user, for each different console included in the sale, whether
     * it should receive an extended warranty instead of the basic one, showing
     * its cost (10% of the console price, per unit).
     *
     * @param products the products of the sale
     * @return the ids of the consoles that must receive an extended warranty
     */
    private List<String> askExtendedWarranties(List<Product> products) {
        List<String> extendedIds = new ArrayList<>();
        List<String> askedIds = new ArrayList<>();
        for (Product product : products) {
            if (!(product instanceof Console) || askedIds.contains(product.getId())) {
                continue;
            }
            askedIds.add(product.getId());
            System.out.print("Add an extended warranty (12 months, +" + formatAmount(product.getPrice() * 0.10)
                    + " per unit) to " + product.getTitle() + "? (y/n): ");
            String answer = scanner.nextLine().trim();
            if (answer.equalsIgnoreCase("y") || answer.equalsIgnoreCase("yes")) {
                extendedIds.add(product.getId());
            }
        }
        return extendedIds;
    }

    /**
     * Prints the certificate of every warranty generated for the consoles of
     * the given sale.
     *
     * @param sale the sale whose warranties are printed
     */
    private void printSaleWarranties(Sale sale) {
        for (Warranty warranty : warrantyService.listAllWarranties()) {
            if (warranty.getSale().getId().equals(sale.getId())) {
                System.out.println(warranty.generateWarrantyCertificate());
            }
        }
    }

    /**
     * Displays the full history of sales registered in the system.
     */
    private void listAllSales() {
        List<Sale> sales = saleService.listAll();
        printSales(sales);
    }

    /**
     * Lists every registered sale with a number and shows the full receipt of
     * the sale chosen by the user, including the discount applied.
     */
    private void viewSaleReceipt() {
        List<Sale> sales = saleService.listAll();
        if (sales.isEmpty()) {
            System.out.println("No sales found.");
            return;
        }
        System.out.println("\n--- Sales ---");
        for (int i = 0; i < sales.size(); i++) {
            System.out.println((i + 1) + ". " + sales.get(i));
        }
        System.out.print("Sale number to view: ");
        int number = readOption();
        if (number < 1 || number > sales.size()) {
            System.out.println("Invalid sale number.");
            return;
        }
        System.out.println(sales.get(number - 1).generateReceipt());
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
     * Displays the accessory management submenu, allowing the user to register
     * controllers, cables and memories, list and filter them, query and
     * register console compatibility, and update their stock.
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
                case 1 ->
                    registerController();
                case 2 ->
                    registerCable();
                case 3 ->
                    registerMemory();
                case 4 ->
                    listAccessories();
                case 5 ->
                    listAccessoriesByType();
                case 6 ->
                    listAccessoriesCompatibleWithConsole();
                case 7 ->
                    addCompatibleConsole();
                case 8 ->
                    updateAccessoryStock();
                case 0 ->
                    back = true;
                default ->
                    System.out.println("Invalid option. Please try again.");
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
            case 1 ->
                Controller.ConnectionType.WIRELESS;
            case 2 ->
                Controller.ConnectionType.WIRED;
            default ->
                null;
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
            case 1 ->
                Memory.MemoryType.SD;
            case 2 ->
                Memory.MemoryType.MICRO_SD;
            case 3 ->
                Memory.MemoryType.INTERNAL_CARD;
            default ->
                null;
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
     * Simple holder for the attributes shared by every accessory type, used
     * only while collecting input in the console UI.
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
     * @param consoleId the id of the console to register
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
     * Lists the accessories registered as compatible with the console chosen by
     * the user.
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
     * Prints the given list of accessories with their type and id, or the given
     * message if the list is empty.
     *
     * @param accessories the accessories to print
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

    // ===================== WARRANTY MENU =====================
    /**
     * Displays the warranty management submenu, allowing the user to consult
     * the warranty of a product in a specific sale, and to list all warranties,
     * the ones active today, or the ones expiring soon.
     */
    private void warrantyMenu() {
        if (warrantyService == null) {
            System.out.println("The warranty module is not available.");
            return;
        }
        boolean back = false;
        while (!back) {
            System.out.println("\n--- Warranty management ---");
            System.out.println("1. View the warranty of a product in a sale");
            System.out.println("2. List all warranties");
            System.out.println("3. List active warranties (today)");
            System.out.println("4. List warranties expiring soon");
            System.out.println("0. Back to main menu");
            System.out.print("Select an option: ");
            int option = readOption();
            switch (option) {
                case 1 ->
                    viewWarrantyOfProduct();
                case 2 ->
                    printWarranties(warrantyService.listAllWarranties(), "No warranties registered yet.");
                case 3 ->
                    printWarranties(warrantyService.listActiveWarranties(), "No warranties are active today.");
                case 4 ->
                    listWarrantiesExpiringSoon();
                case 0 ->
                    back = true;
                default ->
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }

    /**
     * Asks the user for a sale and a product of that sale, and shows the
     * certificate of its warranty and whether it is active today.
     */
    private void viewWarrantyOfProduct() {
        System.out.print("Sale id (e.g. SALE-1): ");
        Sale sale = saleService.findById(scanner.nextLine().trim());
        if (sale == null) {
            System.out.println("No sale found with that id.");
            return;
        }
        System.out.print("Product id: ");
        String productId = scanner.nextLine().trim();
        Product product = null;
        for (Product candidate : sale.getProducts()) {
            if (candidate.getId().equalsIgnoreCase(productId)) {
                product = candidate;
                break;
            }
        }
        if (product == null) {
            System.out.println("That product is not part of sale " + sale.getId() + ".");
            return;
        }
        Warranty warranty = warrantyService.findWarrantyByProduct(product.getId(), sale.getId());
        if (warranty == null) {
            System.out.println(product.getTitle() + " has no warranty in sale " + sale.getId()
                    + " (only consoles receive warranties).");
            return;
        }
        System.out.println(warranty.generateWarrantyCertificate());
        System.out.println(warranty.isActive(LocalDate.now())
                ? "Status: ACTIVE (covered until " + warranty.getEndDate() + ")"
                : "Status: EXPIRED on " + warranty.getEndDate());
    }

    /**
     * Asks the user how many days ahead to look and lists the active warranties
     * that expire within that period.
     */
    private void listWarrantiesExpiringSoon() {
        System.out.print("Days ahead (e.g. 30): ");
        int daysAhead = readOption();
        if (daysAhead < 0) {
            System.out.println("The number of days must be zero or greater.");
            return;
        }
        printWarranties(warrantyService.listWarrantiesExpiringSoon(daysAhead),
                "No warranties expire in the next " + daysAhead + " days.");
    }

    /**
     * Prints a one-line summary of each warranty in the given list, marking it
     * as active or expired today, or the given message if the list is empty.
     *
     * @param warranties the warranties to print
     * @param emptyMessage the message shown when there are no warranties
     */
    private void printWarranties(List<Warranty> warranties, String emptyMessage) {
        if (warranties.isEmpty()) {
            System.out.println(emptyMessage);
            return;
        }
        LocalDate today = LocalDate.now();
        System.out.println("\n--- Warranties ---");
        for (Warranty warranty : warranties) {
            String status = warranty.isActive(today) ? "[ACTIVE] " : "[EXPIRED] ";
            System.out.println(status + warranty.getId() + " | " + warranty.getWarrantyType()
                    + " | " + warranty.getProduct().getTitle()
                    + " | sale " + warranty.getSale().getId()
                    + " | " + warranty.getStartDate() + " to " + warranty.getEndDate()
                    + " | cost " + formatAmount(warranty.getAdditionalCost()));
        }
    }

    // ===================== RETURN MENU =====================
    /**
     * Displays the return management submenu, allowing the user to register
     * returns, consult them (all, by client, or by sale), and view the monthly
     * balance report.
     */
    private void returnMenu() {
        if (returnService == null) {
            System.out.println("The return module is not available.");
            return;
        }
        boolean back = false;
        while (!back) {
            System.out.println("\n--- Return management ---");
            System.out.println("1. Register a new return");
            System.out.println("2. List all returns");
            System.out.println("3. List returns by client");
            System.out.println("4. List returns by sale");
            System.out.println("5. View monthly balance");
            System.out.println("0. Back to main menu");
            System.out.print("Select an option: ");
            int option = readOption();
            switch (option) {
                case 1 ->
                    registerReturn();
                case 2 ->
                    printReturns(returnService.viewAllReturns(), "No returns registered yet.");
                case 3 ->
                    listReturnsByClient();
                case 4 ->
                    listReturnsBySale();
                case 5 ->
                    showMonthlyBalance();
                case 0 ->
                    back = true;
                default ->
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }

    /**
     * Guides the user through registering a return: choosing the original sale,
     * selecting which of its products are returned, and giving a reason. The
     * business rules (30-day window, products belonging to the sale, units not
     * returned twice) are validated by the ReturnService; the sale window is
     * also checked here to warn the user early.
     */
    private void registerReturn() {
        System.out.print("Sale id (e.g. SALE-1): ");
        String saleId = scanner.nextLine().trim();
        Sale sale = saleService.findById(saleId);
        if (sale == null) {
            System.out.println("No sale found with that id.");
            return;
        }
        if (!sale.canBeReturned()) {
            System.out.println("This sale can no longer be returned: more than 30 days have passed since "
                    + sale.getDate() + ".");
            return;
        }

        System.out.println("\nProducts in sale " + sale.getId() + ":");
        for (Product product : sale.getProducts()) {
            System.out.println("  " + product.getId() + " - " + product.getTitle()
                    + " (" + formatAmount(product.getPrice()) + ")");
        }

        List<String> productIds = new ArrayList<>();
        boolean addingProducts = true;
        while (addingProducts) {
            System.out.print("Product id to return (or 0 to finish): ");
            String productId = scanner.nextLine().trim();
            if (productId.equals("0")) {
                addingProducts = false;
            } else if (!productId.isEmpty()) {
                productIds.add(productId);
            }
        }
        if (productIds.isEmpty()) {
            System.out.println("No products selected. The return was cancelled.");
            return;
        }

        System.out.print("Reason for the return: ");
        String reason = scanner.nextLine().trim();

        try {
            Return registeredReturn = returnService.registerReturn(saleId, productIds, reason);
            System.out.println("Return registered successfully.");
            System.out.println(registeredReturn.generateReturnReceipt());
        } catch (IllegalArgumentException e) {
            System.out.println("Could not register return: " + e.getMessage());
        }
    }

    /**
     * Lists the returns of the sales made by a client chosen by the user.
     */
    private void listReturnsByClient() {
        System.out.print("Client id: ");
        String clientId = scanner.nextLine().trim();
        printReturns(returnService.viewReturnsByCustomer(clientId),
                "No returns found for that client.");
    }

    /**
     * Lists the returns associated with a sale chosen by the user.
     */
    private void listReturnsBySale() {
        System.out.print("Sale id: ");
        String saleId = scanner.nextLine().trim();
        printReturns(returnService.viewReturnsBySale(saleId), "No returns found for that sale.");
    }

    /**
     * Asks the user for a month and year and shows the monthly balance: total
     * sales, total refunded by returns, and the net balance.
     */
    private void showMonthlyBalance() {
        System.out.print("Month (1-12): ");
        int month = readOption();
        System.out.print("Year (e.g. 2026): ");
        int year = readOption();
        if (year <= 0) {
            System.out.println("Invalid year.");
            return;
        }
        try {
            double sales = returnService.calculateMonthlySales(month, year);
            double refunds = returnService.calculateMonthlyReturns(month, year);
            double balance = returnService.generateMonthlyBalance(month, year);
            System.out.println("\n===== Monthly balance " + String.format("%02d/%d", month, year) + " =====");
            System.out.println("Total sales:   " + formatAmount(sales));
            System.out.println("Total returns: " + formatAmount(refunds));
            System.out.println("Net balance:   " + formatAmount(balance));
        } catch (IllegalArgumentException e) {
            System.out.println("Could not generate the balance: " + e.getMessage());
        }
    }

    /**
     * Prints a one-line summary of each return in the given list, or the given
     * message if the list is empty.
     *
     * @param returns the returns to print
     * @param emptyMessage the message shown when there are no returns
     */
    private void printReturns(List<Return> returns, String emptyMessage) {
        if (returns.isEmpty()) {
            System.out.println(emptyMessage);
            return;
        }
        System.out.println("\n--- Returns ---");
        for (Return r : returns) {
            Sale sale = r.getOriginalSale();
            System.out.println(r.getId() + " | " + r.getDate()
                    + " | sale " + sale.getId()
                    + " | client " + sale.getClient().getName()
                    + " | " + r.getReturnedProducts().size() + " product(s)"
                    + " | refund " + formatAmount(r.getRefundAmount())
                    + " | reason: " + r.getReason());
        }
    }

    /**
     * Formats an amount of money for display, e.g. $1,250,000.00.
     *
     * @param amount the amount to format
     * @return the formatted amount
     */
    private String formatAmount(double amount) {
        return String.format(Locale.US, "$%,.2f", amount);
    }

    // ===================== PROMOTION MENU =====================
    /**
     * Displays the promotion management submenu, allowing the user to register
     * percentage, category and bulk purchase promotions, and to list all
     * promotions or only the ones active today.
     */
    private void promotionMenu() {
        if (promotionService == null) {
            System.out.println("The promotion module is not available.");
            return;
        }
        boolean back = false;
        while (!back) {
            System.out.println("\n--- Promotion management ---");
            System.out.println("1. Register a new percentage promotion");
            System.out.println("2. Register a new category promotion");
            System.out.println("3. Register a new bulk purchase promotion");
            System.out.println("4. List all promotions");
            System.out.println("5. List active promotions (today)");
            System.out.println("0. Back to main menu");
            System.out.print("Select an option: ");
            int option = readOption();
            switch (option) {
                case 1 ->
                    registerPercentagePromotion();
                case 2 ->
                    registerCategoryPromotion();
                case 3 ->
                    registerBulkPurchasePromotion();
                case 4 ->
                    printPromotions(promotionService.listAllPromotions(),
                            "No promotions registered yet.");
                case 5 ->
                    printPromotions(promotionService.listActivePromotions(),
                            "No promotions are active today.");
                case 0 ->
                    back = true;
                default ->
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }

    /**
     * Prompts the user for the data of a new percentage promotion and registers
     * it.
     */
    private void registerPercentagePromotion() {
        CommonPromotionData data = readCommonPromotionData();
        if (data == null) {
            return;
        }
        Double percentage = readPercentage();
        if (percentage == null) {
            return;
        }
        try {
            promotionService.registerPercentageDiscount(data.id(), data.name(),
                    data.startDate(), data.endDate(), percentage);
            System.out.println("Percentage promotion registered successfully.");
        } catch (IllegalArgumentException e) {
            System.out.println("Could not register promotion: " + e.getMessage());
        }
    }

    /**
     * Prompts the user for the data of a new category promotion and registers
     * it.
     */
    private void registerCategoryPromotion() {
        CommonPromotionData data = readCommonPromotionData();
        if (data == null) {
            return;
        }
        Double percentage = readPercentage();
        if (percentage == null) {
            return;
        }
        System.out.print("Target category (1 = VIDEOGAME, 2 = CONSOLE, 3 = ACCESSORY): ");
        String targetCategory = switch (readOption()) {
            case 1 ->
                "VIDEOGAME";
            case 2 ->
                "CONSOLE";
            case 3 ->
                "ACCESSORY";
            default ->
                null;
        };
        if (targetCategory == null) {
            System.out.println("Invalid category.");
            return;
        }
        try {
            promotionService.registerCategoryDiscount(data.id(), data.name(),
                    data.startDate(), data.endDate(), percentage, targetCategory);
            System.out.println("Category promotion registered successfully.");
        } catch (IllegalArgumentException e) {
            System.out.println("Could not register promotion: " + e.getMessage());
        }
    }

    /**
     * Prompts the user for the data of a new bulk purchase promotion and
     * registers it.
     */
    private void registerBulkPurchasePromotion() {
        CommonPromotionData data = readCommonPromotionData();
        if (data == null) {
            return;
        }
        System.out.print("Minimum number of products: ");
        int minimumQuantity = readOption();
        if (minimumQuantity <= 0) {
            System.out.println("The minimum number of products must be greater than zero.");
            return;
        }
        Double percentage = readPercentage();
        if (percentage == null) {
            return;
        }
        try {
            promotionService.registerBulkPurchaseDiscount(data.id(), data.name(),
                    data.startDate(), data.endDate(), minimumQuantity, percentage);
            System.out.println("Bulk purchase promotion registered successfully.");
        } catch (IllegalArgumentException e) {
            System.out.println("Could not register promotion: " + e.getMessage());
        }
    }

    /**
     * Reads the attributes shared by every promotion type (id, name, start date
     * and end date), rejecting empty or repeated ids and date ranges where the
     * start date is after the end date.
     *
     * @return the common promotion data, or null if any value was invalid
     */
    private CommonPromotionData readCommonPromotionData() {
        System.out.print("Promotion id: ");
        String id = scanner.nextLine().trim();
        if (id.isEmpty()) {
            System.out.println("The promotion id cannot be empty.");
            return null;
        }
        if (promotionService.findById(id) != null) {
            System.out.println("A promotion with that id already exists.");
            return null;
        }
        System.out.print("Name: ");
        String name = scanner.nextLine().trim();
        LocalDate startDate = readDate("Start date (YYYY-MM-DD): ");
        if (startDate == null) {
            return null;
        }
        LocalDate endDate = readDate("End date (YYYY-MM-DD): ");
        if (endDate == null) {
            return null;
        }
        if (startDate.isAfter(endDate)) {
            System.out.println("The start date cannot be after the end date.");
            return null;
        }
        return new CommonPromotionData(id, name, startDate, endDate);
    }

    /**
     * Simple holder for the attributes shared by every promotion type, used
     * only while collecting input in the console UI.
     */
    private record CommonPromotionData(String id, String name, LocalDate startDate, LocalDate endDate) {

    }

    /**
     * Prints the given list of promotions, marking each one as active or
     * inactive today, or the given message if the list is empty.
     *
     * @param promotions the promotions to print
     * @param emptyMessage the message shown when there are no promotions
     */
    private void printPromotions(List<Promotion> promotions, String emptyMessage) {
        if (promotions.isEmpty()) {
            System.out.println(emptyMessage);
            return;
        }
        LocalDate today = LocalDate.now();
        System.out.println("\n--- Promotions ---");
        for (Promotion promotion : promotions) {
            String status = promotion.isActive(today) ? "[ACTIVE] " : "[INACTIVE] ";
            System.out.println(status + promotion);
        }
    }

    // ===================== INPUT HELPERS =====================
    /**
     * Reads a price from the user, returning null if the input is not a valid
     * non-negative number.
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
     * Reads a stock quantity from the user, returning null if the input is not
     * a valid integer.
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

    /**
     * Reads a discount percentage from the user, returning null if the input is
     * not a number between 0 and 100.
     *
     * @return the percentage entered, or null if invalid
     */
    private Double readPercentage() {
        System.out.print("Discount percentage (0-100): ");
        try {
            double percentage = Double.parseDouble(scanner.nextLine().trim());
            if (percentage < 0 || percentage > 100) {
                System.out.println("The percentage must be between 0 and 100.");
                return null;
            }
            return percentage;
        } catch (NumberFormatException e) {
            System.out.println("Invalid percentage.");
            return null;
        }
    }

    /**
     * Reads a date in ISO format (YYYY-MM-DD) from the user, returning null if
     * the input is not a valid date.
     *
     * @param prompt the text shown to the user before reading the date
     * @return the date entered, or null if invalid
     */
    private LocalDate readDate(String prompt) {
        System.out.print(prompt);
        try {
            return LocalDate.parse(scanner.nextLine().trim());
        } catch (DateTimeParseException e) {
            System.out.println("Invalid date. Use the format YYYY-MM-DD.");
            return null;
        }
    }
}
