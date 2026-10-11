package com.gamezone.persistence;

import com.gamezone.model.Accessory;
import com.gamezone.model.Product;
import com.gamezone.model.Return;
import com.gamezone.model.Sale;
import com.gamezone.service.ProductService;
import com.gamezone.service.SaleService;
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
 * Handles saving and loading Return data to and from a file, so that returns
 * information persists between application runs. Since a Return references an
 * original Sale and the returned products and accessories, this class relies
 * on SaleService to resolve the sale by id, and resolves each returned item id
 * against the products of that sale first and then against its accessories
 * (falling back to ProductService for products no longer in the sale).
 */
public class ReturnRepository {

    private static final String FILE_PATH = "data/returns.csv";
    private static final String DELIMITER = ";";
    private static final String ID_SEPARATOR = ",";

    private final SaleService saleService;
    private final ProductService productService;

    /**
     * Creates a new ReturnRepository, using the given services to resolve sale
     * and product references when loading returns.
     *
     * @param saleService the service used to resolve the original sale
     * @param productService the service used to resolve returned products
     */
    public ReturnRepository(SaleService saleService, ProductService productService) {
        this.saleService = saleService;
        this.productService = productService;
    }

    /**
     * Persists the given list of returns to the file, overwriting any
     * previously stored data.
     *
     * @param returns the list of returns to save
     */
    public void saveAll(List<Return> returns) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_PATH))) {
            for (Return r : returns) {
                bw.write(toLine(r));
                bw.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error saving returns: " + e.getMessage());
        }
    }

    /**
     * Loads all returns from the file, resolving each return's original sale
     * and returned products against the currently persisted sales and products.
     *
     * @return the list of returns found, or an empty list if the file does not
     * exist
     */
    public List<Return> loadAll() {
        List<Return> returns = new ArrayList<>();
        File file = new File(FILE_PATH);
        if (!file.exists()) {
            return returns;
        }
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                Return r = fromLine(line);
                if (r != null) {
                    returns.add(r);
                }
            }
        } catch (IOException e) {
            System.out.println("Error loading returns: " + e.getMessage());
        }
        return returns;
    }

    /**
     * Converts a Return into a single delimited line of text for storage, using
     * the ids of its original sale and returned items (products followed by
     * accessories, in the same field).
     *
     * @param r the return to convert
     * @return the text line representing the return
     */
    private String toLine(Return r) {
        List<String> itemIdList = new ArrayList<>();
        for (Product product : r.getReturnedProducts()) {
            itemIdList.add(product.getId());
        }
        for (Accessory accessory : r.getReturnedAccessories()) {
            itemIdList.add(accessory.getId());
        }
        String productIds = String.join(ID_SEPARATOR, itemIdList);

        return r.getId() + DELIMITER
                + r.getDate() + DELIMITER
                + r.getOriginalSale().getId() + DELIMITER
                + productIds + DELIMITER
                + r.getReason().replace(DELIMITER, ",") + DELIMITER
                + r.getRefundAmount();
    }

    /**
     * Parses a single delimited line of text back into a Return instance,
     * resolving the original sale and the returned products and accessories
     * by matching ids against the sales currently registered in the system.
     *
     * @param line the text line to parse
     * @return the reconstructed Return, or null if the line is malformed or a
     * referenced sale or product cannot be found
     */
    private Return fromLine(String line) {
        try {
            return parseLine(line);
        } catch (RuntimeException e) {
            System.out.println("Skipping malformed return line: " + line);
            return null;
        }
    }

    /**
     * Parses the fields of a stored return line. Invalid numbers, dates or data
     * rejected by the Return constructor raise a RuntimeException that is
     * handled by {@link #fromLine(String)}.
     *
     * @param line the text line to parse
     * @return the reconstructed Return, or null if the line is incomplete or a
     * referenced sale or product cannot be found
     */
    private Return parseLine(String line) {
        String[] parts = line.split(DELIMITER);
        if (parts.length < 6) {
            return null;
        }

        String id = parts[0];
        LocalDate date = LocalDate.parse(parts[1]);
        String saleId = parts[2];
        String[] productIds = parts[3].split(ID_SEPARATOR);
        String reason = parts[4];

        Sale sale = saleService.findById(saleId);
        if (sale == null) {
            return null;
        }

        List<Product> returnedProducts = new ArrayList<>();
        List<Accessory> returnedAccessories = new ArrayList<>();
        for (String itemId : productIds) {
            Product product = findProduct(sale, itemId);
            if (product != null) {
                returnedProducts.add(product);
                continue;
            }
            Accessory accessory = findAccessory(sale, itemId);
            if (accessory != null) {
                returnedAccessories.add(accessory);
            }
        }
        if (returnedProducts.isEmpty() && returnedAccessories.isEmpty()) {
            return null;
        }

        return new Return(id, date, sale, returnedProducts, returnedAccessories, reason);
    }

    private Product findProduct(Sale sale, String productId) {
        for (Product product : sale.getProducts()) {
            if (product.getId().equals(productId)) {
                return product;
            }
        }
        return productService.findById(productId);
    }

    private Accessory findAccessory(Sale sale, String accessoryId) {
        for (Accessory accessory : sale.getAccessories()) {
            if (accessory.getId().equals(accessoryId)) {
                return accessory;
            }
        }
        return null;
    }
}