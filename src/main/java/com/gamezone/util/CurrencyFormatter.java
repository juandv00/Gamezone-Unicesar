package com.gamezone.util;

import java.util.Locale;

/**
 * Formats amounts of money for display in receipts, certificates, and the
 * console menu, for example $1,250,000.00. Formatting money is a
 * presentation responsibility that does not belong to any domain class, so
 * it lives in this single class (Pure Fabrication) and every class that
 * shows an amount uses it.
 */
public final class CurrencyFormatter {

    /**
     * Prevents instantiation: this class only offers a static method.
     */
    private CurrencyFormatter() {
    }

    /**
     * Formats an amount of money with a dollar sign, thousands separators,
     * and two decimals, using the US locale (for example $1,250,000.00).
     *
     * @param amount the amount to format
     * @return the formatted amount
     */
    public static String format(double amount) {
        return String.format(Locale.US, "$%,.2f", amount);
    }
}
