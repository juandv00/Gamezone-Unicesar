package com.gamezone.model;

import java.util.Objects;

/**
 * Represents a generic accessory sold at GameZone Unicesar.
 * This class defines the attributes and behavior common to all accessories,
 * and must be extended by specific accessory types (e.g. Controller, Cable, Memory).
 * <p>
 * Accessory forms an independent hierarchy from {@link Product}: although both
 * share common attributes (id, title, price, stock), accessories have their own
 * particular characteristics and compatibility rules that do not apply to
 * video games or consoles. See docs/accessory-analysis.md for the full rationale.
 */
public abstract class Accessory {

    private String id;
    private String title;
    private double price;
    private int stock;

    /**
     * Creates a new Accessory with the given common attributes.
     *
     * @param id    unique identifier of the accessory
     * @param title display title of the accessory
     * @param price unit price of the accessory
     * @param stock initial quantity available in inventory
     */
    public Accessory(String id, String title, double price, int stock) {
        if (price < 0) {
            throw new IllegalArgumentException("Price cannot be negative");
        }
        if (stock < 0) {
            throw new IllegalArgumentException("Stock cannot be negative");
        }
        this.id = id;
        this.title = title;
        this.price = price;
        this.stock = stock;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    /**
     * Returns the type name of this accessory (e.g. "Controller", "Cable", "Memory"),
     * used for filtering and reporting operations.
     *
     * @return the accessory type name
     */
    public abstract String getType();

    /**
     * Returns a full description of the accessory, combining common
     * attributes with the particular characteristics of each subclass.
     *
     * @return a descriptive string of the accessory
     */
    public abstract String getDescription();

    @Override
    public String toString() {
        return "Accessory{id='" + id + "', title='" + title +
                "', price=" + price + ", stock=" + stock + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Accessory)) return false;
        Accessory accessory = (Accessory) o;
        return Objects.equals(id, accessory.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}
