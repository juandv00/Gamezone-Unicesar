package com.gamezone.ui;

import com.gamezone.exceptions.PersistenceException;
import com.gamezone.model.Accessory;
import com.gamezone.model.Console;
import com.gamezone.model.ConsoleCompatibleAccessory;
import com.gamezone.model.Controller;
import com.gamezone.model.Memory;
import com.gamezone.service.AccessoryService;
import com.gamezone.service.ProductLookup;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Console submenu of the accessories module: registers controllers, cables,
 * and memories, manages their console compatibility and stock, and lists
 * them. Every operation runs through the {@link ErrorHandler}, so its errors
 * are shown as clear messages.
 */
public class AccessoryMenu {

    private final AccessoryService accessoryService;
    private final ProductLookup productLookup;
    private final ConsoleInput input;
    private final ErrorHandler errorHandler;

    /**
     * Creates the menu with the services it uses, the reader of the user
     * input, and the handler of the errors of each operation.
     *
     * @param accessoryService the service used for accessory operations,
     *        or null if the accessory module is disabled
     * @param productLookup the role used to find the consoles of the products
     * @param input the reader of the user input
     * @param errorHandler the handler that runs each operation and shows its errors
     */
    public AccessoryMenu(AccessoryService accessoryService,
                         ProductLookup productLookup,
                         ConsoleInput input, ErrorHandler errorHandler) {
        this.accessoryService = accessoryService;
        this.productLookup = productLookup;
        this.input = input;
        this.errorHandler = errorHandler;
    }

    /**
     * Displays the accessory management submenu and runs the operation chosen
     * by the user until the user goes back to the main menu.
     */
    public void show() {
        if (accessoryService == null) {
            System.out.println("El módulo de accesorios no está disponible.");
            return;
        }
        boolean back = false;
        while (!back) {
            System.out.println("\n--- Gestión de accesorios ---");
            System.out.println("1. Registrar un control");
            System.out.println("2. Registrar un cable");
            System.out.println("3. Registrar una memoria");
            System.out.println("4. Listar todos los accesorios");
            System.out.println("5. Listar accesorios por tipo");
            System.out.println("6. Buscar accesorios compatibles con una consola");
            System.out.println("7. Agregar una consola compatible a un control o memoria");
            System.out.println("8. Actualizar el stock de un accesorio");
            System.out.println("0. Volver al menú principal");
            System.out.print("Seleccione una opción: ");
            int option = input.readOption();
            switch (option) {
                case 1 -> errorHandler.executeOperation(this::registerController);
                case 2 -> errorHandler.executeOperation(this::registerCable);
                case 3 -> errorHandler.executeOperation(this::registerMemory);
                case 4 -> errorHandler.executeOperation(this::listAccessories);
                case 5 -> errorHandler.executeOperation(this::listAccessoriesByType);
                case 6 -> errorHandler.executeOperation(this::listAccessoriesCompatibleWithConsole);
                case 7 -> errorHandler.executeOperation(this::addCompatibleConsole);
                case 8 -> errorHandler.executeOperation(this::updateAccessoryStock);
                case 0 -> back = true;
                default -> System.out.println("Opción inválida. Intente de nuevo.");
            }
        }
    }

    /**
     * Prompts the user for the data of a new controller, registers it and
     * optionally registers its compatible consoles.
     */
    private void registerController() {
        CommonAccessoryData data = readCommonAccessoryData();
        if (data == null) {
            return;
        }
        System.out.print("Tipo de conexión (1 = inalámbrico, 2 = cableado): ");
        Controller.ConnectionType connectionType = switch (input.readOption()) {
            case 1 -> Controller.ConnectionType.WIRELESS;
            case 2 -> Controller.ConnectionType.WIRED;
            default -> null;
        };
        if (connectionType == null) {
            System.out.println("Tipo de conexión inválido.");
            return;
        }

        try {
            Controller controller = accessoryService.registerController(
                    data.id(), data.title(), data.price(), data.stock(), connectionType);
            System.out.println("Control registrado correctamente.");
            readCompatibleConsoles(controller.getId());
        } catch (IOException e) {
            throw accessoryFileError(e);
        }
    }

    /**
     * Prompts the user for the data of a new cable and registers it.
     */
    private void registerCable() {
        CommonAccessoryData data = readCommonAccessoryData();
        if (data == null) {
            return;
        }
        System.out.print("Longitud en metros: ");
        double length;
        try {
            length = Double.parseDouble(input.readLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Longitud inválida.");
            return;
        }
        System.out.print("Tipo de conector (p. ej. HDMI, USB): ");
        String connectorType = input.readLine().trim();

        try {
            accessoryService.registerCable(data.id(), data.title(), data.price(), data.stock(),
                    length, connectorType);
            System.out.println("Cable registrado correctamente.");
        } catch (IOException e) {
            throw accessoryFileError(e);
        }
    }

    /**
     * Prompts the user for the data of a new memory, registers it and
     * optionally registers its compatible consoles.
     */
    private void registerMemory() {
        CommonAccessoryData data = readCommonAccessoryData();
        if (data == null) {
            return;
        }
        System.out.print("Capacidad en GB: ");
        int capacity;
        try {
            capacity = Integer.parseInt(input.readLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Capacidad inválida.");
            return;
        }
        System.out.print("Tipo de memoria (1 = SD, 2 = MICRO_SD, 3 = INTERNAL_CARD): ");
        Memory.MemoryType memoryType = switch (input.readOption()) {
            case 1 -> Memory.MemoryType.SD;
            case 2 -> Memory.MemoryType.MICRO_SD;
            case 3 -> Memory.MemoryType.INTERNAL_CARD;
            default -> null;
        };
        if (memoryType == null) {
            System.out.println("Tipo de memoria inválido.");
            return;
        }

        try {
            Memory memory = accessoryService.registerMemory(
                    data.id(), data.title(), data.price(), data.stock(), capacity, memoryType);
            System.out.println("Memoria registrada correctamente.");
            readCompatibleConsoles(memory.getId());
        } catch (IOException e) {
            throw accessoryFileError(e);
        }
    }

    /**
     * Reads the attributes shared by every accessory type (id, title, price,
     * and stock), rejecting empty ids and ids already in use.
     *
     * @return the common accessory data, or null if any value was invalid
     */
    private CommonAccessoryData readCommonAccessoryData() {
        System.out.print("Identificador del accesorio: ");
        String id = input.readLine().trim();
        if (id.isEmpty()) {
            System.out.println("El identificador del accesorio no puede estar vacío.");
            return null;
        }
        if (accessoryService.findById(id) != null) {
            System.out.println("Ya existe un accesorio con ese identificador.");
            return null;
        }
        System.out.print("Título: ");
        String title = input.readLine().trim();
        Double price = input.readPrice();
        Integer stock = input.readStock();
        if (price == null || stock == null) {
            return null;
        }
        return new CommonAccessoryData(id, title, price, stock);
    }

    /**
     * Simple holder for the attributes shared by every accessory type,
     * used only while collecting input in the console UI.
     */
    private record CommonAccessoryData(String id, String title, double price, int stock) {
    }

    /**
     * Asks the user for console ids to register as compatible with the given
     * accessory, one at a time, until the user enters 0.
     *
     * @param accessoryId the id of the controller or memory being updated
     */
    private void readCompatibleConsoles(String accessoryId) {
        boolean adding = true;
        while (adding) {
            System.out.print("Identificador de la consola compatible (o 0 para terminar): ");
            String consoleId = input.readLine().trim();
            if (consoleId.equals("0")) {
                adding = false;
                continue;
            }
            registerCompatibility(accessoryId, consoleId);
        }
    }

    /**
     * Validates that the given id belongs to a registered console and, if so,
     * registers it as compatible with the given accessory.
     *
     * @param accessoryId the id of the controller or memory
     * @param consoleId   the id of the console to register
     */
    private void registerCompatibility(String accessoryId, String consoleId) {
        if (!(productLookup.findById(consoleId) instanceof Console console)) {
            System.out.println("No se encontró una consola con ese identificador.");
            return;
        }
        try {
            if (accessoryService.addCompatibleConsole(accessoryId, consoleId)) {
                System.out.println(console.getTitle() + " registrada como compatible.");
            } else {
                System.out.println("Solo los controles y las memorias admiten compatibilidad con consolas.");
            }
        } catch (IOException e) {
            throw accessoryFileError(e);
        }
    }

    /**
     * Lets the user add a compatible console to an existing controller or
     * memory.
     */
    private void addCompatibleConsole() {
        System.out.print("Identificador del accesorio: ");
        String accessoryId = input.readLine().trim();
        Accessory accessory = accessoryService.findById(accessoryId);
        if (accessory == null) {
            System.out.println("No se encontró un accesorio con ese identificador.");
            return;
        }
        if (!(accessory instanceof ConsoleCompatibleAccessory)) {
            System.out.println("Solo los controles y las memorias admiten compatibilidad con consolas.");
            return;
        }
        System.out.print("Identificador de la consola: ");
        String consoleId = input.readLine().trim();
        registerCompatibility(accessoryId, consoleId);
    }

    /**
     * Lists every accessory currently available in the inventory.
     */
    private void listAccessories() {
        printAccessories(accessoryService.listAllAccessories(), "Aún no hay accesorios registrados.");
    }

    /**
     * Lists the accessories of the type chosen by the user.
     */
    private void listAccessoriesByType() {
        System.out.print("Tipo (Control, Cable o Memoria): ");
        String type = toAccessoryType(input.readLine().trim());
        printAccessories(accessoryService.listAccessoriesByType(type),
                "No se encontraron accesorios de ese tipo.");
    }

    /**
     * Lists the accessories registered as compatible with the console chosen
     * by the user.
     */
    private void listAccessoriesCompatibleWithConsole() {
        System.out.print("Identificador de la consola: ");
        String consoleId = input.readLine().trim();
        printAccessories(accessoryService.findAccessoriesCompatibleWith(consoleId),
                "No hay accesorios registrados como compatibles con esa consola.");
    }

    /**
     * Updates the stock of an accessory to a new non-negative quantity.
     */
    private void updateAccessoryStock() {
        System.out.print("Identificador del accesorio: ");
        String accessoryId = input.readLine().trim();
        if (accessoryService.findById(accessoryId) == null) {
            System.out.println("No se encontró un accesorio con ese identificador.");
            return;
        }
        Integer stock = input.readStock();
        if (stock == null) {
            return;
        }
        if (stock < 0) {
            System.out.println("El stock no puede ser negativo.");
            return;
        }
        try {
            accessoryService.updateStock(accessoryId, stock);
            System.out.println("Stock actualizado correctamente.");
        } catch (IOException e) {
            throw accessoryFileError(e);
        }
    }

    /**
     * Converts an error of the accessory file, which AccessoryService reports
     * as a checked IOException, into a PersistenceException, so it is handled
     * like any other persistence error by {@link ErrorHandler#executeOperation(Runnable)}.
     *
     * @param cause the original error
     * @return the exception to throw
     */
    private PersistenceException accessoryFileError(IOException cause) {
        return new PersistenceException("Error de escritura en el archivo de accesorios.", cause);
    }

    /**
     * Converts the accessory type written by the user in Spanish (or in
     * English) into the type name used by the accessory module.
     *
     * @param input the type written by the user
     * @return "Controller", "Cable", or "Memory", or the input unchanged if it
     *         does not match any type
     */
    private String toAccessoryType(String input) {
        return switch (input.toLowerCase(Locale.ROOT)) {
            case "control", "controller" -> "Controller";
            case "cable" -> "Cable";
            case "memoria", "memory" -> "Memory";
            default -> input;
        };
    }

    /**
     * Returns the Spanish name of an accessory type, shown in the listings.
     *
     * @param type the type name used by the accessory module
     * @return the name shown to the user
     */
    private String accessoryTypeLabel(String type) {
        return switch (type) {
            case "Controller" -> "Control";
            case "Memory" -> "Memoria";
            default -> type;
        };
    }

    /**
     * Prints the given list of accessories with their type and id, or the
     * given message if the list is empty.
     *
     * @param accessories  the accessories to print
     * @param emptyMessage the message shown when there are no accessories
     */
    private void printAccessories(List<Accessory> accessories, String emptyMessage) {
        if (accessories.isEmpty()) {
            System.out.println(emptyMessage);
            return;
        }
        System.out.println("\n--- Accesorios ---");
        for (Accessory accessory : accessories) {
            System.out.println("[" + accessoryTypeLabel(accessory.getType()) + "] " + accessory.getId() + " - "
                    + accessory.getDescription());
        }
    }
}
