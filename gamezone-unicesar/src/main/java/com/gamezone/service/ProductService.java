package com.gamezone.service;

import com.gamezone.model.Product;
import com.gamezone.persistence.ProductPersistence;
import java.util.ArrayList;

import java.util.List;

/**
 * Contains the business rules for managing products at GameZone Unicesar,
 * such as registering new products, listing available inventory, and
 * updating stock when a sale is made.
 */
public class ProductService {

    private final ProductPersistence productPersistence;
    private List<Product> products;

    /**
     * Creates a new ProductService, loading the currently stored products
     * from the repository.
     */
    public ProductService() {
        this.productPersistence = new ProductPersistence();
        this.products = productPersistence.load();
    }

    /**
     * Registers a new product and immediately persists the updated list.
     *
     * @param product the product to register
     */
    public void registerProduct(Product product) {
        products.add(product);
        productPersistence.save(products);
    }

    /**
     * Returns the full list of products currently available.
     *
     * @return the list of registered products
     */
    public List<Product> listAll() {
        return new ArrayList<>(products);
    }

    /**
     * Finds a product by its unique identifier.
     *
     * @param productId the id of the product to find
     * @return the matching product, or null if not found
     */
    public Product findById(String productId) {
        for (Product product : products) {
            if (product.getId().equals(productId)) {
                return product;
            }
        }
        return null;
    }

    /**
     * Reduces the stock of a given product by the specified amount,
     * used when a sale is registered.
     *
     * @param productId the id of the product being sold
     * @param amount    the quantity to subtract from stock
     * @return true if the stock was successfully reduced, false if there
     *         is not enough stock available or the product does not exist
     */
    public boolean reduceStock(String productId, int amount) {
        Product product = findById(productId);
        if (product == null) {
            return false;
        }
        if (product.getStock() < amount) {
            return false;
        }
        product.setStock(product.getStock() - amount);
        productPersistence.save(products);
        return true;
    }

    /**
     * Checks whether a product has enough stock available for a given quantity.
     *
     * @param productId the id of the product to check
     * @param amount    the quantity requested
     * @return true if there is enough stock, false otherwise
     */
    public boolean hasEnoughStock(String productId, int amount) {
        Product product = findById(productId);
        return product != null && product.getStock() >= amount;
    }
}