package com.gamezone.service;

import com.gamezone.model.BulkPurchaseDiscount;
import com.gamezone.model.CategoryDiscount;
import com.gamezone.model.PercentageDiscount;
import com.gamezone.model.Promotion;
import com.gamezone.model.Sale;
import com.gamezone.persistence.PromotionRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Contains the business rules for managing promotions at GameZone Unicesar,
 * such as registering new promotions, listing active campaigns, and selecting
 * the best applicable promotion for a given sale.
 */
public class PromotionService {

    private final PromotionRepository promotionRepository;
    private List<Promotion> promotions;

    /**
     * Creates a new PromotionService, loading the currently stored promotions
     * from the repository.
     *
     * @param promotionRepository the repository used to persist promotions
     */
    public PromotionService(PromotionRepository promotionRepository) {
        this.promotionRepository = promotionRepository;
        this.promotions = promotionRepository.loadAll();
    }

    /**
     * Registers a new percentage-based promotion and persists the updated list.
     *
     * @param id unique identifier of the promotion
     * @param name display name of the promotion
     * @param startDate start date of the promotion's validity period
     * @param endDate end date of the promotion's validity period
     * @param percentage discount percentage applied to the sale total
     * @return the newly registered PercentageDiscount instance
     * @throws IllegalArgumentException if a promotion with the same id already
     * exists
     */
    public PercentageDiscount registerPercentageDiscount(String id, String name,
                                                         LocalDate startDate, LocalDate endDate, double percentage) {
        ensureIdIsAvailable(id);
        PercentageDiscount promotion = new PercentageDiscount(id, name, startDate, endDate, percentage);
        promotions.add(promotion);
        promotionRepository.saveAll(promotions);
        return promotion;
    }

    /**
     * Registers a new category-based promotion and persists the updated list.
     *
     * @param id unique identifier of the promotion
     * @param name display name of the promotion
     * @param startDate start date of the promotion's validity period
     * @param endDate end date of the promotion's validity period
     * @param percentage discount percentage applied to matching products
     * @param targetCategory the category this promotion applies to: VIDEOGAME,
     * CONSOLE, or ACCESSORY
     * @return the newly registered CategoryDiscount instance
     * @throws IllegalArgumentException if the target category is not VIDEOGAME,
     * CONSOLE, or ACCESSORY, or if a promotion with the same id already exists
     */
    public CategoryDiscount registerCategoryDiscount(String id, String name,
                                                     LocalDate startDate, LocalDate endDate, double percentage, String targetCategory) {
        if (targetCategory == null
                || !(targetCategory.equalsIgnoreCase("VIDEOGAME")
                || targetCategory.equalsIgnoreCase("CONSOLE")
                || targetCategory.equalsIgnoreCase("ACCESSORY"))) {
            throw new IllegalArgumentException(
                    "The target category must be VIDEOGAME, CONSOLE, or ACCESSORY.");
        }
        ensureIdIsAvailable(id);
        CategoryDiscount promotion = new CategoryDiscount(id, name, startDate, endDate, percentage, targetCategory);
        promotions.add(promotion);
        promotionRepository.saveAll(promotions);
        return promotion;
    }

    /**
     * Registers a new bulk-purchase promotion and persists the updated list.
     *
     * @param id unique identifier of the promotion
     * @param name display name of the promotion
     * @param startDate start date of the promotion's validity period
     * @param endDate end date of the promotion's validity period
     * @param minimumQuantity minimum number of products required in the sale
     * @param percentage discount percentage applied to the sale total
     * @return the newly registered BulkPurchaseDiscount instance
     * @throws IllegalArgumentException if a promotion with the same id already
     * exists
     */
    public BulkPurchaseDiscount registerBulkPurchaseDiscount(String id, String name,
                                                             LocalDate startDate, LocalDate endDate, int minimumQuantity, double percentage) {
        ensureIdIsAvailable(id);
        BulkPurchaseDiscount promotion
                = new BulkPurchaseDiscount(id, name, startDate, endDate, minimumQuantity, percentage);
        promotions.add(promotion);
        promotionRepository.saveAll(promotions);
        return promotion;
    }

    /**
     * Returns all promotions currently registered, regardless of validity.
     *
     * @return the list of all registered promotions
     */
    public List<Promotion> listAllPromotions() {
        return new ArrayList<>(promotions);
    }

    /**
     * Returns the promotions that are currently active, based on today's date.
     *
     * @return the list of promotions valid for the current date
     */
    public List<Promotion> listActivePromotions() {
        List<Promotion> active = new ArrayList<>();
        LocalDate today = LocalDate.now();
        for (Promotion promotion : promotions) {
            if (promotion.isActive(today)) {
                active.add(promotion);
            }
        }
        return active;
    }

    /**
     * Finds, among the promotions active on the date of the given sale, the one
     * that would grant the highest monetary discount for that sale.
     *
     * @param sale the sale to evaluate promotions against
     * @return the best applicable promotion, or null if no promotion is active
     * on the sale date or the maximum discount would be zero
     */
    public Promotion findBestPromotionFor(Sale sale) {
        Promotion best = null;
        double bestDiscount = 0.0;

        for (Promotion promotion : promotions) {
            if (!promotion.isActive(sale.getDate())) {
                continue;
            }
            double discount = promotion.calculateDiscount(sale);
            if (discount > bestDiscount) {
                bestDiscount = discount;
                best = promotion;
            }
        }
        return best;
    }

    /**
     * Finds a promotion by its unique identifier.
     *
     * @param id the id of the promotion to find
     * @return the matching promotion, or null if not found
     */
    public Promotion findById(String id) {
        for (Promotion promotion : promotions) {
            if (promotion.getId().equals(id)) {
                return promotion;
            }
        }
        return null;
    }

    /**
     * Verifies that no registered promotion already uses the given id.
     *
     * @param id the id to check
     * @throws IllegalArgumentException if the id is already in use
     */
    private void ensureIdIsAvailable(String id) {
        if (findById(id) != null) {
            throw new IllegalArgumentException("A promotion with id " + id + " already exists.");
        }
    }
}