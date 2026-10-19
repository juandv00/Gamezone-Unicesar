package com.gamezone.service;

import com.gamezone.model.Return;
import com.gamezone.model.Sale;

/**
 * Calculates the monthly balance of the store: the income of the sales of a
 * month, the amount refunded by the returns of the same month, and the net
 * balance. The balance is a financial report with its own reason to change,
 * so it lives in this class instead of in {@link ReturnService}, which only
 * manages the returns.
 */
public class BalanceService {

    private final SaleLookup saleLookup;
    private final ReturnLookup returnLookup;

    /**
     * Creates the balance service.
     *
     * @param saleLookup   the queries of the sales
     * @param returnLookup the queries of the returns
     */
    public BalanceService(SaleLookup saleLookup, ReturnLookup returnLookup) {
        this.saleLookup = saleLookup;
        this.returnLookup = returnLookup;
    }

    /**
     * Calculates the total income from the sales registered in the given month
     * and year, using the final total of each sale: subtotal minus the
     * promotion discount plus the cost of the extended warranties.
     *
     * @param month the month to evaluate (1-12)
     * @param year  the year to evaluate
     * @return the total of the sales of that period
     * @throws IllegalArgumentException if the month is not between 1 and 12
     */
    public double calculateMonthlySales(int month, int year) {
        validateMonth(month);
        double totalSales = 0.0;
        for (Sale sale : saleLookup.listAll()) {
            if (sale.getDate().getMonthValue() == month && sale.getDate().getYear() == year) {
                totalSales += sale.calculateFinalTotal();
            }
        }
        return totalSales;
    }

    /**
     * Calculates the total amount refunded by the returns registered in the
     * given month and year.
     *
     * @param month the month to evaluate (1-12)
     * @param year  the year to evaluate
     * @return the total refunded in that period
     * @throws IllegalArgumentException if the month is not between 1 and 12
     */
    public double calculateMonthlyReturns(int month, int year) {
        validateMonth(month);
        double totalReturns = 0.0;
        for (Return r : returnLookup.viewAllReturns()) {
            if (r.getDate().getMonthValue() == month && r.getDate().getYear() == year) {
                totalReturns += r.getRefundAmount();
            }
        }
        return totalReturns;
    }

    /**
     * Generates the monthly balance for the given month and year: the total
     * sales of the period minus the total refunded by returns in the same
     * period.
     *
     * @param month the month to evaluate (1-12)
     * @param year  the year to evaluate
     * @return the net balance (total sales minus total returns) for the period
     * @throws IllegalArgumentException if the month is not between 1 and 12
     */
    public double generateMonthlyBalance(int month, int year) {
        return calculateMonthlySales(month, year) - calculateMonthlyReturns(month, year);
    }

    private void validateMonth(int month) {
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("El mes debe estar entre 1 y 12.");
        }
    }
}
