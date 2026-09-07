package com.gamezone.service;

import com.gamezone.model.Client;
import com.gamezone.model.Product;
import com.gamezone.model.Sale;
import com.gamezone.model.Seller;
import com.gamezone.persistence.SalePersistence;
import java.util.ArrayList;
import java.util.List;

/**
 * Contains the business rules for managing sales at GameZone Unicesar,
 * such as registering new sales with stock validation, updating inventory
 * automatically, and consulting sales history by client or seller.
 */
public class SaleService {

    private final SalePersistence salePersistence;
    private final ProductService productService;
    private List<Sale> sales;

    /**
     * Creates a new SaleService, loading the currently stored sales and
     * using the given ProductService to validate and update stock when
     * a new sale is registered.
     *
     * @param productService the service used to validate and reduce stock
     */
    public SaleService(ProductService productService) {
        this.salePersistence = new SalePersistence();
        this.productService = productService;
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
}