package com.gamezone.service;

import com.gamezone.exceptions.BusinessRuleException;
import com.gamezone.exceptions.InvalidDataException;
import com.gamezone.exceptions.PersistenceException;
import com.gamezone.exceptions.ResourceNotFoundException;
import com.gamezone.model.Accessory;
import com.gamezone.model.Client;
import com.gamezone.model.ExtendedWarranty;
import com.gamezone.model.Product;
import com.gamezone.model.Promotion;
import com.gamezone.model.Sale;
import com.gamezone.model.Seller;
import com.gamezone.persistence.SaleStorage;
import com.gamezone.validation.ProductValidator;
import com.gamezone.validation.SaleValidator;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Contains the business rules for managing sales at GameZone Unicesar,
 * such as registering new sales with stock validation, updating inventory
 * automatically, and consulting sales history by client or seller.
 * Every sale follows one unified registration flow: validate the items and
 * their stock, create the sale, apply the best active promotion on the
 * subtotal, assign the console warranties (basic by default, or extended if
 * requested), update the inventory of products and accessories, and
 * persist the sale. The data and rules of a sale are checked by
 * {@link SaleValidator} and {@link ProductValidator}, which throw the custom
 * exceptions of the system when something is not valid; if any validation
 * fails, nothing is changed.
 */
public class SaleService implements SaleLookup {

    private static final String SALE_ID_PREFIX = "SALE-";

    private final SaleStorage saleStorage;
    private final ProductService productService;
    private final AccessoryService accessoryService;
    private final PromotionService promotionService;
    private final WarrantyService warrantyService;
    private List<Sale> sales;

    /**
     * Creates a new SaleService that supports selling products and
     * accessories together. The storage of the sales is injected, so the
     * service depends only on the {@link SaleStorage} abstraction.
     *
     * @param productService   the service used to validate and reduce product stock
     * @param accessoryService the service used to validate and reduce accessory stock
     * @param saleStorage      the storage used to load and save sales
     */
    public SaleService(ProductService productService, AccessoryService accessoryService,
                       SaleStorage saleStorage) {
        this(productService, accessoryService, saleStorage, null);
    }

    /**
     * Creates a new SaleService that supports accessories and applies the
     * best active promotion to every sale it registers.
     *
     * @param productService   the service used to validate and reduce product stock
     * @param accessoryService the service used to validate and reduce accessory stock
     * @param saleStorage      the storage used to load and save sales
     * @param promotionService the service used to find the best promotion for a
     *                         sale, or null to register sales without discounts
     */
    public SaleService(ProductService productService, AccessoryService accessoryService,
                       SaleStorage saleStorage, PromotionService promotionService) {
        this(productService, accessoryService, saleStorage, promotionService, null);
    }

    /**
     * Creates a new SaleService that supports accessories, applies the best
     * active promotion, and assigns warranties to the consoles of every sale
     * it registers.
     *
     * @param productService   the service used to validate and reduce product stock
     * @param accessoryService the service used to validate and reduce accessory stock
     * @param saleStorage      the storage used to load and save sales
     * @param promotionService the service used to find the best promotion for a
     *                         sale, or null to register sales without discounts
     * @param warrantyService  the service used to assign warranties to consoles,
     *                         or null to register sales without warranties
     */
    public SaleService(ProductService productService, AccessoryService accessoryService,
                       SaleStorage saleStorage, PromotionService promotionService,
                       WarrantyService warrantyService) {
        this.productService = productService;
        this.accessoryService = accessoryService;
        this.saleStorage = saleStorage;
        this.promotionService = promotionService;
        this.warrantyService = warrantyService;
        this.sales = saleStorage.load();
        assignMissingIds();
    }

    /**
     * Registers a new sale that includes only products. It follows the
     * unified registration flow of
     * {@link #registerSale(Client, Seller, List, List, List)}: every console
     * receives a basic warranty and the best active promotion is applied.
     *
     * @param client   the client making the purchase
     * @param seller   the seller attending the sale
     * @param products the list of products included in the sale
     * @return the registered Sale
     * @throws InvalidDataException      if the client or the seller is missing
     * @throws BusinessRuleException     if the product list is empty or a
     *                                   product does not have enough stock
     * @throws ResourceNotFoundException if a product is not registered
     */
    public Sale registerSale(Client client, Seller seller, List<Product> products) {
        SaleValidator.validateSaleData(client, seller, products);
        return processSale(client, seller, products, null, null);
    }

    /**
     * Registers a new sale that includes products and accessories, without
     * extended warranties. It follows the unified registration flow of
     * {@link #registerSale(Client, Seller, List, List, List)}.
     *
     * @param client      the client making the purchase
     * @param seller      the seller attending the sale
     * @param products    the list of products included in the sale (may be empty)
     * @param accessories the list of accessories included in the sale (may be empty)
     * @return the registered Sale
     * @throws InvalidDataException  if the client or the seller is missing
     * @throws BusinessRuleException if the sale has no items or an item does
     *                               not have enough stock
     * @throws PersistenceException  if the sale or the stock cannot be saved
     * @throws IllegalStateException if accessories are given but this service
     *                               was created without an AccessoryService
     */
    public Sale registerSale(Client client, Seller seller, List<Product> products,
                             List<Accessory> accessories) {
        return registerSale(client, seller, products, accessories, null);
    }

    /**
     * Registers a new sale with products, accessories, and optional extended
     * warranties, following the unified registration flow:
     * <ol>
     *   <li>Validate that the sale has at least one item.</li>
     *   <li>Validate the stock of every product and accessory (repeated items
     *       count as several units).</li>
     *   <li>Create the sale (with its id) and calculate its subtotal.</li>
     *   <li>Apply the best active promotion, calculated only on the subtotal.</li>
     *   <li>Assign a warranty to every console: extended if its id is in
     *       {@code productIdsWithExtendedWarranty} (replacing the basic one),
     *       basic otherwise, adding up the cost of the extended ones.</li>
     *   <li>The final total is subtotal - discount + extended warranty cost
     *       ({@link Sale#calculateFinalTotal()}).</li>
     *   <li>Update the inventory through ProductService or AccessoryService,
     *       depending on the type of each item.</li>
     *   <li>Persist the sale (the warranties are persisted by WarrantyService
     *       when they are assigned).</li>
     * </ol>
     * If any validation fails, a custom exception is thrown and nothing is
     * changed.
     *
     * @param client                         the client making the purchase
     * @param seller                         the seller attending the sale
     * @param products                       the list of products included in the sale (may be empty)
     * @param accessories                    the list of accessories included in the sale (may be empty)
     * @param productIdsWithExtendedWarranty the ids of the consoles that must receive an
     *                                       extended warranty (may be null or empty)
     * @return the registered Sale
     * @throws InvalidDataException      if the client or the seller is missing
     * @throws BusinessRuleException     if the sale has no items, an item does not
     *                                   have enough stock, or an extended warranty
     *                                   is requested for a product that is not a
     *                                   console of the sale
     * @throws ResourceNotFoundException if a product is not registered
     * @throws PersistenceException      if the sale or the stock cannot be saved
     * @throws IllegalStateException     if accessories or extended warranties are
     *                                   given but this service does not support them
     */
    public Sale registerSale(Client client, Seller seller, List<Product> products,
                             List<Accessory> accessories, List<String> productIdsWithExtendedWarranty) {
        return processSale(client, seller, products, accessories, productIdsWithExtendedWarranty);
    }

    /**
     * Registers a new sale from the ids of its items. Each id is resolved as
     * a product first and, if no product has that id, as an accessory; then
     * the sale follows the unified registration flow of
     * {@link #registerSale(Client, Seller, List, List, List)}.
     *
     * @param client                         the client making the purchase
     * @param seller                         the seller attending the sale
     * @param itemIds                        the ids of the products and accessories sold;
     *                                       an id repeated n times means n units
     * @param productIdsWithExtendedWarranty the ids of the consoles that must receive an
     *                                       extended warranty (may be null or empty)
     * @return the registered Sale
     * @throws ResourceNotFoundException if an id matches no product or accessory
     * @throws InvalidDataException      if the client or the seller is missing
     * @throws BusinessRuleException     if the sale has no items, an item does not
     *                                   have enough stock, or an extended warranty
     *                                   is requested for a product that is not a
     *                                   console of the sale
     * @throws PersistenceException      if the sale or the stock cannot be saved
     */
    public Sale registerSaleByItemIds(Client client, Seller seller, List<String> itemIds,
                                      List<String> productIdsWithExtendedWarranty) {
        List<Product> products = new ArrayList<>();
        List<Accessory> accessories = new ArrayList<>();
        if (itemIds != null) {
            for (String itemId : itemIds) {
                Product product = productService.findById(itemId);
                if (product != null) {
                    products.add(product);
                    continue;
                }
                Accessory accessory = accessoryService != null ? accessoryService.findById(itemId) : null;
                if (accessory == null) {
                    throw new ResourceNotFoundException("Producto o accesorio", itemId);
                }
                accessories.add(accessory);
            }
        }
        return processSale(client, seller, products, accessories, productIdsWithExtendedWarranty);
    }

    /**
     * Executes the unified sale registration flow described in
     * {@link #registerSale(Client, Seller, List, List, List)}.
     */
    private Sale processSale(Client client, Seller seller, List<Product> products,
                             List<Accessory> accessories, List<String> productIdsWithExtendedWarranty) {
        List<Product> saleProducts = products == null ? new ArrayList<>() : new ArrayList<>(products);
        List<Accessory> saleAccessories = accessories == null ? new ArrayList<>() : new ArrayList<>(accessories);
        List<String> extendedIds = productIdsWithExtendedWarranty == null
                ? new ArrayList<>() : new ArrayList<>(productIdsWithExtendedWarranty);

        // 1. The sale must have a client, a seller, and at least one item.
        SaleValidator.validateSaleData(client, seller, saleProducts, saleAccessories);
        if (!saleAccessories.isEmpty() && accessoryService == null) {
            throw new IllegalStateException("This SaleService was created without accessory support.");
        }
        if (!extendedIds.isEmpty() && warrantyService == null) {
            throw new IllegalStateException("This SaleService was created without warranty support.");
        }
        for (String productId : extendedIds) {
            if (!containsWarrantyEligibleProduct(saleProducts, productId)) {
                throw new BusinessRuleException("La garantía extendida solo se puede asignar a las consolas"
                        + " de la venta, y " + productId + " no es una de ellas.");
            }
        }

        // 2. Validate the stock of every product and accessory.
        Map<String, Integer> productUnits = countProductUnits(saleProducts);
        Map<String, Integer> accessoryUnits = countAccessoryUnits(saleAccessories);
        for (Map.Entry<String, Integer> entry : productUnits.entrySet()) {
            Product product = productService.getProductById(entry.getKey());
            ProductValidator.validateStockAvailability(product, entry.getValue());
        }
        for (Map.Entry<String, Integer> entry : accessoryUnits.entrySet()) {
            if (!accessoryService.hasEnoughStock(entry.getKey(), entry.getValue())) {
                Accessory accessory = accessoryService.findById(entry.getKey());
                String name = accessory != null ? accessory.getTitle() : entry.getKey();
                throw new BusinessRuleException("Stock insuficiente para el accesorio " + name
                        + ": solicitadas " + entry.getValue() + ".");
            }
        }

        // 3. Create the sale; its subtotal is calculateTotal().
        Sale sale = new Sale(client, seller, saleProducts, saleAccessories);
        sale.setId(nextSaleId());

        // 4. Apply the best promotion, calculated only on the subtotal.
        applyBestPromotion(sale);

        // 5. Assign warranties to the consoles and add up the extended cost.
        assignWarranties(sale, extendedIds);

        // 6. The final total is subtotal - discount + warranty cost
        //    (Sale.calculateFinalTotal()).

        // 7. Update the inventory through the service of each item type.
        for (Map.Entry<String, Integer> entry : productUnits.entrySet()) {
            productService.reduceStock(entry.getKey(), entry.getValue());
        }
        try {
            for (Map.Entry<String, Integer> entry : accessoryUnits.entrySet()) {
                accessoryService.reduceStock(entry.getKey(), entry.getValue());
            }
        } catch (IOException e) {
            throw new PersistenceException("Error de escritura al guardar el stock de los accesorios"
                    + " de la venta " + sale.getId() + ".", e);
        }

        // 8. Persist the sale.
        sales.add(sale);
        saleStorage.save(sales);
        return sale;
    }

    /**
     * Returns the full history of sales registered in the system.
     *
     * @return the list of all registered sales
     */
    @Override
    public List<Sale> listAll() {
        return new ArrayList<>(sales);
    }

    /**
     * Finds a registered sale by its identifier.
     *
     * @param saleId the id of the sale to search for
     * @return the sale with the given id, or null if none exists
     */
    @Override
    public Sale findById(String saleId) {
        for (Sale sale : sales) {
            if (sale.getId() != null && sale.getId().equalsIgnoreCase(saleId)) {
                return sale;
            }
        }
        return null;
    }

    /**
     * Finds a registered sale by its identifier, failing if it does not
     * exist. Unlike {@link #findById(String)}, it is meant for operations
     * that cannot continue without the sale.
     *
     * @param saleId the id of the sale to search for
     * @return the sale with the given id
     * @throws ResourceNotFoundException if no sale has the given id
     */
    @Override
    public Sale getSaleById(String saleId) {
        Sale sale = findById(saleId);
        SaleValidator.validateSaleExists(sale, saleId);
        return sale;
    }

    /**
     * Returns the purchase history of a specific client.
     *
     * @param clientId the id of the client to search for
     * @return the list of sales made by the given client
     */
    public List<Sale> findByClient(String clientId) {
        List<Sale> result = new ArrayList<>();
        for (Sale sale : sales) {
            if (sale.getClient().getId().equals(clientId)) {
                result.add(sale);
            }
        }
        return result;
    }

    /**
     * Returns the sales attended by a specific seller.
     *
     * @param sellerId the id of the seller to search for
     * @return the list of sales attended by the given seller
     */
    public List<Sale> findBySeller(String sellerId) {
        List<Sale> result = new ArrayList<>();
        for (Sale sale : sales) {
            if (sale.getSeller().getId().equals(sellerId)) {
                result.add(sale);
            }
        }
        return result;
    }

    private Map<String, Integer> countProductUnits(List<Product> products) {
        Map<String, Integer> units = new LinkedHashMap<>();
        for (Product product : products) {
            units.merge(product.getId(), 1, Integer::sum);
        }
        return units;
    }

    private Map<String, Integer> countAccessoryUnits(List<Accessory> accessories) {
        Map<String, Integer> units = new LinkedHashMap<>();
        for (Accessory accessory : accessories) {
            units.merge(accessory.getId(), 1, Integer::sum);
        }
        return units;
    }

    /**
     * Asks the PromotionService for the best promotion for the given sale
     * and, if there is one, stores its name and discount in the sale. The
     * discount is capped at the sale subtotal, so a misconfigured promotion
     * can never produce a negative final total. Promotions are not
     * cumulative: at most one promotion is applied.
     *
     * @param sale the sale to apply the promotion to
     */
    private void applyBestPromotion(Sale sale) {
        if (promotionService == null) {
            return;
        }
        Promotion bestPromotion = promotionService.findBestPromotionFor(sale);
        if (bestPromotion == null) {
            return;
        }
        double discount = Math.min(bestPromotion.calculateDiscount(sale), sale.calculateTotal());
        sale.setAppliedPromotionName(bestPromotion.getName());
        sale.setDiscountAmount(discount);
    }

    /**
     * Generates the next available sale id, with the format SALE-n, where n
     * is one more than the highest number already in use.
     *
     * @return the next sale id
     */
    private String nextSaleId() {
        int highest = 0;
        for (Sale sale : sales) {
            String id = sale.getId();
            if (id != null && id.startsWith(SALE_ID_PREFIX)) {
                try {
                    highest = Math.max(highest, Integer.parseInt(id.substring(SALE_ID_PREFIX.length())));
                } catch (NumberFormatException e) {
                    // Ids with another format do not affect the numbering.
                }
            }
        }
        return SALE_ID_PREFIX + (highest + 1);
    }

    /**
     * Assigns an id to every loaded sale that does not have one (sales stored
     * before sales had ids) and persists the change, so their ids stay the
     * same in future executions.
     */
    private void assignMissingIds() {
        boolean assigned = false;
        for (Sale sale : sales) {
            if (sale.getId() == null) {
                sale.setId(nextSaleId());
                assigned = true;
            }
        }
        if (assigned) {
            saleStorage.save(sales);
        }
    }

    /**
     * Assigns a warranty to every console of the given sale: an extended
     * warranty if its id is in the given list, or a basic warranty otherwise.
     * Video games and accessories do not receive warranties. The total cost
     * of the extended warranties is stored in the sale.
     *
     * @param sale        the sale whose consoles receive warranties
     * @param extendedIds the ids of the consoles with extended warranty (may be null)
     */
    private void assignWarranties(Sale sale, List<String> extendedIds) {
        if (warrantyService == null) {
            return;
        }
        double warrantyCost = 0.0;
        for (Product product : sale.getProducts()) {
            if (!product.isWarrantyEligible()) {
                continue;
            }
            if (containsId(extendedIds, product.getId())) {
                ExtendedWarranty warranty = warrantyService.assignExtendedWarranty(product, sale, sale.getDate());
                warrantyCost += warranty.getAdditionalCost();
            } else {
                warrantyService.assignBasicWarranty(product, sale, sale.getDate());
            }
        }
        sale.setWarrantyCost(warrantyCost);
    }

    /**
     * Indicates whether the given products include one with the given id that
     * receives a warranty. Each product answers whether it receives one.
     *
     * @param products  the products of the sale
     * @param productId the id to look for
     * @return true if a product with that id receives a warranty
     */
    private boolean containsWarrantyEligibleProduct(List<Product> products, String productId) {
        for (Product product : products) {
            if (product.isWarrantyEligible() && product.getId().equalsIgnoreCase(productId)) {
                return true;
            }
        }
        return false;
    }

    private boolean containsId(List<String> ids, String productId) {
        if (ids == null) {
            return false;
        }
        for (String id : ids) {
            if (id.equalsIgnoreCase(productId)) {
                return true;
            }
        }
        return false;
    }
}