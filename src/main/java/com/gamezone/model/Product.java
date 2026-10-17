package com.gamezone.model;

import com.gamezone.exceptions.InvalidDataException;
import java.util.Objects;
/**
 * Represents a generic product sold at GameZone Unicesar.
 * This class defines the attributes and behavior common to all products,
 * and must be extended by specific product types (e.g. VideoGame, Console).
 */
public abstract class Product {

    private String id;
    private String title;
    private double price;
    private int stock;

    /**
     * Creates a new Product with the given common attributes.
     *
     * @param id    unique identifier of the product
     * @param title display title of the product
     * @param price unit price of the product
     * @param stock initial quantity available in inventory
     * @throws InvalidDataException if the price or the stock is negative
     */
    public Product(String id, String title, double price, int stock) {
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
     * Indicates whether this product has at least the given number of units
     * in stock.
     *
     * @param amount the number of units requested
     * @return true if the stock covers the requested amount
     */
    public boolean hasEnoughStock(int amount) {
        return stock >= amount;
    }

    /**
     * Removes the given number of units from the stock of this product. The
     * caller is responsible for checking first that there is enough stock.
     *
     * @param amount the number of units to remove
     */
    public void decreaseStock(int amount) {
        stock -= amount;
    }

    /**
     * Adds the given number of units to the stock of this product, for
     * example when a sold unit is returned.
     *
     * @param amount the number of units to add
     */
    public void increaseStock(int amount) {
        stock += amount;
    }

    /**
     * Returns a full description of the product, combining common
     * attributes with the particular characteristics of each subclass.
     *
     * @return a descriptive string of the product
     */
    public abstract String getDescription();

    @Override
    public String toString() {
        return "Product{id='" + id + "', title='" + title +
                "', price=" + price + ", stock=" + stock + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Product)) return false;
        Product product = (Product) o;
        return Objects.equals(id, product.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}