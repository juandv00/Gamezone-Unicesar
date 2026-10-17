package com.gamezone.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents an accessory that keeps a list of the consoles it is compatible
 * with, such as a controller or a memory. The compatibility list and its
 * operations live in this class once, so the subclasses do not repeat them
 * and the services can work with any accessory of this type without
 * checking its concrete class.
 */
public abstract class ConsoleCompatibleAccessory extends Accessory {

    private final List<String> compatibleConsoleIds;

    /**
     * Creates a new accessory with console compatibility. It starts with no
     * registered compatible consoles.
     *
     * @param id    unique identifier of the accessory
     * @param title display title of the accessory
     * @param price unit price of the accessory
     * @param stock initial quantity available in inventory
     */
    public ConsoleCompatibleAccessory(String id, String title, double price, int stock) {
        super(id, title, price, stock);
        this.compatibleConsoleIds = new ArrayList<>();
    }

    /**
     * Returns the ids of the consoles this accessory is compatible with.
     *
     * @return a copy of the compatible console ids
     */
    public List<String> getCompatibleConsoleIds() {
        return new ArrayList<>(compatibleConsoleIds);
    }

    /**
     * Registers a console as compatible with this accessory, if it is not
     * already registered.
     *
     * @param consoleId the id of the console to add
     */
    public void addCompatibleConsole(String consoleId) {
        if (!compatibleConsoleIds.contains(consoleId)) {
            compatibleConsoleIds.add(consoleId);
        }
    }

    /**
     * Checks whether this accessory is compatible with the given console.
     *
     * @param consoleId the id of the console to check
     * @return true if the console is registered as compatible, false otherwise
     */
    public boolean isCompatibleWith(String consoleId) {
        return compatibleConsoleIds.contains(consoleId);
    }
}
