package com.gamezone.model;

import java.time.LocalDate;

/**
 * Represents an extended warranty that can be optionally added to a
 * console at the moment of sale. It covers factory defects and accidental
 * damage for 12 months, at an additional cost of 10% of the product price.
 */
public class ExtendedWarranty extends Warranty {

    /**
     * Creates a new ExtendedWarranty for the given product and sale.
     *
     * @param id        unique identifier of the warranty
     * @param product   product covered by this warranty
     * @param sale      sale in which the product was purchased
     * @param startDate date on which the warranty coverage begins
     */

    public ExtendedWarranty(String id, Product product, Sale sale, LocalDate startDate) {
        super(id, product, sale, startDate);
    }

    @Override
    public int getDurationInMonths() {
        return 12;
    }

    @Override
    public String getWarrantyType() {
        return "Extended Warranty";
    }

    @Override
    public double getAdditionalCost() {
        return getProduct().getPrice() * 0.10;
    }
}