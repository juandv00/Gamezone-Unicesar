package com.gamezone;

import com.gamezone.exceptions.GameZoneException;
import com.gamezone.exceptions.PersistenceException;
import com.gamezone.persistence.AccessoryRepository;
import com.gamezone.persistence.PersonPersistence;
import com.gamezone.persistence.ProductPersistence;
import com.gamezone.persistence.PromotionRepository;
import com.gamezone.persistence.ReturnRepository;
import com.gamezone.persistence.SalePersistence;
import com.gamezone.persistence.WarrantyRepository;
import com.gamezone.service.AccessoryService;
import com.gamezone.service.PersonService;
import com.gamezone.service.ProductService;
import com.gamezone.service.PromotionService;
import com.gamezone.service.ReturnService;
import com.gamezone.service.SaleService;
import com.gamezone.service.WarrantyService;
import com.gamezone.ui.AccessoryMenu;
import com.gamezone.ui.ConsoleInput;
import com.gamezone.ui.ConsoleUI;
import com.gamezone.ui.ErrorHandler;
import com.gamezone.ui.Menu;
import com.gamezone.ui.PersonMenu;
import com.gamezone.ui.ProductMenu;
import com.gamezone.ui.PromotionMenu;
import com.gamezone.ui.ReturnMenu;
import com.gamezone.ui.SaleMenu;
import com.gamezone.ui.WarrantyMenu;
import java.util.List;
import java.util.Scanner;

/**
 * Entry point of the GameZone Unicesar application. Wires together the services
 * of the modules (products, people, accessories, promotions, sales, returns,
 * and warranties) and starts the console-based user interface, which loads
 * previously stored data automatically when the application starts.
 * If the stored data cannot be loaded, the application shows a clear
 * message instead of a stack trace and does not start.
 */
public class Main {

    /**
     * Starts the GameZone Unicesar application.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        System.out.println("GameZone Unicesar - Iniciando el sistema...");
        ConsoleUI consoleUI;
        try {
            consoleUI = createConsoleUI();
        } catch (PersistenceException e) {
            System.out.println("No fue posible cargar los datos del sistema. Contacte al administrador.");
            return;
        } catch (GameZoneException e) {
            System.out.println("No fue posible iniciar el sistema: " + e.getMessage());
            return;
        }
        consoleUI.run();
    }

    /**
     * Creates every repository and service in dependency order, loading the
     * stored data, and the console user interface that uses them, with the
     * reader of the user input, the error handler, and the seven submenus.
     * This is the composition root of the application: it is the only place
     * that knows the concrete file classes and submenus, and it passes them to
     * the classes that use them, which depend only on abstractions.
     *
     * @return the console user interface, ready to run
     * @throws PersistenceException if a data file cannot be read
     */
    private static ConsoleUI createConsoleUI() {
        ProductPersistence productPersistence = new ProductPersistence();
        ProductService productService = new ProductService(productPersistence);
        PersonPersistence personPersistence = new PersonPersistence();
        PersonService personService = new PersonService(personPersistence);

        AccessoryRepository accessoryRepository = new AccessoryRepository();
        AccessoryService accessoryService = new AccessoryService(accessoryRepository);

        PromotionRepository promotionRepository = new PromotionRepository();
        PromotionService promotionService = new PromotionService(promotionRepository);

        SalePersistence salePersistence = new SalePersistence(productPersistence, personPersistence,
                accessoryRepository);
        WarrantyRepository warrantyRepository = new WarrantyRepository();
        WarrantyService warrantyService = new WarrantyService(warrantyRepository, salePersistence,
                productService);
        SaleService saleService = new SaleService(productService, accessoryService,
                salePersistence, promotionService, warrantyService);

        ReturnRepository returnRepository = new ReturnRepository();
        ReturnService returnService = new ReturnService(returnRepository, saleService, productService,
                accessoryService, warrantyService);

        ConsoleInput input = new ConsoleInput(new Scanner(System.in));
        ErrorHandler errorHandler = new ErrorHandler();
        List<Menu> menus = List.of(
                new ProductMenu(productService, input, errorHandler),
                new PersonMenu(personService, input, errorHandler),
                new SaleMenu(saleService, personService, productService, accessoryService,
                        warrantyService, input, errorHandler),
                new AccessoryMenu(accessoryService, productService, input, errorHandler),
                new PromotionMenu(promotionService, input, errorHandler),
                new ReturnMenu(returnService, saleService, input, errorHandler),
                new WarrantyMenu(warrantyService, saleService, input, errorHandler));
        return new ConsoleUI(input, menus);
    }
}