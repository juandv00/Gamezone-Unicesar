package com.gamezone.persistence;

import com.gamezone.model.Product;
import java.util.List;

/**
 * Abstraction of the place where the products are stored. The services
 * depend on this interface instead of on a concrete file class, so the
 * storage can change without changing the business rules.
 */
public interface ProductStorage {

    /**
     * Saves the given products, replacing the ones previously stored.
     *
     * @param products the products to store
     */
    void save(List<Product> products);

    /**
     * Loads the stored products.
     *
     * @return the products found, or an empty list if there are none
     */
    List<Product> load();
}
