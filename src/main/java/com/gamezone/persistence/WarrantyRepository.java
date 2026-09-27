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

    private final SalePersistence salePersistence;

    /**
     * Creates a new WarrantyRepository, using the given SalePersistence to
     * resolve sale and product references when loading warranties.
     *
     * @param salePersistence the persistence used to resolve sales and, through
     * them, the products they contain
     */
    public WarrantyRepository(SalePersistence salePersistence) {
        this.salePersistence = salePersistence;
    }

    /**
     * Persists the given list of warranties to the file, overwriting any
     * previously stored data.
     *
     * @param warranties the list of warranties to save
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
     * Loads all warranties from the file, resolving each warranty's sale and
     * product references against the sales currently persisted, and
     * reconstructing the correct concrete subtype based on its discriminator
     * column. Malformed lines and warranties whose sale or product cannot be
     * found are skipped instead of interrupting the load.
     *
     * @return the list of warranties found, or an empty list if the file does
     * not exist
     */
    public List<Warranty> loadAll() {
        List<Warranty> warranties = new ArrayList<>();
        File file = new File(FILE_PATH);
        if (!file.exists()) {
            return warranties;
        }

        List<Sale> sales = salePersistence.load();

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                Warranty w = fromLine(line, sales);
                if (w != null) {
                    warranties.add(w);
                }
            }
        } catch (IOException e) {
            System.out.println("Error loading warranties: " + e.getMessage());
        }
        return warranties;
    }

    /**
     * Converts a Warranty into a single delimited line of text for storage,
     * following the format: type;id;saleId;productId;startDate.
     *
     * @param w the warranty to convert
     * @return the text line representing the warranty
     */
    private String toLine(Warranty w) {
        String type = w instanceof BasicWarranty ? "BASIC" : "EXTENDED";
        return type + DELIMITER
                + w.getId() + DELIMITER
                + w.getSale().getId() + DELIMITER
                + w.getProduct().getId() + DELIMITER
                + w.getStartDate();
    }

    /**
     * Parses a single delimited line of text back into a Warranty instance,
     * resolving the associated sale from the given list and the associated
     * product from that sale's products, then instantiating the correct
     * concrete subtype based on the discriminator column.
     *
     * @param line the text line to parse
     * @param sales the currently persisted sales, used to resolve references
     * @return the reconstructed Warranty, or null if the line is malformed, or
     * the referenced sale or product cannot be found
     */
    private Warranty fromLine(String line, List<Sale> sales) {
        try {
            return parseLine(line, sales);
        } catch (RuntimeException e) {
            System.out.println("Skipping malformed warranty line: " + line);
            return null;
        }
    }

    private Warranty parseLine(String line, List<Sale> sales) {
        String[] parts = line.split(DELIMITER);
        if (parts.length < 5) {
            return null;
        }

        String type = parts[0];
        String id = parts[1];
        String saleId = parts[2];
        String productId = parts[3];
        LocalDate startDate = LocalDate.parse(parts[4]);

        Sale sale = findSaleById(sales, saleId);
        if (sale == null) {
            return null;
        }

        Product product = findProductById(sale.getProducts(), productId);
        if (product == null) {
            return null;
        }

        if (type.equals("BASIC")) {
            return new BasicWarranty(id, product, sale, startDate);
        } else if (type.equals("EXTENDED")) {
            return new ExtendedWarranty(id, product, sale, startDate);
        }
        return null;
    }

    private Sale findSaleById(List<Sale> sales, String saleId) {
        for (Sale sale : sales) {
            if (sale.getId().equals(saleId)) {
                return sale;
            }
        }
        return null;
    }

    private Product findProductById(List<Product> products, String productId) {
        for (Product product : products) {
            if (product.getId().equals(productId)) {
                return product;
            }
        }
        return null;
    }
}
