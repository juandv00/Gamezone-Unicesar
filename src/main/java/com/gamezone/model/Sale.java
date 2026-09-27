package com.gamezone.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a sale transaction at GameZone Unicesar.
 * A sale involves a client, a seller, a list of products purchased and,
 * optionally, a list of accessories purchased in the same transaction.
 * It is responsible for calculating its own total based on both lists.
 */
public class Sale {

    private LocalDate date;
    private Client client;
    private Seller seller;
    private List<Product> products;
    private List<Accessory> accessories;

    /**
     * Creates a new Sale with the current date, the given client, seller,
     * and the list of products purchased. The sale starts with no accessories.
     *
     * @param client   the client who made the purchase
     * @param seller   the seller who attended the sale
     * @param products the list of products included in the sale
     * @throws IllegalArgumentException if products is null
     */
    public Sale(Client client, Seller seller, List<Product> products) {
        this(client, seller, products, new ArrayList<>(), LocalDate.now());
    }

    /**
     * Reconstructs a Sale with a specific date, used when loading previously
     * stored sales from persistence. The sale has no accessories.
     *
     * @param client   the client who made the purchase
     * @param seller   the seller who attended the sale
     * @param products the list of products included in the sale
     * @param date     the original date of the sale
     * @throws IllegalArgumentException if products is null
     */
    public Sale(Client client, Seller seller, List<Product> products, LocalDate date) {
        this(client, seller, products, new ArrayList<>(), date);
    }

    /**
     * Creates a new Sale with the current date that includes both products
     * and accessories sold together in the same transaction.
     *
     * @param client      the client who made the purchase
     * @param seller      the seller who attended the sale
     * @param products    the list of products included in the sale
     * @param accessories the list of accessories included in the sale
     * @throws IllegalArgumentException if products or accessories is null
     */
    public Sale(Client client, Seller seller, List<Product> products, List<Accessory> accessories) {
        this(client, seller, products, accessories, LocalDate.now());
    }

    /**
     * Reconstructs a Sale with a specific date that includes both products
     * and accessories, used when loading previously stored sales from
     * persistence.
     *
     * @param client      the client who made the purchase
     * @param seller      the seller who attended the sale
     * @param products    the list of products included in the sale
     * @param accessories the list of accessories included in the sale
     * @param date        the original date of the sale
     * @throws IllegalArgumentException if products or accessories is null
     */
    public Sale(Client client, Seller seller, List<Product> products,
                List<Accessory> accessories, LocalDate date) {
        if (products == null) {
            throw new IllegalArgumentException("Products list cannot be null.");
        }
        if (accessories == null) {
            throw new IllegalArgumentException("Accessories list cannot be null.");
        }
        this.date = date;
        this.client = client;
        this.seller = seller;
        this.products = products;
        this.accessories = new ArrayList<>(accessories);
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
        return new ArrayList<>(products);
    }

    /**
     * Returns a copy of the accessories included in this sale, so callers
     * cannot modify the sale's internal list.
     *
     * @return a new list with the accessories of the sale (empty if none)
     */
    public List<Accessory> getAccessories() {
        return new ArrayList<>(accessories);
    }

    /**
     * Calculates the total price of the sale by summing the price of
     * every product and every accessory included in it.
     *
     * @return the total amount of the sale
     */
    public double calculateTotal() {
        double total = 0;
        for (Product product : products) {
            total += product.getPrice();
        }
        for (Accessory accessory : accessories) {
            total += accessory.getPrice();
        }
        return total;
    }

    @Override
    public String toString() {
        return "Sale{date=" + date + ", client=" + client.getName() +
                ", seller=" + seller.getName() + ", total=$" + calculateTotal() + "}";
    }
}