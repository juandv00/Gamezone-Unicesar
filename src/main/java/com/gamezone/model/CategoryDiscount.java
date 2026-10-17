package com.gamezone.model;

import java.time.LocalDate;
import java.util.List;

/**
 * Represents a promotion that applies a percentage discount only to the
 * items of a specific target category within a sale (video games, consoles,
 * or accessories).
 */

public class CategoryDiscount extends Promotion {

    private static final List<String> SUPPORTED_CATEGORIES = List.of("VIDEOGAME", "CONSOLE", "ACCESSORY");

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
     * @param targetCategory category the discount applies to ("VIDEOGAME", "CONSOLE" or "ACCESSORY")
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
     * Indicates whether the given category can be the target of a category
     * promotion. This class is the only place that knows the supported
     * categories, so the services ask it instead of repeating the list.
     *
     * @param category the category to check (case is ignored)
     * @return true if the category is VIDEOGAME, CONSOLE, or ACCESSORY
     */
    public static boolean isSupportedCategory(String category) {
        if (category == null) {
            return false;
        }
        for (String supported : SUPPORTED_CATEGORIES) {
            if (supported.equalsIgnoreCase(category)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Calculates the discount on the items of the target category. Each
     * product and accessory reports its own category, so this method only
     * compares it with the target category, without checking any concrete
     * class.
     *
     * @param sale the sale to evaluate
     * @return the discount amount for the items of the target category
     */
    @Override
    public double calculateDiscount(Sale sale) {
        double categorySubtotal = 0.0;
        for (Product product : sale.getProducts()) {
            if (product.getCategory().equalsIgnoreCase(targetCategory)) {
                categorySubtotal += product.getPrice();
            }
        }
        for (Accessory accessory : sale.getAccessories()) {
            if (accessory.getCategory().equalsIgnoreCase(targetCategory)) {
                categorySubtotal += accessory.getPrice();
            }
        }
        return categorySubtotal * (percentage / 100.0);
    }

    @Override
    public String toString() {
        return super.toString() + " | Descuento del " + percentage + "% en la categoría " + targetCategory;
    }
}
