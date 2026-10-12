package com.gamezone.validation;

import com.gamezone.exceptions.BusinessRuleException;
import com.gamezone.exceptions.InvalidDataException;
import com.gamezone.exceptions.ResourceNotFoundException;
import com.gamezone.model.Product;

/**
 * Groups the validations of the product module. Its methods are static and
 * are called by the services before executing a business operation; each
 * one throws the corresponding custom exception when a rule is not met.
 * Validators do not access services or repositories.
 */
public final class ProductValidator {

    private ProductValidator() {
        // Utility class: it is not meant to be instantiated.
    }

    /**
     * Validates the common data of a product: the id and the title cannot be
     * null or empty, the price must be positive, and the stock cannot be
     * negative.
     *
     * @param id    the product id
     * @param title the product title
     * @param price the product price
     * @param stock the units available
     * @throws InvalidDataException if any value is not valid
     */
    public static void validateProductData(String id, String title, double price, int stock) {
        if (id == null || id.isBlank()) {
            throw new InvalidDataException("identificador", "no puede estar vacío.");
        }
        if (title == null || title.isBlank()) {
            throw new InvalidDataException("título", "no puede estar vacío.");
        }
        if (price <= 0) {
            throw new InvalidDataException("precio", "debe ser mayor que cero.");
        }
        if (stock < 0) {
            throw new InvalidDataException("stock", "no puede ser negativo.");
        }
    }

    /**
     * Validates that a product has enough stock for the requested quantity.
     *
     * @param product           the product to check
     * @param requestedQuantity the number of units requested
     * @throws InvalidDataException  if the requested quantity is not positive
     * @throws BusinessRuleException if the product does not have enough stock
     */
    public static void validateStockAvailability(Product product, int requestedQuantity) {
        if (requestedQuantity <= 0) {
            throw new InvalidDataException("cantidad", "debe ser mayor que cero.");
        }
        if (product.getStock() < requestedQuantity) {
            throw new BusinessRuleException("Stock insuficiente para " + product.getTitle()
                    + ": disponibles " + product.getStock() + ", solicitadas " + requestedQuantity + ".");
        }
    }

    /**
     * Validates that a product was found.
     *
     * @param product   the product returned by the search, or null
     * @param productId the id that was searched
     * @throws ResourceNotFoundException if the product is null
     */
    public static void validateProductExists(Product product, String productId) {
        if (product == null) {
            throw new ResourceNotFoundException("Producto", productId);
        }
    }
}
