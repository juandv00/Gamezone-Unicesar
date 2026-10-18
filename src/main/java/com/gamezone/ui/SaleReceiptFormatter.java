package com.gamezone.ui;

import com.gamezone.model.Accessory;
import com.gamezone.model.Product;
import com.gamezone.model.Sale;
import com.gamezone.util.CurrencyFormatter;

/**
 * Builds the text of the receipt of a sale, as it is shown in the console.
 * The layout of the receipt is a presentation concern, so it lives here and
 * not in {@link Sale}, which only keeps the data and the totals of a sale.
 */
public final class SaleReceiptFormatter {

    /**
     * Prevents instantiation: this class only offers a static method.
     */
    private SaleReceiptFormatter() {
    }

    /**
     * Builds the receipt of a sale, listing every product and accessory with
     * its price, followed by the subtotal, the discount applied (with the
     * name of the promotion), the cost of the extended warranties (if any)
     * and the final total.
     *
     * @param sale the sale to describe
     * @return the receipt of the sale as multi-line text
     */
    public static String format(Sale sale) {
        StringBuilder receipt = new StringBuilder();
        receipt.append("===== GameZone Unicesar - Recibo =====\n");
        if (sale.getId() != null) {
            receipt.append("Venta: ").append(sale.getId()).append("\n");
        }
        receipt.append("Fecha: ").append(sale.getDate()).append("\n");
        receipt.append("Cliente: ").append(sale.getClient().getName()).append("\n");
        receipt.append("Vendedor: ").append(sale.getSeller().getName()).append("\n");
        receipt.append("Ítems:\n");
        for (Product product : sale.getProducts()) {
            receipt.append("  - ").append(product.getTitle())
                    .append(": ").append(CurrencyFormatter.format(product.getPrice())).append("\n");
        }
        for (Accessory accessory : sale.getAccessories()) {
            receipt.append("  - ").append(accessory.getTitle())
                    .append(" (accesorio): ").append(CurrencyFormatter.format(accessory.getPrice())).append("\n");
        }
        receipt.append("Subtotal: ").append(CurrencyFormatter.format(sale.calculateTotal())).append("\n");
        if (sale.hasDiscount()) {
            receipt.append("Descuento (").append(sale.getAppliedPromotionName()).append("): -")
                    .append(CurrencyFormatter.format(sale.getDiscountAmount())).append("\n");
        } else {
            receipt.append("Descuento: ninguno\n");
        }
        if (sale.getWarrantyCost() > 0) {
            receipt.append("Garantías extendidas: +")
                    .append(CurrencyFormatter.format(sale.getWarrantyCost())).append("\n");
        }
        receipt.append("Total: ").append(CurrencyFormatter.format(sale.calculateFinalTotal())).append("\n");
        receipt.append("=======================================");
        return receipt.toString();
    }
}
