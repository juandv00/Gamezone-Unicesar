package com.gamezone.ui;

import com.gamezone.exceptions.BusinessRuleException;
import com.gamezone.exceptions.InvalidDataException;
import com.gamezone.exceptions.PersistenceException;
import com.gamezone.exceptions.ResourceNotFoundException;
import com.gamezone.model.Accessory;
import com.gamezone.model.Cable;
import com.gamezone.model.Client;
import com.gamezone.model.Console;
import com.gamezone.model.Controller;
import com.gamezone.model.Memory;
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
import com.gamezone.util.CurrencyFormatter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

/**
 * Provides the console-based user interface for GameZone Unicesar,
 * allowing the user to manage products, people, accessories, sales,
 * promotions, returns, and warranties through a text menu that delegates
 * all operations to the corresponding services. Every operation of the
 * submenus runs through {@link #executeOperation(Runnable)}, which catches
 * the custom exceptions by type and shows a different message to the user
 * for each category of error, so the user never sees a technical message
 * or a stack trace.
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
        this(productService, personService, saleService, accessoryService, null);
    }

    /**
     * Creates a new ConsoleUI that also supports managing promotions, in
     * addition to accessories and sales.
     *
     * @param productService   the service used for product operations
     * @param personService    the service used for people operations
     * @param saleService      the service used for sale operations
     * @param accessoryService the service used for accessory operations, or
     *                         null to disable the accessory module
     * @param promotionService the service used for promotion operations, or
     *                         null to disable the promotion module
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
     * @param productService   the service used for product operations
     * @param personService    the service used for people operations
     * @param saleService      the service used for sale operations
     * @param accessoryService the service used for accessory operations, or
     *                         null to disable the accessory module
     * @param promotionService the service used for promotion operations, or
     *                         null to disable the promotion module
     * @param returnService    the service used for return operations, or
     *                         null to disable the return module
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
     * @param productService   the service used for product operations
     * @param personService    the service used for people operations
     * @param saleService      the service used for sale operations
     * @param accessoryService the service used for accessory operations, or
     *                         null to disable the accessory module
     * @param promotionService the service used for promotion operations, or
     *                         null to disable the promotion module
     * @param returnService    the service used for return operations, or
     *                         null to disable the return module
     * @param warrantyService  the service used for warranty operations, or
     *                         null to disable the warranty module
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
                case 5 -> promotionMenu();
                case 6 -> returnMenu();
                case 7 -> warrantyMenu();
                case 0 -> exit = true;
                default -> System.out.println("Opción inválida. Intente de nuevo.");
            }
        }
        System.out.println("¡Gracias por usar GameZone Unicesar!");
        scanner.close();
    }

    /**
     * Displays the main menu options.
     */
    private void showMainMenu() {
        System.out.println("\n===== GameZone Unicesar =====");
        System.out.println("1. Gestión de productos");
        System.out.println("2. Gestión de personas");
        System.out.println("3. Gestión de ventas");
        System.out.println("4. Gestión de accesorios");
        System.out.println("5. Gestión de promociones");
        System.out.println("6. Gestión de devoluciones");
        System.out.println("7. Gestión de garantías");
        System.out.println("0. Salir");
        System.out.print("Seleccione una opción: ");
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

    /**
     * Runs one operation of a submenu and handles its errors. The custom
     * exceptions are caught by their specific type, from the most specific
     * categories to the most general, and each category shows a different
     * message: a resource that was not found, a business rule that was
     * violated, invalid input data, or a problem accessing the data files
     * (whose technical details are never shown). The modules that still use
     * the standard Java exceptions are handled as business rule errors, and
     * any unexpected error shows a general message instead of a stack trace.
     *
     * @param operation the operation to run
     */
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

    // ===================== PRODUCT MENU =====================

    /**
     * Displays the product management submenu, allowing the user to register
     * video games, register consoles, and list all available products.
     */
    private void productMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- Gestión de productos ---");
            System.out.println("1. Registrar un videojuego");
            System.out.println("2. Registrar una consola");
            System.out.println("3. Listar todos los productos");
            System.out.println("0. Volver al menú principal");
            System.out.print("Seleccione una opción: ");
            int option = readOption();
            switch (option) {
                case 1 -> executeOperation(this::registerVideoGame);
                case 2 -> executeOperation(this::registerConsole);
                case 3 -> executeOperation(this::listProducts);
                case 0 -> back = true;
                default -> System.out.println("Opción inválida. Intente de nuevo.");
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
        System.out.print("Plataforma: ");
        String platform = scanner.nextLine().trim();
        System.out.print("Género: ");
        String genre = scanner.nextLine().trim();
        System.out.print("Clasificación por edad: ");
        String ageRating = scanner.nextLine().trim();

        VideoGame videoGame = new VideoGame(data.id(), data.title(), data.price(), data.stock(),
                platform, genre, ageRating);
        productService.registerProduct(videoGame);
        System.out.println("Videojuego registrado correctamente.");
    }

    /**
     * Prompts the user for the data of a new console and registers it.
     */
    private void registerConsole() {
        CommonProductData data = readCommonProductData();
        if (data == null) {
            return;
        }
        System.out.print("Marca: ");
        String brand = scanner.nextLine().trim();
        System.out.print("Modelo: ");
        String model = scanner.nextLine().trim();
        System.out.print("Generación: ");
        String generation = scanner.nextLine().trim();

        Console console = new Console(data.id(), data.title(), data.price(), data.stock(),
                brand, model, generation);
        productService.registerProduct(console);
        System.out.println("Consola registrada correctamente.");
    }

    /**
     * Reads the attributes shared by every product type (id, title, price,
     * and stock), used when registering either a video game or a console.
     *
     * @return the common product data, or null if price or stock was invalid
     */
    private CommonProductData readCommonProductData() {
        System.out.print("Identificador del producto: ");
        String id = scanner.nextLine().trim();
        System.out.print("Título: ");
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
            System.out.println("Aún no hay productos registrados.");
            return;
        }
        System.out.println("\n--- Inventario de productos ---");
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
            System.out.println("\n--- Gestión de personas ---");
            System.out.println("1. Registrar un cliente");
            System.out.println("2. Listar todos los clientes");
            System.out.println("3. Listar todos los vendedores");
            System.out.println("0. Volver al menú principal");
            System.out.print("Seleccione una opción: ");
            int option = readOption();
            switch (option) {
                case 1 -> executeOperation(this::registerClient);
                case 2 -> executeOperation(this::listClients);
                case 3 -> executeOperation(this::listSellers);
                case 0 -> back = true;
                default -> System.out.println("Opción inválida. Intente de nuevo.");
            }
        }
    }

    /**
     * Prompts the user for the data of a new client and registers it.
     */
    private void registerClient() {
        System.out.print("Identificación del cliente: ");
        String id = scanner.nextLine().trim();
        System.out.print("Nombre: ");
        String name = scanner.nextLine().trim();
        System.out.print("Teléfono: ");
        String phone = scanner.nextLine().trim();
        System.out.print("Correo electrónico: ");
        String email = scanner.nextLine().trim();

        personService.registerClient(id, name, phone, email);
        System.out.println("Cliente registrado correctamente.");
    }

    /**
     * Lists every client currently registered.
     */
    private void listClients() {
        List<Client> clients = personService.listClient();
        if (clients.isEmpty()) {
            System.out.println("Aún no hay clientes registrados.");
            return;
        }
        System.out.println("\n--- Clientes registrados ---");
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
            System.out.println("Aún no hay vendedores registrados.");
            return;
        }
        System.out.println("\n--- Vendedores registrados ---");
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
            System.out.println("\n--- Gestión de ventas ---");
            System.out.println("1. Registrar una venta");
            System.out.println("2. Ver el historial completo de ventas");
            System.out.println("3. Ver el historial de compras de un cliente");
            System.out.println("4. Ver el historial de ventas de un vendedor");
            System.out.println("5. Ver el recibo de una venta");
            System.out.println("0. Volver al menú principal");
            System.out.print("Seleccione una opción: ");
            int option = readOption();
            switch (option) {
                case 1 -> executeOperation(this::registerSale);
                case 2 -> executeOperation(this::listAllSales);
                case 3 -> executeOperation(this::listSalesByClient);
                case 4 -> executeOperation(this::listSalesBySeller);
                case 5 -> executeOperation(this::viewSaleReceipt);
                case 0 -> back = true;
                default -> System.out.println("Opción inválida. Intente de nuevo.");
            }
        }
    }

    /**
     * Guides the user through registering a new sale: selecting the client,
     * the seller, and the items sold (products and accessories are entered
     * in the same list, by id). When the warranty module is available, the
     * user is asked whether each console of the sale should receive an
     * extended warranty. The sale follows the unified registration flow of
     * SaleService, and the receipt and warranty certificates are printed.
     */
    private void registerSale() {
        System.out.print("Identificación del cliente: ");
        Client client = personService.getClientById(scanner.nextLine().trim());

        System.out.print("Identificación del vendedor: ");
        Seller seller = personService.getSellerById(scanner.nextLine().trim());

        List<String> itemIds = new ArrayList<>();
        List<Product> products = new ArrayList<>();
        boolean addingItems = true;
        while (addingItems) {
            System.out.print("Identificador del producto o accesorio a agregar (o 0 para terminar): ");
            String itemId = scanner.nextLine().trim();
            if (itemId.equals("0")) {
                addingItems = false;
                continue;
            }
            Product product = productService.findById(itemId);
            if (product != null) {
                itemIds.add(product.getId());
                products.add(product);
                System.out.println(product.getTitle() + " agregado.");
                continue;
            }
            Accessory accessory = accessoryService != null ? accessoryService.findById(itemId) : null;
            if (accessory != null) {
                itemIds.add(accessory.getId());
                System.out.println(accessory.getTitle() + " (accesorio) agregado.");
                continue;
            }
            System.out.println("No se encontró un producto o accesorio con ese identificador.");
        }

        List<String> extendedIds = warrantyService != null ? askExtendedWarranties(products) : new ArrayList<>();
        Sale sale = saleService.registerSaleByItemIds(client, seller, itemIds, extendedIds);
        System.out.println("Venta registrada correctamente.");
        System.out.println(sale.generateReceipt());
        if (warrantyService != null) {
            printSaleWarranties(sale);
        }
    }

    /**
     * Asks the user, for each different console included in the sale,
     * whether it should receive an extended warranty instead of the basic
     * one, showing its cost (10% of the console price, per unit).
     *
     * @param products the products of the sale
     * @return the ids of the consoles that must receive an extended warranty
     */
    private List<String> askExtendedWarranties(List<Product> products) {
        List<String> extendedIds = new ArrayList<>();
        List<String> askedIds = new ArrayList<>();
        for (Product product : products) {
            if (!product.isWarrantyEligible() || askedIds.contains(product.getId())) {
                continue;
            }
            askedIds.add(product.getId());
            System.out.print("¿Agregar garantía extendida (12 meses, +"
                    + CurrencyFormatter.format(product.getPrice() * 0.10) + " por unidad) a " + product.getTitle() + "? (s/n): ");
            String answer = scanner.nextLine().trim();
            if (answer.equalsIgnoreCase("s") || answer.equalsIgnoreCase("si")
                    || answer.equalsIgnoreCase("sí")) {
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
     * Lists every registered sale with a number and shows the full receipt
     * of the sale chosen by the user, including the discount applied.
     */
    private void viewSaleReceipt() {
        List<Sale> sales = saleService.listAll();
        if (sales.isEmpty()) {
            System.out.println("No se encontraron ventas.");
            return;
        }
        System.out.println("\n--- Ventas ---");
        for (int i = 0; i < sales.size(); i++) {
            System.out.println((i + 1) + ". " + sales.get(i));
        }
        System.out.print("Número de la venta a consultar: ");
        int number = readOption();
        if (number < 1 || number > sales.size()) {
            System.out.println("Número de venta inválido.");
            return;
        }
        System.out.println(sales.get(number - 1).generateReceipt());
    }

    /**
     * Displays the purchase history of a specific client.
     */
    private void listSalesByClient() {
        System.out.print("Identificación del cliente: ");
        String clientId = scanner.nextLine().trim();
        List<Sale> sales = saleService.findByClient(clientId);
        printSales(sales);
    }

    /**
     * Displays the sales history attended by a specific seller.
     */
    private void listSalesBySeller() {
        System.out.print("Identificación del vendedor: ");
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
            System.out.println("No se encontraron ventas.");
            return;
        }
        System.out.println("\n--- Ventas ---");
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
                System.out.println("  Accesorios: " + titles);
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
            System.out.println("El módulo de accesorios no está disponible.");
            return;
        }
        boolean back = false;
        while (!back) {
            System.out.println("\n--- Gestión de accesorios ---");
            System.out.println("1. Registrar un control");
            System.out.println("2. Registrar un cable");
            System.out.println("3. Registrar una memoria");
            System.out.println("4. Listar todos los accesorios");
            System.out.println("5. Listar accesorios por tipo");
            System.out.println("6. Buscar accesorios compatibles con una consola");
            System.out.println("7. Agregar una consola compatible a un control o memoria");
            System.out.println("8. Actualizar el stock de un accesorio");
            System.out.println("0. Volver al menú principal");
            System.out.print("Seleccione una opción: ");
            int option = readOption();
            switch (option) {
                case 1 -> executeOperation(this::registerController);
                case 2 -> executeOperation(this::registerCable);
                case 3 -> executeOperation(this::registerMemory);
                case 4 -> executeOperation(this::listAccessories);
                case 5 -> executeOperation(this::listAccessoriesByType);
                case 6 -> executeOperation(this::listAccessoriesCompatibleWithConsole);
                case 7 -> executeOperation(this::addCompatibleConsole);
                case 8 -> executeOperation(this::updateAccessoryStock);
                case 0 -> back = true;
                default -> System.out.println("Opción inválida. Intente de nuevo.");
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
        System.out.print("Tipo de conexión (1 = inalámbrico, 2 = cableado): ");
        Controller.ConnectionType connectionType = switch (readOption()) {
            case 1 -> Controller.ConnectionType.WIRELESS;
            case 2 -> Controller.ConnectionType.WIRED;
            default -> null;
        };
        if (connectionType == null) {
            System.out.println("Tipo de conexión inválido.");
            return;
        }

        try {
            Controller controller = accessoryService.registerController(
                    data.id(), data.title(), data.price(), data.stock(), connectionType);
            System.out.println("Control registrado correctamente.");
            readCompatibleConsoles(controller.getId());
        } catch (IOException e) {
            throw accessoryFileError(e);
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
        System.out.print("Longitud en metros: ");
        double length;
        try {
            length = Double.parseDouble(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Longitud inválida.");
            return;
        }
        System.out.print("Tipo de conector (p. ej. HDMI, USB): ");
        String connectorType = scanner.nextLine().trim();

        try {
            accessoryService.registerCable(data.id(), data.title(), data.price(), data.stock(),
                    length, connectorType);
            System.out.println("Cable registrado correctamente.");
        } catch (IOException e) {
            throw accessoryFileError(e);
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
        System.out.print("Capacidad en GB: ");
        int capacity;
        try {
            capacity = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Capacidad inválida.");
            return;
        }
        System.out.print("Tipo de memoria (1 = SD, 2 = MICRO_SD, 3 = INTERNAL_CARD): ");
        Memory.MemoryType memoryType = switch (readOption()) {
            case 1 -> Memory.MemoryType.SD;
            case 2 -> Memory.MemoryType.MICRO_SD;
            case 3 -> Memory.MemoryType.INTERNAL_CARD;
            default -> null;
        };
        if (memoryType == null) {
            System.out.println("Tipo de memoria inválido.");
            return;
        }

        try {
            Memory memory = accessoryService.registerMemory(
                    data.id(), data.title(), data.price(), data.stock(), capacity, memoryType);
            System.out.println("Memoria registrada correctamente.");
            readCompatibleConsoles(memory.getId());
        } catch (IOException e) {
            throw accessoryFileError(e);
        }
    }

    /**
     * Reads the attributes shared by every accessory type (id, title, price,
     * and stock), rejecting empty ids and ids already in use.
     *
     * @return the common accessory data, or null if any value was invalid
     */
    private CommonAccessoryData readCommonAccessoryData() {
        System.out.print("Identificador del accesorio: ");
        String id = scanner.nextLine().trim();
        if (id.isEmpty()) {
            System.out.println("El identificador del accesorio no puede estar vacío.");
            return null;
        }
        if (accessoryService.findById(id) != null) {
            System.out.println("Ya existe un accesorio con ese identificador.");
            return null;
        }
        System.out.print("Título: ");
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
            System.out.print("Identificador de la consola compatible (o 0 para terminar): ");
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
            System.out.println("No se encontró una consola con ese identificador.");
            return;
        }
        try {
            if (accessoryService.addCompatibleConsole(accessoryId, consoleId)) {
                System.out.println(console.getTitle() + " registrada como compatible.");
            } else {
                System.out.println("Solo los controles y las memorias admiten compatibilidad con consolas.");
            }
        } catch (IOException e) {
            throw accessoryFileError(e);
        }
    }

    /**
     * Lets the user add a compatible console to an existing controller or
     * memory.
     */
    private void addCompatibleConsole() {
        System.out.print("Identificador del accesorio: ");
        String accessoryId = scanner.nextLine().trim();
        Accessory accessory = accessoryService.findById(accessoryId);
        if (accessory == null) {
            System.out.println("No se encontró un accesorio con ese identificador.");
            return;
        }
        if (!(accessory instanceof Controller) && !(accessory instanceof Memory)) {
            System.out.println("Solo los controles y las memorias admiten compatibilidad con consolas.");
            return;
        }
        System.out.print("Identificador de la consola: ");
        String consoleId = scanner.nextLine().trim();
        registerCompatibility(accessoryId, consoleId);
    }

    /**
     * Lists every accessory currently available in the inventory.
     */
    private void listAccessories() {
        printAccessories(accessoryService.listAllAccessories(), "Aún no hay accesorios registrados.");
    }

    /**
     * Lists the accessories of the type chosen by the user.
     */
    private void listAccessoriesByType() {
        System.out.print("Tipo (Control, Cable o Memoria): ");
        String type = toAccessoryType(scanner.nextLine().trim());
        printAccessories(accessoryService.listAccessoriesByType(type),
                "No se encontraron accesorios de ese tipo.");
    }

    /**
     * Lists the accessories registered as compatible with the console chosen
     * by the user.
     */
    private void listAccessoriesCompatibleWithConsole() {
        System.out.print("Identificador de la consola: ");
        String consoleId = scanner.nextLine().trim();
        printAccessories(accessoryService.findAccessoriesCompatibleWith(consoleId),
                "No hay accesorios registrados como compatibles con esa consola.");
    }

    /**
     * Updates the stock of an accessory to a new non-negative quantity.
     */
    private void updateAccessoryStock() {
        System.out.print("Identificador del accesorio: ");
        String accessoryId = scanner.nextLine().trim();
        if (accessoryService.findById(accessoryId) == null) {
            System.out.println("No se encontró un accesorio con ese identificador.");
            return;
        }
        Integer stock = readStock();
        if (stock == null) {
            return;
        }
        if (stock < 0) {
            System.out.println("El stock no puede ser negativo.");
            return;
        }
        try {
            accessoryService.updateStock(accessoryId, stock);
            System.out.println("Stock actualizado correctamente.");
        } catch (IOException e) {
            throw accessoryFileError(e);
        }
    }

    /**
     * Converts an error of the accessory file, which AccessoryService reports
     * as a checked IOException, into a PersistenceException, so it is handled
     * like any other persistence error by {@link #executeOperation(Runnable)}.
     *
     * @param cause the original error
     * @return the exception to throw
     */
    private PersistenceException accessoryFileError(IOException cause) {
        return new PersistenceException("Error de escritura en el archivo de accesorios.", cause);
    }

    /**
     * Converts the accessory type written by the user in Spanish (or in
     * English) into the type name used by the accessory module.
     *
     * @param input the type written by the user
     * @return "Controller", "Cable", or "Memory", or the input unchanged if it
     *         does not match any type
     */
    private String toAccessoryType(String input) {
        return switch (input.toLowerCase(Locale.ROOT)) {
            case "control", "controller" -> "Controller";
            case "cable" -> "Cable";
            case "memoria", "memory" -> "Memory";
            default -> input;
        };
    }

    /**
     * Returns the Spanish name of an accessory type, shown in the listings.
     *
     * @param type the type name used by the accessory module
     * @return the name shown to the user
     */
    private String accessoryTypeLabel(String type) {
        return switch (type) {
            case "Controller" -> "Control";
            case "Memory" -> "Memoria";
            default -> type;
        };
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
        System.out.println("\n--- Accesorios ---");
        for (Accessory accessory : accessories) {
            System.out.println("[" + accessoryTypeLabel(accessory.getType()) + "] " + accessory.getId() + " - "
                    + accessory.getDescription());
        }
    }

    // ===================== WARRANTY MENU =====================

    /**
     * Displays the warranty management submenu, allowing the user to consult
     * the warranty of a product in a specific sale, and to list all
     * warranties, the ones active today, or the ones expiring soon.
     */
    private void warrantyMenu() {
        if (warrantyService == null) {
            System.out.println("El módulo de garantías no está disponible.");
            return;
        }
        boolean back = false;
        while (!back) {
            System.out.println("\n--- Gestión de garantías ---");
            System.out.println("1. Ver la garantía de un producto en una venta");
            System.out.println("2. Listar todas las garantías");
            System.out.println("3. Listar las garantías vigentes (hoy)");
            System.out.println("4. Listar las garantías próximas a vencer");
            System.out.println("0. Volver al menú principal");
            System.out.print("Seleccione una opción: ");
            int option = readOption();
            switch (option) {
                case 1 -> executeOperation(this::viewWarrantyOfProduct);
                case 2 -> executeOperation(() -> printWarranties(warrantyService.listAllWarranties(),
                        "Aún no hay garantías registradas."));
                case 3 -> executeOperation(() -> printWarranties(warrantyService.listActiveWarranties(),
                        "No hay garantías vigentes hoy."));
                case 4 -> executeOperation(this::listWarrantiesExpiringSoon);
                case 0 -> back = true;
                default -> System.out.println("Opción inválida. Intente de nuevo.");
            }
        }
    }

    /**
     * Asks the user for a sale and a product of that sale, and shows the
     * certificate of its warranty and whether it is active today.
     */
    private void viewWarrantyOfProduct() {
        System.out.print("Identificador de la venta (p. ej. SALE-1): ");
        Sale sale = saleService.getSaleById(scanner.nextLine().trim());
        System.out.print("Identificador del producto: ");
        String productId = scanner.nextLine().trim();
        Product product = null;
        for (Product candidate : sale.getProducts()) {
            if (candidate.getId().equalsIgnoreCase(productId)) {
                product = candidate;
                break;
            }
        }
        if (product == null) {
            System.out.println("Ese producto no forma parte de la venta " + sale.getId() + ".");
            return;
        }
        Warranty warranty = warrantyService.findWarrantyByProduct(product.getId(), sale.getId());
        if (warranty == null) {
            System.out.println(product.getTitle() + " no tiene garantía en la venta " + sale.getId()
                    + " (solo las consolas reciben garantía).");
            return;
        }
        System.out.println(warranty.generateWarrantyCertificate());
        System.out.println(warranty.isActive(LocalDate.now())
                ? "Estado: VIGENTE (cubierta hasta el " + warranty.getEndDate() + ")"
                : "Estado: VENCIDA el " + warranty.getEndDate());
    }

    /**
     * Asks the user how many days ahead to look and lists the active
     * warranties that expire within that period.
     */
    private void listWarrantiesExpiringSoon() {
        System.out.print("Días hacia adelante (p. ej. 30): ");
        int daysAhead = readOption();
        if (daysAhead < 0) {
            System.out.println("El número de días debe ser cero o mayor.");
            return;
        }
        printWarranties(warrantyService.listWarrantiesExpiringSoon(daysAhead),
                "Ninguna garantía vence en los próximos " + daysAhead + " días.");
    }

    /**
     * Prints a one-line summary of each warranty in the given list, marking it
     * as active or expired today, or the given message if the list is empty.
     *
     * @param warranties   the warranties to print
     * @param emptyMessage the message shown when there are no warranties
     */
    private void printWarranties(List<Warranty> warranties, String emptyMessage) {
        if (warranties.isEmpty()) {
            System.out.println(emptyMessage);
            return;
        }
        LocalDate today = LocalDate.now();
        System.out.println("\n--- Garantías ---");
        for (Warranty warranty : warranties) {
            String status = warranty.isActive(today) ? "[VIGENTE] " : "[VENCIDA] ";
            System.out.println(status + warranty.getId() + " | " + warranty.getWarrantyType()
                    + " | " + warranty.getProduct().getTitle()
                    + " | venta " + warranty.getSale().getId()
                    + " | " + warranty.getStartDate() + " a " + warranty.getEndDate()
                    + " | costo " + CurrencyFormatter.format(warranty.getAdditionalCost()));
        }
    }

    // ===================== RETURN MENU =====================

    /**
     * Displays the return management submenu, allowing the user to register
     * returns, consult them (all, by client, or by sale), and view the
     * monthly balance report.
     */
    private void returnMenu() {
        if (returnService == null) {
            System.out.println("El módulo de devoluciones no está disponible.");
            return;
        }
        boolean back = false;
        while (!back) {
            System.out.println("\n--- Gestión de devoluciones ---");
            System.out.println("1. Registrar una devolución");
            System.out.println("2. Listar todas las devoluciones");
            System.out.println("3. Listar devoluciones por cliente");
            System.out.println("4. Listar devoluciones por venta");
            System.out.println("5. Ver el balance mensual");
            System.out.println("0. Volver al menú principal");
            System.out.print("Seleccione una opción: ");
            int option = readOption();
            switch (option) {
                case 1 -> executeOperation(this::registerReturn);
                case 2 -> executeOperation(() -> printReturns(returnService.viewAllReturns(),
                        "Aún no hay devoluciones registradas."));
                case 3 -> executeOperation(this::listReturnsByClient);
                case 4 -> executeOperation(this::listReturnsBySale);
                case 5 -> executeOperation(this::showMonthlyBalance);
                case 0 -> back = true;
                default -> System.out.println("Opción inválida. Intente de nuevo.");
            }
        }
    }

    /**
     * Guides the user through registering a return: choosing the original
     * sale, selecting which of its products and accessories are returned, and
     * giving a reason. The business rules (30-day window, items belonging to
     * the sale, units not returned twice) are validated by the ReturnService;
     * the sale window is also checked here to warn the user early.
     */
    private void registerReturn() {
        System.out.print("Identificador de la venta (p. ej. SALE-1): ");
        String saleId = scanner.nextLine().trim();
        Sale sale = saleService.getSaleById(saleId);
        if (!sale.canBeReturned()) {
            System.out.println("Esta venta ya no admite devoluciones: pasaron más de 30 días desde el "
                    + sale.getDate() + ".");
            return;
        }

        System.out.println("\nÍtems de la venta " + sale.getId() + ":");
        for (Product product : sale.getProducts()) {
            System.out.println("  " + product.getId() + " - " + product.getTitle()
                    + " (" + CurrencyFormatter.format(product.getPrice()) + ")");
        }
        for (Accessory accessory : sale.getAccessories()) {
            System.out.println("  " + accessory.getId() + " - " + accessory.getTitle()
                    + " (accesorio, " + CurrencyFormatter.format(accessory.getPrice()) + ")");
        }

        List<String> itemIds = new ArrayList<>();
        boolean addingItems = true;
        while (addingItems) {
            System.out.print("Identificador del ítem a devolver (o 0 para terminar): ");
            String itemId = scanner.nextLine().trim();
            if (itemId.equals("0")) {
                addingItems = false;
            } else if (!itemId.isEmpty()) {
                itemIds.add(itemId);
            }
        }
        if (itemIds.isEmpty()) {
            System.out.println("No se seleccionaron ítems. La devolución se canceló.");
            return;
        }

        System.out.print("Motivo de la devolución: ");
        String reason = scanner.nextLine().trim();

        Return registeredReturn = returnService.registerReturn(sale.getId(), itemIds, reason);
        System.out.println("Devolución registrada correctamente.");
        System.out.println(registeredReturn.generateReturnReceipt());
    }

    /**
     * Lists the returns of the sales made by a client chosen by the user.
     */
    private void listReturnsByClient() {
        System.out.print("Identificación del cliente: ");
        String clientId = scanner.nextLine().trim();
        printReturns(returnService.viewReturnsByCustomer(clientId),
                "No se encontraron devoluciones para ese cliente.");
    }

    /**
     * Lists the returns associated with a sale chosen by the user.
     */
    private void listReturnsBySale() {
        System.out.print("Identificador de la venta: ");
        String saleId = scanner.nextLine().trim();
        printReturns(returnService.viewReturnsBySale(saleId), "No se encontraron devoluciones para esa venta.");
    }

    /**
     * Asks the user for a month and year and shows the monthly balance:
     * total sales, total refunded by returns, and the net balance.
     */
    private void showMonthlyBalance() {
        System.out.print("Mes (1-12): ");
        int month = readOption();
        System.out.print("Año (p. ej. 2026): ");
        int year = readOption();
        if (year <= 0) {
            System.out.println("Año inválido.");
            return;
        }
        double sales = returnService.calculateMonthlySales(month, year);
        double refunds = returnService.calculateMonthlyReturns(month, year);
        double balance = returnService.generateMonthlyBalance(month, year);
        System.out.println("\n===== Balance mensual " + String.format("%02d/%d", month, year) + " =====");
        System.out.println("Total de ventas:       " + CurrencyFormatter.format(sales));
        System.out.println("Total de devoluciones: " + CurrencyFormatter.format(refunds));
        System.out.println("Balance neto:          " + CurrencyFormatter.format(balance));
    }

    /**
     * Prints a one-line summary of each return in the given list, or the
     * given message if the list is empty.
     *
     * @param returns      the returns to print
     * @param emptyMessage the message shown when there are no returns
     */
    private void printReturns(List<Return> returns, String emptyMessage) {
        if (returns.isEmpty()) {
            System.out.println(emptyMessage);
            return;
        }
        System.out.println("\n--- Devoluciones ---");
        for (Return r : returns) {
            Sale sale = r.getOriginalSale();
            System.out.println(r.getId() + " | " + r.getDate()
                    + " | venta " + sale.getId()
                    + " | cliente " + sale.getClient().getName()
                    + " | " + (r.getReturnedProducts().size() + r.getReturnedAccessories().size()) + " ítem(s)"
                    + " | reembolso " + CurrencyFormatter.format(r.getRefundAmount())
                    + " | motivo: " + r.getReason());
        }
    }

    // ===================== PROMOTION MENU =====================

    /**
     * Displays the promotion management submenu, allowing the user to
     * register percentage, category and bulk purchase promotions, and to
     * list all promotions or only the ones active today.
     */
    private void promotionMenu() {
        if (promotionService == null) {
            System.out.println("El módulo de promociones no está disponible.");
            return;
        }
        boolean back = false;
        while (!back) {
            System.out.println("\n--- Gestión de promociones ---");
            System.out.println("1. Registrar una promoción por porcentaje");
            System.out.println("2. Registrar una promoción por categoría");
            System.out.println("3. Registrar una promoción por compra en volumen");
            System.out.println("4. Listar todas las promociones");
            System.out.println("5. Listar las promociones vigentes (hoy)");
            System.out.println("0. Volver al menú principal");
            System.out.print("Seleccione una opción: ");
            int option = readOption();
            switch (option) {
                case 1 -> executeOperation(this::registerPercentagePromotion);
                case 2 -> executeOperation(this::registerCategoryPromotion);
                case 3 -> executeOperation(this::registerBulkPurchasePromotion);
                case 4 -> executeOperation(() -> printPromotions(promotionService.listAllPromotions(),
                        "Aún no hay promociones registradas."));
                case 5 -> executeOperation(() -> printPromotions(promotionService.listActivePromotions(),
                        "No hay promociones vigentes hoy."));
                case 0 -> back = true;
                default -> System.out.println("Opción inválida. Intente de nuevo.");
            }
        }
    }

    /**
     * Prompts the user for the data of a new percentage promotion and
     * registers it.
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
        promotionService.registerPercentageDiscount(data.id(), data.name(),
                data.startDate(), data.endDate(), percentage);
        System.out.println("Promoción por porcentaje registrada correctamente.");
    }

    /**
     * Prompts the user for the data of a new category promotion and
     * registers it.
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
        System.out.print("Categoría objetivo (1 = videojuegos, 2 = consolas, 3 = accesorios): ");
        String targetCategory = switch (readOption()) {
            case 1 -> "VIDEOGAME";
            case 2 -> "CONSOLE";
            case 3 -> "ACCESSORY";
            default -> null;
        };
        if (targetCategory == null) {
            System.out.println("Categoría inválida.");
            return;
        }
        promotionService.registerCategoryDiscount(data.id(), data.name(),
                data.startDate(), data.endDate(), percentage, targetCategory);
        System.out.println("Promoción por categoría registrada correctamente.");
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
        System.out.print("Cantidad mínima de productos: ");
        int minimumQuantity = readOption();
        if (minimumQuantity <= 0) {
            System.out.println("La cantidad mínima de productos debe ser mayor que cero.");
            return;
        }
        Double percentage = readPercentage();
        if (percentage == null) {
            return;
        }
        promotionService.registerBulkPurchaseDiscount(data.id(), data.name(),
                data.startDate(), data.endDate(), minimumQuantity, percentage);
        System.out.println("Promoción por compra en volumen registrada correctamente.");
    }

    /**
     * Reads the attributes shared by every promotion type (id, name, start
     * date and end date), rejecting empty or repeated ids and date ranges
     * where the start date is after the end date.
     *
     * @return the common promotion data, or null if any value was invalid
     */
    private CommonPromotionData readCommonPromotionData() {
        System.out.print("Identificador de la promoción: ");
        String id = scanner.nextLine().trim();
        if (id.isEmpty()) {
            System.out.println("El identificador de la promoción no puede estar vacío.");
            return null;
        }
        if (promotionService.findById(id) != null) {
            System.out.println("Ya existe una promoción con ese identificador.");
            return null;
        }
        System.out.print("Nombre: ");
        String name = scanner.nextLine().trim();
        LocalDate startDate = readDate("Fecha de inicio (AAAA-MM-DD): ");
        if (startDate == null) {
            return null;
        }
        LocalDate endDate = readDate("Fecha de fin (AAAA-MM-DD): ");
        if (endDate == null) {
            return null;
        }
        if (startDate.isAfter(endDate)) {
            System.out.println("La fecha de inicio no puede ser posterior a la fecha de fin.");
            return null;
        }
        return new CommonPromotionData(id, name, startDate, endDate);
    }

    /**
     * Simple holder for the attributes shared by every promotion type,
     * used only while collecting input in the console UI.
     */
    private record CommonPromotionData(String id, String name, LocalDate startDate, LocalDate endDate) {
    }

    /**
     * Prints the given list of promotions, marking each one as active or
     * inactive today, or the given message if the list is empty.
     *
     * @param promotions   the promotions to print
     * @param emptyMessage the message shown when there are no promotions
     */
    private void printPromotions(List<Promotion> promotions, String emptyMessage) {
        if (promotions.isEmpty()) {
            System.out.println(emptyMessage);
            return;
        }
        LocalDate today = LocalDate.now();
        System.out.println("\n--- Promociones ---");
        for (Promotion promotion : promotions) {
            String status = promotion.isActive(today) ? "[VIGENTE] " : "[NO VIGENTE] ";
            System.out.println(status + promotion);
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
        System.out.print("Precio: ");
        try {
            return Double.parseDouble(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Precio inválido.");
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
            System.out.println("Stock inválido.");
            return null;
        }
    }

    /**
     * Reads a discount percentage from the user, returning null if the input
     * is not a number between 0 and 100.
     *
     * @return the percentage entered, or null if invalid
     */
    private Double readPercentage() {
        System.out.print("Porcentaje de descuento (0-100): ");
        try {
            double percentage = Double.parseDouble(scanner.nextLine().trim());
            if (percentage < 0 || percentage > 100) {
                System.out.println("El porcentaje debe estar entre 0 y 100.");
                return null;
            }
            return percentage;
        } catch (NumberFormatException e) {
            System.out.println("Porcentaje inválido.");
            return null;
        }
    }

    /**
     * Reads a date in ISO format (YYYY-MM-DD) from the user, returning null
     * if the input is not a valid date.
     *
     * @param prompt the text shown to the user before reading the date
     * @return the date entered, or null if invalid
     */
    private LocalDate readDate(String prompt) {
        System.out.print(prompt);
        try {
            return LocalDate.parse(scanner.nextLine().trim());
        } catch (DateTimeParseException e) {
            System.out.println("Fecha inválida. Use el formato AAAA-MM-DD.");
            return null;
        }
    }
}