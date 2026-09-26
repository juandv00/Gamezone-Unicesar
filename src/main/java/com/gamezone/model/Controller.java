package com.gamezone.model;

import java.util.ArrayList;
import java.util.List;


public class Controller extends Accessory {


    public enum ConnectionType {
        WIRELESS,
        WIRED
    }

    private ConnectionType connectionType;
    private final List<String> compatibleConsoleIds;

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



    public List<String> getCompatibleConsoleIds() {
        return new ArrayList<>(compatibleConsoleIds);
    }

    public void addCompatibleConsole(String consoleId) {
        if (!compatibleConsoleIds.contains(consoleId)) {
            compatibleConsoleIds.add(consoleId);
        }
    }

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