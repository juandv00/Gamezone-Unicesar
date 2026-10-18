package com.gamezone.ui;

import com.gamezone.model.Accessory;
import com.gamezone.model.Product;
import com.gamezone.model.Return;
import com.gamezone.model.Sale;
import com.gamezone.service.ReturnService;
import com.gamezone.service.SaleLookup;
import com.gamezone.util.CurrencyFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Console submenu of the returns module: registers returns, lists them by
 * client or by sale, and shows the monthly balance. Every operation runs
 * through the {@link ErrorHandler}, so its errors are shown as clear
 * messages.
 */
public class ReturnMenu implements Menu {

    private final ReturnService returnService;
    private final SaleLookup saleLookup;
    private final ConsoleInput input;
    private final ErrorHandler errorHandler;

    /**
     * Creates the menu with the services it uses, the reader of the user
     * input, and the handler of the errors of each operation.
     *
     * @param returnService the service used for return operations, or
     *        null if the return module is disabled
     * @param saleLookup the role used to find the sales
     * @param input the reader of the user input
     * @param errorHandler the handler that runs each operation and shows its errors
     */
    public ReturnMenu(ReturnService returnService,
                      SaleLookup saleLookup,
                      ConsoleInput input, ErrorHandler errorHandler) {
        this.returnService = returnService;
        this.saleLookup = saleLookup;
        this.input = input;
        this.errorHandler = errorHandler;
    }

    /**
     * Returns the title of the option of this submenu in the main menu.
     *
     * @return the title of the option
     */
    @Override
    public String getTitle() {
        return "Gestión de devoluciones";
    }

    /**
     * Displays the return management submenu and runs the operation chosen
     * by the user until the user goes back to the main menu.
     */
    @Override
    public void show() {
        if (returnService == null) {
            System.out.println("El módulo de devoluciones no está disponible.");
            return;
        }
        boolean back = false;
        while (!back) {
            System.out.println("\n--- Gestión de devoluciones ---");
            System.out.println("1. Registrar una devolución");
            System.out.println("2. Listar todas las devoluciones");
            System.out.println("3. Listar devoluciones por cliente");
            System.out.println("4. Listar devoluciones por venta");
            System.out.println("5. Ver el balance mensual");
            System.out.println("0. Volver al menú principal");
            System.out.print("Seleccione una opción: ");
            int option = input.readOption();
            switch (option) {
                case 1 -> errorHandler.executeOperation(this::registerReturn);
                case 2 -> errorHandler.executeOperation(() -> printReturns(returnService.viewAllReturns(),
                        "Aún no hay devoluciones registradas."));
                case 3 -> errorHandler.executeOperation(this::listReturnsByClient);
                case 4 -> errorHandler.executeOperation(this::listReturnsBySale);
                case 5 -> errorHandler.executeOperation(this::showMonthlyBalance);
                case 0 -> back = true;
                default -> System.out.println("Opción inválida. Intente de nuevo.");
            }
        }
    }

    /**
     * Guides the user through registering a return: choosing the original
     * sale, selecting which of its products and accessories are returned, and
     * giving a reason. The business rules (30-day window, items belonging to
     * the sale, units not returned twice) are validated by the ReturnService;
     * the sale window is also checked here to warn the user early.
     */
    private void registerReturn() {
        System.out.print("Identificador de la venta (p. ej. SALE-1): ");
        String saleId = input.readLine().trim();
        Sale sale = saleLookup.getSaleById(saleId);
        if (!sale.canBeReturned()) {
            System.out.println("Esta venta ya no admite devoluciones: pasaron más de 30 días desde el "
                    + sale.getDate() + ".");
            return;
        }

        System.out.println("\nÍtems de la venta " + sale.getId() + ":");
        for (Product product : sale.getProducts()) {
            System.out.println("  " + product.getId() + " - " + product.getTitle()
                    + " (" + CurrencyFormatter.format(product.getPrice()) + ")");
        }
        for (Accessory accessory : sale.getAccessories()) {
            System.out.println("  " + accessory.getId() + " - " + accessory.getTitle()
                    + " (accesorio, " + CurrencyFormatter.format(accessory.getPrice()) + ")");
        }

        List<String> itemIds = new ArrayList<>();
        boolean addingItems = true;
        while (addingItems) {
            System.out.print("Identificador del ítem a devolver (o 0 para terminar): ");
            String itemId = input.readLine().trim();
            if (itemId.equals("0")) {
                addingItems = false;
            } else if (!itemId.isEmpty()) {
                itemIds.add(itemId);
            }
        }
        if (itemIds.isEmpty()) {
            System.out.println("No se seleccionaron ítems. La devolución se canceló.");
            return;
        }

        System.out.print("Motivo de la devolución: ");
        String reason = input.readLine().trim();

        Return registeredReturn = returnService.registerReturn(sale.getId(), itemIds, reason);
        System.out.println("Devolución registrada correctamente.");
        System.out.println(ReturnReceiptFormatter.format(registeredReturn));
    }

    /**
     * Lists the returns of the sales made by a client chosen by the user.
     */
    private void listReturnsByClient() {
        System.out.print("Identificación del cliente: ");
        String clientId = input.readLine().trim();
        printReturns(returnService.viewReturnsByCustomer(clientId),
                "No se encontraron devoluciones para ese cliente.");
    }

    /**
     * Lists the returns associated with a sale chosen by the user.
     */
    private void listReturnsBySale() {
        System.out.print("Identificador de la venta: ");
        String saleId = input.readLine().trim();
        printReturns(returnService.viewReturnsBySale(saleId), "No se encontraron devoluciones para esa venta.");
    }

    /**
     * Asks the user for a month and year and shows the monthly balance:
     * total sales, total refunded by returns, and the net balance.
     */
    private void showMonthlyBalance() {
        System.out.print("Mes (1-12): ");
        int month = input.readOption();
        System.out.print("Año (p. ej. 2026): ");
        int year = input.readOption();
        if (year <= 0) {
            System.out.println("Año inválido.");
            return;
        }
        double sales = returnService.calculateMonthlySales(month, year);
        double refunds = returnService.calculateMonthlyReturns(month, year);
        double balance = returnService.generateMonthlyBalance(month, year);
        System.out.println("\n===== Balance mensual " + String.format("%02d/%d", month, year) + " =====");
        System.out.println("Total de ventas:       " + CurrencyFormatter.format(sales));
        System.out.println("Total de devoluciones: " + CurrencyFormatter.format(refunds));
        System.out.println("Balance neto:          " + CurrencyFormatter.format(balance));
    }

    /**
     * Prints a one-line summary of each return in the given list, or the
     * given message if the list is empty.
     *
     * @param returns      the returns to print
     * @param emptyMessage the message shown when there are no returns
     */
    private void printReturns(List<Return> returns, String emptyMessage) {
        if (returns.isEmpty()) {
            System.out.println(emptyMessage);
            return;
        }
        System.out.println("\n--- Devoluciones ---");
        for (Return r : returns) {
            Sale sale = r.getOriginalSale();
            System.out.println(r.getId() + " | " + r.getDate()
                    + " | venta " + sale.getId()
                    + " | cliente " + sale.getClient().getName()
                    + " | " + (r.getReturnedProducts().size() + r.getReturnedAccessories().size()) + " ítem(s)"
                    + " | reembolso " + CurrencyFormatter.format(r.getRefundAmount())
                    + " | motivo: " + r.getReason());
        }
    }
}
