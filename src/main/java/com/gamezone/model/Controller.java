package com.gamezone.model;

/**
 * Represents a game controller accessory sold at GameZone Unicesar.
 * A controller is a specific type of Accessory characterized by its
 * connection type and the list of consoles it is compatible with.
 */
public class Controller extends ConsoleCompatibleAccessory {

    /**
     * Connection type of a controller: either wireless or wired.
     */
    public enum ConnectionType {
        WIRELESS,
        WIRED
    }

    private ConnectionType connectionType;

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
    }

    public ConnectionType getConnectionType() {
        return connectionType;
    }

    public void setConnectionType(ConnectionType connectionType) {
        this.connectionType = connectionType;
    }

    @Override
    public String getType() {
        return "Controller";
    }

    @Override
    public String getDescription() {
        String connection = connectionType == ConnectionType.WIRELESS ? "inalámbrica" : "cableada";
        return getTitle() + " - Conexión: " + connection +
                ", Consolas compatibles: " + getCompatibleConsoleIds().size() +
                ", Precio: $" + getPrice() +
                ", Stock: " + getStock();
    }

    @Override
    public String toString() {
        return "Controller{" + getDescription() + "}";
    }

}