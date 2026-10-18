package com.gamezone.ui;

import com.gamezone.model.Product;
import com.gamezone.model.Sale;
import com.gamezone.model.Warranty;
import com.gamezone.service.SaleLookup;
import com.gamezone.service.WarrantyService;
import com.gamezone.util.CurrencyFormatter;
import java.time.LocalDate;
import java.util.List;

/**
 * Console submenu of the warranties module: shows the warranty of a product
 * in a sale and lists all, active, and soon-to-expire warranties. Every
 * operation runs through the {@link ErrorHandler}, so its errors are shown
 * as clear messages.
 */
public class WarrantyMenu {

    private final WarrantyService warrantyService;
    private final SaleLookup saleLookup;
    private final ConsoleInput input;
    private final ErrorHandler errorHandler;

    /**
     * Creates the menu with the services it uses, the reader of the user
     * input, and the handler of the errors of each operation.
     *
     * @param warrantyService the service used for warranty operations, or
     *        null if the warranty module is disabled
     * @param saleLookup the role used to find the sales
     * @param input the reader of the user input
     * @param errorHandler the handler that runs each operation and shows its errors
     */
    public WarrantyMenu(WarrantyService warrantyService,
                        SaleLookup saleLookup,
                        ConsoleInput input, ErrorHandler errorHandler) {
        this.warrantyService = warrantyService;
        this.saleLookup = saleLookup;
        this.input = input;
        this.errorHandler = errorHandler;
    }

    /**
     * Displays the warranty management submenu and runs the operation chosen
     * by the user until the user goes back to the main menu.
     */
    public void show() {
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
            int option = input.readOption();
            switch (option) {
                case 1 -> errorHandler.executeOperation(this::viewWarrantyOfProduct);
                case 2 -> errorHandler.executeOperation(() -> printWarranties(warrantyService.listAllWarranties(),
                        "Aún no hay garantías registradas."));
                case 3 -> errorHandler.executeOperation(() -> printWarranties(warrantyService.listActiveWarranties(),
                        "No hay garantías vigentes hoy."));
                case 4 -> errorHandler.executeOperation(this::listWarrantiesExpiringSoon);
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
        Sale sale = saleLookup.getSaleById(input.readLine().trim());
        System.out.print("Identificador del producto: ");
        String productId = input.readLine().trim();
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
        System.out.println(WarrantyCertificateFormatter.format(warranty));
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
        int daysAhead = input.readOption();
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
}
