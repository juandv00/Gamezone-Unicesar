package com.gamezone.persistence;

import com.gamezone.model.BasicWarranty;
import com.gamezone.model.ExtendedWarranty;
import com.gamezone.model.Product;
import com.gamezone.model.Sale;
import com.gamezone.model.Warranty;
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

/**
 * Handles saving and loading Warranty data to and from a file, so that
 * warranties persist between application runs. A Warranty references a Sale and
 * a Product; this class resolves those references by id using SalePersistence
 * directly, avoiding a dependency on SaleService to prevent a circular
 * dependency (SaleService will depend on WarrantyService for the integration).
 * <p>
 * Each warranty type is registered once in the constructor with its type
 * code and the way to create it. Saving, loading, and rebuilding a warranty
 * look up the registered type instead of checking the class or the code with
 * conditionals, so a new warranty type only needs a new registration.
 */
public class WarrantyRepository implements WarrantyStorage {

    private static final String FILE_PATH = "data/warranties.csv";
    private static final String DELIMITER = ";";

    private final Map<Class<? extends Warranty>, String> codes = new HashMap<>();
    private final Map<String, WarrantyFactory> factories = new HashMap<>();

    /**
     * Creates a warranty of one type from its stored data and the sale and
     * product already resolved by the service.
     */
    @FunctionalInterface
    private interface WarrantyFactory {

        /**
         * Creates the warranty.
         *
         * @param id        the warranty id
         * @param product   the covered product
         * @param sale      the sale the warranty belongs to
         * @param startDate the date the coverage begins
         * @return the new warranty
         */
        Warranty create(String id, Product product, Sale sale, LocalDate startDate);
    }

    /**
     * Creates the repository and registers each warranty type (basic and
     * extended) with its type code.
     */
    public WarrantyRepository() {
        registerType(BasicWarranty.class, "BASIC", BasicWarranty::new);
        registerType(ExtendedWarranty.class, "EXTENDED", ExtendedWarranty::new);
    }

    /**
     * Registers a warranty type with the code stored in the file and the way
     * to create it.
     *
     * @param type    the class of the warranty type
     * @param code    the type code stored at the start of the line
     * @param factory creates a warranty of the type
     */
    private void registerType(Class<? extends Warranty> type, String code, WarrantyFactory factory) {
        codes.put(type, code);
        factories.put(code, factory);
    }

    /**
     * Persists the given warranties, overwriting any previously stored data.
     * Only identifiers are written: type;id;saleId;productId;startDate.
     *
     * @param warranties the warranties to save
     */
    @Override
    public void saveAll(List<Warranty> warranties) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_PATH))) {
            for (Warranty w : warranties) {
                bw.write(toLine(w));
                bw.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error al guardar las garantías: " + e.getMessage());
        }
    }

    /**
     * Loads the stored warranty records without resolving any reference.
     * Malformed lines are skipped.
     *
     * @return the records found, or an empty list if the file does not exist
     */
    @Override
    public List<WarrantyRecord> loadAll() {
        List<WarrantyRecord> records = new ArrayList<>();
        File file = new File(FILE_PATH);
        if (!file.exists()) {
            return records;
        }
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                WarrantyRecord record = fromLine(line);
                if (record != null) {
                    records.add(record);
                }
            }
        } catch (IOException e) {
            System.out.println("Error al cargar las garantías: " + e.getMessage());
        }
        return records;
    }

    /**
     * Rebuilds a warranty from a stored record, using the type registered for
     * the record's type code. The service resolves the sale and the product
     * before calling this method.
     *
     * @param record  the stored record
     * @param product the covered product, already resolved
     * @param sale    the sale of the warranty, already resolved
     * @return the warranty, or null if the type code is not registered
     */
    @Override
    public Warranty toWarranty(WarrantyRecord record, Product product, Sale sale) {
        WarrantyFactory factory = factories.get(record.type());
        if (factory == null) {
            return null;
        }
        return factory.create(record.id(), product, sale, record.startDate());
    }

    private String toLine(Warranty w) {
        String type = codes.get(w.getClass());
        return type + DELIMITER + w.getId() + DELIMITER
                + w.getSale().getId() + DELIMITER
                + w.getProduct().getId() + DELIMITER
                + w.getStartDate();
    }

    private WarrantyRecord fromLine(String line) {
        try {
            String[] parts = line.split(DELIMITER);
            if (parts.length < 5) {
                return null;
            }
            if (!factories.containsKey(parts[0])) {
                return null;
            }
            return new WarrantyRecord(parts[0], parts[1], parts[2], parts[3],
                    LocalDate.parse(parts[4]));
        } catch (RuntimeException e) {
            System.out.println("Se omitió una línea inválida de garantía: " + line);
            return null;
        }
    }
}
