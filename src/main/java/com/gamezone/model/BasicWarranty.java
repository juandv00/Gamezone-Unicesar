package com.gamezone.model;

import java.time.LocalDate;

/**
 * Represents a basic warranty automatically granted to consoles at the
 * moment of sale. It covers factory defects for 6 months and has no
 * additional cost for the customer.
 */
public class BasicWarranty extends Warranty {

    /**
     * Creates a new BasicWarranty for the given product and sale.
     *
     * @param id        unique identifier of the warranty
     * @param product   product covered by this warranty
     * @param sale      sale in which the product was purchased
     * @param startDate date on which the warranty coverage begins
     */
    public BasicWarranty(String id, Product product, Sale sale, LocalDate startDate) {
        super(id, product, sale, startDate);
    }

    @Override
    public int getDurationInMonths() {
        return 6;
    }

    @Override
    public String getWarrantyType() {
        return "Basic Warranty";
    }

    @Override
    public double getAdditionalCost() {
        return 0.0;
    }
}
