package com.gamezone.ui;

import com.gamezone.model.Warranty;
import com.gamezone.util.CurrencyFormatter;

/**
 * Builds the text of the certificate of a warranty, as it is shown in the
 * console. The layout of the certificate is a presentation concern, so it
 * lives here and not in {@link Warranty}, which only keeps the data and the
 * coverage rules of a warranty.
 */
public final class WarrantyCertificateFormatter {

    /**
     * Prevents instantiation: this class only offers a static method.
     */
    private WarrantyCertificateFormatter() {
    }

    /**
     * Builds the certificate of a warranty: its id, type, covered product
     * (id and title), the id of the sale it belongs to, coverage dates, and
     * additional cost.
     *
     * @param warranty the warranty to describe
     * @return the certificate of the warranty as multi-line text
     */
    public static String format(Warranty warranty) {
        StringBuilder sb = new StringBuilder();
        sb.append("===== GameZone Unicesar - Certificado de garantía =====\n");
        sb.append("Garantía: ").append(warranty.getId()).append("\n");
        sb.append("Tipo: ").append(warranty.getWarrantyType()).append("\n");
        sb.append("Producto: ").append(warranty.getProduct().getId())
                .append(" - ").append(warranty.getProduct().getTitle()).append("\n");
        sb.append("Venta: ").append(warranty.getSale().getId()).append("\n");
        sb.append("Fecha de inicio: ").append(warranty.getStartDate()).append("\n");
        sb.append("Fecha de fin: ").append(warranty.getEndDate()).append("\n");
        sb.append("Costo adicional: ").append(CurrencyFormatter.format(warranty.getAdditionalCost())).append("\n");
        sb.append("========================================================");
        return sb.toString();
    }
}
