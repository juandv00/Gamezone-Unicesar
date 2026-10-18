package com.gamezone.ui;

import com.gamezone.model.Accessory;
import com.gamezone.model.Product;
import com.gamezone.model.Return;
import com.gamezone.util.CurrencyFormatter;

/**
 * Builds the text of the receipt of a return, as it is shown in the console.
 * The layout of the receipt is a presentation concern, so it lives here and
 * not in {@link Return}, which only keeps the data and the refund rules of a
 * return.
 */
public final class ReturnReceiptFormatter {

    /**
     * Prevents instantiation: this class only offers a static method.
     */
    private ReturnReceiptFormatter() {
    }

    /**
     * Builds the receipt of a return: its id and date, the original sale it
     * refers to, each returned product and accessory with its list price,
     * proportional discount, and refunded amount, the reason, and the total
     * refunded.
     *
     * @param aReturn the return to describe
     * @return the receipt of the return as multi-line text
     */
    public static String format(Return aReturn) {
        StringBuilder sb = new StringBuilder();
        sb.append("===== GameZone Unicesar - Recibo de devolución =====\n");
        sb.append("Devolución: ").append(aReturn.getId()).append("\n");
        sb.append("Fecha: ").append(aReturn.getDate()).append("\n");
        sb.append("Venta original: ").append(aReturn.getOriginalSale().getId())
                .append(" (").append(aReturn.getOriginalSale().getDate()).append(")\n");
        sb.append("Ítems devueltos:\n");
        for (Product product : aReturn.getReturnedProducts()) {
            appendItemLine(sb, aReturn, product.getTitle(), product.getPrice());
        }
        for (Accessory accessory : aReturn.getReturnedAccessories()) {
            appendItemLine(sb, aReturn, accessory.getTitle() + " (accesorio)", accessory.getPrice());
        }
        if (aReturn.getWarrantyRefund() > 0) {
            sb.append("Garantías extendidas anuladas: +")
                    .append(CurrencyFormatter.format(aReturn.getWarrantyRefund())).append("\n");
        }
        sb.append("Motivo: ").append(aReturn.getReason()).append("\n");
        sb.append("Monto reembolsado: ").append(CurrencyFormatter.format(aReturn.getRefundAmount())).append("\n");
        sb.append("===================================================");
        return sb.toString();
    }

    /**
     * Appends one receipt line for a returned item, showing its list price,
     * the proportional discount, and the refunded amount.
     *
     * @param sb        the receipt being built
     * @param aReturn   the return the item belongs to
     * @param title     the title of the item
     * @param listPrice the list price of the item
     */
    private static void appendItemLine(StringBuilder sb, Return aReturn, String title, double listPrice) {
        double refund = aReturn.calculateRefundFor(listPrice);
        sb.append("  - ").append(title)
                .append(": precio de lista ").append(CurrencyFormatter.format(listPrice))
                .append(", descuento -").append(CurrencyFormatter.format(listPrice - refund))
                .append(", reembolso ").append(CurrencyFormatter.format(refund)).append("\n");
    }
}
