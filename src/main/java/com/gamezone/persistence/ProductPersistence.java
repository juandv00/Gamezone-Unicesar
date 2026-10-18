package com.gamezone.persistence;

import com.gamezone.exceptions.PersistenceException;
import com.gamezone.model.Console;
import com.gamezone.model.Product;
import com.gamezone.model.VideoGame;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Handles saving and loading Product data to and from a file,
 * so that inventory information persists between application runs.
 * Any error while reading or writing the file is reported as a
 * {@link PersistenceException} that names the file and the operation.
 * <p>
 * The format of each product type is registered once in the constructor: a
 * writer for its class and a reader for its type code. Saving and loading
 * look up the registered format instead of checking the type with a chain
 * of conditionals, so a new product type only needs a new registration.
 */
public class ProductPersistence implements ProductStorage {

    private static final String FILE_PATH = "data/products.txt";
    private static final String DELIMITER = ";";

    private final Map<Class<? extends Product>, Function<Product, String>> writers = new HashMap<>();
    private final Map<String, ProductReader> readers = new HashMap<>();

    /**
     * Rebuilds a product of one type from the fields of a stored line.
     */
    @FunctionalInterface
    private interface ProductReader {

        /**
         * Creates the product from its common fields and the stored line.
         *
         * @param id    the product id
         * @param title the product title
         * @param price the unit price
         * @param stock the units in stock
         * @param parts every field of the stored line
         * @return the reconstructed product
         */
        Product read(String id, String title, double price, int stock, String[] parts);
    }

    /**
     * Creates the persistence handler and registers the file format of each
     * product type (video games and consoles).
     */
    public ProductPersistence() {
        registerFormat(VideoGame.class, "VIDEOGAME",
                videoGame -> videoGame.getPlatform() + DELIMITER + videoGame.getGenre() + DELIMITER
                        + videoGame.getAgeRating(),
                (id, title, price, stock, parts) -> new VideoGame(id, title, price, stock,
                        parts[5], parts[6], parts[7]));
        registerFormat(Console.class, "CONSOLE",
                console -> console.getBrand() + DELIMITER + console.getModel() + DELIMITER
                        + console.getGeneration(),
                (id, title, price, stock, parts) -> new Console(id, title, price, stock,
                        parts[5], parts[6], parts[7]));
    }

    /**
     * Registers how a product type is written to and read from the file.
     * Every line starts with the type code followed by the common fields
     * (id, title, price, stock) and then the fields of the type.
     *
     * @param type           the class of the product type
     * @param code           the type code stored at the start of the line
     * @param specificFields builds the fields of the type, already delimited
     * @param reader         rebuilds a product of the type from a line
     * @param <T>            the product type
     */
    private <T extends Product> void registerFormat(Class<T> type, String code,
                                                    Function<T, String> specificFields,
                                                    ProductReader reader) {
        writers.put(type, product -> code + DELIMITER + product.getId() + DELIMITER
                + product.getTitle() + DELIMITER + product.getPrice() + DELIMITER
                + product.getStock() + DELIMITER + specificFields.apply(type.cast(product)));
        readers.put(code, reader);
    }

    /**
     * Saves the given list of products to the data file, overwriting
     * any previously stored content.
     *
     * @param products the list of products to persist
     * @throws PersistenceException if the file cannot be written
     */
    @Override
    public void save(List<Product> products) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH))) {
            for (Product product : products) {
                writer.write(toLine(product));
                writer.newLine();
            }
        } catch (IOException e) {
            throw new PersistenceException("Error de escritura en el archivo " + FILE_PATH
                    + " al guardar los productos.", e);
        }
    }

    /**
     * Loads the list of products stored in the data file.
     *
     * @return the list of products found, or an empty list if the file
     *         does not exist yet
     * @throws PersistenceException if the file cannot be read or contains a
     *                              line with an invalid format
     */
    @Override
    public List<Product> load() {
        List<Product> products = new ArrayList<>();
        File file = new File(FILE_PATH);
        if (!file.exists()) {
            return products;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                Product product = parseLine(line);
                if (product != null) {
                    products.add(product);
                }
            }
        } catch (IOException e) {
            throw new PersistenceException("Error de lectura en el archivo " + FILE_PATH
                    + " al cargar los productos.", e);
        }
        return products;
    }

    /**
     * Converts a Product into a single delimited line of text for storage.
     *
     * @param product the product to convert
     * @return the text line representing the product
     */
    private String toLine(Product product) {
        Function<Product, String> writer = writers.get(product.getClass());
        if (writer == null) {
            return "";
        }
        return writer.apply(product);
    }

    /**
     * Parses a stored line, reporting a line with an invalid number or value
     * as a PersistenceException (the file is corrupted).
     *
     * @param line the text line to parse
     * @return the reconstructed Product, or null if the line is incomplete
     * @throws PersistenceException if the line has an invalid value
     */
    private Product parseLine(String line) {
        try {
            return fromLine(line);
        } catch (RuntimeException e) {
            throw new PersistenceException("Error de lectura en el archivo " + FILE_PATH
                    + ": la línea \"" + line + "\" tiene un formato inválido.", e);
        }
    }

    /**
     * Parses a single delimited line of text back into a Product instance.
     *
     * @param line the text line to parse
     * @return the reconstructed Product, or null if the line is invalid
     */
    private Product fromLine(String line) {
        String[] parts = line.split(DELIMITER);
        if (parts.length < 8) {
            return null;
        }
        String type = parts[0];
        String id = parts[1];
        String title = parts[2];
        double price = Double.parseDouble(parts[3]);
        int stock = Integer.parseInt(parts[4]);

        ProductReader reader = readers.get(type);
        if (reader == null) {
            return null;
        }
        return reader.read(id, title, price, stock, parts);
    }
}