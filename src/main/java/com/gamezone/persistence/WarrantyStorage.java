package com.gamezone.persistence;

import com.gamezone.model.Product;
import com.gamezone.model.Sale;
import com.gamezone.model.Warranty;
import java.util.List;

/**
 * Abstraction of the place where the warranties are stored. The services depend on
 * this interface instead of on a concrete file class, so the storage can
 * change without changing the business rules.
 */
public interface WarrantyStorage {

    /**
     * Saves the given warranties, replacing the ones previously stored.
     *
     * @param warranties the warranties to store
     */
    void saveAll(List<Warranty> warranties);

    /**
     * Loads the stored warranties as records with the ids of their sale and product.
     *
     * @return the records found, or an empty list if there are none
     */
    List<WarrantyRecord> loadAll();

    /**
     * Rebuilds a warranty of the type of a stored record, once its sale and product are resolved.
     *
     * @param record the stored record
     * @param product the covered product
     * @param sale the sale of the warranty
     * @return the warranty, or null if the type of the record is not known
     */
    Warranty toWarranty(WarrantyRecord record, Product product, Sale sale);
}
