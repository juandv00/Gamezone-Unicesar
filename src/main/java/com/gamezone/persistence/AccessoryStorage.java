package com.gamezone.persistence;

import com.gamezone.model.Accessory;
import java.util.List;

/**
 * Abstraction of the place where the accessories are stored. The services depend on
 * this interface instead of on a concrete file class, so the storage can
 * change without changing the business rules.
 */
public interface AccessoryStorage {

    /**
     * Saves the given accessories, replacing the ones previously stored.
     *
     * @param accessories the accessories to store
     */
    void saveAll(List<Accessory> accessories);

    /**
     * Loads the stored accessories.
     *
     * @return the accessories found, or an empty list if there are none
     */
    List<Accessory> loadAll();
}
