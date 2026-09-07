package com.gamezone;

import com.gamezone.service.PersonService;
import com.gamezone.service.ProductService;
import com.gamezone.service.SaleService;
import com.gamezone.ui.ConsoleUI;

/**
 * Entry point of the GameZone Unicesar application. Wires together the
 * services of the three modules (products, people, and sales) and starts
 * the console-based user interface, which loads previously stored data
 * automatically when the application starts.
 */
public class Main {

    /**3
     * Starts the GameZone Unicesar application.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        System.out.println("GameZone Unicesar - System starting...");

        ProductService productService = new ProductService();
        PersonService personService = new PersonService();
        SaleService saleService = new SaleService(productService);

        ConsoleUI consoleUI = new ConsoleUI(productService, personService, saleService);
        consoleUI.run();
    }
}