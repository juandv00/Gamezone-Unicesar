package com.gamezone.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a game controller accessory sold at GameZone Unicesar.
 * A controller is a specific type of Accessory characterized by its
 * connection type and the list of consoles it is compatible with.
 */
public class Controller extends Accessory {

    /**
     * Connection type of a controller: either wireless or wired.
     */
    public enum ConnectionType {
        WIRELESS,
        WIRED
    }

    private ConnectionType connectionType;
    private final List<String> compatibleConsoleIds;

    /**
     * Creates a new Controller with the given common and particular attributes.
     * The controller starts with no registered console compatibilities.
     *
     * @param id             unique identifier of the accessory
     * @param title          display title of the accessory
     * @param price          unit price of the accessory
     * @param stock          initial quantity available in inventory
     * @param connectionType connection type of the controller (wireless or wired)
     */
    public Controller(String id, String title, double price, int stock,
                      ConnectionType connectionType) {
        super(id, title, price, stock);
        this.connectionType = connectionType;
        this.compatibleConsoleIds = new ArrayList<>();
    }

    public ConnectionType getConnectionType() {
        return connectionType;
    }

    public void setConnectionType(ConnectionType connectionType) {
        this.connectionType = connectionType;
    }

    /**
     * Returns the ids of the consoles this controller is compatible with.
     *
     * @return an unmodifiable-safe copy of the compatible console ids
     */
    public List<String> getCompatibleConsoleIds() {
        return new ArrayList<>(compatibleConsoleIds);
    }

    /**
     * Registers a console as compatible with this controller, if it is
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
     * Checks whether this controller is compatible with the given console.
     *
     * @param consoleId the id of the console to check
     * @return true if the console is registered as compatible, false otherwise
     */
    public boolean isCompatibleWith(String consoleId) {
        return compatibleConsoleIds.contains(consoleId);
    }

    @Override
    public String getType() {
        return "Controller";
    }

    @Override
    public String getDescription() {
        return getTitle() + " - Connection: " + connectionType +
                ", Compatible consoles: " + compatibleConsoleIds.size() +
                ", Price: $" + getPrice() +
                ", Stock: " + getStock();
    }

    @Override
    public String toString() {
        return "Controller{" + getDescription() + "}";
    }

}