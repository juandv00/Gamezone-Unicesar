package com.gamezone.ui;

import com.gamezone.model.Promotion;
import com.gamezone.service.PromotionService;
import java.time.LocalDate;
import java.util.List;

/**
 * Console submenu of the promotions module: registers percentage, category,
 * and bulk purchase promotions and lists all and active promotions. Every
 * operation runs through the {@link ErrorHandler}, so its errors are shown
 * as clear messages.
 */
public class PromotionMenu implements Menu {

    private final PromotionService promotionService;
    private final ConsoleInput input;
    private final ErrorHandler errorHandler;

    /**
     * Creates the menu with the services it uses, the reader of the user
     * input, and the handler of the errors of each operation.
     *
     * @param promotionService the service used for promotion operations,
     *        or null if the promotion module is disabled
     * @param input the reader of the user input
     * @param errorHandler the handler that runs each operation and shows its errors
     */
    public PromotionMenu(PromotionService promotionService,
                         ConsoleInput input, ErrorHandler errorHandler) {
        this.promotionService = promotionService;
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
        return "Gestión de promociones";
    }

    /**
     * Displays the promotion management submenu and runs the operation chosen
     * by the user until the user goes back to the main menu.
     */
    @Override
    public void show() {
        if (promotionService == null) {
            System.out.println("El módulo de promociones no está disponible.");
            return;
        }
        boolean back = false;
        while (!back) {
            System.out.println("\n--- Gestión de promociones ---");
            System.out.println("1. Registrar una promoción por porcentaje");
            System.out.println("2. Registrar una promoción por categoría");
            System.out.println("3. Registrar una promoción por compra en volumen");
            System.out.println("4. Listar todas las promociones");
            System.out.println("5. Listar las promociones vigentes (hoy)");
            System.out.println("0. Volver al menú principal");
            System.out.print("Seleccione una opción: ");
            int option = input.readOption();
            switch (option) {
                case 1 -> errorHandler.executeOperation(this::registerPercentagePromotion);
                case 2 -> errorHandler.executeOperation(this::registerCategoryPromotion);
                case 3 -> errorHandler.executeOperation(this::registerBulkPurchasePromotion);
                case 4 -> errorHandler.executeOperation(() -> printPromotions(promotionService.listAllPromotions(),
                        "Aún no hay promociones registradas."));
                case 5 -> errorHandler.executeOperation(() -> printPromotions(promotionService.listActivePromotions(),
                        "No hay promociones vigentes hoy."));
                case 0 -> back = true;
                default -> System.out.println("Opción inválida. Intente de nuevo.");
            }
        }
    }

    /**
     * Prompts the user for the data of a new percentage promotion and
     * registers it.
     */
    private void registerPercentagePromotion() {
        CommonPromotionData data = readCommonPromotionData();
        if (data == null) {
            return;
        }
        Double percentage = input.readPercentage();
        if (percentage == null) {
            return;
        }
        promotionService.registerPercentageDiscount(data.id(), data.name(),
                data.startDate(), data.endDate(), percentage);
        System.out.println("Promoción por porcentaje registrada correctamente.");
    }

    /**
     * Prompts the user for the data of a new category promotion and
     * registers it.
     */
    private void registerCategoryPromotion() {
        CommonPromotionData data = readCommonPromotionData();
        if (data == null) {
            return;
        }
        Double percentage = input.readPercentage();
        if (percentage == null) {
            return;
        }
        System.out.print("Categoría objetivo (1 = videojuegos, 2 = consolas, 3 = accesorios): ");
        String targetCategory = switch (input.readOption()) {
            case 1 -> "VIDEOGAME";
            case 2 -> "CONSOLE";
            case 3 -> "ACCESSORY";
            default -> null;
        };
        if (targetCategory == null) {
            System.out.println("Categoría inválida.");
            return;
        }
        promotionService.registerCategoryDiscount(data.id(), data.name(),
                data.startDate(), data.endDate(), percentage, targetCategory);
        System.out.println("Promoción por categoría registrada correctamente.");
    }

    /**
     * Prompts the user for the data of a new bulk purchase promotion and
     * registers it.
     */
    private void registerBulkPurchasePromotion() {
        CommonPromotionData data = readCommonPromotionData();
        if (data == null) {
            return;
        }
        System.out.print("Cantidad mínima de productos: ");
        int minimumQuantity = input.readOption();
        if (minimumQuantity <= 0) {
            System.out.println("La cantidad mínima de productos debe ser mayor que cero.");
            return;
        }
        Double percentage = input.readPercentage();
        if (percentage == null) {
            return;
        }
        promotionService.registerBulkPurchaseDiscount(data.id(), data.name(),
                data.startDate(), data.endDate(), minimumQuantity, percentage);
        System.out.println("Promoción por compra en volumen registrada correctamente.");
    }

    /**
     * Reads the attributes shared by every promotion type (id, name, start
     * date and end date), rejecting empty or repeated ids and date ranges
     * where the start date is after the end date.
     *
     * @return the common promotion data, or null if any value was invalid
     */
    private CommonPromotionData readCommonPromotionData() {
        System.out.print("Identificador de la promoción: ");
        String id = input.readLine().trim();
        if (id.isEmpty()) {
            System.out.println("El identificador de la promoción no puede estar vacío.");
            return null;
        }
        if (promotionService.findById(id) != null) {
            System.out.println("Ya existe una promoción con ese identificador.");
            return null;
        }
        System.out.print("Nombre: ");
        String name = input.readLine().trim();
        LocalDate startDate = input.readDate("Fecha de inicio (AAAA-MM-DD): ");
        if (startDate == null) {
            return null;
        }
        LocalDate endDate = input.readDate("Fecha de fin (AAAA-MM-DD): ");
        if (endDate == null) {
            return null;
        }
        if (startDate.isAfter(endDate)) {
            System.out.println("La fecha de inicio no puede ser posterior a la fecha de fin.");
            return null;
        }
        return new CommonPromotionData(id, name, startDate, endDate);
    }

    /**
     * Simple holder for the attributes shared by every promotion type,
     * used only while collecting input in the console UI.
     */
    private record CommonPromotionData(String id, String name, LocalDate startDate, LocalDate endDate) {
    }

    /**
     * Prints the given list of promotions, marking each one as active or
     * inactive today, or the given message if the list is empty.
     *
     * @param promotions   the promotions to print
     * @param emptyMessage the message shown when there are no promotions
     */
    private void printPromotions(List<Promotion> promotions, String emptyMessage) {
        if (promotions.isEmpty()) {
            System.out.println(emptyMessage);
            return;
        }
        LocalDate today = LocalDate.now();
        System.out.println("\n--- Promociones ---");
        for (Promotion promotion : promotions) {
            String status = promotion.isActive(today) ? "[VIGENTE] " : "[NO VIGENTE] ";
            System.out.println(status + promotion);
        }
    }
}
