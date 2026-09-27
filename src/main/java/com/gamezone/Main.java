package com.gamezone;

import com.gamezone.persistence.AccessoryRepository;
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
import com.gamezone.ui.ConsoleUI;

/**
 * Entry point of the GameZone Unicesar application. Wires together the
 * services of the modules (products, people, accessories, promotions,
 * sales, returns, and warranties) and starts the console-based user interface, which loads
 * previously stored data automatically when the application starts.
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

        PromotionRepository promotionRepository = new PromotionRepository();
        PromotionService promotionService = new PromotionService(promotionRepository);

        SalePersistence salePersistence = new SalePersistence(accessoryRepository);
        WarrantyRepository warrantyRepository = new WarrantyRepository(salePersistence);
        WarrantyService warrantyService = new WarrantyService(warrantyRepository);
        SaleService saleService = new SaleService(productService, accessoryService,
                salePersistence, promotionService, warrantyService);

        ReturnRepository returnRepository = new ReturnRepository(saleService, productService);
        ReturnService returnService = new ReturnService(returnRepository, saleService, productService);

        ConsoleUI consoleUI = new ConsoleUI(productService, personService, saleService,
                accessoryService, promotionService, returnService, warrantyService);
        consoleUI.run();
    }
}