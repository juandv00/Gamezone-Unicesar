package com.gamezone.persistence;

import com.gamezone.model.Accessory;
import com.gamezone.model.Cable;
import com.gamezone.model.Controller;
import com.gamezone.model.Memory;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles saving and loading Accessory data (controllers, cables, and memories)
 * to and from a file, so that this information persists between application
 * runs.
 */
public class AccessoryRepository {

    private static final String FILE_PATH = "data/accessories.csv";
    private static final String DELIMITER = "\t";

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
            System.out.println("Error saving accessories: " + e.getMessage());
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
            System.out.println("Error loading accessories: " + e.getMessage());
        }
        return accessories;
    }

    /**
     * Converts a single accessory into its delimited text representation,
     * choosing the fields to write based on its concrete subtype.
     *
     * @param accessory the accessory to convert
     * @return the delimited line representing this accessory, or an empty
     * string if the subtype is not recognized
     */
    private String toLine(Accessory accessory) {
        if (accessory instanceof Memory memory) {
            String consoles = String.join(",", memory.getCompatibleConsoleIds());
            return "MEMORY" + DELIMITER + memory.getId() + DELIMITER + memory.getTitle() + DELIMITER
                    + memory.getPrice() + DELIMITER + memory.getStock() + DELIMITER
                    + memory.getCapacityInGB() + DELIMITER + memory.getMemoryType() + DELIMITER + consoles;
        } else if (accessory instanceof Controller controller) {
            String consoles = String.join(",", controller.getCompatibleConsoleIds());
            return "CONTROLLER" + DELIMITER + controller.getId() + DELIMITER + controller.getTitle() + DELIMITER
                    + controller.getPrice() + DELIMITER + controller.getStock() + DELIMITER
                    + controller.getConnectionType() + DELIMITER + consoles;
        } else if (accessory instanceof Cable cable) {
            return "CABLE" + DELIMITER + cable.getId() + DELIMITER + cable.getTitle() + DELIMITER
                    + cable.getPrice() + DELIMITER + cable.getStock() + DELIMITER
                    + cable.getLengthInMeters() + DELIMITER + cable.getConnectorType();
        }
        return "";
    }

    /**
     * Reconstructs a single Accessory from its delimited text representation,
     * using the discriminator column to determine which concrete subtype to
     * instantiate.
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

        if (type.equals("MEMORY")) {
            int capacity = Integer.parseInt(parts[5]);
            Memory.MemoryType memoryType = Memory.MemoryType.valueOf(parts[6]);
            Memory memory = new Memory(id, title, price, stock, capacity, memoryType);

            if (parts.length > 7 && !parts[7].isEmpty()) {
                for (String consoleId : parts[7].split(",")) {
                    memory.addCompatibleConsole(consoleId);
                }
            }
            return memory;

        } else if (type.equals("CONTROLLER")) {
            Controller.ConnectionType connectionType = Controller.ConnectionType.valueOf(parts[5]);
            Controller controller = new Controller(id, title, price, stock, connectionType);

            if (parts.length > 6 && !parts[6].isEmpty()) {
                for (String consoleId : parts[6].split(",")) {
                    controller.addCompatibleConsole(consoleId);
                }
            }
            return controller;

        } else if (type.equals("CABLE")) {
            double length = Double.parseDouble(parts[5]);
            String connector = parts[6];
            return new Cable(id, title, price, stock, length, connector);
        }

        return null;
    }
}
