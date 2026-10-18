package com.gamezone.service;

import com.gamezone.model.BasicWarranty;
import com.gamezone.model.ExtendedWarranty;
import com.gamezone.model.Product;
import com.gamezone.model.Sale;
import com.gamezone.model.Warranty;
import com.gamezone.persistence.SaleStorage;
import com.gamezone.persistence.WarrantyRecord;
import com.gamezone.persistence.WarrantyStorage;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Contains the business rules for managing product warranties at GameZone
 * Unicesar, such as assigning basic and extended warranties to consoles,
 * querying warranty validity, listing warranties expiring soon, and
 * cancelling the warranty of a returned console.
 */
public class WarrantyService {

    private static final String WARRANTY_ID_PREFIX = "WAR-";

    private final WarrantyStorage warrantyStorage;
    private final SaleStorage saleStorage;
    private final ProductService productService;
    private List<Warranty> warranties;

    /**
     * Creates a new WarrantyService, loading the stored warranty records and
     * resolving their sale and product references from the identifiers.
     *
     * @param warrantyStorage the storage of the warranties
     * @param saleStorage     the storage of the sales, used to resolve the
     *                        sale of each record
     * @param productService  used to resolve the product of each record
     */
    public WarrantyService(WarrantyStorage warrantyStorage,
                           SaleStorage saleStorage, ProductService productService) {
        this.warrantyStorage = warrantyStorage;
        this.saleStorage = saleStorage;
        this.productService = productService;
        this.warranties = resolveWarranties(warrantyStorage.loadAll());
    }

    /**
     * Builds the warranties from the stored records, skipping the records
     * whose sale or product can no longer be found.
     *
     * @param records the stored warranty records
     * @return the warranties that could be fully resolved
     */
    private List<Warranty> resolveWarranties(List<WarrantyRecord> records) {
        List<Sale> sales = saleStorage.load();
        List<Warranty> resolved = new ArrayList<>();
        for (WarrantyRecord record : records) {
            Sale sale = findSaleById(sales, record.saleId());
            Product product = productService.findById(record.productId());
            if (sale == null || product == null) {
                continue;
            }
            Warranty warranty = warrantyStorage.toWarranty(record, product, sale);
            if (warranty != null) {
                resolved.add(warranty);
            }
        }
        return resolved;
    }

    /**
     * Finds a sale by id in the given list, ignoring case and skipping
     * sales without an id.
     *
     * @param sales  the sales to search
     * @param saleId the id to look for
     * @return the matching sale, or null if not found
     */
    private Sale findSaleById(List<Sale> sales, String saleId) {
        for (Sale sale : sales) {
            if (sale.getId() != null && sale.getId().equalsIgnoreCase(saleId)) {
                return sale;
            }
        }
        return null;
    }

    /**
     * Creates and persists an automatic basic warranty for the given
     * console and sale.
     *
     * @param product   the product to cover; must be a Console
     * @param sale      the sale the warranty originates from
     * @param startDate the date the warranty coverage begins
     * @return the newly assigned BasicWarranty
     * @throws IllegalArgumentException if the product is not a Console
     */
    public BasicWarranty assignBasicWarranty(Product product, Sale sale, LocalDate startDate) {
        validateWarrantyEligible(product);
        BasicWarranty warranty = new BasicWarranty(nextWarrantyId(), product, sale, startDate);
        warranties.add(warranty);
        warrantyStorage.saveAll(warranties);
        return warranty;
    }

    /**
     * Creates and persists an extended warranty for the given console and
     * sale, requested optionally by the seller at the time of sale.
     *
     * @param product   the product to cover; must be a Console
     * @param sale      the sale the warranty originates from
     * @param startDate the date the warranty coverage begins
     * @return the newly assigned ExtendedWarranty
     * @throws IllegalArgumentException if the product is not a Console
     */
    public ExtendedWarranty assignExtendedWarranty(Product product, Sale sale, LocalDate startDate) {
        validateWarrantyEligible(product);
        ExtendedWarranty warranty = new ExtendedWarranty(nextWarrantyId(), product, sale, startDate);
        warranties.add(warranty);
        warrantyStorage.saveAll(warranties);
        return warranty;
    }

    /**
     * Ensures the given product receives warranties. The product answers it
     * itself; today only consoles do.
     *
     * @param product the product to validate
     * @throws IllegalArgumentException if the product does not receive warranties
     */
    private void validateWarrantyEligible(Product product) {
        if (!product.isWarrantyEligible()) {
            throw new IllegalArgumentException("Solo se pueden asignar garantías a las consolas.");
        }
    }

    /**
     * Finds the warranty associated with a specific product within a
     * specific sale.
     *
     * @param productId the id of the product to search for
     * @param saleId    the id of the sale to search within
     * @return the first matching warranty, or null if none exists
     */
    public Warranty findWarrantyByProduct(String productId, String saleId) {
        for (Warranty w : warranties) {
            if (w.getProduct().getId().equals(productId) && w.getSale().getId().equals(saleId)) {
                return w;
            }
        }
        return null;
    }

    /**
     * Cancels the warranty of one returned unit of a console in a specific
     * sale: the warranty is removed from the system and the change is saved.
     * A returned console cannot keep an active warranty. If the sale included
     * several units of the same console, each unit has its own warranty, so
     * this method cancels one warranty per call; ReturnService calls it once
     * for every returned unit.
     *
     * @param productId the id of the returned console
     * @param saleId    the id of the original sale
     * @return the refundable cost of the cancelled warranty: 0 for a basic
     *         warranty, its additional cost for an extended warranty, or 0 if
     *         the product has no warranty in that sale
     */
    public double cancelWarranties(String productId, String saleId) {
        for (Warranty warranty : warranties) {
            if (warranty.getProduct().getId().equalsIgnoreCase(productId)
                    && warranty.getSale().getId().equalsIgnoreCase(saleId)) {
                warranties.remove(warranty);
                warrantyStorage.saveAll(warranties);
                return warranty.getAdditionalCost();
            }
        }
        return 0.0;
    }

    /**
     * Returns all warranties currently registered.
     *
     * @return the list of all registered warranties
     */
    public List<Warranty> listAllWarranties() {
        return new ArrayList<>(warranties);
    }

    /**
     * Returns the warranties that are currently active, based on today's date.
     *
     * @return the list of warranties valid for the current date
     */
    public List<Warranty> listActiveWarranties() {
        List<Warranty> active = new ArrayList<>();
        LocalDate today = LocalDate.now();
        for (Warranty w : warranties) {
            if (w.isActive(today)) {
                active.add(w);
            }
        }
        return active;
    }

    /**
     * Returns the warranties that are active today and whose end date
     * falls on or before today plus the given number of days.
     *
     * @param daysAhead the number of days to look ahead
     * @return the list of warranties expiring within that window
     * @throws IllegalArgumentException if daysAhead is negative
     */
    public List<Warranty> listWarrantiesExpiringSoon(int daysAhead) {
        if (daysAhead < 0) {
            throw new IllegalArgumentException("El número de días no puede ser negativo.");
        }
        List<Warranty> expiringSoon = new ArrayList<>();
        LocalDate today = LocalDate.now();
        LocalDate limit = today.plusDays(daysAhead);
        for (Warranty w : warranties) {
            if (w.isActive(today) && !w.getEndDate().isAfter(limit)) {
                expiringSoon.add(w);
            }
        }
        return expiringSoon;
    }

    /**
     * Generates the next available warranty id, with the format WAR-n,
     * where n is one more than the highest number already in use.
     *
     * @return the next warranty id
     */
    private String nextWarrantyId() {
        int highest = 0;
        for (Warranty w : warranties) {
            String id = w.getId();
            if (id.startsWith(WARRANTY_ID_PREFIX)) {
                try {
                    highest = Math.max(highest, Integer.parseInt(id.substring(WARRANTY_ID_PREFIX.length())));
                } catch (NumberFormatException e) {
                    // Ids with another format do not affect the numbering.
                }
            }
        }
        return WARRANTY_ID_PREFIX + (highest + 1);
    }
}