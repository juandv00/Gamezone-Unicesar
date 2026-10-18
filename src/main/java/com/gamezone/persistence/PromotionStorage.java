package com.gamezone.persistence;

import com.gamezone.model.Promotion;
import java.util.List;

/**
 * Abstraction of the place where the promotions are stored. The services depend on
 * this interface instead of on a concrete file class, so the storage can
 * change without changing the business rules.
 */
public interface PromotionStorage {

    /**
     * Saves the given promotions, replacing the ones previously stored.
     *
     * @param promotions the promotions to store
     */
    void saveAll(List<Promotion> promotions);

    /**
     * Loads the stored promotions.
     *
     * @return the promotions found, or an empty list if there are none
     */
    List<Promotion> loadAll();
}
