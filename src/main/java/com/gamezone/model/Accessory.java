package com.gamezone.model;

import java.util.Objects;

public abstract class Accessory {

    private String id;
    private String title;
    private double price;
    private int stock;

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


    public abstract String getType();

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
        Accessory accesory = (Accessory) o;
        return Objects.equals(id, accesory.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}