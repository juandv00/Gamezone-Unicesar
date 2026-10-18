package com.gamezone.persistence;

import com.gamezone.model.Return;
import java.util.List;

/**
 * Abstraction of the place where the returns are stored. The services depend on
 * this interface instead of on a concrete file class, so the storage can
 * change without changing the business rules.
 */
public interface ReturnStorage {

    /**
     * Saves the given returns, replacing the ones previously stored.
     *
     * @param returns the returns to store
     */
    void saveAll(List<Return> returns);

    /**
     * Loads the stored returns as records with the ids of their sale and items.
     *
     * @return the records found, or an empty list if there are none
     */
    List<ReturnRecord> loadAll();
}
