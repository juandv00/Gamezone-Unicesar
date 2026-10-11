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
import java.util.List;

/**
 * Handles saving and loading Warranty data to and from a file, so that
 * warranties persist between application runs. A Warranty references a Sale and
 * a Product; this class resolves those references by id using SalePersistence
 * directly, avoiding a dependency on SaleService to prevent a circular
 * dependency (SaleService will depend on WarrantyService for the integration).
 */
public class WarrantyRepository {

    private static final String FILE_PATH = "data/warranties.csv";
    private static final String DELIMITER = ";";

    /**
     * Persists the given warranties, overwriting any previously stored data.
     * Only identifiers are written: type;id;saleId;productId;startDate.
     *
     * @param warranties the warranties to save
     */
    public void saveAll(List<Warranty> warranties) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_PATH))) {
            for (Warranty w : warranties) {
                bw.write(toLine(w));
                bw.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error saving warranties: " + e.getMessage());
        }
    }

    /**
     * Loads the stored warranty records without resolving any reference.
     * Malformed lines are skipped.
     *
     * @return the records found, or an empty list if the file does not exist
     */
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
            System.out.println("Error loading warranties: " + e.getMessage());
        }
        return records;
    }

    private String toLine(Warranty w) {
        String type = w instanceof BasicWarranty ? "BASIC" : "EXTENDED";
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
            if (!parts[0].equals("BASIC") && !parts[0].equals("EXTENDED")) {
                return null;
            }
            return new WarrantyRecord(parts[0], parts[1], parts[2], parts[3],
                    LocalDate.parse(parts[4]));
        } catch (RuntimeException e) {
            System.out.println("Skipping malformed warranty line: " + line);
            return null;
        }
    }
}
