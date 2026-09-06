package com.gamezone.model;

import java.time.LocalDate;
import java.util.List;

/**
 * Represents a sale transaction at GameZone Unicesar.
 * A sale involves a client, a seller, and a list of products purchased,
 * and is responsible for calculating its own total based on those products.
 */
public class Sale {

    private LocalDate date;
    private Client client;
    private Seller seller;
    private List<Product> products;

    /**
     * Creates a new Sale with the current date, the given client, seller,
     * and the list of products purchased.
     *
     * @param client   the client who made the purchase
     * @param seller   the seller who attended the sale
     * @param products the list of products included in the sale
     */
    public Sale(Client client, Seller seller, List<Product> products) {
        this.date = LocalDate.now();
        this.client = client;
        this.seller = seller;
        this.products = products;
    }

    /**
     * Reconstructs a Sale with a specific date, used when loading previously
     * stored sales from persistence.
     *
     * @param client   the client who made the purchase
     * @param seller   the seller who attended the sale
     * @param products the list of products included in the sale
     * @param date     the original date of the sale
     */
    public Sale(Client client, Seller seller, List<Product> products, LocalDate date) {
        this.date = date;
        this.client = client;
        this.seller = seller;
        this.products = products;
    }

    public LocalDate getDate() {
        return date;
    }

    public Client getClient() {
        return client;
    }

    public Seller getSeller() {
        return seller;
    }

    public List<Product> getProducts() {
        return products;
    }

    /**
     * Calculates the total price of the sale by summing the price of
     * every product included in it.
     *
     * @return the total amount of the sale
     */
    public double calculateTotal() {
        double total = 0;
        for (Product product : products) {
            total += product.getPrice();
        }
        return total;
    }
}