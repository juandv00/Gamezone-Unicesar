package com.gamezone.persistence;

import com.gamezone.model.Accessory;
import com.gamezone.model.Client;
import com.gamezone.model.Person;
import com.gamezone.model.Product;
import com.gamezone.model.Sale;
import com.gamezone.model.Seller;
import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles saving and loading Sale data to and from a file, so that sales
 * information persists between application runs. Since a Sale references
 * a client, a seller, a list of products and a list of accessories, this
 * class relies on ProductPersistence, PersonPersistence and
 * AccessoryRepository to resolve those references by id when
 * reconstructing a Sale from stored data.
 *
 * <p>Each sale is stored as one line with the format
 * {@code date;clientId;sellerId;productIds[;accessoryIds]}, where ids are
 * separated by commas. The accessory field is only written when the sale
 * includes accessories, so lines stored before accessories existed are
 * still read correctly.</p>
 */
public class SalePersistence {

    private static final String FILE_PATH = "data/sales.txt";
    private static final String DELIMITER = ";";
    private static final String ID_SEPARATOR = ",";

    private final ProductPersistence productPersistence;
    private final PersonPersistence personPersistence;
    private final AccessoryRepository accessoryRepository;

    /**
     * Creates a new SalePersistence, using the given persistence classes
     * to resolve client, seller, product and accessory references when
     * loading sales. A default AccessoryRepository is used.
     */
    public SalePersistence() {
        this(new AccessoryRepository());
    }

    /**
     * Creates a new SalePersistence that resolves accessory references
     * with the given repository when loading sales.
     *
     * @param accessoryRepository the repository used to resolve accessories by id
     * @throws IllegalArgumentException if accessoryRepository is null
     */
    public SalePersistence(AccessoryRepository accessoryRepository) {
        if (accessoryRepository == null) {
            throw new IllegalArgumentException("Accessory repository cannot be null.");
        }
        this.productPersistence = new ProductPersistence();
        this.personPersistence = new PersonPersistence();
        this.accessoryRepository = accessoryRepository;
    }

    /**
     * Saves the given list of sales to the data file, overwriting any
     * previously stored content.
     *
     * @param sales the list of sales to persist
     */
    public void save(List<Sale> sales) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_PATH))) {
            for (Sale sale : sales) {
                bw.write(toLine(sale));
                bw.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error saving sales: " + e.getMessage());
        }
    }

    /**
     * Loads the list of sales stored in the data file, resolving each
     * client, seller, product and accessory reference against the
     * currently persisted people, products and accessories.
     *
     * @return the list of sales found, or an empty list if the file does
     *         not exist yet
     */
    public List<Sale> load() {
        List<Sale> sales = new ArrayList<>();
        File file = new File(FILE_PATH);
        if (!file.exists()) {
            return sales;
        }

        List<Person> people = personPersistence.load();
        List<Product> products = productPersistence.load();
        List<Accessory> accessories = accessoryRepository.loadAll();

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                Sale sale = fromLine(line, people, products, accessories);
                if (sale != null) {
                    sales.add(sale);
                }
            }
        } catch (IOException e) {
            System.out.println("Error loading sales: " + e.getMessage());
        }
        return sales;
    }

    /**
     * Converts a Sale into a single delimited line of text for storage,
     * using the ids of its client, seller, products and accessories.
     *
     * @param sale the sale to convert
     * @return the text line representing the sale
     */
    private String toLine(Sale sale) {
        StringBuilder productIds = new StringBuilder();
        List<Product> products = sale.getProducts();
        for (int i = 0; i < products.size(); i++) {
            productIds.append(products.get(i).getId());
            if (i < products.size() - 1) {
                productIds.append(ID_SEPARATOR);
            }
        }

        StringBuilder line = new StringBuilder()
                .append(sale.getDate()).append(DELIMITER)
                .append(sale.getClient().getId()).append(DELIMITER)
                .append(sale.getSeller().getId()).append(DELIMITER)
                .append(productIds);

        List<Accessory> accessories = sale.getAccessories();
        if (!accessories.isEmpty()) {
            line.append(DELIMITER);
            for (int i = 0; i < accessories.size(); i++) {
                line.append(accessories.get(i).getId());
                if (i < accessories.size() - 1) {
                    line.append(ID_SEPARATOR);
                }
            }
        }
        return line.toString();
    }

    /**
     * Parses a single delimited line of text back into a Sale instance,
     * resolving the client, seller, products and accessories by matching
     * ids against the given lists.
     *
     * @param line        the text line to parse
     * @param people      the list of people to search for the client and seller
     * @param products    the list of products to search for the sale's items
     * @param accessories the list of accessories to search for the sale's accessories
     * @return the reconstructed Sale, or null if the line is invalid, a
     *         referenced client or seller cannot be found, or no product
     *         or accessory of the sale can be resolved
     */
    private Sale fromLine(String line, List<Person> people, List<Product> products,
                          List<Accessory> accessories) {
        String[] parts = line.split(DELIMITER);
        if (parts.length < 4) {
            return null;
        }

        LocalDate date = LocalDate.parse(parts[0]);
        String clientId = parts[1];
        String sellerId = parts[2];
        String[] productIds = parts[3].split(ID_SEPARATOR);
        String[] accessoryIds = parts.length > 4 ? parts[4].split(ID_SEPARATOR) : new String[0];

        Client client = null;
        Seller seller = null;
        for (Person person : people) {
            if (person.getId().equals(clientId) && person instanceof Client c) {
                client = c;
            }
            if (person.getId().equals(sellerId) && person instanceof Seller s) {
                seller = s;
            }
        }

        if (client == null || seller == null) {
            return null;
        }

        List<Product> saleProducts = new ArrayList<>();
        for (String productId : productIds) {
            for (Product product : products) {
                if (product.getId().equals(productId)) {
                    saleProducts.add(product);
                }
            }
        }

        List<Accessory> saleAccessories = new ArrayList<>();
        for (String accessoryId : accessoryIds) {
            for (Accessory accessory : accessories) {
                if (accessory.getId().equals(accessoryId)) {
                    saleAccessories.add(accessory);
                }
            }
        }

        if (saleProducts.isEmpty() && saleAccessories.isEmpty()) {
            return null;
        }

        return new Sale(client, seller, saleProducts, saleAccessories, date);
    }
}