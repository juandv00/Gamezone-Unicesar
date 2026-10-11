package com.gamezone.persistence;

import java.time.LocalDate;

/**
 * Holds the identifier-only data of a stored warranty, exactly as it is
 * read from the file, so that the repository does not need to resolve
 * sales or products. The service is responsible for turning a record
 * into a Warranty.
 *
 * @param type      the discriminator, BASIC or EXTENDED
 * @param id        the warranty id
 * @param saleId    the id of the associated sale
 * @param productId the id of the covered product
 * @param startDate the date the coverage begins
 */
public record WarrantyRecord(String type, String id, String saleId,
        String productId, LocalDate startDate) {
}