package com.gamezone;

import com.gamezone.persistence.AccessoryRepository;
import com.gamezone.persistence.SalePersistence;
import com.gamezone.service.AccessoryService;
import com.gamezone.service.PersonService;
import com.gamezone.service.ProductService;
import com.gamezone.service.SaleService;
import com.gamezone.ui.ConsoleUI;

/**
 * Entry point of the GameZone Unicesar application. Wires together the
 * services of the modules (products, people, accessories, and sales) and
 * starts the console-based user interface, which loads previously stored
 * data automatically when the application starts.
 */
public class Main {

    /**
     * Starts the GameZone Unicesar application.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        System.out.println("GameZone Unicesar - System starting...");

        ProductService productService = new ProductService();
        PersonService personService = new PersonService();

        AccessoryRepository accessoryRepository = new AccessoryRepository();
        AccessoryService accessoryService = new AccessoryService(accessoryRepository);
        SalePersistence salePersistence = new SalePersistence(accessoryRepository);
        SaleService saleService = new SaleService(productService, accessoryService, salePersistence);

        ConsoleUI consoleUI = new ConsoleUI(productService, personService, saleService, accessoryService);
        consoleUI.run();
    }
}