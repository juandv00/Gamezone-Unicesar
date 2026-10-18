package com.gamezone.service;

import com.gamezone.model.Product;

/**
 * Read-only queries over the registered products. The classes that only
 * need to find a product depend on this interface instead of on the whole
 * {@link ProductService}, so they do not depend on the operations that
 * register products or change the stock.
 */
public interface ProductLookup {

    /**
     * Finds a product by its id.
     *
     * @param productId the id of the product
     * @return the product, or null if it does not exist
     */
    Product findById(String productId);

    /**
     * Returns the product with the given id.
     *
     * @param productId the id of the product
     * @return the product
     * @throws com.gamezone.exceptions.ResourceNotFoundException if the product does not exist
     */
    Product getProductById(String productId);
}
