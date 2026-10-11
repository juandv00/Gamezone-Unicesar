package com.gamezone.model;

import java.time.LocalDate;

/**
 * Represents a promotion that applies a percentage discount over the total
 * amount of a sale, but only when the sale includes at least a minimum
 * quantity of products.
 */
public class BulkPurchaseDiscount extends Promotion {

    private int minimumQuantity;
    private double percentage;

    /**
     * Creates a new BulkPurchaseDiscount promotion.
     *
     * @param id              unique identifier of the promotion
     * @param name            display name of the promotion
     * @param startDate       date on which the promotion becomes valid
     * @param endDate         date on which the promotion stops being valid
     * @param minimumQuantity minimum number of products required in the sale
     * @param percentage      discount percentage to apply (between 0 and 100)
     */
    public BulkPurchaseDiscount(String id, String name, LocalDate startDate, LocalDate endDate,
                                int minimumQuantity, double percentage) {
        super(id, name, startDate, endDate);
        this.minimumQuantity = minimumQuantity;
        this.percentage = percentage;
    }

    public int getMinimumQuantity() {
        return minimumQuantity;
    }

    public void setMinimumQuantity(int minimumQuantity) {
        this.minimumQuantity = minimumQuantity;
    }

    public double getPercentage() {
        return percentage;
    }

    public void setPercentage(double percentage) {
        this.percentage = percentage;
    }

    @Override
    public double calculateDiscount(Sale sale) {
        if (sale.getProducts().size() >= minimumQuantity) {
            return sale.calculateTotal() * (percentage / 100.0);
        }
        return 0.0;
    }

    @Override
    public String toString() {
        return "BulkPurchaseDiscount{" + super.toString() +
                ", minimumQuantity=" + minimumQuantity + ", percentage=" + percentage + "}";
    }
}
