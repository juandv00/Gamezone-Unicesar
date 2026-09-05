package com.gamezone.model;

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
     */
    public Product(String id, String title, double price, int stock) {
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
     * Returns a full description of the product, combining common
     * attributes with the particular characteristics of each subclass.
     *
     * @return a descriptive string of the product
     */
    public abstract String getDescription();
}