package com.gamezone.model;

import java.time.LocalDate;

/**
 * Represents a promotion that applies a percentage discount only to the
 * products of a specific target category within a sale (e.g. only video
 * games, or only consoles).
 */
public class CategoryDiscount extends Promotion {

    private double percentage;
    private String targetCategory;

    /**
     * Creates a new CategoryDiscount promotion.
     *
     * @param id             unique identifier of the promotion
     * @param name           display name of the promotion
     * @param startDate      date on which the promotion becomes valid
     * @param endDate        date on which the promotion stops being valid
     * @param percentage     discount percentage to apply (between 0 and 100)
     * @param targetCategory category the discount applies to ("VIDEOGAME" or "CONSOLE")
     */
    public CategoryDiscount(String id, String name, LocalDate startDate, LocalDate endDate,
                            double percentage, String targetCategory) {
        super(id, name, startDate, endDate);
        this.percentage = percentage;
        this.targetCategory = targetCategory;
    }

    public double getPercentage() {
        return percentage;
    }

    public void setPercentage(double percentage) {
        this.percentage = percentage;
    }

    public String getTargetCategory() {
        return targetCategory;
    }

    public void setTargetCategory(String targetCategory) {
        this.targetCategory = targetCategory;
    }

    /**
     * Determines whether the given product belongs to this promotion's
     * target category.
     *
     * @param product the product to check
     * @return true if the product matches the target category
     */
    private boolean matchesCategory(Product product) {
        if ("VIDEOGAME".equalsIgnoreCase(targetCategory)) {
            return product instanceof VideoGame;
        }
        if ("CONSOLE".equalsIgnoreCase(targetCategory)) {
            return product instanceof Console;
        }
        return false;
    }

    @Override
    public double calculateDiscount(Sale sale) {
        double categorySubtotal = 0.0;
        for (Product product : sale.getProducts()) {
            if (matchesCategory(product)) {
                categorySubtotal += product.getPrice();
            }
        }
        return categorySubtotal * (percentage / 100.0);
    }

    @Override
    public String toString() {
        return "CategoryDiscount{" + super.toString() +
                ", percentage=" + percentage + ", targetCategory='" + targetCategory + "'}";
    }
}
