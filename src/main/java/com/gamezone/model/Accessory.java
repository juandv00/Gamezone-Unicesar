package com.gamezone.model;

import com.gamezone.exceptions.InvalidDataException;
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
     * @throws InvalidDataException if the price or the stock is negative
     */
    public Accessory(String id, String title, double price, int stock) {
        if (price < 0) {
            throw new InvalidDataException("precio", "no puede ser negativo.");
        }
        if (stock < 0) {
            throw new InvalidDataException("stock", "no puede ser negativo.");
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
     * Indicates whether this accessory has at least the given number of units
     * in stock.
     *
     * @param amount the number of units requested
     * @return true if the stock covers the requested amount
     */
    public boolean hasEnoughStock(int amount) {
        return stock >= amount;
    }

    /**
     * Removes the given number of units from the stock of this accessory. The
     * caller is responsible for checking first that there is enough stock.
     *
     * @param amount the number of units to remove
     */
    public void decreaseStock(int amount) {
        stock -= amount;
    }

    /**
     * Adds the given number of units to the stock of this accessory, for
     * example when a sold unit is returned.
     *
     * @param amount the number of units to add
     */
    public void increaseStock(int amount) {
        stock += amount;
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

    /**
     * Returns the category of accessories used by the category promotions.
     * Every accessory type belongs to the same category.
     *
     * @return "ACCESSORY"
     */
    public String getCategory() {
        return "ACCESSORY";
    }

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
