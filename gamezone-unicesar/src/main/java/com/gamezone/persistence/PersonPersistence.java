package com.gamezone.persistence;

import com.gamezone.model.Client;
import com.gamezone.model.Person;
import com.gamezone.model.Seller;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles saving and loading Person data (clients and sellers) to and from a
 * file, so that this information persists between application runs.
 */
public class PersonPersistence {

    private static final String FILE_PATH = "data/persons.txt";
    private static final String DELIMITER = "\t";

    /**
     * Saves the given list of people to the data file, overwriting any
     * previously stored content.
     *
     * @param people the list of people to persist
     */
    public void save(List<Person> people) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_PATH))) {
            for (Person p : people) {
                bw.write(toLine(p));
                bw.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error saving people: " + e.getMessage());
        }
    }

    /**
     * Loads the list of people stored in the data file.
     *
     * @return the list of people found, or an empty list if the file does not
     * exist yet
     */
    public List<Person> load() {
        List<Person> people = new ArrayList<>();
        File file = new File(FILE_PATH);
        if (!file.exists()) {
            return people;
        }
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                Person person = fromLine(line);
                if (person != null) {
                    people.add(person);
                }
            }
        } catch (IOException e) {
            System.out.println("Error loading people: " + e.getMessage());
        }
        return people;
    }

    /**
     * Converts a Person into a single delimited line of text for storage.
     *
     * @param person the person to convert
     * @return the text line representing the person
     */
    private String toLine(Person person) {
        if (person instanceof Client client) {
            return "CLIENT" + DELIMITER + client.getId() + DELIMITER
                    + client.getName() + DELIMITER + client.getPhone() + DELIMITER
                    + client.getEmail();
        } else if (person instanceof Seller seller) {
            return "SELLER" + DELIMITER + seller.getId() + DELIMITER
                    + seller.getName() + DELIMITER + seller.getPhone() + DELIMITER
                    + seller.getEmployeeCode() + DELIMITER + seller.getShift();
        }
        return "";
    }

    /**
     * Parses a single delimited line of text back into a Person instance.
     *
     * @param line the text line to parse
     * @return the reconstructed Person, or null if the line is invalid
     */
    private Person fromLine(String line) {
        String[] parts = line.split(DELIMITER);
        if (parts.length < 5) {
            return null;
        }
        String type = parts[0];
        String id = parts[1];
        String name = parts[2];
        String phone = parts[3];

        if (type.equals("CLIENT")) {
            return new Client(id, name, phone, parts[4]);
        } else if (type.equals("SELLER")) {
            return new Seller(id, name, phone, parts[4], parts[5]);
        }
        return null;
    }
}
