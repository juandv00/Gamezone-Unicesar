package com.gamezone.persistence;

import com.gamezone.model.Accessory;
import com.gamezone.model.Cable;
import com.gamezone.model.ConsoleCompatibleAccessory;
import com.gamezone.model.Controller;
import com.gamezone.model.Memory;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Handles saving and loading Accessory data (controllers, cables, and memories)
 * to and from a file, so that this information persists between application
 * runs.
 * <p>
 * The format of each accessory type is registered once in the constructor:
 * a writer for its class and a reader for its type code. Saving and loading
 * look up the registered format instead of checking the type with a chain
 * of conditionals, so a new accessory type only needs a new registration.
 */
public class AccessoryRepository {

    private static final String FILE_PATH = "data/accessories.csv";
    private static final String DELIMITER = "\t";

    private final Map<Class<? extends Accessory>, Function<Accessory, String>> writers = new HashMap<>();
    private final Map<String, AccessoryReader> readers = new HashMap<>();

    /**
     * Rebuilds an accessory of one type from the fields of a stored line.
     */
    @FunctionalInterface
    private interface AccessoryReader {

        /**
         * Creates the accessory from its common fields and the stored line.
         *
         * @param id    the accessory id
         * @param title the accessory title
         * @param price the unit price
         * @param stock the units in stock
         * @param parts every field of the stored line
         * @return the reconstructed accessory
         */
        Accessory read(String id, String title, double price, int stock, String[] parts);
    }

    /**
     * Creates the repository and registers the file format of each accessory
     * type (memories, controllers, and cables).
     */
    public AccessoryRepository() {
        registerFormat(Memory.class, "MEMORY",
                memory -> memory.getCapacityInGB() + DELIMITER + memory.getMemoryType() + DELIMITER
                        + consolesField(memory),
                (id, title, price, stock, parts) -> {
                    int capacity = Integer.parseInt(parts[5]);
                    Memory.MemoryType memoryType = Memory.MemoryType.valueOf(parts[6]);
                    Memory memory = new Memory(id, title, price, stock, capacity, memoryType);
                    readConsoles(memory, parts, 7);
                    return memory;
                });
        registerFormat(Controller.class, "CONTROLLER",
                controller -> controller.getConnectionType() + DELIMITER + consolesField(controller),
                (id, title, price, stock, parts) -> {
                    Controller.ConnectionType connectionType = Controller.ConnectionType.valueOf(parts[5]);
                    Controller controller = new Controller(id, title, price, stock, connectionType);
                    readConsoles(controller, parts, 6);
                    return controller;
                });
        registerFormat(Cable.class, "CABLE",
                cable -> cable.getLengthInMeters() + DELIMITER + cable.getConnectorType(),
                (id, title, price, stock, parts) -> new Cable(id, title, price, stock,
                        Double.parseDouble(parts[5]), parts[6]));
    }

    /**
     * Registers how an accessory type is written to and read from the file.
     * Every line starts with the type code followed by the common fields
     * (id, title, price, stock) and then the fields of the type.
     *
     * @param type           the class of the accessory type
     * @param code           the type code stored at the start of the line
     * @param specificFields builds the fields of the type, already delimited
     * @param reader         rebuilds an accessory of the type from a line
     * @param <T>            the accessory type
     */
    private <T extends Accessory> void registerFormat(Class<T> type, String code,
                                                      Function<T, String> specificFields,
                                                      AccessoryReader reader) {
        writers.put(type, accessory -> code + DELIMITER + accessory.getId() + DELIMITER
                + accessory.getTitle() + DELIMITER + accessory.getPrice() + DELIMITER
                + accessory.getStock() + DELIMITER + specificFields.apply(type.cast(accessory)));
        readers.put(code, reader);
    }

    /**
     * Joins the compatible console ids of an accessory with commas.
     *
     * @param accessory the accessory with console compatibility
     * @return the ids separated by commas, or an empty text if there are none
     */
    private String consolesField(ConsoleCompatibleAccessory accessory) {
        return String.join(",", accessory.getCompatibleConsoleIds());
    }

    /**
     * Registers in the accessory the compatible consoles stored in the given
     * field of the line, if the field exists and is not empty.
     *
     * @param accessory the accessory being rebuilt
     * @param parts     every field of the stored line
     * @param index     the position of the compatible consoles field
     */
    private void readConsoles(ConsoleCompatibleAccessory accessory, String[] parts, int index) {
        if (parts.length > index && !parts[index].isEmpty()) {
            for (String consoleId : parts[index].split(",")) {
                accessory.addCompatibleConsole(consoleId);
            }
        }
    }

    /**
     * Persists the given list of accessories to the file, overwriting any
     * previously stored data. Each accessory is written as a single line,
     * starting with a type discriminator (MEMORY, CONTROLLER, or CABLE)
     * followed by its common and type-specific attributes.
     *
     * @param accessories the list of accessories to save
     */
    public void saveAll(List<Accessory> accessories) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_PATH))) {
            for (Accessory accessory : accessories) {
                bw.write(toLine(accessory));
                bw.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error al guardar los accesorios: " + e.getMessage());
        }
    }

    /**
     * Loads all accessories from the file, reconstructing each concrete subtype
     * (Memory, Controller, or Cable) based on its discriminator column.
     *
     * @return the list of accessories found, or an empty list if the file does
     * not exist
     */
    public List<Accessory> loadAll() {
        List<Accessory> accessories = new ArrayList<>();
        File file = new File(FILE_PATH);
        if (!file.exists()) {
            return accessories;
        }
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                Accessory accessory = fromLine(line);
                if (accessory != null) {
                    accessories.add(accessory);
                }
            }
        } catch (IOException e) {
            System.out.println("Error al cargar los accesorios: " + e.getMessage());
        }
        return accessories;
    }

    /**
     * Converts a single accessory into its delimited text representation,
     * using the format registered for its type.
     *
     * @param accessory the accessory to convert
     * @return the delimited line representing this accessory, or an empty
     * string if the subtype is not recognized
     */
    private String toLine(Accessory accessory) {
        Function<Accessory, String> writer = writers.get(accessory.getClass());
        if (writer == null) {
            return "";
        }
        return writer.apply(accessory);
    }

    /**
     * Reconstructs a single Accessory from its delimited text representation,
     * using the format registered for the type code in the first column.
     *
     * @param line the delimited line read from the file
     * @return the reconstructed Accessory, or null if the line is malformed or
     * its type is not recognized
     */
    private Accessory fromLine(String line) {
        String[] parts = line.split(DELIMITER);
        if (parts.length < 5) {
            return null;
        }
        String type = parts[0];
        String id = parts[1];
        String title = parts[2];
        double price = Double.parseDouble(parts[3]);
        int stock = Integer.parseInt(parts[4]);

        AccessoryReader reader = readers.get(type);
        if (reader == null) {
            return null;
        }
        return reader.read(id, title, price, stock, parts);
    }
}
