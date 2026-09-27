package com.gamezone.service;

import com.gamezone.model.Product;
import com.gamezone.model.Return;
import com.gamezone.model.Sale;
import com.gamezone.persistence.ReturnRepository;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Contains the business rules for managing product returns at GameZone
 * Unicesar, such as registering new returns with deadline and ownership
 * validation, restoring stock, and generating the monthly balance report.
 */
public class ReturnService {

    private static final int RETURN_WINDOW_DAYS = 30;

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
     * window has expired, or a product does not belong to the original sale
     */
    public Return registerReturn(String saleId, List<String> productIds, String reason) {
        Sale sale = saleService.findById(saleId);
        if (sale == null) {
            throw new IllegalArgumentException("La venta indicada no existe.");
        }

        long daysSinceSale = ChronoUnit.DAYS.between(sale.getDate(), LocalDate.now());
        if (daysSinceSale > RETURN_WINDOW_DAYS) {
            throw new IllegalArgumentException(
                    "No se puede procesar la devolución: han pasado más de 30 días desde la venta.");
        }

        List<Product> saleProducts = sale.getProducts();
        List<Product> returnedProducts = new ArrayList<>();
        for (String productId : productIds) {
            Product match = findProductById(saleProducts, productId);
            if (match == null) {
                throw new IllegalArgumentException(
                        "El producto con id " + productId + " no pertenece a la venta indicada.");
            }
            returnedProducts.add(match);
        }

        Return newReturn = new Return(
                UUID.randomUUID().toString(),
                LocalDate.now(),
                sale,
                returnedProducts,
                reason
        );
        newReturn.calculateRefundAmount();

        for (Product product : returnedProducts) {
            productService.restoreStock(product.getId(), 1);
        }

        returns.add(newReturn);
        returnRepository.saveAll(returns);
        return newReturn;
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
     * Generates the monthly balance for the given month and year, calculating
     * the total sales, total returns, and the net balance between them.
     *
     * @param month the month to evaluate (1-12)
     * @param year the year to evaluate
     * @return the net balance (total sales minus total returns) for the given
     * period
     */
    public double generateMonthlyBalance(int month, int year) {
        double totalSales = 0.0;
        for (Sale sale : saleService.listAll()) {
            if (sale.getDate().getMonthValue() == month && sale.getDate().getYear() == year) {
                totalSales += sale.calculateTotal();
            }
        }

        double totalReturns = 0.0;
        for (Return r : returns) {
            if (r.getDate().getMonthValue() == month && r.getDate().getYear() == year) {
                totalReturns += r.getRefundAmount();
            }
        }

        return totalSales - totalReturns;
    }
}
