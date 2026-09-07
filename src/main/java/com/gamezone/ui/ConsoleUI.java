package com.gamezone.ui;

import com.gamezone.model.Client;
import com.gamezone.model.Console;
import com.gamezone.model.Person;
import com.gamezone.model.Product;
import com.gamezone.model.Sale;
import com.gamezone.model.Seller;
import com.gamezone.model.VideoGame;
import com.gamezone.service.PersonService;
import com.gamezone.service.ProductService;
import com.gamezone.service.SaleService;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Provides the console-based user interface for GameZone Unicesar,
 * allowing the user to manage products, people, and sales through a
 * text menu that delegates all operations to the corresponding services.
 */
public class ConsoleUI {

    private final ProductService productService;
    private final PersonService personService;
    private final SaleService saleService;
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
        this.productService = productService;
        this.personService = personService;
        this.saleService = saleService;
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
                case 0 -> exit = true;
                default -> System.out.println("Invalid option. Please try again.");
            }
        }
        System.out.println("Thank you for using GameZone Unicesar!");
    }

    /**
     * Displays the main menu options.
     */
    private void showMainMenu() {
        System.out.println("\n===== GameZone Unicesar =====");
        System.out.println("1. Product management");
        System.out.println("2. Person management");
        System.out.println("3. Sale management");
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
        System.out.print("Product id: ");
        String id = scanner.nextLine().trim();
        System.out.print("Title: ");
        String title = scanner.nextLine().trim();
        Double price = readPrice();
        Integer stock = readStock();
        if (price == null || stock == null) {
            return;
        }
        System.out.print("Platform: ");
        String platform = scanner.nextLine().trim();
        System.out.print("Genre: ");
        String genre = scanner.nextLine().trim();
        System.out.print("Age rating: ");
        String ageRating = scanner.nextLine().trim();

        try {
            VideoGame videoGame = new VideoGame(id, title, price, stock, platform, genre, ageRating);
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
        System.out.print("Product id: ");
        String id = scanner.nextLine().trim();
        System.out.print("Title: ");
        String title = scanner.nextLine().trim();
        Double price = readPrice();
        Integer stock = readStock();
        if (price == null || stock == null) {
            return;
        }
        System.out.print("Brand: ");
        String brand = scanner.nextLine().trim();
        System.out.print("Model: ");
        String model = scanner.nextLine().trim();
        System.out.print("Generation: ");
        String generation = scanner.nextLine().trim();

        try {
            Console console = new Console(id, title, price, stock, brand, model, generation);
            productService.registerProduct(console);
            System.out.println("Console registered successfully.");
        } catch (IllegalArgumentException e) {
            System.out.println("Could not register console: " + e.getMessage());
        }
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
     * the seller, and one or more products.
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

        Sale sale = saleService.registerSale(client, seller, products);
        if (sale == null) {
            System.out.println("Sale could not be registered (no products, or insufficient stock).");
        } else {
            System.out.println("Sale registered successfully. Total: $" + sale.calculateTotal());
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