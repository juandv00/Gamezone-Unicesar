package com.gamezone.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a memory accessory sold at GameZone Unicesar.
 * A memory is a specific type of Accessory characterized by its
 * storage capacity, its type, and (for some memory types, such as
 * console-specific internal storage) the consoles it is compatible with.
 */
public class Memory extends Accessory {

    /**
     * Type of memory: SD card, microSD card, or internal storage card.
     */
    public enum MemoryType {
        SD,
        MICRO_SD,
        INTERNAL_CARD
    }

    private int capacityInGB;
    private MemoryType memoryType;
    private final List<String> compatibleConsoleIds;

    /**
     * Creates a new Memory with the given common and particular attributes.
     * The memory starts with no registered console compatibilities; not every
     * memory needs them (e.g. a generic SD card may be universally usable),
     * so compatibility is registered separately via {@link #addCompatibleConsole(String)}.
     *
     * @param id           unique identifier of the accessory
     * @param title        display title of the accessory
     * @param price        unit price of the accessory
     * @param stock        initial quantity available in inventory
     * @param capacityInGB storage capacity of the memory, in gigabytes
     * @param memoryType   type of the memory (SD, microSD, internal card)
     */
    public Memory(String id, String title, double price, int stock,
                  int capacityInGB, MemoryType memoryType) {
        super(id, title, price, stock);
        if (capacityInGB <= 0) {
            throw new IllegalArgumentException("Capacity must be greater than zero");
        }
        this.capacityInGB = capacityInGB;
        this.memoryType = memoryType;
        this.compatibleConsoleIds = new ArrayList<>();
    }

    public int getCapacityInGB() {
        return capacityInGB;
    }

    public void setCapacityInGB(int capacityInGB) {
        this.capacityInGB = capacityInGB;
    }

    public MemoryType getMemoryType() {
        return memoryType;
    }

    public void setMemoryType(MemoryType memoryType) {
        this.memoryType = memoryType;
    }

    /**
     * Returns the ids of the consoles this memory is compatible with.
     *
     * @return an unmodifiable-safe copy of the compatible console ids
     */
    public List<String> getCompatibleConsoleIds() {
        return new ArrayList<>(compatibleConsoleIds);
    }

    /**
     * Registers a console as compatible with this memory, if it is
     * not already registered.
     *
     * @param consoleId the id of the console to add
     */
    public void addCompatibleConsole(String consoleId) {
        if (!compatibleConsoleIds.contains(consoleId)) {
            compatibleConsoleIds.add(consoleId);
        }
    }

    /**
     * Checks whether this memory is compatible with the given console.
     *
     * @param consoleId the id of the console to check
     * @return true if the console is registered as compatible, false otherwise
     */
    public boolean isCompatibleWith(String consoleId) {
        return compatibleConsoleIds.contains(consoleId);
    }

    @Override
    public String getType() {
        return "Memory";
    }

    @Override
    public String getDescription() {
        return getTitle() + " - Capacity: " + capacityInGB + "GB" +
                ", Type: " + memoryType +
                ", Compatible consoles: " + compatibleConsoleIds.size() +
                ", Price: $" + getPrice() +
                ", Stock: " + getStock();
    }

    @Override
    public String toString() {
        return "Memory{" + getDescription() + "}";
    }

}