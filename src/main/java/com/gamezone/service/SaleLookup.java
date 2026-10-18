package com.gamezone.service;

import com.gamezone.model.Sale;
import java.util.List;

/**
 * Read-only queries over the registered sales. The classes that only need
 * to find or list sales depend on this interface instead of on the whole
 * {@link SaleService}, so they do not depend on the operations that
 * register sales.
 */
public interface SaleLookup {

    /**
     * Finds a sale by its id, ignoring upper and lower case.
     *
     * @param saleId the id of the sale
     * @return the sale, or null if it does not exist
     */
    Sale findById(String saleId);

    /**
     * Returns the sale with the given id.
     *
     * @param saleId the id of the sale
     * @return the sale
     * @throws com.gamezone.exceptions.ResourceNotFoundException if the sale does not exist
     */
    Sale getSaleById(String saleId);

    /**
     * Returns every registered sale.
     *
     * @return a copy of the list of sales
     */
    List<Sale> listAll();
}
