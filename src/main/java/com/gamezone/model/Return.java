package com.gamezone.model;

import com.gamezone.util.CurrencyFormatter;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents the return of one or more items (products and accessories)
 * from a previously registered sale. A return does not need to be total:
 * the customer may return only some of the items included in the original
 * sale.
 */
public class Return {

    private String id;
    private LocalDate date;
    private Sale originalSale;
    private List<Product> returnedProducts;
    private List<Accessory> returnedAccessories;
    private String reason;
    private double warrantyRefund;
    private double refundAmount;

    /**
     * Creates a new Return referencing the original sale and the specific
     * products being returned.
     *
     * @param id               unique identifier of the return
     * @param date             date on which the return is registered
     * @param originalSale     sale the returned products belong to
     * @param returnedProducts products being returned
     * @param reason           reason given by the customer for the return
     * @throws IllegalArgumentException if the id or reason is blank, the date
     *                                  or original sale is null, or there are
     *                                  no returned products
     */
    public Return(String id, LocalDate date, Sale originalSale,
                  List<Product> returnedProducts, String reason) {
        this(id, date, originalSale, returnedProducts, new ArrayList<>(), reason);
    }

    /**
     * Creates a new Return referencing the original sale and the specific
     * products and accessories being returned.
     *
     * @param id                  unique identifier of the return
     * @param date                date on which the return is registered
     * @param originalSale        sale the returned items belong to
     * @param returnedProducts    products being returned (may be empty)
     * @param returnedAccessories accessories being returned (may be empty)
     * @param reason              reason given by the customer for the return
     * @throws IllegalArgumentException if the id or reason is blank, the date
     *                                  or original sale is null, or there are
     *                                  no returned products or accessories
     */
    public Return(String id, LocalDate date, Sale originalSale, List<Product> returnedProducts,
                  List<Accessory> returnedAccessories, String reason) {
        this(id, date, originalSale, returnedProducts, returnedAccessories, reason, 0.0);
    }

    /**
     * Creates a new Return that also refunds the cost of the warranties
     * cancelled because consoles were returned (the additional cost of the
     * extended warranties; basic warranties have no cost).
     *
     * @param id                  unique identifier of the return
     * @param date                date on which the return is registered
     * @param originalSale        sale the returned items belong to
     * @param returnedProducts    products being returned (may be empty)
     * @param returnedAccessories accessories being returned (may be empty)
     * @param reason              reason given by the customer for the return
     * @param warrantyRefund      amount refunded for cancelled warranties
     * @throws IllegalArgumentException if the id or reason is blank, the date
     *                                  or original sale is null, there are no
     *                                  returned products or accessories, or the
     *                                  warranty refund is negative
     */
    public Return(String id, LocalDate date, Sale originalSale, List<Product> returnedProducts,
                  List<Accessory> returnedAccessories, String reason, double warrantyRefund) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("El identificador de la devolución no puede estar vacío.");
        }
        if (date == null) {
            throw new IllegalArgumentException("La fecha de la devolución es obligatoria.");
        }
        if (originalSale == null) {
            throw new IllegalArgumentException("La devolución debe referirse a una venta existente.");
        }
        List<Product> products = returnedProducts == null ? new ArrayList<>() : returnedProducts;
        List<Accessory> accessories = returnedAccessories == null ? new ArrayList<>() : returnedAccessories;
        if (products.isEmpty() && accessories.isEmpty()) {
            throw new IllegalArgumentException("La devolución debe incluir al menos un producto o accesorio.");
        }
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("El motivo de la devolución no puede estar vacío.");
        }
        if (warrantyRefund < 0) {
            throw new IllegalArgumentException("El reembolso por garantías no puede ser negativo.");
        }
        this.id = id;
        this.date = date;
        this.originalSale = originalSale;
        this.returnedProducts = new ArrayList<>(products);
        this.returnedAccessories = new ArrayList<>(accessories);
        this.reason = reason;
        this.warrantyRefund = warrantyRefund;
        calculateRefundAmount();
    }

    public String getId() {
        return id;
    }

    public LocalDate getDate() {
        return date;
    }

    public Sale getOriginalSale() {
        return originalSale;
    }

    public List<Product> getReturnedProducts() {
        return new ArrayList<>(returnedProducts);
    }

    /**
     * Returns a copy of the accessories returned in this return.
     *
     * @return the returned accessories (empty if none)
     */
    public List<Accessory> getReturnedAccessories() {
        return new ArrayList<>(returnedAccessories);
    }

    public String getReason() {
        return reason;
    }

    /**
     * Returns the amount refunded for the warranties cancelled in this return.
     *
     * @return the warranty refund, or 0 if no extended warranty was cancelled
     */
    public double getWarrantyRefund() {
        return warrantyRefund;
    }

    public double getRefundAmount() {
        return refundAmount;
    }

    /**
     * Calculates the refund amount, stores it in the refundAmount attribute,
     * and returns it. The refund of each returned product or accessory is
     * proportional to the discount of the original sale:
     * {@code price * (1 - discount / subtotal)}, so the customer never gets
     * back more than what was actually paid for the item. The cost of the
     * warranties cancelled because consoles were returned is added on top.
     *
     * @return the calculated refund amount
     */
    public double calculateRefundAmount() {
        double total = 0.0;
        for (Product product : returnedProducts) {
            total += refundFor(product.getPrice());
        }
        for (Accessory accessory : returnedAccessories) {
            total += refundFor(accessory.getPrice());
        }
        total += warrantyRefund;
        this.refundAmount = total;
        return this.refundAmount;
    }

    /**
     * Calculates the refund of one item from its list price, applying the
     * proportional discount of the original sale. The original sale, which
     * owns the subtotal and the discount, calculates the paid ratio.
     *
     * @param listPrice the list price of the item
     * @return the amount to refund for that item
     */
    private double refundFor(double listPrice) {
        return listPrice * originalSale.calculatePaidRatio();
    }

    /**
     * Generates a text receipt describing this return: its id and date, the
     * original sale it refers to, each returned product and accessory with its
     * list price, proportional discount, and refunded amount, the reason, and
     * the total refunded.
     *
     * @return the receipt of the return as multi-line text
     */
    public String generateReturnReceipt() {
        StringBuilder sb = new StringBuilder();
        sb.append("===== GameZone Unicesar - Recibo de devolución =====\n");
        sb.append("Devolución: ").append(id).append("\n");
        sb.append("Fecha: ").append(date).append("\n");
        sb.append("Venta original: ").append(originalSale.getId())
                .append(" (").append(originalSale.getDate()).append(")\n");
        sb.append("Ítems devueltos:\n");
        for (Product product : returnedProducts) {
            appendItemLine(sb, product.getTitle(), product.getPrice());
        }
        for (Accessory accessory : returnedAccessories) {
            appendItemLine(sb, accessory.getTitle() + " (accesorio)", accessory.getPrice());
        }
        if (warrantyRefund > 0) {
            sb.append("Garantías extendidas anuladas: +").append(CurrencyFormatter.format(warrantyRefund))
                    .append("\n");
        }
        sb.append("Motivo: ").append(reason).append("\n");
        sb.append("Monto reembolsado: ").append(CurrencyFormatter.format(refundAmount)).append("\n");
        sb.append("===================================================");
        return sb.toString();
    }

    /**
     * Appends one receipt line for a returned item, showing its list price,
     * the proportional discount, and the refunded amount.
     *
     * @param sb        the receipt being built
     * @param title     the title of the item
     * @param listPrice the list price of the item
     */
    private void appendItemLine(StringBuilder sb, String title, double listPrice) {
        double refund = refundFor(listPrice);
        sb.append("  - ").append(title)
                .append(": precio de lista ").append(CurrencyFormatter.format(listPrice))
                .append(", descuento -").append(CurrencyFormatter.format(listPrice - refund))
                .append(", reembolso ").append(CurrencyFormatter.format(refund)).append("\n");
    }

    @Override
    public String toString() {
        return "Return{id='" + id + "', date=" + date +
                ", reason='" + reason + "', refundAmount=" + refundAmount + "}";
    }
}