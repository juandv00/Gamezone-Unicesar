package com.gamezone.ui;

import com.gamezone.model.Accessory;
import com.gamezone.model.Client;
import com.gamezone.model.Product;
import com.gamezone.model.Sale;
import com.gamezone.model.Seller;
import com.gamezone.model.Warranty;
import com.gamezone.service.AccessoryInventory;
import com.gamezone.service.PersonService;
import com.gamezone.service.ProductLookup;
import com.gamezone.service.SaleService;
import com.gamezone.service.WarrantyService;
import com.gamezone.util.CurrencyFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Console submenu of the sales module: registers sales of products and
 * accessories (asking for extended warranties) and shows the sales history
 * and receipts. Every operation runs through the {@link ErrorHandler}, so
 * its errors are shown as clear messages.
 */
public class SaleMenu {

    private final SaleService saleService;
    private final PersonService personService;
    private final ProductLookup productLookup;
    private final AccessoryInventory accessoryInventory;
    private final WarrantyService warrantyService;
    private final ConsoleInput input;
    private final ErrorHandler errorHandler;

    /**
     * Creates the menu with the services it uses, the reader of the user
     * input, and the handler of the errors of each operation.
     *
     * @param saleService the service used for sale operations
     * @param personService the service used for people operations
     * @param productLookup the role used to find the products of a sale
     * @param accessoryInventory the role used to find the accessories of a
     *        sale, or null if the accessory module is disabled
     * @param warrantyService the service used for warranty operations, or
     *        null if the warranty module is disabled
     * @param input the reader of the user input
     * @param errorHandler the handler that runs each operation and shows its errors
     */
    public SaleMenu(SaleService saleService,
                    PersonService personService,
                    ProductLookup productLookup,
                    AccessoryInventory accessoryInventory,
                    WarrantyService warrantyService,
                    ConsoleInput input, ErrorHandler errorHandler) {
        this.saleService = saleService;
        this.personService = personService;
        this.productLookup = productLookup;
        this.accessoryInventory = accessoryInventory;
        this.warrantyService = warrantyService;
        this.input = input;
        this.errorHandler = errorHandler;
    }

    /**
     * Displays the sales management submenu and runs the operation chosen
     * by the user until the user goes back to the main menu.
     */
    public void show() {
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
            int option = input.readOption();
            switch (option) {
                case 1 -> errorHandler.executeOperation(this::registerSale);
                case 2 -> errorHandler.executeOperation(this::listAllSales);
                case 3 -> errorHandler.executeOperation(this::listSalesByClient);
                case 4 -> errorHandler.executeOperation(this::listSalesBySeller);
                case 5 -> errorHandler.executeOperation(this::viewSaleReceipt);
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
        Client client = personService.getClientById(input.readLine().trim());

        System.out.print("Identificación del vendedor: ");
        Seller seller = personService.getSellerById(input.readLine().trim());

        List<String> itemIds = new ArrayList<>();
        List<Product> products = new ArrayList<>();
        boolean addingItems = true;
        while (addingItems) {
            System.out.print("Identificador del producto o accesorio a agregar (o 0 para terminar): ");
            String itemId = input.readLine().trim();
            if (itemId.equals("0")) {
                addingItems = false;
                continue;
            }
            Product product = productLookup.findById(itemId);
            if (product != null) {
                itemIds.add(product.getId());
                products.add(product);
                System.out.println(product.getTitle() + " agregado.");
                continue;
            }
            Accessory accessory = accessoryInventory != null ? accessoryInventory.findById(itemId) : null;
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
        System.out.println(SaleReceiptFormatter.format(sale));
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
                    + CurrencyFormatter.format(product.getPrice() * 0.10) + " por unidad) a "
                    + product.getTitle() + "? (s/n): ");
            String answer = input.readLine().trim();
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
        int number = input.readOption();
        if (number < 1 || number > sales.size()) {
            System.out.println("Número de venta inválido.");
            return;
        }
        System.out.println(SaleReceiptFormatter.format(sales.get(number - 1)));
    }

    /**
     * Displays the purchase history of a specific client.
     */
    private void listSalesByClient() {
        System.out.print("Identificación del cliente: ");
        String clientId = input.readLine().trim();
        List<Sale> sales = saleService.findByClient(clientId);
        printSales(sales);
    }

    /**
     * Displays the sales history attended by a specific seller.
     */
    private void listSalesBySeller() {
        System.out.print("Identificación del vendedor: ");
        String sellerId = input.readLine().trim();
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
}
