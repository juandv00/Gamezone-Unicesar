package com.gamezone.validation;

import com.gamezone.exceptions.BusinessRuleException;
import com.gamezone.exceptions.InvalidDataException;
import com.gamezone.exceptions.ResourceNotFoundException;
import com.gamezone.model.Accessory;
import com.gamezone.model.Client;
import com.gamezone.model.Product;
import com.gamezone.model.Sale;
import com.gamezone.model.Seller;
import java.util.List;

/**
 * Groups the validations of the sale module. Its methods are static and are
 * called by the services before executing a business operation; each one
 * throws the corresponding custom exception when a rule is not met.
 * Validators do not access services or repositories.
 */
public final class SaleValidator {

    private SaleValidator() {
        // Utility class: it is not meant to be instantiated.
    }

    /**
     * Validates the data of a sale that includes only products: the client
     * and the seller are required, and the list of products cannot be empty.
     *
     * @param client   the client making the purchase
     * @param seller   the seller attending the sale
     * @param products the products of the sale
     * @throws InvalidDataException  if the client or the seller is missing
     * @throws BusinessRuleException if the sale has no products
     */
    public static void validateSaleData(Client client, Seller seller, List<Product> products) {
        validateSaleData(client, seller, products, null);
    }

    /**
     * Validates the data of a sale that can include products and
     * accessories: the client and the seller are required, and the sale must
     * have at least one item (integration adjustment A3 allows sales with
     * only accessories).
     *
     * @param client      the client making the purchase
     * @param seller      the seller attending the sale
     * @param products    the products of the sale (may be null or empty)
     * @param accessories the accessories of the sale (may be null or empty)
     * @throws InvalidDataException  if the client or the seller is missing
     * @throws BusinessRuleException if the sale has no items
     */
    public static void validateSaleData(Client client, Seller seller, List<Product> products,
                                        List<Accessory> accessories) {
        if (client == null) {
            throw new InvalidDataException("cliente", "es obligatorio.");
        }
        if (seller == null) {
            throw new InvalidDataException("vendedor", "es obligatorio.");
        }
        boolean noProducts = products == null || products.isEmpty();
        boolean noAccessories = accessories == null || accessories.isEmpty();
        if (noProducts && noAccessories) {
            throw new BusinessRuleException("La venta debe incluir al menos un producto o accesorio.");
        }
    }

    /**
     * Validates that a sale was found.
     *
     * @param sale   the sale returned by the search, or null
     * @param saleId the id that was searched
     * @throws ResourceNotFoundException if the sale is null
     */
    public static void validateSaleExists(Sale sale, String saleId) {
        if (sale == null) {
            throw new ResourceNotFoundException("Venta", saleId);
        }
    }
}
