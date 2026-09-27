package com.gamezone.service;

import com.gamezone.model.Accessory;
import com.gamezone.model.Client;
import com.gamezone.model.Product;
import com.gamezone.model.Sale;
import com.gamezone.model.Seller;
import com.gamezone.persistence.SalePersistence;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Contains the business rules for managing sales at GameZone Unicesar,
 * such as registering new sales with stock validation, updating inventory
 * automatically, and consulting sales history by client or seller.
 * Sales may include products, accessories, or both.
 */
public class SaleService {

    private final SalePersistence salePersistence;
    private final ProductService productService;
    private final AccessoryService accessoryService;
    private List<Sale> sales;

    /**
     * Creates a new SaleService, loading the currently stored sales and
     * using the given ProductService to validate and update stock when
     * a new sale is registered. A SaleService created with this constructor
     * does not support accessories.
     *
     * @param productService the service used to validate and reduce stock
     */
    public SaleService(ProductService productService) {
        this(productService, null, new SalePersistence());
    }

    /**
     * Creates a new SaleService that supports selling products and
     * accessories together. The sale persistence is injected so it can be
     * configured to resolve accessories when loading stored sales.
     *
     * @param productService   the service used to validate and reduce product stock
     * @param accessoryService the service used to validate and reduce accessory stock
     * @param salePersistence  the persistence used to load and save sales
     */
    public SaleService(ProductService productService, AccessoryService accessoryService,
                       SalePersistence salePersistence) {
        this.productService = productService;
        this.accessoryService = accessoryService;
        this.salePersistence = salePersistence;
        this.sales = salePersistence.load();
    }

    /**
     * Registers a new sale after validating that it contains at least
     * one product and that every product has enough stock available.
     * If valid, the stock of each product is reduced automatically and
     * the sale is persisted.
     *
     * @param client   the client making the purchase
     * @param seller   the seller attending the sale
     * @param products the list of products included in the sale
     * @return the registered Sale if successful, or null if the sale
     *         could not be registered (empty product list or insufficient
     *         stock for any product)
     */
    public Sale registerSale(Client client, Seller seller, List<Product> products) {
        if (products == null || products.isEmpty()) {
            return null;
        }

        for (Product product : products) {
            if (!productService.hasEnoughStock(product.getId(), 1)) {
                return null;
            }
        }

        for (Product product : products) {
            productService.reduceStock(product.getId(), 1);
        }

        Sale sale = new Sale(client, seller, products);
        sales.add(sale);
        salePersistence.save(sales);
        return sale;
    }

    /**
     * Registers a new sale that may include products and accessories.
     * The sale must contain at least one item in total. Each occurrence of
     * an item in its list counts as one unit, so if the same item appears
     * several times, the stock is validated against the total requested.
     * Stock is validated for every item before anything is reduced, so a
     * rejected sale leaves the inventory untouched.
     *
     * @param client      the client making the purchase
     * @param seller      the seller attending the sale
     * @param products    the list of products included in the sale (may be empty)
     * @param accessories the list of accessories included in the sale (may be empty)
     * @return the registered Sale if successful, or null if the sale could
     *         not be registered (no items or insufficient stock for any item)
     * @throws IOException           if the accessory stock could not be persisted
     * @throws IllegalStateException if accessories are given but this service
     *                               was created without an AccessoryService
     */
    public Sale registerSale(Client client, Seller seller, List<Product> products,
                             List<Accessory> accessories) throws IOException {
        List<Product> saleProducts = products == null ? new ArrayList<>() : new ArrayList<>(products);
        List<Accessory> saleAccessories = accessories == null ? new ArrayList<>() : new ArrayList<>(accessories);

        if (saleProducts.isEmpty() && saleAccessories.isEmpty()) {
            return null;
        }
        if (!saleAccessories.isEmpty() && accessoryService == null) {
            throw new IllegalStateException("This SaleService was created without accessory support.");
        }

        Map<String, Integer> productUnits = countProductUnits(saleProducts);
        Map<String, Integer> accessoryUnits = countAccessoryUnits(saleAccessories);

        for (Map.Entry<String, Integer> entry : productUnits.entrySet()) {
            if (!productService.hasEnoughStock(entry.getKey(), entry.getValue())) {
                return null;
            }
        }
        for (Map.Entry<String, Integer> entry : accessoryUnits.entrySet()) {
            if (!accessoryService.hasEnoughStock(entry.getKey(), entry.getValue())) {
                return null;
            }
        }

        for (Map.Entry<String, Integer> entry : productUnits.entrySet()) {
            productService.reduceStock(entry.getKey(), entry.getValue());
        }
        for (Map.Entry<String, Integer> entry : accessoryUnits.entrySet()) {
            accessoryService.reduceStock(entry.getKey(), entry.getValue());
        }

        Sale sale = new Sale(client, seller, saleProducts, saleAccessories);
        sales.add(sale);
        salePersistence.save(sales);
        return sale;
    }

    /**
     * Returns the full history of sales registered in the system.
     *
     * @return the list of all registered sales
     */
    public List<Sale> listAll() {
        return new ArrayList<>(sales);
    }

    /**
     * Returns the purchase history of a specific client.
     *
     * @param clientId the id of the client to search for
     * @return the list of sales made by the given client
     */
    public List<Sale> findByClient(String clientId) {
        List<Sale> result = new ArrayList<>();
        for (Sale sale : sales) {
            if (sale.getClient().getId().equals(clientId)) {
                result.add(sale);
            }
        }
        return result;
    }

    /**
     * Returns the sales attended by a specific seller.
     *
     * @param sellerId the id of the seller to search for
     * @return the list of sales attended by the given seller
     */
    public List<Sale> findBySeller(String sellerId) {
        List<Sale> result = new ArrayList<>();
        for (Sale sale : sales) {
            if (sale.getSeller().getId().equals(sellerId)) {
                result.add(sale);
            }
        }
        return result;
    }

    private Map<String, Integer> countProductUnits(List<Product> products) {
        Map<String, Integer> units = new LinkedHashMap<>();
        for (Product product : products) {
            units.merge(product.getId(), 1, Integer::sum);
        }
        return units;
    }

    private Map<String, Integer> countAccessoryUnits(List<Accessory> accessories) {
        Map<String, Integer> units = new LinkedHashMap<>();
        for (Accessory accessory : accessories) {
            units.merge(accessory.getId(), 1, Integer::sum);
        }
        return units;
    }
}