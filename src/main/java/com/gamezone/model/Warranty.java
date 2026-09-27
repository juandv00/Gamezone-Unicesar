package com.gamezone.model;

import java.time.LocalDate;

/**
 * Represents a generic warranty granted to a product sold at GameZone
 * Unicesar. This class defines the attributes and behavior common to all
 * warranty types, and must be extended by specific warranty strategies
 * (e.g. BasicWarranty, ExtendedWarranty).
 */
public abstract class Warranty {

    private String id;
    private Product product;
    private Sale sale;
    private LocalDate startDate;
    private LocalDate endDate;

    /**
     * Creates a new Warranty with the given identifier, associated product,
     * associated sale, and start date. The end date is calculated
     * automatically based on the duration defined by the concrete subclass.
     *
     * @param id        unique identifier of the warranty
     * @param product   product covered by this warranty
     * @param sale      sale in which the product was purchased
     * @param startDate date on which the warranty coverage begins
     * @throws IllegalArgumentException if the id is blank, or product, sale
     *                                  or startDate is null
     */
    public Warranty(String id, Product product, Sale sale, LocalDate startDate) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Warranty id cannot be empty.");
        }
        if (product == null) {
            throw new IllegalArgumentException("A warranty must reference an existing product.");
        }
        if (sale == null) {
            throw new IllegalArgumentException("A warranty must reference an existing sale.");
        }
        if (startDate == null) {
            throw new IllegalArgumentException("Warranty start date cannot be null.");
        }
        this.id = id;
        this.product = product;
        this.sale = sale;
        this.startDate = startDate;
        this.endDate = startDate.plusMonths(getDurationInMonths());
    }

    public abstract int getDurationInMonths();

    public abstract String getWarrantyType();

    public abstract double getAdditionalCost();
}