package com.gamezone.service;

import com.gamezone.model.Accessory;
import java.io.IOException;

/**
 * The inventory role of the accessories: finding an accessory and the stock
 * checks and changes that sales and returns make. The sale and return flows
 * depend on this interface instead of on the whole {@link AccessoryService},
 * so they do not depend on the registration, listing, and compatibility
 * operations of the accessories.
 */
public interface AccessoryInventory {

    /**
     * Finds an accessory by its id.
     *
     * @param id the id of the accessory
     * @return the accessory, or null if it does not exist
     */
    Accessory findById(String id);

    /**
     * Indicates whether an accessory has at least the given units in stock.
     *
     * @param accessoryId the id of the accessory
     * @param amount      the number of units requested
     * @return true if the accessory exists and has enough stock
     */
    boolean hasEnoughStock(String accessoryId, int amount);

    /**
     * Reduces the stock of an accessory after a sale.
     *
     * @param accessoryId the id of the accessory
     * @param amount      the number of units sold
     * @return true when the stock was reduced
     * @throws IOException if the accessories cannot be saved
     */
    boolean reduceStock(String accessoryId, int amount) throws IOException;

    /**
     * Restores the stock of an accessory after a return.
     *
     * @param accessoryId the id of the accessory
     * @param quantity    the number of units returned
     * @return true when the stock was restored
     */
    boolean restoreStock(String accessoryId, int quantity);
}
