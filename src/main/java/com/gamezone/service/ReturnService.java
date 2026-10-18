package com.gamezone.service;

import com.gamezone.model.Accessory;
import com.gamezone.model.Product;
import com.gamezone.model.Return;
import com.gamezone.model.Sale;
import com.gamezone.persistence.ReturnRecord;
import com.gamezone.persistence.ReturnStorage;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Contains the business rules for managing returns at GameZone Unicesar,
 * such as registering new returns of products and accessories with deadline
 * and ownership validation, restoring the stock of each item through its own
 * service, and generating the monthly balance report.
 */
public class ReturnService {

    private static final String RETURN_ID_PREFIX = "RET-";

    private final ReturnStorage returnStorage;
    private final SaleLookup saleLookup;
    private final ProductInventory productInventory;
    private final AccessoryInventory accessoryInventory;
    private final WarrantyService warrantyService;
    private List<Return> returns;

    /**
     * Creates a new ReturnService, loading the currently stored returns from
     * the repository.
     *
     * @param returnStorage the storage of the returns
     * @param saleLookup the queries of the sales, used to validate the original sale
     * @param productInventory the inventory of the products, used to restore stock
     */
    public ReturnService(ReturnStorage returnStorage, SaleLookup saleLookup,
                         ProductInventory productInventory) {
        this(returnStorage, saleLookup, productInventory, null);
    }

    /**
     * Creates a new ReturnService that also supports returning accessories,
     * restoring their stock through the given AccessoryService.
     *
     * @param returnStorage the storage of the returns
     * @param saleLookup the queries of the sales, used to validate the original sale
     * @param productInventory the inventory of the products, used to restore stock
     * @param accessoryInventory the inventory of the accessories, used to restore stock, or
     * null if accessories cannot be returned
     */
    public ReturnService(ReturnStorage returnStorage, SaleLookup saleLookup,
                         ProductInventory productInventory, AccessoryInventory accessoryInventory) {
        this(returnStorage, saleLookup, productInventory, accessoryInventory, null);
    }

    /**
     * Creates a new ReturnService that also cancels the warranty of every
     * returned console and refunds the cost of the cancelled extended
     * warranties.
     *
     * @param returnStorage the storage of the returns
     * @param saleLookup the queries of the sales, used to validate the original sale
     * @param productInventory the inventory of the products, used to restore stock
     * @param accessoryInventory the inventory of the accessories, used to restore stock, or
     * null if accessories cannot be returned
     * @param warrantyService the service used to cancel the warranties of
     * returned consoles, or null if warranties are not managed
     */
    public ReturnService(ReturnStorage returnStorage, SaleLookup saleLookup,
                         ProductInventory productInventory, AccessoryInventory accessoryInventory,
                         WarrantyService warrantyService) {
        this.returnStorage = returnStorage;
        this.saleLookup = saleLookup;
        this.productInventory = productInventory;
        this.accessoryInventory = accessoryInventory;
        this.warrantyService = warrantyService;
        this.returns = resolveReturns(returnStorage.loadAll());
    }

    /**
     * Rebuilds the stored returns from their records, resolving the original
     * sale through SaleService and each returned item id against the products
     * of that sale first and then against its accessories (falling back to
     * ProductService for products no longer in the sale). Records whose sale
     * or items cannot be resolved are skipped.
     *
     * @param records the records loaded by the repository
     * @return the resolved returns
     */
    private List<Return> resolveReturns(List<ReturnRecord> records) {
        List<Return> resolved = new ArrayList<>();
        for (ReturnRecord record : records) {
            Sale sale = saleLookup.findById(record.saleId());
            if (sale == null) {
                continue;
            }
            List<Product> returnedProducts = new ArrayList<>();
            List<Accessory> returnedAccessories = new ArrayList<>();
            for (String itemId : record.itemIds()) {
                Product product = findProductById(sale.getProducts(), itemId);
                if (product == null) {
                    product = productInventory.findById(itemId);
                }
                if (product != null) {
                    returnedProducts.add(product);
                    continue;
                }
                Accessory accessory = findAccessoryById(sale.getAccessories(), itemId);
                if (accessory != null) {
                    returnedAccessories.add(accessory);
                }
            }
            if (returnedProducts.isEmpty() && returnedAccessories.isEmpty()) {
                continue;
            }
            try {
                resolved.add(new Return(record.id(), record.date(), sale, returnedProducts,
                        returnedAccessories, record.reason(), record.warrantyRefund()));
            } catch (IllegalArgumentException e) {
                System.out.println("Se omitió una devolución guardada inválida: " + record.id());
            }
        }
        return resolved;
    }

    /**
     * Registers a new return for the given sale, validating the sale's
     * existence, the 30-day return window, and that every item requested for
     * return actually belongs to the original sale. Each id is resolved as a
     * product of the sale first and, if no product matches, as an accessory of
     * the sale. On success, restores the stock of every returned item through
     * ProductService or AccessoryService, depending on its type, cancels the
     * warranty of every returned console unit (adding the cost of cancelled
     * extended warranties to the refund), and persists the new return.
     *
     * @param saleId the id of the original sale
     * @param productIds the ids of the items (products or accessories) being
     * returned; an id repeated n times means n units
     * @param reason the reason given for the return
     * @return the newly registered Return
     * @throws IllegalArgumentException if the sale does not exist, the 30-day
     * window has expired (checked with {@link Sale#canBeReturned()}), no item
     * is given, the reason is empty, an item does not belong to the original
     * sale, or more units of an item are requested than remain returnable
     */
    public Return registerReturn(String saleId, List<String> productIds, String reason) {
        Sale sale = saleLookup.findById(saleId);
        if (sale == null) {
            throw new IllegalArgumentException("La venta " + saleId + " no existe.");
        }
        if (!sale.canBeReturned()) {
            throw new IllegalArgumentException(
                    "No se puede registrar la devolución: pasaron más de 30 días desde la venta.");
        }
        if (productIds == null || productIds.isEmpty()) {
            throw new IllegalArgumentException("Seleccione al menos un ítem para devolver.");
        }
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("El motivo de la devolución no puede estar vacío.");
        }

        Map<String, Integer> returnableUnits = countReturnableUnits(sale);
        List<Product> returnedProducts = new ArrayList<>();
        List<Accessory> returnedAccessories = new ArrayList<>();
        for (String itemId : productIds) {
            Product product = findProductById(sale.getProducts(), itemId);
            Accessory accessory = product == null ? findAccessoryById(sale.getAccessories(), itemId) : null;
            if (product == null && accessory == null) {
                throw new IllegalArgumentException(
                        "El ítem " + itemId + " no pertenece a la venta " + sale.getId() + ".");
            }
            if (accessory != null && accessoryInventory == null) {
                throw new IllegalStateException("This ReturnService was created without accessory support.");
            }
            String key = product != null ? productKey(product.getId()) : accessoryKey(accessory.getId());
            int remaining = returnableUnits.getOrDefault(key, 0);
            if (remaining <= 0) {
                throw new IllegalArgumentException(
                        "Todas las unidades del ítem " + itemId + " de esta venta ya fueron devueltas.");
            }
            returnableUnits.put(key, remaining - 1);
            if (product != null) {
                returnedProducts.add(product);
            } else {
                returnedAccessories.add(accessory);
            }
        }

        // All validations passed: cancel the warranty of each returned console unit.
        double warrantyRefund = 0.0;
        if (warrantyService != null) {
            for (Product product : returnedProducts) {
                if (product.isWarrantyEligible()) {
                    warrantyRefund += warrantyService.cancelWarranties(product.getId(), sale.getId());
                }
            }
        }

        Return newReturn = new Return(
                nextReturnId(),
                LocalDate.now(),
                sale,
                returnedProducts,
                returnedAccessories,
                reason,
                warrantyRefund
        );

        for (Product product : returnedProducts) {
            productInventory.restoreStock(product.getId(), 1);
        }
        for (Accessory accessory : returnedAccessories) {
            accessoryInventory.restoreStock(accessory.getId(), 1);
        }

        returns.add(newReturn);
        returnStorage.saveAll(returns);
        return newReturn;
    }

    /**
     * Counts, for each item of the given sale, how many units can still be
     * returned: the units sold minus the units already returned in previous
     * returns of the same sale. Products and accessories are counted with
     * separate keys, so an accessory and a product with the same id are not
     * mixed.
     *
     * @param sale the original sale
     * @return a map from item key to the number of units still returnable
     */
    private Map<String, Integer> countReturnableUnits(Sale sale) {
        Map<String, Integer> units = new HashMap<>();
        for (Product product : sale.getProducts()) {
            units.merge(productKey(product.getId()), 1, Integer::sum);
        }
        for (Accessory accessory : sale.getAccessories()) {
            units.merge(accessoryKey(accessory.getId()), 1, Integer::sum);
        }
        for (Return previous : returns) {
            if (previous.getOriginalSale().getId().equals(sale.getId())) {
                for (Product product : previous.getReturnedProducts()) {
                    units.merge(productKey(product.getId()), -1, Integer::sum);
                }
                for (Accessory accessory : previous.getReturnedAccessories()) {
                    units.merge(accessoryKey(accessory.getId()), -1, Integer::sum);
                }
            }
        }
        return units;
    }

    private String productKey(String productId) {
        return "P:" + productId;
    }

    private String accessoryKey(String accessoryId) {
        return "A:" + accessoryId;
    }

    /**
     * Finds an accessory with the given id within a specific list of
     * accessories, ignoring case.
     *
     * @param accessories the list of accessories to search
     * @param accessoryId the id to look for
     * @return the matching accessory, or null if not found
     */
    private Accessory findAccessoryById(List<Accessory> accessories, String accessoryId) {
        for (Accessory accessory : accessories) {
            if (accessory.getId().equalsIgnoreCase(accessoryId)) {
                return accessory;
            }
        }
        return null;
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
            throw new IllegalArgumentException("El mes debe estar entre 1 y 12.");
        }
    }
}