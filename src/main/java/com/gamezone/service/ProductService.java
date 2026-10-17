package com.gamezone.service;

import com.gamezone.exceptions.InvalidDataException;
import com.gamezone.exceptions.ResourceNotFoundException;
import com.gamezone.model.Product;
import com.gamezone.persistence.ProductPersistence;
import com.gamezone.validation.ProductValidator;
import java.util.ArrayList;

import java.util.List;

/**
 * Contains the business rules for managing products at GameZone Unicesar,
 * such as registering new products, listing available inventory, and
 * updating stock when a sale is made. The data and rules are checked by
 * {@link ProductValidator} before each operation, which throws the custom
 * exceptions of the system when something is not valid.
 */
public class ProductService {

    private final ProductPersistence productPersistence;
    private List<Product> products;

    /**
     * Creates a new ProductService, loading the currently stored products
     * from the repository.
     */
    public ProductService() {
        this.productPersistence = new ProductPersistence();
        this.products = productPersistence.load();
    }

    /**
     * Registers a new product and immediately persists the updated list.
     *
     * @param product the product to register
     * @throws InvalidDataException if the id or title is empty, the price is
     *                              not positive, or the stock is negative
     */
    public void registerProduct(Product product) {
        ProductValidator.validateProductData(product.getId(), product.getTitle(),
                product.getPrice(), product.getStock());
        products.add(product);
        productPersistence.save(products);
    }

    /**
     * Returns the full list of products currently available.
     *
     * @return the list of registered products
     */
    public List<Product> listAll() {
        return new ArrayList<>(products);
    }

    /**
     * Finds a product by its unique identifier.
     *
     * @param productId the id of the product to find
     * @return the matching product, or null if not found
     */
    public Product findById(String productId) {
        for (Product product : products) {
            if (product.getId().equals(productId)) {
                return product;
            }
        }
        return null;
    }

    /**
     * Finds a product by its unique identifier, failing if it does not exist.
     * Unlike {@link #findById(String)}, it is meant for operations that cannot
     * continue without the product.
     *
     * @param productId the id of the product to find
     * @return the matching product
     * @throws ResourceNotFoundException if no product has the given id
     */
    public Product getProductById(String productId) {
        Product product = findById(productId);
        ProductValidator.validateProductExists(product, productId);
        return product;
    }

    /**
     * Reduces the stock of a given product by the specified amount,
     * used when a sale is registered.
     *
     * @param productId the id of the product being sold
     * @param amount    the quantity to subtract from stock
     * @return true when the stock was reduced
     * @throws ResourceNotFoundException if the product does not exist
     * @throws InvalidDataException      if the amount is not positive
     * @throws com.gamezone.exceptions.BusinessRuleException if there is not
     *         enough stock available
     */
    public boolean reduceStock(String productId, int amount) {
        Product product = getProductById(productId);
        ProductValidator.validateStockAvailability(product, amount);
        product.decreaseStock(amount);
        productPersistence.save(products);
        return true;
    }

    /**
     * Checks whether a product has enough stock available for a given quantity.
     *
     * @param productId the id of the product to check
     * @param amount    the quantity requested
     * @return true if there is enough stock, false otherwise
     */
    public boolean hasEnoughStock(String productId, int amount) {
        Product product = findById(productId);
        return product != null && product.hasEnoughStock(amount);
    }

    /**
     * Increases the stock of a given product by the specified quantity and
     * persists the change, used when products are returned. It is the
     * counterpart of {@link #reduceStock(String, int)}.
     *
     * @param productId the id of the product being returned
     * @param quantity  the quantity to add to the stock
     * @return true when the stock was restored
     * @throws ResourceNotFoundException if the product does not exist
     * @throws InvalidDataException      if the quantity is not positive
     */
    public boolean restoreStock(String productId, int quantity) {
        Product product = getProductById(productId);
        if (quantity <= 0) {
            throw new InvalidDataException("cantidad", "debe ser mayor que cero.");
        }
        product.increaseStock(quantity);
        productPersistence.save(products);
        return true;
    }
}