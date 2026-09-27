package com.gamezone.persistence;

import com.gamezone.model.BulkPurchaseDiscount;
import com.gamezone.model.CategoryDiscount;
import com.gamezone.model.PercentageDiscount;
import com.gamezone.model.Promotion;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles saving and loading Promotion data (percentage, category, and bulk
 * purchase discounts) to and from a file, so that this information persists
 * between application runs.
 */
public class PromotionRepository {

    private static final String FILE_PATH = "data/promotions.csv";
    private static final String DELIMITER = ";";

    /**
     * Persists the given list of promotions to the file, overwriting any
     * previously stored data.
     *
     * @param promotions the list of promotions to save
     */
    public void saveAll(List<Promotion> promotions) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_PATH))) {
            for (Promotion p : promotions) {
                bw.write(toLine(p));
                bw.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error saving promotions: " + e.getMessage());
        }
    }

    /**
     * Loads all promotions from the file, reconstructing each concrete subtype
     * based on its discriminator column.
     *
     * @return the list of promotions found, or an empty list if the file does
     * not exist
     */
    public List<Promotion> loadAll() {
        List<Promotion> promotions = new ArrayList<>();
        File file = new File(FILE_PATH);
        if (!file.exists()) {
            return promotions;
        }
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                Promotion promotion = fromLine(line);
                if (promotion != null) {
                    promotions.add(promotion);
                }
            }
        } catch (IOException e) {
            System.out.println("Error loading promotions: " + e.getMessage());
        }
        return promotions;
    }

    /**
     * Converts a single promotion into its delimited text representation,
     * choosing the fields to write based on its concrete subtype.
     *
     * @param promotion the promotion to convert
     * @return the delimited line representing this promotion, or an empty
     * string if the subtype is not recognized
     */
    private String toLine(Promotion promotion) {
        String common = promotion.getId() + DELIMITER + promotion.getName() + DELIMITER
                + promotion.getStartDate() + DELIMITER + promotion.getEndDate();

        if (promotion instanceof PercentageDiscount p) {
            return "PERCENTAGE" + DELIMITER + common + DELIMITER + p.getPercentage();

        } else if (promotion instanceof CategoryDiscount c) {
            return "CATEGORY" + DELIMITER + common + DELIMITER + c.getPercentage()
                    + DELIMITER + c.getTargetCategory();

        } else if (promotion instanceof BulkPurchaseDiscount b) {
            return "BULK" + DELIMITER + common + DELIMITER + b.getMinimumQuantity()
                    + DELIMITER + b.getPercentage();
        }
        return "";
    }

    /**
     * Reconstructs a single Promotion from its delimited text representation,
     * using the discriminator column to determine which concrete subtype to
     * instantiate. Malformed lines (missing fields, invalid numbers or dates)
     * are skipped with a warning instead of stopping the whole load.
     *
     * @param line the delimited line read from the file
     * @return the reconstructed Promotion, or null if the line is malformed or
     * its type is not recognized
     */
    private Promotion fromLine(String line) {
        if (line.isBlank()) {
            return null;
        }
        String[] parts = line.split(DELIMITER);
        try {
            String type = parts[0];
            String id = parts[1];
            String name = parts[2];
            LocalDate startDate = LocalDate.parse(parts[3]);
            LocalDate endDate = LocalDate.parse(parts[4]);

            if (type.equals("PERCENTAGE")) {
                double percentage = Double.parseDouble(parts[5]);
                return new PercentageDiscount(id, name, startDate, endDate, percentage);

            } else if (type.equals("CATEGORY")) {
                double percentage = Double.parseDouble(parts[5]);
                String targetCategory = parts[6];
                return new CategoryDiscount(id, name, startDate, endDate, percentage, targetCategory);

            } else if (type.equals("BULK")) {
                int minimumQuantity = Integer.parseInt(parts[5]);
                double percentage = Double.parseDouble(parts[6]);
                return new BulkPurchaseDiscount(id, name, startDate, endDate, minimumQuantity, percentage);
            }
        } catch (RuntimeException e) {
            System.out.println("Skipping malformed promotion line: " + line);
        }
        return null;
    }
}
