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
    private double warrantyRefund;
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
        this(id, date, originalSale, returnedProducts, returnedAccessories, reason, 0.0);
    }

    /**
     * Creates a new Return that also refunds the cost of the warranties
     * cancelled because consoles were returned (the additional cost of the
     * extended warranties; basic warranties have no cost).
     *
     * @param id                  unique identifier of the return
     * @param date                date on which the return is registered
     * @param originalSale        sale the returned items belong to
     * @param returnedProducts    products being returned (may be empty)
     * @param returnedAccessories accessories being returned (may be empty)
     * @param reason              reason given by the customer for the return
     * @param warrantyRefund      amount refunded for cancelled warranties
     * @throws IllegalArgumentException if the id or reason is blank, the date
     *                                  or original sale is null, there are no
     *                                  returned products or accessories, or the
     *                                  warranty refund is negative
     */
    public Return(String id, LocalDate date, Sale originalSale, List<Product> returnedProducts,
                  List<Accessory> returnedAccessories, String reason, double warrantyRefund) {
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
        if (warrantyRefund < 0) {
            throw new IllegalArgumentException("The warranty refund cannot be negative.");
        }
        this.id = id;
        this.date = date;
        this.originalSale = originalSale;
        this.returnedProducts = new ArrayList<>(products);
        this.returnedAccessories = new ArrayList<>(accessories);
        this.reason = reason;
        this.warrantyRefund = warrantyRefund;
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

    /**
     * Returns the amount refunded for the warranties cancelled in this return.
     *
     * @return the warranty refund, or 0 if no extended warranty was cancelled
     */
    public double getWarrantyRefund() {
        return warrantyRefund;
    }

    public double getRefundAmount() {
        return refundAmount;
    }

    /**
     * Calculates the refund amount, stores it in the refundAmount attribute,
     * and returns it. The refund of each returned product or accessory is
     * proportional to the discount of the original sale:
     * {@code price * (1 - discount / subtotal)}, so the customer never gets
     * back more than what was actually paid for the item. The cost of the
     * warranties cancelled because consoles were returned is added on top.
     *
     * @return the calculated refund amount
     */
    public double calculateRefundAmount() {
        double total = 0.0;
        for (Product product : returnedProducts) {
            total += refundFor(product.getPrice());
        }
        for (Accessory accessory : returnedAccessories) {
            total += refundFor(accessory.getPrice());
        }
        total += warrantyRefund;
        this.refundAmount = total;
        return this.refundAmount;
    }

    /**
     * Returns the fraction of the list price that the customer actually paid
     * in the original sale: {@code 1 - discount / subtotal}. If the sale has
     * no subtotal, the factor is 1 (no discount).
     *
     * @return the factor to apply to each list price
     */
    private double paidFactor() {
        double subtotal = originalSale.calculateTotal();
        if (subtotal <= 0) {
            return 1.0;
        }
        return 1.0 - originalSale.getDiscountAmount() / subtotal;
    }

    /**
     * Calculates the refund of one item from its list price, applying the
     * proportional discount of the original sale.
     *
     * @param listPrice the list price of the item
     * @return the amount to refund for that item
     */
    private double refundFor(double listPrice) {
        return listPrice * paidFactor();
    }

    /**
     * Generates a text receipt describing this return: its id and date, the
     * original sale it refers to, each returned product and accessory with its
     * list price, proportional discount, and refunded amount, the reason, and
     * the total refunded.
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
            appendItemLine(sb, product.getTitle(), product.getPrice());
        }
        for (Accessory accessory : returnedAccessories) {
            appendItemLine(sb, accessory.getTitle() + " (accessory)", accessory.getPrice());
        }
        if (warrantyRefund > 0) {
            sb.append("Cancelled extended warranties: +").append(formatAmount(warrantyRefund)).append("\n");
        }
        sb.append("Reason: ").append(reason).append("\n");
        sb.append("Refund amount: ").append(formatAmount(refundAmount)).append("\n");
        sb.append("==============================================");
        return sb.toString();
    }

    /**
     * Appends one receipt line for a returned item, showing its list price,
     * the proportional discount, and the refunded amount.
     *
     * @param sb        the receipt being built
     * @param title     the title of the item
     * @param listPrice the list price of the item
     */
    private void appendItemLine(StringBuilder sb, String title, double listPrice) {
        double refund = refundFor(listPrice);
        sb.append("  - ").append(title)
                .append(": list price ").append(formatAmount(listPrice))
                .append(", discount -").append(formatAmount(listPrice - refund))
                .append(", refund ").append(formatAmount(refund)).append("\n");
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