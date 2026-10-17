package com.gamezone.ui;

import com.gamezone.model.Console;
import com.gamezone.model.Product;
import com.gamezone.model.VideoGame;
import com.gamezone.service.ProductService;
import java.util.List;

/**
 * Console submenu of the product module: registers video games and consoles
 * and lists the inventory. Every operation runs through the
 * {@link ErrorHandler}, so its errors are shown as clear messages.
 */
public class ProductMenu {

    private final ProductService productService;
    private final ConsoleInput input;
    private final ErrorHandler errorHandler;

    /**
     * Creates the menu with the services it uses, the reader of the user
     * input, and the handler of the errors of each operation.
     *
     * @param productService the service used for product operations
     * @param input the reader of the user input
     * @param errorHandler the handler that runs each operation and shows its errors
     */
    public ProductMenu(ProductService productService,
                       ConsoleInput input, ErrorHandler errorHandler) {
        this.productService = productService;
        this.input = input;
        this.errorHandler = errorHandler;
    }

    /**
     * Displays the product management submenu and runs the operation chosen
     * by the user until the user goes back to the main menu.
     */
    public void show() {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- Gestión de productos ---");
            System.out.println("1. Registrar un videojuego");
            System.out.println("2. Registrar una consola");
            System.out.println("3. Listar todos los productos");
            System.out.println("0. Volver al menú principal");
            System.out.print("Seleccione una opción: ");
            int option = input.readOption();
            switch (option) {
                case 1 -> errorHandler.executeOperation(this::registerVideoGame);
                case 2 -> errorHandler.executeOperation(this::registerConsole);
                case 3 -> errorHandler.executeOperation(this::listProducts);
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
        String platform = input.readLine().trim();
        System.out.print("Género: ");
        String genre = input.readLine().trim();
        System.out.print("Clasificación por edad: ");
        String ageRating = input.readLine().trim();

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
        String brand = input.readLine().trim();
        System.out.print("Modelo: ");
        String model = input.readLine().trim();
        System.out.print("Generación: ");
        String generation = input.readLine().trim();

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
        String id = input.readLine().trim();
        System.out.print("Título: ");
        String title = input.readLine().trim();
        Double price = input.readPrice();
        Integer stock = input.readStock();
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
}
