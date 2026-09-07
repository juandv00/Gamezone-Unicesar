package com.gamezone.model;

public abstract class Accesory {

    private String title;
    private double price;
    private int stock;
    private String identifier;

    public Accesory(String title, double price, int stock, String identifier) {
        this.title = title;
        this.price = price;
        this.stock = stock;
        this.identifier = identifier;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String identifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setStock(int stock) { this.stock= stock;}

    @Override

    public String toString() { return "Accesory{" + "tittle=" + title  + ", price=" + price + ", stock=" + stock + "identifier=" + identifier;
    }

}
