package com.gamezone.service;

import com.gamezone.model.Accessory;
import com.gamezone.model.Client;
import com.gamezone.model.Console;
import com.gamezone.model.ExtendedWarranty;
import com.gamezone.model.Product;
import com.gamezone.model.Promotion;
import com.gamezone.model.Sale;
import com.gamezone.model.Seller;
import com.gamezone.persistence.SalePersistence;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Contains the business rules for managing sales at GameZone Unicesar,
 * such as registering new sales with stock validation, updating inventory
 * automatically, and consulting sales history by client or seller.
 * Sales may include products and accessories; when a sale is registered,
 * the best active promotion is applied automatically and every console
 * receives a warranty (basic by default, or extended if requested).
 */
public class SaleService {

    private static final String SALE_ID_PREFIX = "SALE-";

    private final SalePersistence salePersistence;
    private final ProductService productService;
    private final AccessoryService accessoryService;
    private final PromotionService promotionService;
    private final WarrantyService warrantyService;
    private List<Sale> sales;

    /**
     * Creates a new SaleService, loading the currently stored sales and
     * using the given ProductService to validate and update stock when
     * a new sale is registered. A SaleService created with this constructor
     * does not support accessories.
     *
     * @param productService the service used to validate and reduce stock
     */
    public SaleService(ProductService productService) {
        this(productService, null, new SalePersistence());
    }

    /**
     * Creates a new SaleService that supports selling products and
     * accessories together. The sale persistence is injected so it can be
     * configured to resolve accessories when loading stored sales.
     *
     * @param productService   the service used to validate and reduce product stock
     * @param accessoryService the service used to validate and reduce accessory stock
     * @param salePersistence  the persistence used to load and save sales
     */
    public SaleService(ProductService productService, AccessoryService accessoryService,
                       SalePersistence salePersistence) {
        this(productService, accessoryService, salePersistence, null);
    }

    /**
     * Creates a new SaleService that supports accessories and applies the
     * best active promotion to every sale it registers.
     *
     * @param productService   the service used to validate and reduce product stock
     * @param accessoryService the service used to validate and reduce accessory stock
     * @param salePersistence  the persistence used to load and save sales
     * @param promotionService the service used to find the best promotion for a
     *                         sale, or null to register sales without discounts
     */
    public SaleService(ProductService productService, AccessoryService accessoryService,
                       SalePersistence salePersistence, PromotionService promotionService) {
        this(productService, accessoryService, salePersistence, promotionService, null);
    }

    /**
     * Creates a new SaleService that supports accessories, applies the best
     * active promotion, and assigns warranties to the consoles of every sale
     * it registers.
     *
     * @param productService   the service used to validate and reduce product stock
     * @param accessoryService the service used to validate and reduce accessory stock
     * @param salePersistence  the persistence used to load and save sales
     * @param promotionService the service used to find the best promotion for a
     *                         sale, or null to register sales without discounts
     * @param warrantyService  the service used to assign warranties to consoles,
     *                         or null to register sales without warranties
     */
    public SaleService(ProductService productService, AccessoryService accessoryService,
                       SalePersistence salePersistence, PromotionService promotionService,
                       WarrantyService warrantyService) {
        this.productService = productService;
        this.accessoryService = accessoryService;
        this.salePersistence = salePersistence;
        this.promotionService = promotionService;
        this.warrantyService = warrantyService;
        this.sales = salePersistence.load();
        assignMissingIds();
    }

    /**
     * Registers a new sale after validating that it contains at least
     * one product and that every product has enough stock available.
     * If valid, the stock of each product is reduced automatically, every
     * console receives a basic warranty, the best active promotion (if any)
     * is applied, and the sale is persisted.
     *
     * @param client   the client making the purchase
     * @param seller   the seller attending the sale
     * @param products the list of products included in the sale
     * @return the registered Sale if successful, or null if the sale
     *         could not be registered (empty product list or insufficient
     *         stock for any product)
     */
    public Sale registerSale(Client client, Seller seller, List<Product> products) {
        if (products == null || products.isEmpty()) {
            return null;
        }

        for (Product product : products) {
            if (!productService.hasEnoughStock(product.getId(), 1)) {
                return null;
            }
        }

        for (Product product : products) {
            productService.reduceStock(product.getId(), 1);
        }

        Sale sale = new Sale(client, seller, products);
        sale.setId(nextSaleId());
        assignWarranties(sale, null);
        applyBestPromotion(sale);
        sales.add(sale);
        salePersistence.save(sales);
        return sale;
    }

    /**
     * Registers a new sale that includes products and, optionally,
     * accessories. The sale must contain at least one product; accessories
     * can only be sold together with products. Each occurrence of
     * an item in its list counts as one unit, so if the same item appears
     * several times, the stock is validated against the total requested.
     * Stock is validated for every item before anything is reduced, so a
     * rejected sale leaves the inventory untouched. The best active
     * promotion (if any) is applied before the sale is persisted.
     *
     * @param client      the client making the purchase
     * @param seller      the seller attending the sale
     * @param products    the list of products included in the sale
     * @param accessories the list of accessories included in the sale (may be empty)
     * @return the registered Sale if successful, or null if the sale could
     *         not be registered (no products or insufficient stock for any item)
     * @throws IOException           if the accessory stock could not be persisted
     * @throws IllegalStateException if accessories are given but this service
     *                               was created without an AccessoryService
     */
    public Sale registerSale(Client client, Seller seller, List<Product> products,
                             List<Accessory> accessories) throws IOException {
        return registerSale(client, seller, products, accessories, null);
    }

    /**
     * Registers a new sale that includes products, optional accessories, and
     * optional extended warranties. It follows the same rules as
     * {@link #registerSale(Client, Seller, List, List)}; in addition, every
     * console in the sale receives a warranty: an extended warranty if its id
     * is in {@code productIdsWithExtendedWarranty} (which replaces the basic
     * one, since it covers the same and more), or a basic warranty otherwise.
     * The cost of the extended warranties (10% of each console price, per
     * unit) is added to the sale total and is not affected by promotions.
     *
     * @param client                         the client making the purchase
     * @param seller                         the seller attending the sale
     * @param products                       the list of products included in the sale
     * @param accessories                    the list of accessories included in the sale (may be empty)
     * @param productIdsWithExtendedWarranty the ids of the consoles that must receive an
     *                                       extended warranty (may be null or empty)
     * @return the registered Sale if successful, or null if the sale could
     *         not be registered (no products or insufficient stock for any item)
     * @throws IOException              if the accessory stock could not be persisted
     * @throws IllegalArgumentException if an extended warranty is requested for a
     *                                  product that is not a console of the sale
     * @throws IllegalStateException    if accessories or extended warranties are
     *                                  given but this service does not support them
     */
    public Sale registerSale(Client client, Seller seller, List<Product> products,
                             List<Accessory> accessories, List<String> productIdsWithExtendedWarranty)
            throws IOException {
        List<Product> saleProducts = products == null ? new ArrayList<>() : new ArrayList<>(products);
        List<Accessory> saleAccessories = accessories == null ? new ArrayList<>() : new ArrayList<>(accessories);

        if (saleProducts.isEmpty()) {
            return null;
        }
        if (!saleAccessories.isEmpty() && accessoryService == null) {
            throw new IllegalStateException("This SaleService was created without accessory support.");
        }
        List<String> extendedIds = productIdsWithExtendedWarranty == null
                ? new ArrayList<>() : new ArrayList<>(productIdsWithExtendedWarranty);
        if (!extendedIds.isEmpty() && warrantyService == null) {
            throw new IllegalStateException("This SaleService was created without warranty support.");
        }
        for (String productId : extendedIds) {
            if (!containsConsole(saleProducts, productId)) {
                throw new IllegalArgumentException(
                        "Extended warranty requested for " + productId + ", which is not a console in this sale.");
            }
        }

        Map<String, Integer> productUnits = countProductUnits(saleProducts);
        Map<String, Integer> accessoryUnits = countAccessoryUnits(saleAccessories);

        for (Map.Entry<String, Integer> entry : productUnits.entrySet()) {
            if (!productService.hasEnoughStock(entry.getKey(), entry.getValue())) {
                return null;
            }
        }
        for (Map.Entry<String, Integer> entry : accessoryUnits.entrySet()) {
            if (!accessoryService.hasEnoughStock(entry.getKey(), entry.getValue())) {
                return null;
            }
        }

        for (Map.Entry<String, Integer> entry : productUnits.entrySet()) {
            productService.reduceStock(entry.getKey(), entry.getValue());
        }
        for (Map.Entry<String, Integer> entry : accessoryUnits.entrySet()) {
            accessoryService.reduceStock(entry.getKey(), entry.getValue());
        }

        Sale sale = new Sale(client, seller, saleProducts, saleAccessories);
        sale.setId(nextSaleId());
        assignWarranties(sale, extendedIds);
        applyBestPromotion(sale);
        sales.add(sale);
        salePersistence.save(sales);
        return sale;
    }

    /**
     * Returns the full history of sales registered in the system.
     *
     * @return the list of all registered sales
     */
    public List<Sale> listAll() {
        return new ArrayList<>(sales);
    }

    /**
     * Finds a registered sale by its identifier.
     *
     * @param saleId the id of the sale to search for
     * @return the sale with the given id, or null if none exists
     */
    public Sale findById(String saleId) {
        for (Sale sale : sales) {
            if (sale.getId() != null && sale.getId().equalsIgnoreCase(saleId)) {
                return sale;
            }
        }
        return null;
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
            salePersistence.save(sales);
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
            if (!(product instanceof Console)) {
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

    private boolean containsConsole(List<Product> products, String productId) {
        for (Product product : products) {
            if (product instanceof Console && product.getId().equalsIgnoreCase(productId)) {
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