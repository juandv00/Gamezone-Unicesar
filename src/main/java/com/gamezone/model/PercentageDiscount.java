package com.gamezone.model;
import java.time.LocalDate;

/**
 * Represents a promotion that applies a flat percentage discount
 * over the total amount of a sale.
 */
public class PercentageDiscount extends Promotion {

    private double percentage;

    /**
     * Creates a new PercentageDiscount promotion.
     *
     * @param id         unique identifier of the promotion
     * @param name       display name of the promotion
     * @param startDate  date on which the promotion becomes valid
     * @param endDate    date on which the promotion stops being valid
     * @param percentage discount percentage to apply (between 0 and 100)
     */
    public PercentageDiscount(String id, String name, LocalDate startDate, LocalDate endDate,
                              double percentage) {
        super(id, name, startDate, endDate);
        this.percentage = percentage;
    }

    public double getPercentage() {
        return percentage;
    }

    public void setPercentage(double percentage) {
        this.percentage = percentage;
    }

    @Override
    public double calculateDiscount(Sale sale) {
        return sale.calculateTotal() * (percentage / 100.0);
    }

    @Override
    public String toString() {
        return "PercentageDiscount{" + super.toString() + ", percentage=" + percentage + "}";
    }
}