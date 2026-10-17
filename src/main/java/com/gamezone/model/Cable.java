package com.gamezone.model;

import com.gamezone.exceptions.InvalidDataException;

/**
 * Represents a cable accessory sold at GameZone Unicesar.
 * A cable is a specific type of Accessory characterized by its
 * length in meters and its connector type.
 * <p>
 * Cables do not register console compatibility, since a cable's
 * connector type (e.g. HDMI, USB) is a generic standard rather than
 * a console-specific characteristic.
 */
public class Cable extends Accessory {

    private double lengthInMeters;
    private String connectorType;

    /**
     * Creates a new Cable with the given common and particular attributes.
     *
     * @param id             unique identifier of the accessory
     * @param title          display title of the accessory
     * @param price          unit price of the accessory
     * @param stock          initial quantity available in inventory
     * @param lengthInMeters length of the cable, in meters
     * @param connectorType  connector type of the cable (e.g. HDMI, USB, optical)
     * @throws InvalidDataException if the length is not positive, or the price
     *                              or the stock is negative
     */
    public Cable(String id, String title, double price, int stock,
                 double lengthInMeters, String connectorType) {
        super(id, title, price, stock);
        if (lengthInMeters <= 0) {
            throw new InvalidDataException("longitud", "debe ser mayor que cero.");
        }
        this.lengthInMeters = lengthInMeters;
        this.connectorType = connectorType;
    }

    public double getLengthInMeters() {
        return lengthInMeters;
    }

    public void setLengthInMeters(double lengthInMeters) {
        this.lengthInMeters = lengthInMeters;
    }

    public String getConnectorType() {
        return connectorType;
    }

    public void setConnectorType(String connectorType) {
        this.connectorType = connectorType;
    }

    @Override
    public String getType() {
        return "Cable";
    }

    @Override
    public String getDescription() {
        return getTitle() + " - Longitud: " + lengthInMeters + "m" +
                ", Conector: " + connectorType +
                ", Precio: $" + getPrice() +
                ", Stock: " + getStock();
    }

    @Override
    public String toString() {
        return "Cable{" + getDescription() + "}";
    }

}
