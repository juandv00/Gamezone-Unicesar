package com.gamezone.persistence;

import com.gamezone.model.Sale;
import java.util.List;

/**
 * Abstraction of the place where the sales are stored. The services depend
 * on this interface instead of on a concrete file class, so the storage can
 * change (another file format, a database, or a test double) without
 * changing the business rules.
 */
public interface SaleStorage {

    /**
     * Saves the given sales, replacing the ones previously stored.
     *
     * @param sales the sales to store
     */
    void save(List<Sale> sales);

    /**
     * Loads the stored sales.
     *
     * @return the sales found, or an empty list if there are none
     */
    List<Sale> load();
}
