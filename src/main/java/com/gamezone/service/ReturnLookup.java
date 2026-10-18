package com.gamezone.service;

import com.gamezone.model.Return;
import java.util.List;

/**
 * Read-only queries over the registered returns. The classes that only need
 * to consult returns depend on this interface instead of on the whole
 * {@link ReturnService}, so they do not depend on the operation that
 * registers returns.
 */
public interface ReturnLookup {

    /**
     * Returns every registered return.
     *
     * @return a copy of the list of returns
     */
    List<Return> viewAllReturns();

    /**
     * Returns the returns of the sales of a client.
     *
     * @param customerId the id of the client
     * @return the returns of that client
     */
    List<Return> viewReturnsByCustomer(String customerId);

    /**
     * Returns the returns of a sale.
     *
     * @param saleId the id of the sale
     * @return the returns of that sale
     */
    List<Return> viewReturnsBySale(String saleId);
}
