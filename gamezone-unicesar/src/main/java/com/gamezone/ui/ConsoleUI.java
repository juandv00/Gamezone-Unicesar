package com.gamezone.ui;

import com.gamezone.service.PersonService;
import com.gamezone.service.ProductService;
import com.gamezone.service.SaleService;
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

    /**
     * Displays the product management submenu. (To be implemented.)
     */
    private void productMenu() {
        System.out.println("Product menu - coming soon.");
    }

    /**
     * Displays the person management submenu. (To be implemented.)
     */
    private void personMenu() {
        System.out.println("Person menu - coming soon.");
    }

    /**
     * Displays the sale management submenu. (To be implemented.)
     */
    private void saleMenu() {
        System.out.println("Sale menu - coming soon.");
    }
}