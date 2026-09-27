package com.gamezone.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Represents a sale transaction at GameZone Unicesar.
 * A sale involves a client, a seller, a list of products purchased and,
 * optionally, a list of accessories purchased in the same transaction.
 * It is responsible for calculating its own subtotal based on both lists,
 * and it keeps the promotion discount applied to it (if any), so it can
 * report its final total and generate a receipt with the full breakdown.
 */
public class Sale {

    private String id;
    private LocalDate date;
    private Client client;
    private Seller seller;
    private List<Product> products;
    private List<Accessory> accessories;
    private String appliedPromotionName;
    private double discountAmount;

    public Sale(Client client, Seller seller, List<Product> products) {
        this(client, seller, products, new ArrayList<>(), LocalDate.now());
    }

    public Sale(Client client, Seller seller, List<Product> products, LocalDate date) {
        this(client, seller, products, new ArrayList<>(), date);
    }

    public Sale(Client client, Seller seller, List<Product> products, List<Accessory> accessories) {
        this(client, seller, products, accessories, LocalDate.now());
    }

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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Sale id cannot be empty.");
        }
        this.id = id;
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

    public List<Accessory> getAccessories() {
        return new ArrayList<>(accessories);
    }

    public String getAppliedPromotionName() {
        return appliedPromotionName;
    }

    public void setAppliedPromotionName(String appliedPromotionName) {
        this.appliedPromotionName = appliedPromotionName;
    }

    public double getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(double discountAmount) {
        if (discountAmount < 0) {
            throw new IllegalArgumentException("Discount amount cannot be negative.");
        }
        this.discountAmount = discountAmount;
    }

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

    public double calculateFinalTotal() {
        return calculateTotal() - discountAmount;
    }

    public boolean hasDiscount() {
        return appliedPromotionName != null && discountAmount > 0;
    }

    /**
     * Determines whether this sale is still within the 30 calendar day
     * window in which a return may be registered against it.
     *
     * @return true if the current date is within 30 days of the sale date
     */
    public boolean canBeReturned() {
        LocalDate deadline = date.plusDays(30);
        return !LocalDate.now().isAfter(deadline);
    }

    public String generateReceipt() {
        StringBuilder receipt = new StringBuilder();
        receipt.append("===== GameZone Unicesar - Receipt =====\n");
        if (id != null) {
            receipt.append("Sale ID: ").append(id).append("\n");
        }
        receipt.append("Date: ").append(date).append("\n");
        receipt.append("Client: ").append(client.getName()).append("\n");
        receipt.append("Seller: ").append(seller.getName()).append("\n");
        receipt.append("Items:\n");
        for (Product product : products) {
            receipt.append("  - ").append(product.getTitle())
                    .append(": ").append(formatAmount(product.getPrice())).append("\n");
        }
        for (Accessory accessory : accessories) {
            receipt.append("  - ").append(accessory.getTitle())
                    .append(" (accessory): ").append(formatAmount(accessory.getPrice())).append("\n");
        }
        receipt.append("Subtotal: ").append(formatAmount(calculateTotal())).append("\n");
        if (hasDiscount()) {
            receipt.append("Discount (").append(appliedPromotionName).append("): -")
                    .append(formatAmount(discountAmount)).append("\n");
        } else {
            receipt.append("Discount: none\n");
        }
        receipt.append("Total: ").append(formatAmount(calculateFinalTotal())).append("\n");
        receipt.append("=======================================");
        return receipt.toString();
    }

    private String formatAmount(double amount) {
        return String.format(Locale.US, "$%,.2f", amount);
    }

    @Override
    public String toString() {
        String discount = hasDiscount()
                ? ", discount=$" + discountAmount + " (" + appliedPromotionName + ")"
                : "";
        String idText = id != null ? "id=" + id + ", " : "";
        return "Sale{" + idText + "date=" + date + ", client=" + client.getName() +
                ", seller=" + seller.getName() + discount +
                ", total=$" + calculateFinalTotal() + "}";
    }
}