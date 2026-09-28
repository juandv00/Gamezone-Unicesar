package com.gamezone.service;

import com.gamezone.model.Product;
import com.gamezone.model.Return;
import com.gamezone.model.Sale;
import com.gamezone.persistence.ReturnRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Contains the business rules for managing product returns at GameZone
 * Unicesar, such as registering new returns with deadline and ownership
 * validation, restoring stock, and generating the monthly balance report.
 */
public class ReturnService {

    private static final String RETURN_ID_PREFIX = "RET-";

    private final ReturnRepository returnRepository;
    private final SaleService saleService;
    private final ProductService productService;
    private List<Return> returns;

    /**
     * Creates a new ReturnService, loading the currently stored returns from
     * the repository.
     *
     * @param returnRepository the repository used to persist returns
     * @param saleService the service used to validate the original sale
     * @param productService the service used to restore stock
     */
    public ReturnService(ReturnRepository returnRepository, SaleService saleService,
            ProductService productService) {
        this.returnRepository = returnRepository;
        this.saleService = saleService;
        this.productService = productService;
        this.returns = returnRepository.loadAll();
    }

    /**
     * Registers a new return for the given sale, validating the sale's
     * existence, the 30-day return window, and that every product requested for
     * return actually belongs to the original sale. On success, restores the
     * stock of every returned product and persists the new return.
     *
     * @param saleId the id of the original sale
     * @param productIds the ids of the products being returned
     * @param reason the reason given for the return
     * @return the newly registered Return
     * @throws IllegalArgumentException if the sale does not exist, the 30-day
     * window has expired (checked with {@link Sale#canBeReturned()}), no
     * product is given, a product does not belong to the original sale, or more
     * units of a product are requested than remain returnable
     */
    public Return registerReturn(String saleId, List<String> productIds, String reason) {
        Sale sale = saleService.findById(saleId);
        if (sale == null) {
            throw new IllegalArgumentException("The sale " + saleId + " does not exist.");
        }
        if (!sale.canBeReturned()) {
            throw new IllegalArgumentException(
                    "The return cannot be processed: more than 30 days have passed since the sale.");
        }
        if (productIds == null || productIds.isEmpty()) {
            throw new IllegalArgumentException("Select at least one product to return.");
        }

        List<Product> saleProducts = sale.getProducts();
        Map<String, Integer> returnableUnits = countReturnableUnits(sale);
        List<Product> returnedProducts = new ArrayList<>();
        for (String productId : productIds) {
            Product match = findProductById(saleProducts, productId);
            if (match == null) {
                throw new IllegalArgumentException(
                        "The product " + productId + " does not belong to sale " + saleId + ".");
            }
            int remaining = returnableUnits.getOrDefault(match.getId(), 0);
            if (remaining <= 0) {
                throw new IllegalArgumentException(
                        "All units of product " + productId + " from this sale have already been returned.");
            }
            returnableUnits.put(match.getId(), remaining - 1);
            returnedProducts.add(match);
        }

        Return newReturn = new Return(
                nextReturnId(),
                LocalDate.now(),
                sale,
                returnedProducts,
                reason
        );

        for (Product product : returnedProducts) {
            productService.restoreStock(product.getId(), 1);
        }

        returns.add(newReturn);
        returnRepository.saveAll(returns);
        return newReturn;
    }

    /**
     * Counts, for each product of the given sale, how many units can still be
     * returned: the units sold minus the units already returned in previous
     * returns of the same sale.
     *
     * @param sale the original sale
     * @return a map from product id to the number of units still returnable
     */
    private Map<String, Integer> countReturnableUnits(Sale sale) {
        Map<String, Integer> units = new HashMap<>();
        for (Product product : sale.getProducts()) {
            units.merge(product.getId(), 1, Integer::sum);
        }
        for (Return previous : returns) {
            if (previous.getOriginalSale().getId().equals(sale.getId())) {
                for (Product product : previous.getReturnedProducts()) {
                    units.merge(product.getId(), -1, Integer::sum);
                }
            }
        }
        return units;
    }

    /**
     * Finds a product with the given id within a specific list of products.
     *
     * @param products the list of products to search
     * @param productId the id to look for
     * @return the matching product, or null if not found
     */
    private Product findProductById(List<Product> products, String productId) {
        for (Product product : products) {
            if (product.getId().equals(productId)) {
                return product;
            }
        }
        return null;
    }

    /**
     * Returns all returns registered in the system.
     *
     * @return the list of all registered returns
     */
    public List<Return> viewAllReturns() {
        return new ArrayList<>(returns);
    }

    /**
     * Returns the returns whose original sale belongs to the given customer.
     *
     * @param customerId the id of the customer to filter by
     * @return the list of returns associated with that customer
     */
    public List<Return> viewReturnsByCustomer(String customerId) {
        List<Return> result = new ArrayList<>();
        for (Return r : returns) {
            if (r.getOriginalSale().getClient().getId().equals(customerId)) {
                result.add(r);
            }
        }
        return result;
    }

    /**
     * Returns the returns associated with a specific sale.
     *
     * @param saleId the id of the sale to filter by
     * @return the list of returns associated with that sale
     */
    public List<Return> viewReturnsBySale(String saleId) {
        List<Return> result = new ArrayList<>();
        for (Return r : returns) {
            if (r.getOriginalSale().getId().equals(saleId)) {
                result.add(r);
            }
        }
        return result;
    }

    /**
     * Calculates the total income from the sales registered in the given month
     * and year, using the final total of each sale: subtotal minus the
     * promotion discount plus the cost of the extended warranties.
     *
     * @param month the month to evaluate (1-12)
     * @param year the year to evaluate
     * @return the total of the sales of that period
     * @throws IllegalArgumentException if the month is not between 1 and 12
     */
    public double calculateMonthlySales(int month, int year) {
        validateMonth(month);
        double totalSales = 0.0;
        for (Sale sale : saleService.listAll()) {
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
     * @param year the year to evaluate
     * @return the total refunded in that period
     * @throws IllegalArgumentException if the month is not between 1 and 12
     */
    public double calculateMonthlyReturns(int month, int year) {
        validateMonth(month);
        double totalReturns = 0.0;
        for (Return r : returns) {
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
     * @param year the year to evaluate
     * @return the net balance (total sales minus total returns) for the given
     * period
     * @throws IllegalArgumentException if the month is not between 1 and 12
     */
    public double generateMonthlyBalance(int month, int year) {
        return calculateMonthlySales(month, year) - calculateMonthlyReturns(month, year);
    }

    /**
     * Generates the next available return id, with the format RET-n, where n is
     * one more than the highest number already in use.
     *
     * @return the next return id
     */
    private String nextReturnId() {
        int highest = 0;
        for (Return r : returns) {
            String id = r.getId();
            if (id.startsWith(RETURN_ID_PREFIX)) {
                try {
                    highest = Math.max(highest, Integer.parseInt(id.substring(RETURN_ID_PREFIX.length())));
                } catch (NumberFormatException e) {
                    // Ids with another format do not affect the numbering.
                }
            }
        }
        return RETURN_ID_PREFIX + (highest + 1);
    }

    private void validateMonth(int month) {
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("The month must be between 1 and 12.");
        }
    }
}
