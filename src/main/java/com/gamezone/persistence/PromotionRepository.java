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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Handles saving and loading Promotion data (percentage, category, and bulk
 * purchase discounts) to and from a file, so that this information persists
 * between application runs.
 * <p>
 * The format of each promotion type is registered once in the constructor:
 * a writer for its class and a reader for its type code. Saving and loading
 * look up the registered format instead of checking the type with a chain
 * of conditionals, so a new promotion type only needs a new registration.
 */
public class PromotionRepository {

    private static final String FILE_PATH = "data/promotions.csv";
    private static final String DELIMITER = ";";

    private final Map<Class<? extends Promotion>, Function<Promotion, String>> writers = new HashMap<>();
    private final Map<String, PromotionReader> readers = new HashMap<>();

    /**
     * Rebuilds a promotion of one type from the fields of a stored line.
     */
    @FunctionalInterface
    private interface PromotionReader {

        /**
         * Creates the promotion from its common fields and the stored line.
         *
         * @param id        the promotion id
         * @param name      the promotion name
         * @param startDate the first day of validity
         * @param endDate   the last day of validity
         * @param parts     every field of the stored line
         * @return the reconstructed promotion
         */
        Promotion read(String id, String name, LocalDate startDate, LocalDate endDate, String[] parts);
    }

    /**
     * Creates the repository and registers the file format of each promotion
     * type (percentage, category, and bulk purchase discounts).
     */
    public PromotionRepository() {
        registerFormat(PercentageDiscount.class, "PERCENTAGE",
                percentage -> String.valueOf(percentage.getPercentage()),
                (id, name, startDate, endDate, parts) -> new PercentageDiscount(id, name, startDate,
                        endDate, Double.parseDouble(parts[5])));
        registerFormat(CategoryDiscount.class, "CATEGORY",
                category -> category.getPercentage() + DELIMITER + category.getTargetCategory(),
                (id, name, startDate, endDate, parts) -> new CategoryDiscount(id, name, startDate,
                        endDate, Double.parseDouble(parts[5]), parts[6]));
        registerFormat(BulkPurchaseDiscount.class, "BULK",
                bulk -> bulk.getMinimumQuantity() + DELIMITER + bulk.getPercentage(),
                (id, name, startDate, endDate, parts) -> new BulkPurchaseDiscount(id, name, startDate,
                        endDate, Integer.parseInt(parts[5]), Double.parseDouble(parts[6])));
    }

    /**
     * Registers how a promotion type is written to and read from the file.
     * Every line starts with the type code followed by the common fields
     * (id, name, start date, end date) and then the fields of the type.
     *
     * @param type           the class of the promotion type
     * @param code           the type code stored at the start of the line
     * @param specificFields builds the fields of the type, already delimited
     * @param reader         rebuilds a promotion of the type from a line
     * @param <T>            the promotion type
     */
    private <T extends Promotion> void registerFormat(Class<T> type, String code,
                                                      Function<T, String> specificFields,
                                                      PromotionReader reader) {
        writers.put(type, promotion -> code + DELIMITER + promotion.getId() + DELIMITER
                + promotion.getName() + DELIMITER + promotion.getStartDate() + DELIMITER
                + promotion.getEndDate() + DELIMITER + specificFields.apply(type.cast(promotion)));
        readers.put(code, reader);
    }

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
            System.out.println("Error al guardar las promociones: " + e.getMessage());
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
            System.out.println("Error al cargar las promociones: " + e.getMessage());
        }
        return promotions;
    }

    /**
     * Converts a single promotion into its delimited text representation,
     * using the format registered for its type.
     *
     * @param promotion the promotion to convert
     * @return the delimited line representing this promotion, or an empty
     * string if the subtype is not recognized
     */
    private String toLine(Promotion promotion) {
        Function<Promotion, String> writer = writers.get(promotion.getClass());
        if (writer == null) {
            return "";
        }
        return writer.apply(promotion);
    }

    /**
     * Reconstructs a single Promotion from its delimited text representation,
     * using the format registered for the type code in the first column.
     * Malformed lines (missing fields, invalid numbers or dates) are skipped
     * with a warning instead of stopping the whole load.
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

            PromotionReader reader = readers.get(type);
            if (reader != null) {
                return reader.read(id, name, startDate, endDate, parts);
            }
        } catch (RuntimeException e) {
            System.out.println("Se omitió una línea inválida de promoción: " + line);
        }
        return null;
    }
}
