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
    private double warrantyCost;

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

    /**
     * Returns the unique identifier of this sale.
     *
     * @return the sale id, or null if the sale has not been assigned one yet
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the unique identifier of this sale. The id is assigned by the
     * service layer when the sale is registered or loaded.
     *
     * @param id the sale id
     * @throws IllegalArgumentException if id is null or blank
     */
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
     * Returns the name of the promotion applied to this sale.
     *
     * @return the name of the applied promotion, or null if no promotion
     *         was applied
     */
    public String getAppliedPromotionName() {
        return appliedPromotionName;
    }

    /**
     * Sets the name of the promotion applied to this sale.
     *
     * @param appliedPromotionName the name of the applied promotion, or null
     *                             if no promotion was applied
     */
    public void setAppliedPromotionName(String appliedPromotionName) {
        this.appliedPromotionName = appliedPromotionName;
    }

    /**
     * Returns the discount amount, in pesos, applied to this sale.
     *
     * @return the discount amount, or 0 if no promotion was applied
     */
    public double getDiscountAmount() {
        return discountAmount;
    }

    /**
     * Sets the discount amount, in pesos, applied to this sale.
     *
     * @param discountAmount the discount amount to apply
     * @throws IllegalArgumentException if discountAmount is negative
     */
    public void setDiscountAmount(double discountAmount) {
        if (discountAmount < 0) {
            throw new IllegalArgumentException("Discount amount cannot be negative.");
        }
        this.discountAmount = discountAmount;
    }

    /**
     * Calculates the subtotal of the sale by summing the price of every
     * product and every accessory included in it, before any promotion
     * discount is applied.
     *
     * @return the subtotal of the sale
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

    /**
     * Returns the additional cost, in pesos, of the extended warranties
     * purchased with this sale.
     *
     * @return the total cost of the extended warranties, or 0 if none
     */
    public double getWarrantyCost() {
        return warrantyCost;
    }

    /**
     * Sets the additional cost, in pesos, of the extended warranties
     * purchased with this sale. This cost is added to the final total and is
     * not affected by promotion discounts.
     *
     * @param warrantyCost the total cost of the extended warranties
     * @throws IllegalArgumentException if warrantyCost is negative
     */
    public void setWarrantyCost(double warrantyCost) {
        if (warrantyCost < 0) {
            throw new IllegalArgumentException("Warranty cost cannot be negative.");
        }
        this.warrantyCost = warrantyCost;
    }

    /**
     * Calculates the final total of the sale: the subtotal minus the
     * promotion discount applied to it, plus the cost of the extended
     * warranties purchased with it.
     *
     * @return the final amount to be paid for the sale
     */
    public double calculateFinalTotal() {
        return calculateTotal() - discountAmount + warrantyCost;
    }

    /**
     * Indicates whether a promotion discount was applied to this sale.
     *
     * @return true if a promotion with a positive discount was applied
     */
    public boolean hasDiscount() {
        return appliedPromotionName != null && discountAmount > 0;
    }

    /**
     * Indicates whether this sale can still receive returns: a return can
     * only be registered within the 30 calendar days that follow the sale
     * date (the 30th day included).
     *
     * @return true if today is at most 30 days after the sale date
     */
    public boolean canBeReturned() {
        LocalDate deadline = date.plusDays(30);
        return !LocalDate.now().isAfter(deadline);
    }

    /**
     * Generates a text receipt for the sale, listing every product and
     * accessory with its price, followed by the subtotal, the discount
     * applied (with the name of the promotion), the cost of the extended
     * warranties (if any) and the final total.
     *
     * @return the receipt of the sale as multi-line text
     */
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
        if (warrantyCost > 0) {
            receipt.append("Extended warranties: +").append(formatAmount(warrantyCost)).append("\n");
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
        String warranty = warrantyCost > 0 ? ", warranties=$" + warrantyCost : "";
        String idText = id != null ? "id=" + id + ", " : "";
        return "Sale{" + idText + "date=" + date + ", client=" + client.getName() +
                ", seller=" + seller.getName() + discount + warranty +
                ", total=$" + calculateFinalTotal() + "}";
    }
}