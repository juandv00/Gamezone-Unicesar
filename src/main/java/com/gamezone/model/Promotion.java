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

}
