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
            throw new IllegalArgumentException("El identificador de la garantía no puede estar vacío.");
        }
        if (product == null) {
            throw new IllegalArgumentException("La garantía debe referirse a un producto existente.");
        }
        if (sale == null) {
            throw new IllegalArgumentException("La garantía debe referirse a una venta existente.");
        }
        if (startDate == null) {
            throw new IllegalArgumentException("La fecha de inicio de la garantía es obligatoria.");
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


    /**
     * Determines whether this warranty is still active on the given date.
     *
     * @param date the date to check
     * @return true if date is within the [startDate, endDate] range
     */
    public boolean isActive(LocalDate date) {
        return !date.isBefore(startDate) && !date.isAfter(endDate);
    }

    /**
     * Generates a text certificate describing this warranty: its id, type,
     * covered product (id and title), the id of the sale it belongs to,
     * coverage dates and additional cost.
     *
     * @return the certificate of the warranty as multi-line text
     */
    public String generateWarrantyCertificate() {
        StringBuilder sb = new StringBuilder();
        sb.append("===== GameZone Unicesar - Certificado de garantía =====\n");
        sb.append("Garantía: ").append(id).append("\n");
        sb.append("Tipo: ").append(getWarrantyType()).append("\n");
        sb.append("Producto: ").append(product.getId())
                .append(" - ").append(product.getTitle()).append("\n");
        sb.append("Venta: ").append(sale.getId()).append("\n");
        sb.append("Fecha de inicio: ").append(startDate).append("\n");
        sb.append("Fecha de fin: ").append(endDate).append("\n");
        sb.append("Costo adicional: ").append(formatAmount(getAdditionalCost())).append("\n");
        sb.append("========================================================");
        return sb.toString();
    }

    private String formatAmount(double amount) {
        return String.format(java.util.Locale.US, "$%,.2f", amount);
    }

    public String getId() {
        return id;
    }

    public Product getProduct() {
        return product;
    }

    public Sale getSale() {
        return sale;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }


}