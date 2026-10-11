package com.gamezone.persistence;

import com.gamezone.model.Accessory;
import com.gamezone.model.Product;
import com.gamezone.model.Return;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Handles saving and loading Return data to and from a file, so that returns
 * information persists between application runs. A Return references an
 * original Sale and the returned products and accessories; this class stores
 * and loads only their ids (as {@link ReturnRecord}), and ReturnService
 * resolves them. This way the persistence layer does not depend on the
 * service layer.
 */
public class ReturnRepository {

    private static final String FILE_PATH = "data/returns.csv";
    private static final String DELIMITER = ";";
    private static final String ID_SEPARATOR = ",";

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
     * Loads all stored returns from the file as records that reference the
     * original sale and the returned items by id. Malformed lines are skipped.
     *
     * @return the list of records found, or an empty list if the file does not
     * exist
     */
    public List<ReturnRecord> loadAll() {
        List<ReturnRecord> returns = new ArrayList<>();
        File file = new File(FILE_PATH);
        if (!file.exists()) {
            return returns;
        }
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                ReturnRecord r = fromLine(line);
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
     * accessories, in the same field), followed by the refund amount and the
     * amount refunded for cancelled warranties. Lines stored without the last
     * field are loaded with a warranty refund of 0.
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
                + r.getRefundAmount() + DELIMITER
                + r.getWarrantyRefund();
    }

    /**
     * Parses a single delimited line of text into a ReturnRecord.
     *
     * @param line the text line to parse
     * @return the record, or null if the line is malformed
     */
    private ReturnRecord fromLine(String line) {
        try {
            return parseLine(line);
        } catch (RuntimeException e) {
            System.out.println("Skipping malformed return line: " + line);
            return null;
        }
    }

    /**
     * Parses the fields of a stored return line. Invalid numbers or dates
     * raise a RuntimeException that is handled by {@link #fromLine(String)}.
     * Lines stored without the warranty refund field are loaded with a
     * warranty refund of 0.
     *
     * @param line the text line to parse
     * @return the record, or null if the line is incomplete
     */
    private ReturnRecord parseLine(String line) {
        String[] parts = line.split(DELIMITER);
        if (parts.length < 6) {
            return null;
        }

        String id = parts[0];
        LocalDate date = LocalDate.parse(parts[1]);
        String saleId = parts[2];
        List<String> itemIds = Arrays.asList(parts[3].split(ID_SEPARATOR));
        String reason = parts[4];
        double warrantyRefund = parts.length > 6 ? Double.parseDouble(parts[6]) : 0.0;
        return new ReturnRecord(id, date, saleId, itemIds, reason, warrantyRefund);
    }
}