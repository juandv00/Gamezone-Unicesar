package com.gamezone.model;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Represents a generic promotional campaign at GameZone Unicesar.
 * This class defines the attributes and behavior common to all promotions,
 * and must be extended by specific promotion strategies (e.g. PercentageDiscount,
 * CategoryDiscount, BulkPurchaseDiscount).
 */
public abstract class Promotion {

    private String id;
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;

    /**
     * Creates a new Promotion with the given common attributes.
     *
     * @param id        unique identifier of the promotion
     * @param name      display name of the promotion
     * @param startDate date on which the promotion becomes valid
     * @param endDate   date on which the promotion stops being valid
     */
    public Promotion(String id, String name, LocalDate startDate, LocalDate endDate) {
        this.id = id;
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    /**
     * Determines whether this promotion is valid on the given date.
     *
     * @param date the date to check
     * @return true if date is within the [startDate, endDate] range
     */
    public boolean isActive(LocalDate date) {
        return !date.isBefore(startDate) && !date.isAfter(endDate);
    }

    /**
     * Calculates the monetary discount that this promotion would grant
     * to the given sale. Each concrete subclass defines its own
     * calculation strategy.
     *
     * @param sale the sale to evaluate
     * @return the discount amount in currency units
     */
    public abstract double calculateDiscount(Sale sale);

    @Override
    public String toString() {
        return "Promotion{id='" + id + "', name='" + name +
                "', startDate=" + startDate + ", endDate=" + endDate + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Promotion)) return false;
        Promotion promotion = (Promotion) o;
        return Objects.equals(id, promotion.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }



}
