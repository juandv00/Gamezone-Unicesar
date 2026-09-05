package com.gamezone.model;

/**
 * Represents a console product sold at GameZone Unicesar.
 * A console is a specific type of Product characterized by
 * its brand, model, and generation.
 */
public class Console extends Product {

    private String brand;
    private String model;
    private String generation;

    /**
     * Creates a new Console with the given common and particular attributes.
     *
     * @param id         unique identifier of the product
     * @param title      display title of the product
     * @param price      unit price of the product
     * @param stock      initial quantity available in inventory
     * @param brand      manufacturer of the console (e.g. Sony, Microsoft)
     * @param model      commercial model name (e.g. PlayStation 5)
     * @param generation console generation (e.g. 9th generation)
     */
    public Console(String id, String title, double price, int stock,
                   String brand, String model, String generation) {
        super(id, title, price, stock);
        this.brand = brand;
        this.model = model;
        this.generation = generation;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getGeneration() {
        return generation;
    }

    public void setGeneration(String generation) {
        this.generation = generation;
    }

    @Override
    public String getDescription() {
        return getTitle() + " - Brand: " + brand +
                ", Model: " + model +
                ", Generation: " + generation +
                ", Price: $" + getPrice() +
                ", Stock: " + getStock();
    }
}