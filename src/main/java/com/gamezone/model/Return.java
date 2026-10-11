package com.gamezone.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Represents the return of one or more items (products and accessories)
 * from a previously registered sale. A return does not need to be total:
 * the customer may return only some of the items included in the original
 * sale.
 */
public class Return {

    private String id;
    private LocalDate date;
    private Sale originalSale;
    private List<Product> returnedProducts;
    private List<Accessory> returnedAccessories;
    private String reason;
    private double refundAmount;

    /**
     * Creates a new Return referencing the original sale and the specific
     * products being returned.
     *
     * @param id               unique identifier of the return
     * @param date             date on which the return is registered
     * @param originalSale     sale the returned products belong to
     * @param returnedProducts products being returned
     * @param reason           reason given by the customer for the return
     * @throws IllegalArgumentException if the id or reason is blank, the date
     *                                  or original sale is null, or there are
     *                                  no returned products
     */
    public Return(String id, LocalDate date, Sale originalSale,
                  List<Product> returnedProducts, String reason) {
        this(id, date, originalSale, returnedProducts, new ArrayList<>(), reason);
    }

    /**
     * Creates a new Return referencing the original sale and the specific
     * products and accessories being returned.
     *
     * @param id                  unique identifier of the return
     * @param date                date on which the return is registered
     * @param originalSale        sale the returned items belong to
     * @param returnedProducts    products being returned (may be empty)
     * @param returnedAccessories accessories being returned (may be empty)
     * @param reason              reason given by the customer for the return
     * @throws IllegalArgumentException if the id or reason is blank, the date
     *                                  or original sale is null, or there are
     *                                  no returned products or accessories
     */
    public Return(String id, LocalDate date, Sale originalSale, List<Product> returnedProducts,
                  List<Accessory> returnedAccessories, String reason) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Return id cannot be empty.");
        }
        if (date == null) {
            throw new IllegalArgumentException("Return date cannot be null.");
        }
        if (originalSale == null) {
            throw new IllegalArgumentException("A return must reference an existing sale.");
        }
        List<Product> products = returnedProducts == null ? new ArrayList<>() : returnedProducts;
        List<Accessory> accessories = returnedAccessories == null ? new ArrayList<>() : returnedAccessories;
        if (products.isEmpty() && accessories.isEmpty()) {
            throw new IllegalArgumentException("A return must include at least one product or accessory.");
        }
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("The return reason cannot be empty.");
        }
        this.id = id;
        this.date = date;
        this.originalSale = originalSale;
        this.returnedProducts = new ArrayList<>(products);
        this.returnedAccessories = new ArrayList<>(accessories);
        this.reason = reason;
        calculateRefundAmount();
    }

    public String getId() {
        return id;
    }

    public LocalDate getDate() {
        return date;
    }

    public Sale getOriginalSale() {
        return originalSale;
    }

    public List<Product> getReturnedProducts() {
        return new ArrayList<>(returnedProducts);
    }

    /**
     * Returns a copy of the accessories returned in this return.
     *
     * @return the returned accessories (empty if none)
     */
    public List<Accessory> getReturnedAccessories() {
        return new ArrayList<>(returnedAccessories);
    }

    public String getReason() {
        return reason;
    }

    public double getRefundAmount() {
        return refundAmount;
    }

    /**
     * Calculates the refund amount by summing the price of every returned
     * product and accessory, stores it in the refundAmount attribute, and
     * returns it.
     *
     * @return the calculated refund amount
     */
    public double calculateRefundAmount() {
        double total = 0.0;
        for (Product product : returnedProducts) {
            total += product.getPrice();
        }
        for (Accessory accessory : returnedAccessories) {
            total += accessory.getPrice();
        }
        this.refundAmount = total;
        return this.refundAmount;
    }

    /**
     * Generates a text receipt describing this return: its id and date, the
     * original sale it refers to, each returned product and accessory with its
     * price, the reason, and the refunded amount.
     *
     * @return the receipt of the return as multi-line text
     */
    public String generateReturnReceipt() {
        StringBuilder sb = new StringBuilder();
        sb.append("===== GameZone Unicesar - Return Receipt =====\n");
        sb.append("Return ID: ").append(id).append("\n");
        sb.append("Date: ").append(date).append("\n");
        sb.append("Original sale: ").append(originalSale.getId())
                .append(" (").append(originalSale.getDate()).append(")\n");
        sb.append("Returned items:\n");
        for (Product product : returnedProducts) {
            sb.append("  - ").append(product.getTitle())
                    .append(": ").append(formatAmount(product.getPrice())).append("\n");
        }
        for (Accessory accessory : returnedAccessories) {
            sb.append("  - ").append(accessory.getTitle())
                    .append(" (accessory): ").append(formatAmount(accessory.getPrice())).append("\n");
        }
        sb.append("Reason: ").append(reason).append("\n");
        sb.append("Refund amount: ").append(formatAmount(refundAmount)).append("\n");
        sb.append("==============================================");
        return sb.toString();
    }

    private String formatAmount(double amount) {
        return String.format(Locale.US, "$%,.2f", amount);
    }

    @Override
    public String toString() {
        return "Return{id='" + id + "', date=" + date +
                ", reason='" + reason + "', refundAmount=" + refundAmount + "}";
    }
}