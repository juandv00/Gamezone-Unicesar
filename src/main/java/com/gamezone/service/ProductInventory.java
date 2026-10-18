package com.gamezone.service;

/**
 * The inventory role of the products: the queries of {@link ProductLookup}
 * plus the stock changes that sales and returns make. The sale and return
 * flows depend on this interface instead of on the whole
 * {@link ProductService}, so they do not depend on the registration and
 * listing of products.
 */
public interface ProductInventory extends ProductLookup {

    /**
     * Reduces the stock of a product after a sale.
     *
     * @param productId the id of the product
     * @param amount    the number of units sold
     * @return true when the stock was reduced
     */
    boolean reduceStock(String productId, int amount);

    /**
     * Restores the stock of a product after a return.
     *
     * @param productId the id of the product
     * @param quantity  the number of units returned
     * @return true when the stock was restored
     */
    boolean restoreStock(String productId, int quantity);
}
