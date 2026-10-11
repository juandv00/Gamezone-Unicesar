package com.gamezone.service;

import com.gamezone.model.Accessory;
import com.gamezone.model.Cable;
import com.gamezone.model.Controller;
import com.gamezone.model.Memory;
import com.gamezone.persistence.AccessoryRepository;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Contains the business rules for managing accessories at GameZone Unicesar,
 * such as registering new controllers, cables, and memories, listing available
 * inventory, querying compatibility with consoles, and validating/reducing
 * stock when a sale is registered.
 */
public class AccessoryService {

    private final AccessoryRepository accessoryRepository;
    private List<Accessory> accessories;

    /**
     * Creates a new AccessoryService, loading the currently stored accessories
     * from the repository.
     *
     * @param accessoryRepository the repository used to persist accessories
     */
    public AccessoryService(AccessoryRepository accessoryRepository) {
        this.accessoryRepository = accessoryRepository;
        this.accessories = accessoryRepository.loadAll();
    }

    /**
     * Registers a new controller and immediately persists the updated list.
     *
     * @param id unique identifier of the controller
     * @param title display title of the controller
     * @param price unit price of the controller
     * @param stock initial quantity available in inventory
     * @param connectionType connection type of the controller (wireless or
     * wired)
     * @return the newly registered Controller instance
     */
    public Controller registerController(String id, String title, double price,
                                         int stock, Controller.ConnectionType connectionType) throws IOException {
        Controller controller = new Controller(id, title, price, stock, connectionType);
        accessories.add(controller);
        accessoryRepository.saveAll(accessories);
        return controller;
    }

    /**
     * Registers a new cable and immediately persists the updated list.
     *
     * @param id unique identifier of the cable
     * @param title display title of the cable
     * @param price unit price of the cable
     * @param stock initial quantity available in inventory
     * @param lengthInMeters length of the cable, in meters
     * @param connectorType connector type of the cable (e.g. HDMI, USB)
     * @return the newly registered Cable instance
     */
    public Cable registerCable(String id, String title, double price, int stock,
                               double lengthInMeters, String connectorType) throws IOException {
        Cable cable = new Cable(id, title, price, stock, lengthInMeters, connectorType);
        accessories.add(cable);
        accessoryRepository.saveAll(accessories);
        return cable;
    }

    /**
     * Registers a new memory and immediately persists the updated list.
     *
     * @param id unique identifier of the memory
     * @param title display title of the memory
     * @param price unit price of the memory
     * @param stock initial quantity available in inventory
     * @param capacityInGB storage capacity of the memory, in gigabytes
     * @param memoryType type of the memory (SD, microSD, internal card)
     * @return the newly registered Memory instance
     */
    public Memory registerMemory(String id, String title, double price, int stock,
                                 int capacityInGB, Memory.MemoryType memoryType) throws IOException {
        Memory memory = new Memory(id, title, price, stock, capacityInGB, memoryType);
        accessories.add(memory);
        accessoryRepository.saveAll(accessories);
        return memory;
    }

    /**
     * Returns the full list of accessories currently available.
     *
     * @return the list of registered accessories
     */
    public List<Accessory> listAllAccessories() {
        return new ArrayList<>(accessories);
    }

    /**
     * Returns the accessories matching a given type (Controller, Cable, or
     * Memory), matched case-insensitively against each accessory's
     * {@code getType()} value.
     *
     * @param type the accessory type to filter by
     * @return the list of accessories matching the given type
     */
    public List<Accessory> listAccessoriesByType(String type) {
        List<Accessory> result = new ArrayList<>();
        for (Accessory accessory : accessories) {
            if (accessory.getType().equalsIgnoreCase(type)) {
                result.add(accessory);
            }
        }
        return result;
    }

    /**
     * Returns the accessories that are compatible with the given console. Only
     * accessories that expose console compatibility (Controller and Memory) are
     * considered; Cable accessories never match, since cables do not register
     * console compatibility.
     *
     * @param consoleId the id of the console to check compatibility for
     * @return the list of accessories compatible with that console
     */
    public List<Accessory> findAccessoriesCompatibleWith(String consoleId) {
        List<Accessory> result = new ArrayList<>();
        for (Accessory accessory : accessories) {
            if (accessory instanceof Controller controller
                    && controller.isCompatibleWith(consoleId)) {
                result.add(accessory);
            } else if (accessory instanceof Memory memory
                    && memory.isCompatibleWith(consoleId)) {
                result.add(accessory);
            }
        }
        return result;
    }

    /**
     * Finds an accessory by its unique identifier.
     *
     * @param id the id of the accessory to find
     * @return the matching accessory, or null if not found
     */
    public Accessory findById(String id) {
        for (Accessory accessory : accessories) {
            if (accessory.getId().equals(id)) {
                return accessory;
            }
        }
        return null;
    }

    /**
     * Updates the stock of a given accessory to the specified quantity and
     * persists the change.
     *
     * @param accessoryId the id of the accessory to update
     * @param quantity the new stock quantity to set
     * @return true if the accessory was found and updated, false otherwise
     */
    public boolean updateStock(String accessoryId, int quantity) throws IOException {
        Accessory accessory = findById(accessoryId);
        if (accessory == null) {
            return false;
        }
        accessory.setStock(quantity);
        accessoryRepository.saveAll(accessories);
        return true;
    }

    /**
     * Checks whether an accessory has enough stock available for a given
     * quantity.
     *
     * @param accessoryId the id of the accessory to check
     * @param amount the quantity requested
     * @return true if there is enough stock, false otherwise
     */
    public boolean hasEnoughStock(String accessoryId, int amount) {
        Accessory accessory = findById(accessoryId);
        return accessory != null && accessory.getStock() >= amount;
    }

    /**
     * Reduces the stock of a given accessory by the specified amount, used when
     * a sale is registered.
     *
     * @param accessoryId the id of the accessory being sold
     * @param amount the quantity to subtract from stock
     * @return true if the stock was successfully reduced, false if there is not
     * enough stock available or the accessory does not exist
     */
    public boolean reduceStock(String accessoryId, int amount) throws IOException {
        Accessory accessory = findById(accessoryId);
        if (accessory == null || accessory.getStock() < amount) {
            return false;
        }
        accessory.setStock(accessory.getStock() - amount);
        accessoryRepository.saveAll(accessories);
        return true;
    }

    /**
     * Registers a console as compatible with the given accessory and persists
     * the change. Only accessories that support console compatibility
     * (Controller and Memory) can be updated; Cable accessories are rejected.
     *
     * @param accessoryId the id of the controller or memory to update
     * @param consoleId the id of the console to register as compatible
     * @return true if the compatibility was registered, false if the accessory
     * does not exist or does not support console compatibility
     */
    public boolean addCompatibleConsole(String accessoryId, String consoleId) throws IOException {
        Accessory accessory = findById(accessoryId);
        if (accessory instanceof Controller controller) {
            controller.addCompatibleConsole(consoleId);
        } else if (accessory instanceof Memory memory) {
            memory.addCompatibleConsole(consoleId);
        } else {
            return false;
        }
        accessoryRepository.saveAll(accessories);
        return true;
    }
}