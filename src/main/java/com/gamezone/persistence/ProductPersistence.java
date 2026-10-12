package com.gamezone.persistence;

import com.gamezone.exceptions.PersistenceException;
import com.gamezone.model.Console;
import com.gamezone.model.Product;
import com.gamezone.model.VideoGame;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles saving and loading Product data to and from a file,
 * so that inventory information persists between application runs.
 * Any error while reading or writing the file is reported as a
 * {@link PersistenceException} that names the file and the operation.
 */
public class ProductPersistence {

    private static final String FILE_PATH = "data/products.txt";
    private static final String DELIMITER = ";";

    /**
     * Saves the given list of products to the data file, overwriting
     * any previously stored content.
     *
     * @param products the list of products to persist
     * @throws PersistenceException if the file cannot be written
     */
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
        if (product instanceof VideoGame videoGame) {
            return "VIDEOGAME" + DELIMITER + videoGame.getId() + DELIMITER +
                    videoGame.getTitle() + DELIMITER + videoGame.getPrice() + DELIMITER +
                    videoGame.getStock() + DELIMITER + videoGame.getPlatform() + DELIMITER +
                    videoGame.getGenre() + DELIMITER + videoGame.getAgeRating();
        } else if (product instanceof Console console) {
            return "CONSOLE" + DELIMITER + console.getId() + DELIMITER +
                    console.getTitle() + DELIMITER + console.getPrice() + DELIMITER +
                    console.getStock() + DELIMITER + console.getBrand() + DELIMITER +
                    console.getModel() + DELIMITER + console.getGeneration();
        }
        return "";
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

        if (type.equals("VIDEOGAME")) {
            return new VideoGame(id, title, price, stock, parts[5], parts[6], parts[7]);
        } else if (type.equals("CONSOLE")) {
            return new Console(id, title, price, stock, parts[5], parts[6], parts[7]);
        }
        return null;
    }
}