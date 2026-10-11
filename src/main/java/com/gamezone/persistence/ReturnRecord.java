package com.gamezone.persistence;

import java.time.LocalDate;
import java.util.List;

/**
 * Holds the data of a return exactly as it is stored in the returns file,
 * using only identifiers to reference the original sale and the returned
 * items. ReturnService resolves these identifiers into the real Sale,
 * Product and Accessory objects, so the persistence layer does not depend
 * on the service layer.
 *
 * @param id the id of the return
 * @param date the date the return was registered
 * @param saleId the id of the original sale
 * @param itemIds the ids of the returned items (products or accessories);
 * an id repeated n times means n units
 * @param reason the reason given for the return
 * @param warrantyRefund the amount refunded for cancelled warranties
 */
public record ReturnRecord(String id, LocalDate date, String saleId,
        List<String> itemIds, String reason, double warrantyRefund) {

    /**
     * Creates a new ReturnRecord, storing an unmodifiable copy of the item
     * ids.
     */
    public ReturnRecord {
        itemIds = List.copyOf(itemIds);
    }
}