package com.gamezone.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents the return of one or more products from a previously
 * registered sale. A return does not need to be total: the customer may
 * return only some of the products included in the original sale.
 */
public class Return {

    private String id;
    private LocalDate date;
    private Sale originalSale;
    private List<Product> returnedProducts;
    private String reason;
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
     */
    public Return(String id, LocalDate date, Sale originalSale,
                  List<Product> returnedProducts, String reason) {
        this.id = id;
        this.date = date;
        this.originalSale = originalSale;
        this.returnedProducts = returnedProducts;
        this.reason = reason;
        this.refundAmount = 0.0;
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

    public String getReason() {
        return reason;
    }

    public double getRefundAmount() {
        return refundAmount;
    }

    /**
     * Calculates the refund amount by summing the price of every returned
     * product, stores it in the refundAmount attribute, and returns it.
     *
     * @return the calculated refund amount
     */
    public double calculateRefundAmount() {
        double total = 0.0;
        for (Product product : returnedProducts) {
            total += product.getPrice();
        }
        this.refundAmount = total;
        return this.refundAmount;
    }

    /**
     * Generates a Spanish-formatted receipt describing this return.
     *
     * @return a descriptive string of the return
     */
    public String generateReturnReceipt() {
        StringBuilder sb = new StringBuilder();
        sb.append("Recibo de Devolución\n");
        sb.append("Identificador: ").append(id).append("\n");
        sb.append("Fecha: ").append(date).append("\n");
        sb.append("Venta original: ").append(originalSale.getDate()).append("\n");
        sb.append("Productos devueltos:\n");
        for (Product product : returnedProducts) {
            sb.append(" - ").append(product.getTitle())
                    .append(" ($").append(product.getPrice()).append(")\n");
        }
        sb.append("Motivo: ").append(reason).append("\n");
        sb.append("Monto reembolsado: $").append(refundAmount);
        return sb.toString();
    }

    @Override
    public String toString() {
        return "Return{id='" + id + "', date=" + date +
                ", reason='" + reason + "', refundAmount=" + refundAmount + "}";
    }
}
