package com.gamezone.ui;

import com.gamezone.service.AccessoryService;
import com.gamezone.service.PersonService;
import com.gamezone.service.ProductService;
import com.gamezone.service.PromotionService;
import com.gamezone.service.ReturnService;
import com.gamezone.service.SaleService;
import com.gamezone.service.WarrantyService;
import java.util.Scanner;

/**
 * Provides the console-based user interface for GameZone Unicesar. This
 * class only shows the main menu and delegates each module to its own
 * submenu class ({@link ProductMenu}, {@link PersonMenu}, {@link SaleMenu},
 * {@link AccessoryMenu}, {@link PromotionMenu}, {@link ReturnMenu}, and
 * {@link WarrantyMenu}). The submenus read the user input through
 * {@link ConsoleInput} and run every operation through
 * {@link ErrorHandler}, which shows a different message for each category
 * of error, so the user never sees a technical message or a stack trace.
 */
public class ConsoleUI {

    private final ConsoleInput input;
    private final ProductMenu productMenu;
    private final PersonMenu personMenu;
    private final SaleMenu saleMenu;
    private final AccessoryMenu accessoryMenu;
    private final PromotionMenu promotionMenu;
    private final ReturnMenu returnMenu;
    private final WarrantyMenu warrantyMenu;

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
        this.input = new ConsoleInput(new Scanner(System.in));
        ErrorHandler errorHandler = new ErrorHandler();
        this.productMenu = new ProductMenu(productService, input, errorHandler);
        this.personMenu = new PersonMenu(personService, input, errorHandler);
        this.saleMenu = new SaleMenu(saleService, personService, productService, accessoryService,
                warrantyService, input, errorHandler);
        this.accessoryMenu = new AccessoryMenu(accessoryService, productService, input, errorHandler);
        this.promotionMenu = new PromotionMenu(promotionService, input, errorHandler);
        this.returnMenu = new ReturnMenu(returnService, saleService, input, errorHandler);
        this.warrantyMenu = new WarrantyMenu(warrantyService, saleService, input, errorHandler);
    }

    /**
     * Starts the application's main loop, showing the main menu until
     * the user chooses to exit.
     */
    public void run() {
        boolean exit = false;
        while (!exit) {
            showMainMenu();
            int option = input.readOption();
            switch (option) {
                case 1 -> productMenu.show();
                case 2 -> personMenu.show();
                case 3 -> saleMenu.show();
                case 4 -> accessoryMenu.show();
                case 5 -> promotionMenu.show();
                case 6 -> returnMenu.show();
                case 7 -> warrantyMenu.show();
                case 0 -> exit = true;
                default -> System.out.println("Opción inválida. Intente de nuevo.");
            }
        }
        System.out.println("¡Gracias por usar GameZone Unicesar!");
        input.close();
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
}
